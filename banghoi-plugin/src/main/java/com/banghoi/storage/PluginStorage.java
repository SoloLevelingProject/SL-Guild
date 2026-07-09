package com.banghoi.storage;

import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;

import java.util.List;

public interface PluginStorage {
    ClanData getClanData(String clanName);

    void saveClanData(String clanName, IClanData clanData);

    PlayerData getPlayerData(String playerName);

    void savePlayerData(String playerName, IPlayerData playerData);

    boolean deleteClanData(String clanName);

    List<String> getAllClans();

    List<String> getAllPlayers();

    void disableStorage();
}
