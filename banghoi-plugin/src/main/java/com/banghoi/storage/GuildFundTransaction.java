package com.banghoi.storage;

public class GuildFundTransaction {
    private final String clanName;
    private final String playerName;
    private final String action;
    private final long amount;
    private final long balanceAfter;
    private final long createdAt;

    public GuildFundTransaction(String clanName, String playerName, String action, long amount, long balanceAfter, long createdAt) {
        this.clanName = clanName;
        this.playerName = playerName;
        this.action = action;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public String getClanName() {
        return clanName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAction() {
        return action;
    }

    public long getAmount() {
        return amount;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
