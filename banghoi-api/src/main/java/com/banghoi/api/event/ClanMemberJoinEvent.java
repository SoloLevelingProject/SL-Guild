package com.banghoi.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player joins a clan.
 */
public class ClanMemberJoinEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String playerName;
    private final String clanName;

    public ClanMemberJoinEvent(String playerName, String clanName) {
        this.playerName = playerName;
        this.clanName = clanName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getClanName() {
        return clanName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
