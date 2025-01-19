package com.ancientmc.cuneiform.tasks.csv;

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
import java.util.List;
import java.util.Map;

public abstract class WriteMethodCsv extends DefaultTask {
    @TaskAction
    public void exec() {
        File file = getJarFile().getAsFile().get();
        File methodsCsv = getMethodCsv().getAsFile().get();
        File paramCsv = getParamCsv().getAsFile().get();

        List<String> methods = new ArrayList<>();
        List<String> params = new ArrayList<>();

        MinecraftJar jar = new MinecraftJar(file);
        Map<Types.Method, String> methodIds = Ids.getAllMethodIds(jar.methods);
        String header = "srg,cnf\n";

        for (int i = 0; i < jar.methods.size(); i++) {
            Types.Method method = jar.methods.get(i);
            Types.Method superMethod = method.inherited ? getSuperMethod(jar, method) : method;
            String id = method.inherited ? methodIds.get(superMethod) : methodIds.get(method);

            // only include non-inherited methods and methods which are obfuscated (aka usually below 2 characters)
            if (!method.inherited) {
                methods.add(getDeobfMethod(method, id) + ",");

                // params use parent method's id
                for (int j = 0; j < method.params; j++) {
                    params.add("p_" + id + "_" + j + ",");
                }
            }
        }

        write(header, methodsCsv, methods);
        write(header, paramCsv, params);
    }

    public static String getDeobfMethod(Types.Method method, String id) {
        return method.name.length() <= 2 ? "m_" + id : method.name;
    }

    public void write(String header, File csv, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            writer.write(header);
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Types.Method getSuperMethod(MinecraftJar jar, Types.Method method) {
        Types.Method superMethod = getImmediateSuperMethod(jar, method);
        if (superMethod.superParent.equals("")) {
            return superMethod;
        }
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
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getMethodCsv();

    @OutputFile
    public abstract RegularFileProperty getParamCsv();
}
