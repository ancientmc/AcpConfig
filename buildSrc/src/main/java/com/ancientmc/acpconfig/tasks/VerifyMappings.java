package com.ancientmc.acpconfig.tasks;

import net.minecraftforge.mappingverifier.IVerifier;
import net.minecraftforge.mappingverifier.MappingVerifier;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class VerifyMappings extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File jar = getJar().getAsFile().get();
            File tsrg = getTsrg().getAsFile().get();

            MappingVerifier verifier = new MappingVerifier();
            verifier.loadMap(tsrg);
            verifier.loadJar(jar);

            // We only care about overrides.
            verifier.addTask("OverrideNames");

            if (verifier.verify()) {
                getProject().getLogger().log(LogLevel.LIFECYCLE, "Verification successful");
            } else {
                for (IVerifier task : verifier.getTasks()) {
                    List<String> errors = task.getErrors();
                    errors.forEach(e -> log(e, task.getName()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String error, String task) {
        getProject().getLogger().log(LogLevel.LIFECYCLE, "ERROR: " + error + " in task " + task);
    }

    @InputFile
    public abstract RegularFileProperty getJar();

    @InputFile
    public abstract RegularFileProperty getTsrg();
}
