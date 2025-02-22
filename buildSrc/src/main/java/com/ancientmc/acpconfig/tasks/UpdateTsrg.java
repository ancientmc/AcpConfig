package com.ancientmc.acpconfig.tasks;

import com.ancientmc.acpconfig.util.mapping.Match;
import com.ancientmc.acpconfig.util.mapping.Tsrg;
import com.ancientmc.acpconfig.util.jar.MinecraftJar;
import com.ancientmc.acpconfig.util.jar.Types;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Very similar to WriteTsrg, but also parses the match file for the new JAR version, and assigns new IDs to everything else.
 */
public abstract class UpdateTsrg extends DefaultTask {

    @TaskAction
    public void exec() {
        System.out.println("Well shit, there's nothing here!");
    }

    @InputFile
    public abstract RegularFileProperty getOldTsrg();

    @InputFile
    public abstract RegularFileProperty getMatch();

    @InputFile
    public abstract RegularFileProperty getNewJar();

    @InputFile
    public abstract RegularFileProperty getOldIds();

    @InputFile
    public abstract RegularFileProperty getInheritanceJson();

    @OutputFile
    public abstract RegularFileProperty getNewTsrg();

    @OutputFile
    public abstract RegularFileProperty getNewIds();
}
