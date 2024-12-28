package com.spectrasonic.CodeWhitelist;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LanguageManager {
    private final Plugin plugin;
    private final FileManager fileManager;
    @Getter
    private final Map<String, String> messages = new HashMap<>(); // Mapa de mensajes

    public LanguageManager(Plugin plugin) {
        this.plugin = plugin;
        this.fileManager = new FileManager(plugin);
        setupLanguageFiles();
        loadLanguage();
    }

    /**
     * Configura los archivos de idioma necesarios
     */
    private void setupLanguageFiles() {
        // Crear el directorio de traducciones si no existe
        File translateDir = new File(plugin.getDataFolder(), "translate");
        if (!translateDir.exists()) {
            translateDir.mkdirs();
        }

        // Crear el archivo de idioma inglés por defecto
        fileManager.createFileFromResource("translate/en.yml");
    }

    public void loadLanguage() {
        FileConfiguration langConfig = getLanguageConfig();

        if (langConfig.contains("messages")) {
            for (String key : Objects.requireNonNull(langConfig.getConfigurationSection("messages")).getKeys(false)) {
                messages.put(key, translateToEnglish(langConfig.getString("messages." + key, "Message not found: " + key)));
            }
            plugin.getLogger().info("Language file loaded successfully.");
        } else {
            plugin.getLogger().warning("No 'messages' section found in language file.");
        }
    }

    private String translateToEnglish(String message) {
        // Implementar una traducción básica o dejar el mensaje original
        return message;
    }

    private FileConfiguration getLanguageConfig() {
        String language = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "translate/" + language + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + language + ". Using default.");
            langFile = new File(plugin.getDataFolder(), "translate/en.yml");
        }

        return YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        String rawMessage = messages.getOrDefault(key, "Message not found: " + key);
        return ChatColor.translateAlternateColorCodes('&', rawMessage);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String rawMessage = messages.getOrDefault(key, "Message not found: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rawMessage = rawMessage.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', rawMessage);
    }
}