package com.ancientmc.acpconfig.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JSON parsing, mainly Minecraft's version JSON.
 */
public class Json {
    private static final String LWJGL_VERSION = "2.9.0";
    private static final String LWJGL_MAC_VERSION = "2.9.1";

    /**
     * Gets the JSON URL for the specified version from the version manifest file.
     * @param manifest The version manifest JSON.
     * @param version The Minecraft version, specified in the ACP end-user workspace.
     * @return The URL for the JSON file on Minecraft's website.
     */
    public static String getJsonUrl(File manifest, String version) {
        try {
            if (manifest.exists()) {
                JsonObject manifestObj = Util.getJsonAsObject(manifest);
                JsonArray versions = manifestObj.getAsJsonArray("versions");

                for (int i = 0; i < versions.size(); i++) {
                    JsonElement id = versions.get(i).getAsJsonObject().get("id");
                    if(id.getAsString().equals(version)) {
                        return versions.get(i).getAsJsonObject().get("url").getAsString();
                    }
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a list of the libraries that will be added as dependencies.
     * All the libraries are formatted as maven paths (group.sub:name:version).
     * @param jsons The JSON files that the libraries are parsed from. Two JSONS are parsed: Minecraft's Version JSON created by
     *              Mojang, and a jar dependencies JSON file for libraries that are stored in the Minecraft JAR file (usually sound libraries).
     * @return The list of libraries.
     * @throws IOException
     */
    public static Map<String, String> getLibraryMap(List<File> jsons) throws IOException {
        Map<String, String> map = new HashMap<>();

        for(File json : jsons) {
            JsonObject object = Util.getJsonAsObject(json);
            JsonArray libraries = object.getAsJsonArray("libraries");

            for(int i = 0; i < libraries.size(); i++) {
                JsonObject entry = libraries.get(i).getAsJsonObject();
                JsonObject artifacts = entry.getAsJsonObject("downloads").getAsJsonObject("artifact");
                if (artifacts != null) {
                    String path = artifacts.get("path").getAsString();
                    String url = artifacts.get("url").getAsString();
                    map.put(url, path);
                }
            }
        }
        return map;
    }

    /**
     * Gets a list of the native URLs from the JSON.
     * @param json The Minecraft version JSON.
     * @return The list of URLs.
     * @throws IOException
     */
    public static List<URL> getNativeUrls(File json) throws IOException {
        JsonObject jsonObj = Util.getJsonAsObject(json);
        JsonArray libraries = jsonObj.getAsJsonArray("libraries");
        List<URL> urls = new ArrayList<>();

        for (int i = 0; i < libraries.size(); i++) {
            String name = libraries.get(i).getAsJsonObject().get("name").getAsString();
            JsonObject entry = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
            if(entry.has("classifiers")) {
                String os = Util.getOSName();
                JsonObject natives = entry.getAsJsonObject("classifiers").getAsJsonObject("natives-" + os);
                if (natives != null && isAllowed(name)) {
                    URL url = new URL(natives.get("url").getAsString());
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * Gets the URL to the asset index from within the version JSON, which contains hashes that correspond to
     * Minecraft's resources (ones not already present within the JAR).
     * @param json The Minecraft version JSON.
     * @return The asset index URL.
     * @throws IOException
     */
    public static URL getAssetIndexUrl(File json) throws IOException {
        JsonObject jsonObj = Util.getJsonAsObject(json);
        return new URL(jsonObj.getAsJsonObject("assetIndex").get("url").getAsString());
    }

    /**
     * Gets the URL for Minecraft's JAR file(s).
     * @param json The Minecraft Version JSON.
     * @param side The game side. Acceptable inputs are "client" and "server", though older versions may not have the server JAR in their
     *             JSONs.
     * @return The URL to the JAR file.
     * @throws IOException
     */
    public static String getJarUrl(File json, String side) {
        try {
            if (json.exists()) {
                JsonObject jsonObj = Util.getJsonAsObject(json);
                JsonObject sideObj = jsonObj.getAsJsonObject("downloads").getAsJsonObject(side);
                return sideObj.get("url").getAsString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filters through the correct LWJGL version to download. All libraries are passed through this method in above methods, but any non-LWJGL library
     * will get skipped through the first if statement.
     */
    public static boolean isAllowed(String name) {
        if (!name.contains("org.lwjgl")) {
            return true;
        }
        return (OperatingSystem.current().isMacOsX()) ? name.contains(LWJGL_MAC_VERSION) : name.contains(LWJGL_VERSION);
    }
}
