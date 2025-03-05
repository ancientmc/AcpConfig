package acp.client;

import com.mojang.minecraft.Minecraft;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MinecraftWindowListener extends WindowAdapter {
    private final Minecraft minecraft;
    private final Thread thread;

    public MinecraftWindowListener(Minecraft minecraft, Thread thread) {
        this.minecraft = minecraft;
        this.thread = thread;
    }

    public void windowClosing() {
        try {
            Field running = minecraft.getClass().getDeclaredField("running");
            running.setAccessible(true);
            running.set(minecraft, false);
            thread.join();

            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}