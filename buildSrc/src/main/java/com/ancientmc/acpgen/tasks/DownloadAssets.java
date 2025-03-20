package com.ancientmc.acpgen.tasks;

import com.ancientmc.acpgen.util.Util;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public abstract class DownloadAssets {

    @TaskAction
    public void exec() {
        URL index = getIndex().get();
        File directory = Util.getFile(getDirectory());

        try {
            if (!directory.exists()) {
                FileUtils.forceMkdir(directory);
            }

            JsonObject object = Util.getJson(index);
            Map<String, String> assets = getAssets(object);
            download(assets, directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getAssets(JsonObject index) throws IOException {
        Map<String, String> assets = new HashMap<>();
        JsonObject objects = index.getAsJsonObject("objects");

        objects.keySet().forEach(name -> {
            String hash = objects.getAsJsonObject(name).get("hash").getAsString();
            assets.put(name, hash);
        });

        return assets;
    }

    public static void download(Map<String, String> map, File dest) throws IOException {
        if(!dest.exists()) {
            FileUtils.forceMkdir(dest);
        }

        map.forEach((key, value) -> {
            try {
                String path = value.substring(0, 2) + '/' + value;
                URL url = Util.toUrl("https://resources.download.minecraft.net/" + path);
                File file = new File(dest, key);

                if(!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }

                writeToFile(url.openStream(), Files.newOutputStream(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void writeToFile(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int len;

        while ((len = in.read(b)) > 0) {
            out.write(b, 0, len);
            out.flush();
        }
        in.close();
        out.close();
    }

    @Input
    public abstract Property<URL> getIndex();

    @OutputDirectory
    public abstract DirectoryProperty getDirectory();
}
