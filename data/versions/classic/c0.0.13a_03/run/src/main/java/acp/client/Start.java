package acp.client;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.client.UserSession;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;

public class Start {
    public static void main(String[] args) {
        String username = "Player" + System.currentTimeMillis() % 1000L;
        if (args.length > 0) {
            username = args[0];
        }

        String sessionId = "-";
        if (args.length > 1) {
            sessionId = args[1];
        }

        startThread(username, sessionId);
    }

    public static void startThread(String username, String sessionId) {
        boolean fullscreen = false;
        Frame frame = new Frame("Minecraft");
        Canvas canvas = new Canvas();
        frame.setLayout(new BorderLayout());
        frame.add(canvas, "Center");
        canvas.setPreferredSize(new Dimension(854, 480));
        frame.pack();
        frame.setLocationRelativeTo(null);
        Minecraft minecraft = new Minecraft(canvas, 854, 480, fullscreen);

        if (username != null && sessionId != null) {
            minecraft.session = new UserSession(username, sessionId);
        } else {
            minecraft.session = new UserSession("Player" + System.currentTimeMillis() % 1000L, "");
        }

        minecraft.width = 854;
        minecraft.height = 480;
        Thread thread = new Thread(minecraft, "Minecraft main thread");
        thread.setPriority(10);
        frame.setVisible(true);
        frame.addWindowListener(new MinecraftWindowListener(minecraft, thread));
        thread.start();
    }
}