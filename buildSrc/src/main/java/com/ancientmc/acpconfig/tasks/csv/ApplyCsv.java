package com.ancientmc.acpconfig.tasks.csv;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ApplyCsv extends DefaultTask {

    @TaskAction
    public void exec() {
        File tsrg = getTsrg().getAsFile().get();
        File csv = getCsv().getAsFile().get();

        try {
            List<String> oldLines = Files.readAllLines(tsrg.toPath());
            List<String> newLines = new ArrayList<>();
            Map<String, String> map = getMap(csv);

            // Add first line of old to new
            newLines.add(oldLines.get(0));

            for (String line : oldLines) {
                String replacedLine = getReplacedLine(line, map);
                newLines.add(replacedLine);
            }

            write(tsrg, newLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(File tsrg, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsrg))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getReplacedLine(String line, Map<String, String> map) {
        if (map.keySet().stream().anyMatch(line::contains)) {
            String sub = line.substring(line.lastIndexOf(' ') + 1);
            String srg = map.keySet().stream().filter(sub::equals).findAny().get();
            String cuneiform = srg.isEmpty() ? srg : map.get(srg);

            line = line.replace(srg, cuneiform);
        }
        return line;
    }

    public Map<String, String> getMap(File csv) {
        Map<String, String> map = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(csv.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] split = line.split(",");
                String data = split[1].isBlank() ? split[0] : split[1];

                // split[0] = srg, split[1] = cuneiform
                map.put(split[0], data);
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InputFile
    public abstract RegularFileProperty getCsv();

    @InputFile
    public abstract RegularFileProperty getTsrg();
}