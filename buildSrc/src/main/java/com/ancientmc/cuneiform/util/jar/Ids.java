package com.ancientmc.cuneiform.util.jar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ids {
    public static Map<Types.Clazz, String> getAllClassIds(List<Types.Clazz> classes) {
        Map<Types.Clazz, String> ids = new HashMap<>();
        for (int i = 0; i < classes.size(); i++) {
            Types.Clazz entry = classes.get(i);

            NumberFormat format = new DecimalFormat("00000");
            String id = format.format(i + 1);
            ids.put(entry, id);
        }
        return ids;
    }

    public static Map<Types.Field, String> getAllFieldIds(List<Types.Field> fields) {
        Map<Types.Field, String> ids = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            Types.Field field = fields.get(i);
            NumberFormat format = new DecimalFormat("00000");
            String id = format.format(i + 1);
            ids.put(field, id);
        }
        return ids;
    }

    public static Map<Types.Method, String> getAllMethodIds(List<Types.Method> methods) {
        Map<Types.Method, String> ids = new HashMap<>();

        List<Types.Method> sortedMethods = methods.stream().filter(m -> !m.inherited).toList();
        for (int i = 0; i < sortedMethods.size(); i++) {
            Types.Method method = sortedMethods.get(i);
            NumberFormat format = new DecimalFormat("00000");
            String id = format.format(i + 1);
            ids.put(method, id);
        }
        return ids;
    }
}
