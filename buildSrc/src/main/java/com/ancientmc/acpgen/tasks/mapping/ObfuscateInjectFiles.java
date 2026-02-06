package com.ancientmc.acpgen.tasks.mapping;

import com.ancientmc.acpgen.util.Util;
import net.minecraftforge.srgutils.IMappingFile;
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
import java.util.ArrayList;
import java.util.List;

public abstract class ObfuscateInjectFiles extends DefaultTask {

    @TaskAction
    public void exec() {
        File tsrg = Util.getFile(getIntermediateTsrg());
        File intermAccess = Util.getFile(getIntermediateAccess());
        File intermExc = Util.getFile(getIntermediateExceptions());
        File obfAccess = Util.getFile(getObfuscatedAccess());
        File obfExc = Util.getFile(getObfuscatedExceptions());

        write(tsrg, intermAccess, obfAccess);
        write(tsrg, intermExc, obfExc);
    }

    public void write(File tsrg, File interm, File obf) {
        try {
            IMappingFile mapping = IMappingFile.load(tsrg);
            List<String> lines = Files.readAllLines(interm.toPath());
            List<String> newLines = new ArrayList<>();

            if (!lines.isEmpty()) {
                for (String line : lines) {
                    line = getNewLine(mapping, line);
                    newLines.add(line);
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(obf))) {
                for (String line : newLines) {
                    writer.write(line + "\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNewLine(IMappingFile mapping, String line) {
        for (IMappingFile.IClass cls : mapping.getClasses()) {
            if (line.contains(cls.getMapped())) {
                line = line.replace(cls.getMapped(), cls.getOriginal());
            }

            for (IMappingFile.IField fld : cls.getFields()) {
                if (line.contains(fld.getMapped())) {
                    line = line.replace(fld.getMapped(), fld.getOriginal());
                }
            }

            for (IMappingFile.IMethod mtd : cls.getMethods()) {
                if (line.contains(mtd.getMapped())) {
                    line = line.replace(mtd.getMapped(), mtd.getOriginal());
                }
            }
        }

        return line;
    }

    @InputFile
    public abstract RegularFileProperty getIntermediateAccess();

    @InputFile
    public abstract RegularFileProperty getIntermediateExceptions();

    @InputFile
    public abstract RegularFileProperty getIntermediateTsrg();

    @OutputFile
    public abstract RegularFileProperty getObfuscatedAccess();

    @OutputFile
    public abstract RegularFileProperty getObfuscatedExceptions();
}
