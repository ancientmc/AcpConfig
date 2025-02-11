package com.ancientmc.acpconfig.tasks;

import com.ancientmc.acpconfig.util.MatchParser;
import com.ancientmc.acpconfig.util.jar.MinecraftJar;
import com.ancientmc.acpconfig.util.jar.Types;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Very similar to WriteTsrg, but also parses the match file for the new JAR version, and assigns new IDs to everything else.
 */
public abstract class UpdateTsrg extends DefaultTask {

    @TaskAction
    public void exec() {
        File match = getMatch().getAsFile().get();
        File oldIds = getOldIds().getAsFile().get();
        File newJar = getNewJar().getAsFile().get();
        File inheritance = getInheritanceJson().getAsFile().get();
        File tsrg = getTsrg().getAsFile().get();
        File newIds = getNewIds().getAsFile().get();
        MinecraftJar jar = new MinecraftJar(newJar, inheritance);

        List<String> lines = getLines(jar, match, oldIds);

        write(tsrg, lines);
    }

    public List<String> getLines(MinecraftJar jar, File match, File ids) {
        List<String> lines = new ArrayList<>();

        int classCounter = getCount(ids, "classes") + 1;
        int methodCounter = getCount(ids, "methods") + 1;
        int fieldCounter = getCount(ids, "fields") + 1;

        // first line
        lines.add("tsrg2 obf cnf\n");

        // Only include Minecraft classes
        String[] exclude = {"com/jcraft", "paulscode/sound"};
        List<Types.Clazz> sortedClasses = jar.classes.stream().filter(c -> Arrays.stream(exclude).noneMatch(c.name::startsWith)).toList();

        for(Types.Clazz clazz : sortedClasses) {
            lines.add(clazz.name + " " + getDeobfClass(clazz, match, classCounter) + "\n");
            System.out.println("CLASS " + clazz.name + " -> " + getDeobfClass(clazz, match, classCounter));
            if (counted) {
                counted = false;
                classCounter++;
            }

            List<Types.Field> sortedFields = jar.fields.stream().filter(f -> f.parent.equals(clazz.name)).toList();
            for (Types.Field field : sortedFields) {
                lines.add("\t" + field.name + " " + getDeobfField(field, match, fieldCounter) + "\n");
                System.out.println("FIELD: " + field.name + " -> " + getDeobfField(field, match, fieldCounter));
                if (counted) {
                    counted = false;
                    fieldCounter++;
                }
            }

            List<Types.Method> sortedMethods = jar.methods.stream().filter(m -> m.parent.equals(clazz.name)).toList();
            for (Types.Method method : sortedMethods) {
                lines.add("\t" + method.name + " " + method.desc + " " + getDeobfMethod(method, match, methodCounter) + "\n");
                System.out.println("METHOD: " + method.name + " -> " + getDeobfMethod(method, match, methodCounter));
                if (counted) {
                    counted = false;
                    methodCounter++;
                }
            };
        }
        return lines;
    }

    public static int getCount(File ids, String type) {
        try {
            String line = Files.readAllLines(ids.toPath()).stream().filter(l -> l.startsWith(type)).findAny().get();
            String[] split = line.split(",");
            return Integer.parseInt(split[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDeobfClass(Types.Clazz clazz, File match, int counter) {
        String old = MatchParser.getOldClass(clazz.name, match, "c", 1);
        if (old != null) {
            return old;
        } else {
            counted = true;
            return "com/mojang/minecraft/src/c_" + new DecimalFormat("00000").format(counter);
        }
    }

    public static String getDeobfField(Types.Field field, File match, int counter) {
        String old = MatchParser.getOldInner(field.name, field.parent, field.desc, match, "\tf", 2);
        if (old != null) {
            return old;
        } else {
            counted = true;
            return "f_" + new DecimalFormat("00000").format(counter);
        }
    }

    public static String getDeobfMethod(Types.Method method, File match, int counter) {
        String old = MatchParser.getOldInner(method.name, method.parent, method.desc, match, "\tf", 2);
        return old != null ? old : getNewDeobfMethod(method, match, counter);
    }

    public static String getNewDeobfMethod(Types.Method method, File match, int counter) {
        if (method.name.contains("init>"))
            return method.name;
        if (!method.inherited) {
            String old = MatchParser.getOldInner(method.name, method.parent, method.desc, match, "\tm", 2);
            return Objects.requireNonNullElseGet(old, () -> "m_" + new DecimalFormat("00000").format(counter)); // thanks IntelliJ!
        } else {    // We have to worry about inheritance.
            return getInheritedFormattedId(match, method, counter);
        }
    }

    // Two possibilities:
    // a) the method is inherited from a class found in the first jar version. We parse the Match file and find the method entry in the super parent's block.
    // b) the method is inherited from a brand-new class. Then what do we do...?
    public static String getInheritedFormattedId(File match, Types.Method method, int counter) {
        try {
            boolean condition = Files.readAllLines(match.toPath()).stream().anyMatch(l -> l.contains("L" + method.superParent + ";"));
            if (condition) {
                return MatchParser.getOldInner(method.name, method.superParent, method.desc, match, "\tm", 2);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return method.name;
    }

    public static void write(File tsrg, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsrg))) {
            for (String line : lines) {
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean counted = false;

    @InputFile
    public abstract RegularFileProperty getMatch();

    @InputFile
    public abstract RegularFileProperty getNewJar();

    @InputFile
    public abstract RegularFileProperty getOldIds();

    @InputFile
    public abstract RegularFileProperty getInheritanceJson();

    @OutputFile
    public abstract RegularFileProperty getTsrg();

    @OutputFile
    public abstract RegularFileProperty getNewIds();
}
