package com.ancientmc.acpgen.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class DownloadNatives extends DefaultTask {
    @Input public abstract ListProperty<URL> getUrls();
    @OutputDirectory public abstract DirectoryProperty getOutput();

    @TaskAction
    private void exec() {
        try {
            List<File> jars = new ArrayList<>();
            for (URL url : getUrls().get()) {
                String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
                File file = new File(getOutput().getAsFile().get(), path);
                FileUtils.copyURLToFile(url, file);
                jars.add(file);
            }

            jars.forEach(jar -> {
                getProject().copy(action -> {
                   action.from(getProject().zipTree(jar));
                   action.into(getProject().file(getOutput().getAsFile().get()));
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
