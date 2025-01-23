package com.ancientmc.acpconfig.util.jar;

import com.ancientmc.acpconfig.util.Util;
import com.google.gson.JsonObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class allows the creation of a Minecraft JAR object. It lets us get lists of members from the
 */
public class MinecraftJar {
    public List<Types.Clazz> classes;
    public List<Types.Field> fields;
    public List<Types.Method> methods;

    /**
     * The inheritance JSON, input via tasks using this JAR class.
     */
    public File json;

    public MinecraftJar(File file) {
        this(file, null);
    }

    public MinecraftJar(File file, File json) {
        this.json = json;
        classes = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();

        try (ZipFile zip = new ZipFile(file)) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                if(!entry.getName().endsWith(".class")) {
                    continue;
                }
                ClassReader reader = new ClassReader(zip.getInputStream(entry));
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);

                // Exclude non-Minecraft classes from the JAR object.
                String[] exclude = {"com/jcraft", "paulscode/sound"};

                if (!classNode.sourceFile.contains(Arrays.stream(exclude).findAny().get())) {
                    classes.add(new Types.Clazz(classNode.name, classNode.superName));

                    for (FieldNode fieldNode : classNode.fields) {
                        fields.add(new Types.Field(classNode.name, fieldNode.desc, fieldNode.name));
                    }

                    for (MethodNode methodNode : classNode.methods) {
                        String superParent = getSuperParent(json, classNode.name, methodNode.name, methodNode.desc);
                        boolean inherited = superParent != null;
                        methods.add(new Types.Method(classNode.name, superParent, methodNode.desc, methodNode.name, getParameters(methodNode), inherited));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getParameters(MethodNode methodNode) {
        return Type.getArgumentTypes(methodNode.desc).length;
    }

    // TODO: This works but is slow, maybe preload the JSON and then add?
    public static String getSuperParent(File file, String className, String methodName, String methodDesc) {
        try {
            JsonObject json = Util.getJsonAsObject(file);
            JsonObject methods = json.getAsJsonObject(className).getAsJsonObject("methods");
            if (methods != null) {
                JsonObject method = methods.getAsJsonObject(methodName + " " + methodDesc);
                if (method.get("override") != null) {

                    // Some methods have overrides that are descendants of JDK classes. We want to treat these as having no parents.
                    if (method.get("override").getAsString().contains("java")) {
                        return null;
                    }
                    return method.get("override").getAsString();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
