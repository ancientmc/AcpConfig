package com.ancientmc.acpgen.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public abstract class Download extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            URL url = URI.create(getSrc().get()).toURL();
            FileUtils.copyURLToFile(url, getDest().getAsFile().get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Input
    public abstract Property<String> getSrc();

    @OutputFile
    public abstract RegularFileProperty getDest();
}
