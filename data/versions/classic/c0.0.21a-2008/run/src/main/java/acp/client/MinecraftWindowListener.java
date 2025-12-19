package acp.client;

import com.mojang.minecraft.Minecraft;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

/**
 * Class based on similar class used in RetroMCP https://github.com/mcphackers/mcpHackers.github.io/versions
 */
public class MinecraftWindowListener extends WindowAdapter {
    private final Minecraft minecraft;
    private final Thread thread;

    public MinecraftWindowListener(Minecraft minecraft, Thread thread) {
        this.minecraft = minecraft;
        this.thread = thread;
    }

    public void windowClosing(WindowEvent e) {
        try {
            Field running = minecraft.getClass().getDeclaredField("running");
            running.setAccessible(true);
            running.set(minecraft, false);
            thread.join();

            System.exit(0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}