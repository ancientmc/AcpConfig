package com.ancientmc.acpconfig.util.mapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Match {
    private final File match;

    public Match(File match) {
        this.match = match;
    }

    public List<MatchClass> getClasses() throws IOException {
        List<MatchClass> classes = new ArrayList<>();
        List<String> lines = Files.readAllLines(match.toPath()).stream().filter(l -> l.startsWith("c\t")).toList();

        lines.forEach(line -> {
            String[] split = line.split("\t");
            String oldName = split[1].substring(1, split[1].indexOf(';')); // L<class_name>; -> <class_name>
            String newName = split[2].substring(1, split[2].indexOf(';'));
            classes.add(new MatchClass(oldName, newName));
        });
        return classes;
    }

    public List<MatchField> getFields(List<MatchClass> classes) {
        List<MatchField> fields = new ArrayList<>();

        try {
            List<String> matchLines = Files.readAllLines(match.toPath());
            classes.forEach(cls -> {
                String classLine = matchLines.stream().filter(line -> line.startsWith("c\tL" + cls.oldName)).findAny().get();
                List<String> classBlock = matchLines.subList(matchLines.indexOf(classLine) + 1, getNextClassIndex(matchLines, classLine));

                classBlock.forEach(line -> {
                    if (line.startsWith("\tf\t")) { // field prefix
                        String[] split = line.split("\t");
                        String oldName = split[1].substring(0, split[1].indexOf(';') - 1); // <field_name>;;<descriptor> -> <field_name>
                        String newName = split[2].substring(0, split[2].indexOf(';') - 1);
                        fields.add(new MatchField(cls.oldName, cls.newName, oldName, newName));
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fields;
    }

    public static class MatchClass {
        public String oldName;
        public String newName;

        public MatchClass(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }
    }

    public static class MatchField {
        public String oldParent;
        public String newParent;
        public String oldName;
        public String newName;

        public MatchField(String oldParent, String newParent, String oldName, String newName) {
            this.oldParent = oldParent;
            this.newParent = newParent;
            this.oldName = oldName;
            this.newName = newName;
        }
    }

    public static class MatchMethod {
        public String oldParent;
        public String newParent;
        public String oldName;
        public String newName;
        public String oldDesc;
        public String newDesc;

        public MatchMethod(String oldParent, String newParent, String oldName, String newName, String oldDesc, String newDesc) {
            this.oldParent = oldParent;
            this.newParent = newParent;
            this.oldName = oldName;
            this.newName = newName;
            this.oldDesc = oldDesc;
            this.newDesc = newDesc;
        }
    }

    public static int getNextClassIndex(List<String> lines, String classLine) {
        for (int i = lines.indexOf(classLine) + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("c\tL")) {
                return i;
            }
        }
        return lines.size();
    }
}
