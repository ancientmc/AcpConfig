package com.ancientmc.cuneiform.util.jar;

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

    public MinecraftJar(File file) {
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
                ClassNode superClass = getSuperClass(zip, classNode);
                String[] exclude = {"com/jcraft", "paulscode/sound"};

                if (!classNode.sourceFile.contains(Arrays.stream(exclude).findAny().get())) {
                    classes.add(new Types.Clazz(classNode.name, classNode.superName));

                    for (FieldNode fieldNode : classNode.fields) {
                        fields.add(new Types.Field(classNode.name, fieldNode.desc, fieldNode.name));
                    }

                    for (MethodNode methodNode : classNode.methods) {
                        boolean inherited = isInherited(superClass, methodNode);
                        String superParent = inherited ? superClass.name : "";
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

    public ClassNode getSuperClass(ZipFile zip, ClassNode node) {
        try {
            ClassNode superClass = new ClassNode();
            ZipEntry entry = zip.getEntry(node.superName + ".class");
            if (entry != null) {
                ClassReader reader = new ClassReader(zip.getInputStream(entry));
                reader.accept(superClass, 0);
                if (!superClass.name.equals("java/lang/Object")) {
                    return superClass;
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInherited(ClassNode superClass, MethodNode methodNode) {
        if (superClass != null) {
            if (!methodNode.name.equals("<init>")) {
                return superClass.methods.stream().anyMatch(m -> m.desc.equals(methodNode.desc) && m.name.equals(methodNode.name));
            }
        }
        return false;
    }
}
