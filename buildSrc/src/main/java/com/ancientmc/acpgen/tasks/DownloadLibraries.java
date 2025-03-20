package com.ancientmc.acpgen.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.List;

import com.ancientmc.acpgen.util.Json;
import com.ancientmc.acpgen.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadLibraries extends DefaultTask {

    @TaskAction
    public void exec() {
        try {
            List<File> jsons = getJsons().get();
            File libs = Util.getFile(getLibs());

            Map<String, String> map = Json.getLibraryMap(jsons);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                URL url = Util.toUrl(entry.getKey());
                File jar = new File(libs, entry.getValue());

                getProject().getLogger().lifecycle("Download " + url);
                FileUtils.copyURLToFile(url, jar);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Input
    public abstract ListProperty<File> getJsons();

    @OutputDirectory
    public abstract RegularFileProperty getLibs();

}
