package com.banghoi.api.storage;

import com.banghoi.api.enums.Rank;

public interface IPlayerData {

    String getPlayerName();

    void setPlayerName(String playerName);

    String getUUID();

    void setUUID(String UUID);

    String getClan();

    void setClan(String clan);

    Rank getRank();

    void setRank(Rank rank);

    long getJoinDate();

    void setJoinDate(long joinDate);

    long getScoreCollected();

    void setScoreCollected(long scoreCollected);

    long getLastActivated();

    void setLastActivated(long lastActivated);

    long getLastContributeTime();

    void setLastContributeTime(long lastContributeTime);

    int getMoneyContributeCountToday();

    void setMoneyContributeCountToday(int count);

}
