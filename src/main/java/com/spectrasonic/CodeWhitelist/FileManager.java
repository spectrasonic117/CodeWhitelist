package com.spectrasonic.CodeWhitelist;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileManager {
    private final Plugin plugin;

    public FileManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Crea un archivo desde los recursos del plugin si no existe
     *
     * @param fileName nombre del archivo a crear
     */
    public void createFileFromResource(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (file.exists()) return;

        file.getParentFile().mkdirs();
        try (InputStream in = plugin.getResource(fileName)) {
            if (in == null) {
                plugin.getLogger().warning("Resource not found: " + fileName);
                return;
            }
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Error to create file: " + fileName);
        }
    }
}