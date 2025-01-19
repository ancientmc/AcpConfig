package com.ancientmc.cuneiform.util.jar;

public class Types {
    public static class Clazz {
        public String name;
        public String superName;

        public Clazz(String name, String superName) {
            this.name = name;
            this.superName = superName;
        }
    }

    public static class Field {
        public String parent;
        public String type;
        public String name;

        public Field(String parent, String type, String name) {
            this.parent = parent;
            this.type = type;
            this.name = name;
        }
    }

    public static class Method {
        public String parent;
        public String superParent;
        public String desc;
        public String name;
        public int params;
        public boolean inherited;

        public Method(String parent, String superParent, String desc, String name, int params, boolean inherited) {
            this.parent = parent;
            this.superParent = superParent;
            this.desc = desc;
            this.name = name;
            this.params = params;
            this.inherited = inherited;
        }
    }
}
