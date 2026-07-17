package com.banghoi.storage;

import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;

import java.util.List;

public interface PluginStorage {
    ClanData getClanData(String clanName);

    void saveClanData(String clanName, IClanData clanData);

    PlayerData getPlayerData(String playerName);

    boolean savePlayerData(String playerName, IPlayerData playerData);

    boolean deleteClanData(String clanName);

    void addGuildFundTransaction(String clanName, String playerName, String action, long amount, long balanceAfter, long createdAt);

    List<GuildFundTransaction> getGuildFundTransactions(String clanName, int limit);

    List<String> getAllClans();

    List<String> getAllPlayers();

    void disableStorage();
}
