package com.ancientmc.acpgen.tasks.csv;

import com.ancientmc.acpgen.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public abstract class ApplyCsvs extends DefaultTask {

    @TaskAction
    public void exec() {
        File inTsrg = Util.getFile(getInTsrg());
        File csvDirectory = Util.getFile(getCsvDirectory());
        File outTsrg = Util.getFile(getOutTsrg());

        try {
            List<String> oldLines = Files.readAllLines(inTsrg.toPath());
            List<String> newLines = new ArrayList<>();
            Map<String, String> map = getMap(csvDirectory);

            for (String line : oldLines) {
                String replacedLine = getReplacedLine(line, map);
                newLines.add(replacedLine);
            }

            if (!outTsrg.getParentFile().exists()) {
                Files.createDirectories(outTsrg.getParentFile().toPath());
            }

            write(outTsrg, newLines);
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
            String srg = map.keySet().stream().filter(line::contains).findAny().orElse(null);

            if (srg != null) {
                String cuneiform = map.get(srg);
                line = line.replace(srg, cuneiform);
            }
        }

        return line;
    }

    public Map<String, String> getMap(File csvDirectory) {
        Map<String, String> map = new HashMap<>();
        Collection<File> csvs = FileUtils.listFiles(csvDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);

        csvs.forEach(csv -> {
            try {
                List<String> lines = Files.readAllLines(csv.toPath());
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] split = line.split(",");

                    // split[0] = srg, split[1] = cuneiform
                    map.put(split[0], split[1]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return map;
    }

    @InputDirectory
    public abstract DirectoryProperty getCsvDirectory();

    @InputFile
    public abstract RegularFileProperty getInTsrg();

    @OutputFile
    public abstract RegularFileProperty getOutTsrg();
}