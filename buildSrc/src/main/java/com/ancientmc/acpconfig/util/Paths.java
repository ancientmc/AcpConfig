package com.ancientmc.acpconfig.util;

public class Paths {
    public static String DIR_VERSION;
    public static String VERSION_MANIFEST;
    public static String JSON;
    public static String BASE_JAR;
    public static String SLIM_JAR;
    public static String EXTRA_JAR;
    public static String INJECT_JAR;
    public static String SRG_JAR;
    public static String CNF_JAR;
    public static String JARDEP_JSON;
    public static String INTERMEDIATE_TSRG;
    public static String CUNEIFORM_TSRG;
    public static String EXC;
    public static String BLACKLIST;
    public static String INHERITANCE;
    public static String CLASS_CSV;
    public static String FIELD_CSV;
    public static String METHOD_CSV;
    public static String PARAM_CSV;


    public Paths(String version) {
        DIR_VERSION = String.join("/", "data/versions", getPhase(version), version); // data/versions/${phase}/${version}
        VERSION_MANIFEST = "data/version_manifest.json";
        JSON = DIR_VERSION + "/game/" + version + ".json";
        BASE_JAR = DIR_VERSION + "/game/jars/" + version + ".jar";
        SLIM_JAR = DIR_VERSION + "/game/jars/" + version + "-slim.jar";
        EXTRA_JAR = DIR_VERSION + "/game/jars/" + version + "-extra.jar";
        INJECT_JAR = DIR_VERSION + "/game/jars/" + version + "-inject.jar";
        SRG_JAR = DIR_VERSION + "/game/jars/" + version + "-srg.jar";
        CNF_JAR = DIR_VERSION + "/game/jars/" + version + "-cnf.jar";

        JARDEP_JSON = "data/jardep.json";
        INTERMEDIATE_TSRG = DIR_VERSION + "/mappings/intermediate.tsrg";
        CUNEIFORM_TSRG = DIR_VERSION + "/mappings/cuneiform.tsrg";
        EXC = DIR_VERSION + "/inject/exceptions.txt";
        BLACKLIST = DIR_VERSION + "/inject/blacklist.txt";
        INHERITANCE = DIR_VERSION + "/inheritance_map.json";
        CLASS_CSV = DIR_VERSION + "/mappings/csv/classes.csv";
        FIELD_CSV = DIR_VERSION + "/mappings/csv/fields.csv";
        METHOD_CSV = DIR_VERSION + "/mappings/csv/methods.csv";
        PARAM_CSV = DIR_VERSION + "/mappings/csv/params.csv";
    }

    public static String getPhase(String version) {
        if (version.startsWith("c")) {
            return "classic";
        } else if (version.startsWith("in-")) {
            return "indev";
        } else if (version.startsWith("inf-")) {
            return "infdev";
        } else if (version.startsWith("a")) {
            return "alpha";
        } else if (version.startsWith("b")) {
            return "beta";
        } else if (version.matches("[0-9]+w[0-9]+[a-z]+")) { // snapshot version format YYwWWx
            return "snapshot";
        } else {
            return "release";
        }
    }
}
