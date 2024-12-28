package com.spectrasonic.CodeWhitelist.Model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PlayerVerification {
    private final Plugin plugin;
    private final FileConfiguration config;

    @Getter @Setter
    private Map<UUID, ItemStack[]> storedInventories = new HashMap<>();

    @Getter @Setter
    private Map<String, Boolean> frozenPlayers = new HashMap<>();

    public PlayerVerification(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public boolean isPlayerVerified(Player player) {
        String playerIp = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        boolean ipCheckEnabled = config.getBoolean("settings.ip-check", true);

        return !ipCheckEnabled ||
                config.getStringList("players").contains(player.getName() + ":" + playerIp);
    }

    public void verifyPlayer(Player player, String serverCode) {
        String playerIp = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        List<String> playerList = config.getStringList("players");
        playerList.add(player.getName() + ":" + playerIp);
        config.set("players", playerList);
        plugin.saveConfig();
    }

    public void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId().toString(), true);
        player.setWalkSpeed(0f);
    }

    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId().toString());
        player.setWalkSpeed(0.2f);
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.getOrDefault(player.getUniqueId().toString(), false);
    }

    public void storeAndClearInventory(Player player) {
        UUID playerId = player.getUniqueId();
        storedInventories.put(playerId, player.getInventory().getContents());
        player.getInventory().clear();
    }

    public void restoreInventory(Player player) {
        UUID playerId = player.getUniqueId();
        if (storedInventories.containsKey(playerId)) {
            player.getInventory().setContents(storedInventories.get(playerId));
            storedInventories.remove(playerId);
        }
    }
}