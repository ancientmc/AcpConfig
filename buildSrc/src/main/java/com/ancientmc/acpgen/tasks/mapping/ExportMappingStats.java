package com.ancientmc.acpgen.tasks.mapping;

import com.ancientmc.acpgen.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public abstract class ExportMappingStats extends DefaultTask {
    @TaskAction
    public void exec() {
        File csvDirectory = Util.getFile(getCsvDirectory());
        File statsCsv = Util.getFile(getStatsCsv());
        Collection<File> csvs = FileUtils.listFiles(csvDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(statsCsv))) {
            writer.write("type,unmapped,total" + "\n");

            for (File csv : csvs) {
                String name = csv.getName().replace(".csv", "");
                List<String> lines = Files.readAllLines(csv.toPath())
                        .subList(1, Files.readAllLines(csv.toPath()).size()); // exclude header
                int count = getCount(lines);
                writer.write(String.join(",", name, Integer.toString(count), Integer.toString(lines.size())) + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCount(List<String> lines) {
        int count = 0;
        for (String line : lines) {
            String[] split = line.split(",");
            if (split[0].equals(split[1])) {
                ++count;
            }
        }
        return count;
    }

    @InputDirectory
    public abstract DirectoryProperty getCsvDirectory();

    @OutputFile
    public abstract RegularFileProperty getStatsCsv();
}
