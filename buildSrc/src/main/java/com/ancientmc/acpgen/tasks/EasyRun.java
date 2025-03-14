package com.ancientmc.acpgen.tasks;

import com.ancientmc.acpgen.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class EasyRun extends DefaultTask {

    @TaskAction
    void exec() {
        try {
            String runName = getRunName().get();
            JsonObject json = Util.getJson(getProject().file("easyruns.json"));
            String newVersion = getProject().getProperties().get("new_version").toString();
            JsonArray run = json.getAsJsonArray(runName);
            List<String> entries = new ArrayList<>();

            run.asList().forEach(e -> {
                String entry = e.getAsJsonPrimitive().getAsString();
                entry = entry.replace("{new_version}", newVersion);
                entries.add(entry);
            });

            for (String entry : entries) {
                if (entry.contains(":")) {
                    String[] split = entry.split(":");
                    execSubTask(newVersion, getProject(), split[1]);
                } else {
                    Task task = getProject().getTasks().findByName(entry);
                    execTask(task);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void execTask(Task task) {
        if (task != null) {
            task.getProject().getLogger().lifecycle("Running task -> " + task.getName());
            task.getActions().forEach(a -> a.execute(task));
        }
    }

    public static void execSubTask(String newVersion, Project root, String name) {
        Project project = root.getAllprojects().stream().filter(p -> p.getName().equals(newVersion)).findAny().orElse(root);
        Task task = project.getTasks().findByName(name);
        execTask(task);
    }

    @Input
    public abstract Property<String> getRunName();
}
