package com.ancientmc.cuneiform.tasks;

import com.ancientmc.cuneiform.util.jar.Ids;
import com.ancientmc.cuneiform.util.jar.MinecraftJar;
import com.ancientmc.cuneiform.util.jar.Types;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TSRGv2 is the mapping format used by Forge and NeoForge. It is the format preferred for ACP.
 * This task generates a fresh TSRGv2 based on the input Minecraft JAR. Classes, methods, fields, and parameters are assigned
 * intermediary values with generated IDs via com.ancientmc.util.jar.Ids
 */
public abstract class WriteTsrg extends DefaultTask {

    @TaskAction
    public void exec() {
        File file = getJar().getAsFile().get();
        File tsrg = getTsrg().getAsFile().get();
        MinecraftJar jar = new MinecraftJar(file);

        Map<Types.Clazz, String> classIds = Ids.getAllClassIds(jar.classes);
        Map<Types.Field, String> fieldIds = Ids.getAllFieldIds(jar.fields);
        Map<Types.Method, String> methodIds = Ids.getAllMethodIds(jar.methods);

        List<String> lines = getLines(jar, classIds, fieldIds, methodIds);
        write(tsrg, lines);
    }

    public static List<String> getLines(MinecraftJar jar, Map<Types.Clazz, String> classIds, Map<Types.Field, String> fieldIds, Map<Types.Method, String> methodIds) {
        List<String> lines = new ArrayList<>();

        // first line
        lines.add("tsrg2 obf cnf\n");

        // Only include Minecraft classes
        String[] exclude = {"com/jcraft", "paulscode/sound"};
        List<Types.Clazz> sortedClasses = jar.classes.stream().filter(c -> Arrays.stream(exclude).noneMatch(c.name::startsWith)).toList();


        sortedClasses.forEach(clazz -> {
            lines.add(clazz.name + " " + getDeobfClass(clazz, classIds) + "\n");
            System.out.println("CLASS: " + clazz.name + " -> " + getDeobfClass(clazz, classIds));

            // Get fields in the currently iterated class
            List<Types.Field> sortedFields = jar.fields.stream().filter(f -> f.parent.equals(clazz.name)).toList();
            sortedFields.forEach(field -> {
                lines.add("\t" + field.name + " " + getDeobfField(field, fieldIds) + "\n");
                System.out.println("FIELD: " + field.name + " -> " + getDeobfField(field, fieldIds));
            });

            // Get methods in the currently iterated class
            List<Types.Method> sortedMethods = jar.methods.stream().filter(m -> m.parent.equals(clazz.name)).toList();
            sortedMethods.forEach(method -> {

                // We have to deal with inheritance. If a method is inherited, get the id of the root parent. If not, just get the id of the normal method.
                String id = method.inherited ? methodIds.get(getSuperMethod(jar, method)) : methodIds.get(method);
                lines.add("\t" + method.name + " " + method.desc + " " + getDeobfMethod(method, id) + "\n");
                System.out.println("METHOD: " + method.name + " -> " + getDeobfMethod(method, id));

                // Get lines for the params of the currently iterated method
                if (method.params > 0) {
                    for (int i = 0; i < method.params; i++) {
                        lines.add("\t\t" + i + " o " + "p_" + id + "_" + i + "\n");
                        System.out.println("PARAM: p_" + id + "_" + i);
                    }
                }
            });
        });

        return lines;
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

    // get the name if it's a prenamed class file, otherwise get the intermediary with an id
    public static String getDeobfClass(Types.Clazz clazz, Map<Types.Clazz, String> classIds) {
        return clazz.name.contains("net/minecraft") ? clazz.name : "net/minecraft/src/c_" + classIds.get(clazz);
    }

    public static String getDeobfMethod(Types.Method method, String id) {
        return method.name.length() <= 2 ? "m_" + id : method.name;
    }

    public static String getDeobfField(Types.Field field, Map<Types.Field, String> fieldIds) {
        return field.name.length() <= 2 ? "f_" + fieldIds.get(field) : field.name;
    }


    public static Types.Method getSuperMethod(MinecraftJar jar, Types.Method method) {
        Types.Method superMethod = getImmediateSuperMethod(jar, method);

        // If the method has no parent, just return the method since it doesn't need to be iterated further.
        if (superMethod.superParent.equals("")) {
            return superMethod;
        }

        // Crawl through the class inheritance
        // The while loop stops when the method's super parent class is blank, meaning the currently iterated method *has* no super parent.
        // That means we've found the root parent, and that's what gets returned.
        Types.Method superDuperMethod;
        while (!superMethod.superParent.equals("")) {
            superDuperMethod = getImmediateSuperMethod(jar, superMethod);
            superMethod = superDuperMethod;
        }
        return superMethod;
    }

    public static Types.Method getImmediateSuperMethod(MinecraftJar jar, Types.Method method) {
        Types.Clazz superParent = jar.classes.stream().filter(c -> c.name.equals(method.superParent)).findAny().get();
        if (!superParent.name.equals("")) {
            List<Types.Method> superMethods = jar.methods.stream().filter(m -> m.parent.equals(superParent.name)).toList();
            return superMethods.stream().filter(m -> (m.desc.equals(method.desc) && m.name.equals(method.name))).findAny().get();
        } else {
            return null;
        }
    }

    @InputFile
    public abstract RegularFileProperty getJar();

    @OutputFile
    public abstract RegularFileProperty getTsrg();
}
