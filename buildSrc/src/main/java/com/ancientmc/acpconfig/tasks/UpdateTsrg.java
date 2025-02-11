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

        classCounter = getCount(oldIds, "classes");
        fieldCounter = getCount(oldIds, "fields");

        List<String> lines = getLines(jar, match, oldIds);

        write(tsrg, lines);
    }

    public List<String> getLines(MinecraftJar jar, File match, File ids) {
        List<String> lines = new ArrayList<>();

        // first line
        lines.add("tsrg2 obf cnf\n");

        // Only include Minecraft classes
        String[] exclude = {"com/jcraft", "paulscode/sound"};
        List<Types.Clazz> sortedClasses = jar.classes.stream().filter(c -> Arrays.stream(exclude).noneMatch(c.name::startsWith)).toList();

        sortedClasses.forEach(clazz -> {
            lines.add(clazz.name + " " + getDeobfClass(clazz, match) + "\n");
            System.out.println("CLASS " + clazz.name + " -> " + getDeobfClass(clazz, match));

            List<Types.Field> sortedFields = jar.fields.stream().filter(f -> f.parent.equals(clazz.name)).toList();
            sortedFields.forEach(field -> {
                lines.add("\t" + field.name + " " + getDeobfField(field, match) + "\n");
                System.out.println("FIELD: " + field.name + " -> " + getDeobfField(field, match));
            });
        });

        // WIP...

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

    public static String getDeobfClass(Types.Clazz clazz, File match) {
        String old = MatchParser.getOldClass(clazz.name, match, "c", 1);
        return old != null ? old : getNewDeobfClass();
    }

    public static String getDeobfField(Types.Field field, File match) {
        String old = MatchParser.getOldInner(field.name, field.parent, field.desc, match, "\tf", 2);
        return old != null ? old : getNewDeobfField();
    }

    public static String getNewDeobfClass() {
        classCounter = classCounter + 1;
        return "com/mojang/minecraft/src/c_" + getFormattedId(classCounter);
    }

    public static String getNewDeobfField() {
        fieldCounter = fieldCounter + 1;
        return "f_" + getFormattedId(fieldCounter);
    }

    public static String getFormattedId(int counter) {
        return new DecimalFormat("00000").format(counter + 1);
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

    private static int classCounter = 0;
    private static int fieldCounter = 0;
    private static int methodCounter = 0;

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
