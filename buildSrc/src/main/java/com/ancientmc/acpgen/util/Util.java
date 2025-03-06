package com.ancientmc.acpgen.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

public class Util {
    public static JsonObject getJson(File json) throws IOException {
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        return element.getAsJsonObject();
    }

    public static String getOSName() {
        OperatingSystem os = OperatingSystem.current();
        if(os.isWindows()) {
            return "windows";
        } else if (os.isMacOsX()) {
            return "osx";
        } else if (os.isLinux() || os.isUnix()) {
            return "linux";
        }
        return "unknown";
    }

    public static URL toUrl(String path) {
        try {
            return URI.create(path).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getFile(FileSystemLocationProperty<?> property) {
        return property.getAsFile().get();
    }
}
