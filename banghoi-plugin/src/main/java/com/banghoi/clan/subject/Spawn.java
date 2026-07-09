package com.banghoi.clan.subject;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.listener.PlayerMovementListener;
import com.banghoi.storage.ClanData;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class Spawn extends SubjectManager {

    public Spawn(Rank rank, Player player, String playerName) {
        super(rank, player, playerName, null, null);
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.SPAWN));

        if (!Settings.CLAN_SETTINGS_SPAWN_SETTINGS_ENABLED) {
            MessageUtil.sendMessage(player, Messages.FEATURE_DISABLED);
            return false;
        }

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();

        if (playerClanData.getSpawnPoint() == null) {
            MessageUtil.sendMessage(player, Messages.UNKNOWN_SPAWN_POINT);
            return false;
        }

        Location spawnPoint = resolveSpawnPointWorld(playerClanData);
        if (spawnPoint.getWorld() == null) {
            MessageUtil.sendMessage(player, Messages.UNKNOWN_SPAWN_POINT);
            return false;
        }

        if (Settings.CLAN_SETTING_SET_SPAWN_BLACKLIST_WORLDS_ENABLED)
            if (Settings.CLAN_SETTING_SET_SPAWN_BLACKLIST_WORLDS_WORLDS.contains(spawnPoint.getWorld().getName())) {
                MessageUtil.sendMessage(player, Messages.CLAN_SPAWN_BLACK_LIST_WORLD);
                return false;
            }

        if (Settings.CLAN_SETTING_SPAWN_SETTINGS_COUNTDOWN_ENABLED) {
            if (PlayerMovementListener.spawnCountDownPlayers.contains(player)) return false;
            PlayerMovementListener.spawnCountDownPlayers.add(player);

            AtomicInteger countDownSeconds = new AtomicInteger(Settings.CLAN_SETTING_SPAWN_SETTINGS_COUNTDOWN_SECONDS);
            MessageUtil.sendMessage(player, Messages.SPAWN_POINT_COUNT_DOWN.replace("%seconds%", String.valueOf(countDownSeconds.get())));
            BangHoi.support.getFoliaLib().getScheduler().runAtEntityTimer(player, task -> {
                if (!PlayerMovementListener.spawnCountDownPlayers.contains(player)) {
                    MessageUtil.sendMessage(player, Messages.MOVE_WHILE_SPAWNING);
                    task.cancel();
                    return;
                }

                countDownSeconds.set(countDownSeconds.get() - 1);
                if (countDownSeconds.get() <= 0) {
                    PlayerMovementListener.spawnCountDownPlayers.remove(player);
                    spawn();
                    task.cancel();
                }
            }, 1, 20L);
        } else {
            spawn();
            return true;
        }

        return true;
    }

    public void spawn() {
        IClanData playerClanData = getPlayerClanData();
        Location spawnPoint = resolveSpawnPointWorld(playerClanData);
        if (spawnPoint == null || spawnPoint.getWorld() == null) {
            MessageUtil.sendMessage(player, Messages.UNKNOWN_SPAWN_POINT);
            return;
        }
        MessageUtil.sendMessage(player, Messages.SPAWN_SUCCESS);
        if (BangHoi.support.getFoliaLib().isSpigot() || BangHoi.support.getFoliaLib().isUnsupported()) {
            getPlayer().teleport(spawnPoint);
            return;
        }
        if (BangHoi.support.getFoliaLib().isPaper() || BangHoi.support.getFoliaLib().isFolia())
            getPlayer().teleportAsync(spawnPoint);
    }

    private static Location resolveSpawnPointWorld(IClanData clanData) {
        Location spawnPoint = clanData.getSpawnPoint();
        if (spawnPoint == null || spawnPoint.getWorld() != null || !(clanData instanceof ClanData cd)) {
            return spawnPoint;
        }
        String worldName = cd.getSpawnWorldName();
        if (worldName == null || worldName.isBlank()) {
            return spawnPoint;
        }
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            spawnPoint.setWorld(world);
            clanData.setSpawnPoint(spawnPoint);
        }
        return spawnPoint;
    }
}
