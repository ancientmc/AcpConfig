package com.ancientmc.cuneiform.util;

public class Paths {
    public static String VERSION_MANIFEST;
    public static String JSON;
    public static String BASE_JAR;
    public static String SLIM_JAR;
    public static String EXTRA_JAR;
    public static String INJECT_JAR;
    public static String SRG_JAR;
    public static String JARDEP_JSON;
    public static String INTERMEDIATE_TSRG;
    public static String EXC;
    public static String BLACKLIST;


    public Paths(String version) {
        VERSION_MANIFEST = "data/version_manifest.json";
        JSON = "data/versions/" + version + "/game/" + version + ".json";
        BASE_JAR = "data/versions/" + version + "/game/jars/" + version + ".jar";
        SLIM_JAR = "data/versions/" + version + "/game/jars/" + version + "-slim.jar";
        EXTRA_JAR = "data/versions/" + version + "/game/jars/" + version + "-extra.jar";
        INJECT_JAR = "data/versions/" + version + "/game/jars/" + version + "-inject.jar";
        SRG_JAR = "data/versions/" + version + "/game/jars/" + version + "-srg.jar";

        JARDEP_JSON = "data/jardep.json";
        INTERMEDIATE_TSRG = "data/versions/" + version + "/mappings/intermediate.tsrg";
        EXC = "data/versions/" + version + "/inject/exceptions.txt";
        BLACKLIST = "data/versions/" + version + "/inject/blacklist.txt";
    }
}
