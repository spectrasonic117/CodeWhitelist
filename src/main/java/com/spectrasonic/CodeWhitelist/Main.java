package com.spectrasonic.CodeWhitelist;

import com.spectrasonic.CodeWhitelist.Model.PlayerVerification;
import com.spectrasonic.CodeWhitelist.Utils.MessageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Main extends JavaPlugin implements Listener {
    @Getter
    private LanguageManager languageManager;

    @Getter
    private PlayerVerification playerVerification;

    private String serverCode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupConfigWithCode();

        languageManager = new LanguageManager(this);
        playerVerification = new PlayerVerification(this);

        Bukkit.getPluginManager().registerEvents(this, this);
        MessageUtils.sendStartupMessage(this);


    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

    @EventHandler
    public void onPreLogin(PlayerLoginEvent event) {
        String playerHostname = event.getHostname();
        String playerName = event.getPlayer().getName();

        FileConfiguration config = getConfig();
        boolean allowedLoginIPEnabled = config.getBoolean("settings.allowed-login-ip", false);
        String allowedLoginHostname = config.getString("settings.login-ip", "play.xxx.com");

        if (allowedLoginIPEnabled && !playerHostname.startsWith(allowedLoginHostname)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    languageManager.getMessage("hostname_not_allowed"));
            getLogger().info(languageManager.getMessage("hostname_rejected")
                    .replace("{hostname}", playerHostname)
                    .replace("{player}", playerName));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerIp = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        if (!playerVerification.isPlayerVerified(player)) {
            playerVerification.freezePlayer(player);
            playerVerification.storeAndClearInventory(player);

            player.sendTitle(
                    languageManager.getMessage("title_required_code"),
                    languageManager.getMessage("subtitle_required_code"),
                    10, 100, 10
            );

            player.sendMessage(languageManager.getMessage("frozen_message"));
            getLogger().info(languageManager.getMessage("server_not_verified")
                    .replace("{playerip}", playerIp));
        } else {
            player.sendMessage(languageManager.getMessage("ip_verified"));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerVerification.isFrozen(player)) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (playerVerification.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player &&
                playerVerification.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player &&
                playerVerification.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player &&
                playerVerification.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(languageManager.getMessage("cannot_attack"));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (playerVerification.isFrozen(player)) {
            if (command.startsWith("/code ")) {
                return;
            }

            event.setCancelled(true);
            player.sendMessage(languageManager.getMessage("command_blocked"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("code")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(languageManager.getMessage("command_only_players"));
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(languageManager.getMessage("usage_command"));
                return true;
            }

            if (args[0].equals(serverCode)) {
                playerVerification.unfreezePlayer(player);
                playerVerification.restoreInventory(player);
                playerVerification.verifyPlayer(player, serverCode);

                sender.sendMessage(languageManager.getMessage("verification_success"));
            } else {
                sender.sendMessage(languageManager.getMessage("invalid_code"));
            }
            return true;
        }

        return false;
    }

    private void setupConfigWithCode() {
        FileConfiguration config = getConfig();

        if (!config.contains("server-code") || Objects.requireNonNull(config.getString("server-code")).isEmpty()) {
            serverCode = generateRandomCode();
            config.set("server-code", serverCode);
            saveConfig();
        } else {
            serverCode = config.getString("server-code");
        }

        if (!config.contains("players")) {
            config.set("players", new ArrayList<>());
        }

        saveConfig();
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}