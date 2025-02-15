package com.ancientmc.acpconfig.tasks;

import net.minecraftforge.mappingverifier.IVerifier;
import net.minecraftforge.mappingverifier.MappingVerifier;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class VerifyMappings extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File jar = getJar().getAsFile().get();
            File tsrg = getTsrg().getAsFile().get();
            File log = getLog().getAsFile().get();

            MappingVerifier verifier = new MappingVerifier();
            verifier.loadMap(tsrg);
            verifier.loadJar(jar);

            // We only care about overrides.
            verifier.addTask("OverrideNames");

            if (verifier.verify()) {
                getProject().getLogger().log(LogLevel.LIFECYCLE, "Verification successful");
            } else {
                List<String> lines = new ArrayList<>();
                for (IVerifier task : verifier.getTasks()) {
                    List<String> errors = task.getErrors();
                    errors.forEach(e -> {
                        getProject().getLogger().log(LogLevel.LIFECYCLE, "ERROR: " + e + " in task " + task.getName());
                        lines.add("ERROR: " + e + " in task " + task.getName());
                    });
                }
                write(lines, log);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(List<String> lines, File log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(log))) {
            writer.write("DATE\t" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");

            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InputFile
    public abstract RegularFileProperty getJar();

    @InputFile
    public abstract RegularFileProperty getTsrg();

    @OutputFile
    public abstract RegularFileProperty getLog();
}
