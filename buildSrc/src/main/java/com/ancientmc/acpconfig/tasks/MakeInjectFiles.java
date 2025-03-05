package com.ancientmc.acpconfig.tasks;

import com.ancientmc.acpconfig.util.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class MakeInjectFiles extends DefaultTask {
    @TaskAction
    public void exec() {
        File bugReport = Util.getFile(getBugReport());
        File tsrg = Util.getFile(getTsrg());
        File exceptions = Util.getFile(getExceptions());
        File access = Util.getFile(getAccess());


    }

    @InputFile
    public abstract RegularFileProperty getBugReport();

    @InputFile
    public abstract RegularFileProperty getTsrg();

    @OutputFile
    public abstract RegularFileProperty getExceptions();

    @OutputFile
    public abstract RegularFileProperty getAccess();
}
