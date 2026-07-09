package com.banghoi.listener;

import com.banghoi.BangHoi;
import com.banghoi.clan.ClanManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityDamageListener implements Listener {

    private static final long PVP_DENY_COOLDOWN_MS = 15_000; // 15 seconds
    private final HashMap<UUID, Long> pvpDenyMessageCooldown = new HashMap<>();

    public EntityDamageListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    private boolean canSendPvPDenyMessage(Player player) {
        long now = System.currentTimeMillis();
        Long lastSent = pvpDenyMessageCooldown.get(player.getUniqueId());
        if (lastSent != null && (now - lastSent) < PVP_DENY_COOLDOWN_MS) {
            return false;
        }
        pvpDenyMessageCooldown.put(player.getUniqueId(), now);
        return true;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER || event.getEntity().getType() != EntityType.PLAYER)
            return;

        // Check clan PVP --
        Player damager = (Player) event.getDamager();
        Player entity = (Player) event.getEntity();

        if (!ClanManager.isPlayerInClan(damager) || !ClanManager.isPlayerInClan(entity))
            return;

        int checkEvent = 0;

        String victimClanName = PluginDataManager.getPlayerDatabase(entity.getName()).getClan();
        if (PluginDataManager.getPlayerDatabase(damager.getName()).getClan().equals(victimClanName)) {
            if (!ClanManager.getPlayerTogglingPvP().contains(damager))
                checkEvent = 1;
            if (!ClanManager.getPlayerTogglingPvP().contains(entity))
                checkEvent = 2;
        } else {
            // check if entity's clan is an ally of damager's clan
            List<String> damagerClanAllies = PluginDataManager.getClanDatabaseByPlayerName(damager.getName())
                    .getAllies();
            if (damagerClanAllies.isEmpty())
                return;
            if (damagerClanAllies.contains(victimClanName)) {
                if (!ClanManager.getPlayerTogglingPvP().contains(damager))
                    checkEvent = 1;
                if (!ClanManager.getPlayerTogglingPvP().contains(entity))
                    checkEvent = 2;
            }
        }

        if (checkEvent > 0) {
            event.setCancelled(true);
            if (canSendPvPDenyMessage(damager)) {
                if (checkEvent == 1) {
                    MessageUtil.sendMessage(damager, Messages.CLAN_MEMBER_PVP_DENY);
                    return;
                }
                MessageUtil.sendMessage(damager,
                        Messages.CLAN_MEMBER_PVP_DENY_VICTIM.replace("%player%", entity.getName()));
            }
        }
        // --
    }

    @EventHandler
    public void onShooting(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (event.getEntity() == null || damager == null)
            return;

        try {
            if (damager instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player shooter) {
                    Damageable damageableVictim = (Damageable) event.getEntity();
                    if (damageableVictim instanceof Player victim) {

                        if (shooter.getName().equals(victim.getName()))
                            return;

                        if (!ClanManager.isPlayerInClan(shooter) || !ClanManager.isPlayerInClan(victim))
                            return;

                        int checkEvent = 0;

                        String victimClanName = PluginDataManager.getPlayerDatabase(victim.getName()).getClan();
                        if (PluginDataManager.getPlayerDatabase(shooter.getName()).getClan().equals(victimClanName)) {
                            if (!ClanManager.getPlayerTogglingPvP().contains(shooter))
                                checkEvent = 1;
                            if (!ClanManager.getPlayerTogglingPvP().contains(victim))
                                checkEvent = 2;
                        } else {
                            // check if entity's clan is an ally of damager's clan
                            List<String> damagerClanAllies = PluginDataManager
                                    .getClanDatabaseByPlayerName(shooter.getName()).getAllies();
                            if (damagerClanAllies.isEmpty())
                                return;
                            if (damagerClanAllies.contains(victimClanName)) {
                                if (!ClanManager.getPlayerTogglingPvP().contains(shooter))
                                    checkEvent = 1;
                                if (!ClanManager.getPlayerTogglingPvP().contains(victim))
                                    checkEvent = 2;
                            }
                        }

                        if (checkEvent > 0) {
                            event.setCancelled(true);
                            if (canSendPvPDenyMessage(shooter)) {
                                if (checkEvent == 1) {
                                    MessageUtil.sendMessage(shooter, Messages.CLAN_MEMBER_PVP_DENY);
                                    return;
                                }
                                MessageUtil.sendMessage(shooter,
                                        Messages.CLAN_MEMBER_PVP_DENY_VICTIM.replace("%player%", victim.getName()));
                            }
                        }
                    }
                }
            }
        } catch (Exception exceptions) {
            // ignore
        }
    }

}
