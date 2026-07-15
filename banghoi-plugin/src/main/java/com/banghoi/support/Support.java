package com.banghoi.support;

import com.banghoi.BangHoi;
import com.banghoi.util.MessageUtil;
import com.tcoded.folialib.FoliaLib;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Support {

    public PlayerPointsAPI playerPointsAPI;
    public Economy vaultEconomyAPI;
    public FoliaLib foliaLib;
    public boolean placeholderAPISupported = false;
    public boolean playerPointsSupported = false;
    public boolean vaultSupported = false;

    public boolean isPlaceholderAPISupported() {
        return placeholderAPISupported;
    }

    public boolean isPlayerPointsSupported() {
        return playerPointsSupported;
    }

    public boolean isVaultSupported() {
        return vaultSupported;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public boolean isFoliaLibSupported() {
        return foliaLib.isFolia();
    }

    public void setupSupports() {
        // Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            setupVault();
        }

        // PlayerPoints
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            playerPointsSupported = true;
        }

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPISupport().register();
            placeholderAPISupported = true;
        }

        // FoliaLib
        foliaLib = new FoliaLib(BangHoi.plugin);
        foliaLib.enableInvalidTickValueDebug();
    }

    public boolean setupVault() {
        if (BangHoi.plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = BangHoi.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomyAPI = rsp.getProvider();
        vaultSupported = true;
        return true;
    }

    public Economy getVault() {
        if (vaultEconomyAPI == null) if (!setupVault()) {
            MessageUtil.throwErrorMessage("KHÔNG THỂ TÌM THẤY PLUGIN VAULT");
            return null;
        }
        return vaultEconomyAPI;
    }

}
