package com.ancientmc.acpconfig.util.mapping;

import net.minecraftforge.srgutils.IMappingFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a TSRG file as an object.
 */
public class Tsrg {
    private final IMappingFile mapping;
    private final File file;

    private Tsrg(IMappingFile mapping, File file) {
        this.mapping = mapping;
        this.file = file;
    }

    public static Tsrg load(File file) {
        try {
            return new Tsrg(IMappingFile.load(file), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TsrgClass> getClasses() {
        List<TsrgClass> classes = new ArrayList<>();
        this.mapping.getClasses().forEach(cls -> {
            String id = getClassId(this.file, cls.getMapped());
            TsrgClass tsrgCls = new TsrgClass(cls.getOriginal(), cls.getMapped(), id);
            classes.add(tsrgCls);
        });
        return classes;
    }

    public List<TsrgField> getFields() {
        List<TsrgField> fields = new ArrayList<>();
        this.mapping.getClasses().forEach(cls -> {
            if (!cls.getFields().isEmpty()) {
                cls.getFields().forEach(fld -> {
                    String id = getFieldId(this.file, cls.getOriginal(), fld.getMapped());
                    TsrgField tsrgField = new TsrgField(fld.getOriginal(), fld.getMapped(), cls.getOriginal(), id);
                    fields.add(tsrgField);
                });
            }
        });
        return fields;
    }

    public List<TsrgMethod> getMethods() {
        List<TsrgMethod> methods = new ArrayList<>();
        this.mapping.getClasses().forEach(cls -> {
            if (!cls.getMethods().isEmpty()) {
                cls.getMethods().forEach(mtd -> {
                    String id = getMethodId(this.file, cls.getOriginal(), mtd.getMapped(), mtd.getDescriptor());
                    TsrgMethod tsrgMethod = new TsrgMethod(mtd.getOriginal(), mtd.getMapped(), cls.getOriginal(), mtd.getDescriptor(), id);
                    methods.add(tsrgMethod);
                });
            }
        });
        return methods;
    }

    public static String getClassId(File file, String mapped) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String line = lines.stream().filter(l -> l.contains(mapped)).findAny().get();
            return line.split(" ")[2];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFieldId(File file, String parent, String mapped) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String classLine = lines.stream().filter(l -> l.startsWith(parent + " ")).findAny().get();
            List<String> classBlock = lines.subList(lines.indexOf(classLine), getNextClassIndex(lines, classLine));
            String fieldLine = classBlock.stream().filter(l -> l.contains(mapped)).findAny().get();
            return fieldLine.split(" ")[2];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMethodId(File file, String parent, String mapped, String desc) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String classLine = lines.stream().filter(l -> l.startsWith(parent + " ")).findAny().get();
            List<String> classBlock = lines.subList(lines.indexOf(classLine), getNextClassIndex(lines, classLine));
            String methodLine = classBlock.stream().filter(l -> l.contains(mapped) && l.contains(desc)).findAny().get();
            return methodLine.split(" ")[3];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getNextClassIndex(List<String> lines, String classLine) {
        for (int i = lines.indexOf(classLine) + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("\t")) { // class line = any line without prefixed indents
                return i;
            }
        }
        return lines.size(); // returned after everything's parsed.
    }

    public TsrgClass getIntermediateClass(Match.MatchClass cls) {
        for (TsrgClass tsrgClass : this.getClasses()) {
            if (tsrgClass.obf.equals(cls.oldName)) {
                return tsrgClass;
            }
        }
        return null;
    }

    public TsrgField getIntermediateField(Match.MatchField field) {
        for (TsrgField tsrgField : this.getFields()) {
            if (tsrgField.parent.equals(field.oldParent) && tsrgField.obf.equals(field.oldName)) {
                return tsrgField;
            }
        }
        return null;
    }

    public TsrgMethod getIntermediateMethod(Match.MatchMethod method) {
        for (TsrgMethod tsrgMethod : this.getMethods()) {
            if (tsrgMethod.parent.equals(method.oldParent) && tsrgMethod.obf.equals(method.oldName) && tsrgMethod.desc.equals(method.oldDesc)) {
                return tsrgMethod;
            }
        }
        return null;
    }

    public static class TsrgClass {
        public String obf;
        public String mapped;
        public String id;

        public TsrgClass(String obf, String mapped, String id) {
            this.obf = obf;
            this.mapped = mapped;
            this.id = id;
        }
    }

    public static class TsrgField {
        public String obf;
        public String mapped;
        public String parent;
        public String id;

        public TsrgField(String obf, String mapped, String parent, String id) {
            this.obf = obf;
            this.mapped = mapped;
            this.parent = parent;
            this.id = id;
        }

        public String toString() {
            return "TsrgField obf=" + obf + " mapped=" + mapped + " parent=" + parent + " id" + id;
        }
    }

    public static class TsrgMethod {
        public String obf;
        public String mapped;
        public String parent;
        public String desc;
        public String id;

        public TsrgMethod(String obf, String mapped, String parent, String desc, String id) {
            this.obf = obf;
            this.mapped = mapped;
            this.parent = parent;
            this.desc = desc;
            this.id = id;
        }
    }
}
