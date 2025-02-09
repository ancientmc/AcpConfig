package com.ancientmc.acpconfig.util;

public class Paths {
    public static String DIR_LIBRARIES;
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
        DIR_LIBRARIES = "game/lib/";
        VERSION_MANIFEST = "../../../version_manifest.json";
        JSON = "game/" + version + ".json";
        BASE_JAR = "game/jars/" + version + ".jar";
        SLIM_JAR = "game/jars/" + version + "-slim.jar";
        EXTRA_JAR = "game/jars/" + version + "-extra.jar";
        INJECT_JAR = "game/jars/" + version + "-inject.jar";
        SRG_JAR = "game/jars/" + version + "-srg.jar";
        CNF_JAR = "game/jars/" + version + "-cnf.jar";

        JARDEP_JSON = "data/jardep.json";
        INTERMEDIATE_TSRG = "mappings/intermediate.tsrg";
        CUNEIFORM_TSRG = "mappings/cuneiform.tsrg";
        EXC = "inject/exceptions.txt";
        BLACKLIST = "inject/blacklist.txt";
        INHERITANCE = "inheritance_map.json";
        CLASS_CSV = "mappings/csv/classes.csv";
        FIELD_CSV = "mappings/csv/fields.csv";
        METHOD_CSV = "mappings/csv/methods.csv";
        PARAM_CSV = "mappings/csv/params.csv";
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
