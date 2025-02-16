package com.ancientmc.acpconfig.tasks;

import com.ancientmc.acpconfig.util.mapping.Match;
import com.ancientmc.acpconfig.util.mapping.Tsrg;
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
import java.util.*;

/**
 * Very similar to WriteTsrg, but also parses the match file for the new JAR version, and assigns new IDs to everything else.
 */
public abstract class UpdateTsrg extends DefaultTask {

    // The updated counters for each type, which are exported as a CSV file.
    private static int lastClassCounter;
    private static int lastFieldCounter;
    private static int lastMethodCounter;

    @TaskAction
    public void exec() {
        File oldTsrg = getOldTsrg().getAsFile().get();
        File matchFile = getMatch().getAsFile().get();
        File oldIds = getOldIds().getAsFile().get();
        File inheritance = getInheritanceJson().getAsFile().get();
        File newJar = getNewJar().getAsFile().get();
        File newTsrg = getNewTsrg().getAsFile().get();
        File newIds = getNewIds().getAsFile().get();

        MinecraftJar jar = new MinecraftJar(newJar, inheritance);
        Tsrg tsrg = Tsrg.load(oldTsrg);
        Match match = new Match(matchFile);

        try {
            List<String> lines = getLines(tsrg, jar, match, oldIds);
            write(newTsrg, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getLines(Tsrg tsrg, MinecraftJar jar, Match match, File ids) throws IOException {
        List<String> lines = new ArrayList<>();

        // Maps of types that are new to our new version, and not found in the match file.
        Map<Types.Clazz, String> newClassIds = getNewClassIds(jar.classes, match, ids);
        Map<Types.Field, String> newFieldIds = getNewFieldIds(jar.fields, match, ids);
        Map<Types.Method, String> newMethodIds = getNewMethodIds(jar.methods, match, ids);

        lines.add("tsrg2 obf cnf id\n");
        String[] exclude = {"com/jcraft", "paulscode/sound"};

        List<Types.Clazz> sortedClasses = jar.classes.stream().filter(c -> Arrays.stream(exclude).noneMatch(c.name::startsWith)).toList();
        for (Types.Clazz cls : sortedClasses) {
            if (isOldClass(cls, match)) {
                Match.MatchClass oldClass = match.getOldClass(cls.name);
                Tsrg.TsrgClass intermediateClass = tsrg.getIntermediateClass(oldClass);
                lines.add(String.join(" ", cls.name, intermediateClass.mapped, intermediateClass.id) + "\n");
            } else {
                String id = newClassIds.get(cls);
                lines.add(String.join(" ", cls.name, "com/mojang/minecraft/src/c_" + id, id) + "\n");
            }

            // Fields
            List<Types.Field> sortedFields = jar.fields.stream().filter(f -> f.parent.equals(cls.name)).toList();
            for (Types.Field field : sortedFields) {
                if (isOldField(field, match)) {
                    Match.MatchField oldField = match.getOldField(field);
                    Tsrg.TsrgField intermediateField = tsrg.getIntermediateField(oldField);
                    lines.add("\t" + String.join(" ", field.name, intermediateField.mapped, intermediateField.id) + "\n");
                } else {
                    String id = newFieldIds.get(field);
                    lines.add("\t" + String.join(" ", field.name, "f_" + id, id) + "\n");
                }
            }

            // Methods
            List<Types.Method> sortedMethods = jar.methods.stream().filter(m -> m.parent.equals(cls.name)).toList();
            for (Types.Method method : sortedMethods) {
                String id = "";
                if (isOldMethod(method, match)) {
                    Match.MatchMethod oldMethod = match.getOldMethod(method);
                    System.out.println(oldMethod.toString());
                    Tsrg.TsrgMethod intermediateMethod = tsrg.getIntermediateMethod(oldMethod);
                    id = intermediateMethod.id;
                    lines.add("\t" + String.join(" ", method.name, method.desc, intermediateMethod.mapped, id) + "\n");
                } else {
                    if (method.inherited) {
                        Types.Method superMethod = getSuperMethod(jar, method);
                        if (isOldMethod(superMethod, match)) { // if the method's parent is old
                            Match.MatchMethod oldSuperMethod = match.getOldMethod(superMethod);
                            id = tsrg.getIntermediateMethod(oldSuperMethod).id;
                        } else { // if the method's parent id is also new
                            id = newMethodIds.get(superMethod);
                        }
                    } else {
                        id = newMethodIds.get(method);
                    }
                    lines.add("\t" + String.join(" ", method.name, method.desc, "m_" + id, id) + "\n");
                }

                // Parameters
                for (int i = 0; i < method.params; i++) {
                    String index = Integer.toString(i);
                    String pid = id + "_" + index;
                    lines.add("\t\t" + String.join(" ", index, "o", "p_" + pid, pid) + "\n");
                }
            }
        }
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

    public static void writeIds(File ids) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ids))) {
            writer.write(String.join(",", "classes", Integer.toString(lastClassCounter)) + "\n");
            writer.write(String.join(",", "fields", Integer.toString(lastFieldCounter)) + "\n");
            writer.write(String.join(",", "methods", Integer.toString(lastMethodCounter)) + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Types.Method getSuperMethod(MinecraftJar jar, Types.Method method) {
        if (jar.classes.stream().anyMatch(c -> c.name.equals(method.superParent))) {
            Types.Clazz superParent = jar.classes.stream().filter(c -> c.name.equals(method.superParent)).findAny().get();
            if (!superParent.name.isEmpty()) {
                List<Types.Method> superMethods = jar.methods.stream().filter(m -> m.parent.equals(superParent.name)).toList();
                return superMethods.stream().filter(m -> (m.desc.equals(method.desc) && m.name.equals(method.name))).findAny().get();
            }
        } else {
            return method;
        }
        return null;
    }

    /*
     * These methods check if a given type is old, (i.e. if it's present in the match file).
     */

    public static boolean isOldClass(Types.Clazz cls, Match match) throws IOException {
        return match.getClasses().stream().anyMatch(mc -> mc.newName.equals(cls.name));
    }

    public static boolean isOldField(Types.Field field, Match match) throws IOException {
        List<Match.MatchClass> classes = match.getClasses();
        return match.getFields(classes).stream().anyMatch(mf -> mf.newName.equals(field.name) && mf.newParent.equals(field.parent));
    }

    public static boolean isOldMethod(Types.Method method, Match match) throws IOException {
        List<Match.MatchClass> classes = match.getClasses();
        return match.getMethods(classes).stream().anyMatch(mm -> mm.newName.equals(method.name) && mm.newParent.equals(method.parent) && mm.newDesc.equals(method.desc));
    }

    /*
     * These three methods return ID lists of types that are new (not in the match file).
     * The counters for each type are also updated so they can be exported into the CSV file.
     */
    public static Map<Types.Clazz, String> getNewClassIds(List<Types.Clazz> classes, Match match, File ids) throws IOException {
        int counter = getCount(ids, "classes") + 1;
        Map<Types.Clazz, String> newClasses = new HashMap<>();

        for (Types.Clazz cls : classes) {
            if (!isOldClass(cls, match)) {
                newClasses.put(cls, getFormattedId(counter));
                counter++;
            }
        }

        lastClassCounter = counter;
        return newClasses;
    }

    public static Map<Types.Field, String> getNewFieldIds(List<Types.Field> fields, Match match, File ids) throws IOException {
        int counter = getCount(ids, "fields") + 1;
        Map<Types.Field, String> newField = new HashMap<>();

        for (Types.Field field : fields) {
            if (!isOldField(field, match)) {
                newField.put(field, getFormattedId(counter));
                counter++;
            }
        }

        lastFieldCounter = counter;
        return newField;
    }

    public static Map<Types.Method, String> getNewMethodIds(List<Types.Method> methods, Match match, File ids) throws IOException {
        int counter = getCount(ids, "fields") + 1;
        Map<Types.Method, String> newMethods = new HashMap<>();

        for (Types.Method method : methods) {
            if (!isOldMethod(method, match) && !method.inherited) {
                newMethods.put(method, getFormattedId(counter));
                counter++;
            }
        }

        lastMethodCounter = counter;
        return newMethods;
    }

    public static String getFormattedId(int id) {
        return new DecimalFormat("00000").format(id);
    }

    public static int getCount(File ids, String type) throws IOException {
        String line = Files.readAllLines(ids.toPath()).stream().filter(l -> l.startsWith(type)).findAny().get();
        String[] split = line.split(",");
        return Integer.parseInt(split[1]);
    }

    @InputFile
    public abstract RegularFileProperty getOldTsrg();

    @InputFile
    public abstract RegularFileProperty getMatch();

    @InputFile
    public abstract RegularFileProperty getNewJar();

    @InputFile
    public abstract RegularFileProperty getOldIds();

    @InputFile
    public abstract RegularFileProperty getInheritanceJson();

    @OutputFile
    public abstract RegularFileProperty getNewTsrg();

    @OutputFile
    public abstract RegularFileProperty getNewIds();
}
