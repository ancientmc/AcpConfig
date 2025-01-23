package com.ancientmc.acpconfig.tasks.csv;

import com.ancientmc.acpconfig.util.jar.Ids;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class WriteFieldCsv extends DefaultTask {

    @TaskAction
    public void exec() {
        File file = getJarFile().getAsFile().get();
        File csv = getFieldCsv().getAsFile().get();
        List<String> lines = new ArrayList<>();
        MinecraftJar jar = new MinecraftJar(file);
        Map<Types.Field, String> fieldIds = Ids.getAllFieldIds(jar.fields);

        for(int i = 0; i < jar.fields.size(); i++) {
            Types.Field field = jar.fields.get(i);
            lines.add("f_" + fieldIds.get(field) + ",");
        }

        write(csv, lines);
    }

    public static void write(File csv, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            writer.write("srg,cnf\n");
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getFieldCsv();
}
