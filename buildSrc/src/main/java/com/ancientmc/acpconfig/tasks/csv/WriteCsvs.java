package com.ancientmc.acpconfig.tasks.csv;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WriteCsvs extends DefaultTask {
    @TaskAction
    public void exec() {
        File tsrg = getTsrg().getAsFile().get();
        File classCsv = getClassCsv().getAsFile().get();
        File fieldCsv = getFieldCsv().getAsFile().get();
        File methodCsv = getMethodCsv().getAsFile().get();
        File paramCsv = getParamCsv().getAsFile().get();

        List<String> classes = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        List<String> params = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(tsrg.toPath());
            for(String line : lines) {
                if (line.contains("/src/c_")) {
                    classes.add(line.substring(line.lastIndexOf("net/minecraft/src/c_")));
                } else if (line.contains(" f_")) {
                    fields.add(line.substring(line.lastIndexOf("f_")));
                } else if (line.contains(" m_")) {
                    methods.add(line.substring(line.lastIndexOf("m_")));
                } else if (line.contains(" p_")) {
                    params.add(line.substring(line.lastIndexOf("p_")));
                }
            }

            // sort through and filter out duplicates
            classes = classes.stream().sorted().collect(Collectors.toList());
            fields = fields.stream().sorted().collect(Collectors.toList());
            methods = methods.stream().sorted().distinct().collect(Collectors.toList());
            params = params.stream().sorted().distinct().collect(Collectors.toList());

            write(classCsv, classes);
            write(fieldCsv, fields);
            write(methodCsv, methods);
            write(paramCsv, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(File csv, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            writer.write("srg,cnf\n");
            for (String line : lines) {
                writer.write(line + ",\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InputFile
    public abstract RegularFileProperty getTsrg();

    @OutputFile
    public abstract RegularFileProperty getClassCsv();

    @OutputFile
    public abstract RegularFileProperty getFieldCsv();

    @OutputFile
    public abstract RegularFileProperty getMethodCsv();

    @OutputFile
    public abstract RegularFileProperty getParamCsv();
}
