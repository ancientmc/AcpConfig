package com.ancientmc.acpgen.tasks.mapping;

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
import java.util.*;

@Deprecated(forRemoval = true) // set for possible removal after finalizing new update process
public abstract class UpdateCsv extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File input = getInput().getAsFile().get();
            File blankOutput = getBlankOutput().getAsFile().get();
            File output = getOutput().getAsFile().get();

            Map<String, String> inMap = getMap(input);
            List<String> newEntries = getNewEntries(blankOutput);
            Map<String, String> outMap = new LinkedHashMap<>();

            newEntries.forEach(entry -> {
                if (inMap.containsKey(entry)) {
                    String mapped = inMap.get(entry);
                    outMap.put(entry, mapped);
                } else {
                    outMap.put(entry, entry); // put the SRG name as the mapped.
                }
            });

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
                writer.write("srg,cnf\n");
                for (Map.Entry<String, String> entry : outMap.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getMap(File csv) throws IOException {
        List<String> lines = Files.readAllLines(csv.toPath());
        Map<String, String> map = new HashMap<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] split = line.split(",");
            map.put(split[0], split[1]);
        }

        return map;
    }

    public List<String> getNewEntries(File csv) throws IOException {
        List<String> lines = Files.readAllLines(csv.toPath());
        List<String> entries = new LinkedList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            entries.add(line.substring(0, line.indexOf(','))); // don't include the comma
        }

        return entries;
    }

    @InputFile
    public abstract RegularFileProperty getInput();

    @InputFile
    public abstract RegularFileProperty getBlankOutput();

    @OutputFile
    public abstract RegularFileProperty getOutput();
}
