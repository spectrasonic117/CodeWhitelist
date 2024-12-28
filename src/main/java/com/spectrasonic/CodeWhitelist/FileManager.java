package com.spectrasonic.CodeWhitelist;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@RequiredArgsConstructor
public class FileManager {
    private final Plugin plugin;

    /**
     * Crea un archivo desde los recursos del plugin si no existe
     * @param fileName nombre del archivo a crear
     * @return true si se creó correctamente o ya existía
     */
    public boolean createFileFromResource(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (file.exists()) return true;

        file.getParentFile().mkdirs();
        try (InputStream in = plugin.getResource(fileName)) {
            if (in == null) {
                plugin.getLogger().warning("Resource not found: " + fileName);
                return false;
            }
            Files.copy(in, file.toPath());
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Error to create file: " + fileName);
            return false;
        }
    }
}