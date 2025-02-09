package com.ancientmc.acpconfig.tasks;

import com.ancientmc.acpconfig.util.MatchParser;
import com.ancientmc.acpconfig.util.jar.MinecraftJar;
import com.ancientmc.acpconfig.util.jar.Types;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Very similar to WriteTsrg, but also parses the match file for the new JAR version, and assigns new IDs to everything else.
 */
public abstract class UpdateTsrg extends DefaultTask {

    @TaskAction
    public void exec() {
        File match = getMatch().getAsFile().get();
        File newJar = getNewJar().getAsFile().get();
        File inheritance = getInheritanceJson().getAsFile().get();
        File newTsrg = getNewTsrg().getAsFile().get();
        MinecraftJar jar = new MinecraftJar(newJar, inheritance);

        List<String> lines = getLines(jar, newTsrg, match);

        // WIP...
    }

    public List<String> getLines(MinecraftJar jar, File tsrg, File match) {
        List<String> lines = new ArrayList<>();

        // first line
        lines.add("tsrg2 obf cnf\n");

        // Only include Minecraft classes
        String[] exclude = {"com/jcraft", "paulscode/sound"};
        List<Types.Clazz> sortedClasses = jar.classes.stream().filter(c -> Arrays.stream(exclude).noneMatch(c.name::startsWith)).toList();

        sortedClasses.forEach(clazz -> {
           lines.add(clazz.name + " " + getDeobfClass(clazz, match) + "\n");
        });

        // WIP...

        return lines;
    }

    public static String getDeobfClass(Types.Clazz clazz, File match) {
        String old = MatchParser.getOld(clazz.name, match, "c", 1);
        return old != null ? old : clazz.name; // TODO: finish. Make sure new classes get new IDs.
    }

    @InputFile
    public abstract RegularFileProperty getMatch();

    @InputFile
    public abstract RegularFileProperty getNewJar();

    @InputFile
    public abstract RegularFileProperty getInheritanceJson();

    @OutputFile
    public abstract RegularFileProperty getNewTsrg();
}
