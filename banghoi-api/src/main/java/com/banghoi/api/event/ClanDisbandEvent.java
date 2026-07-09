package com.banghoi.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClanDisbandEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String clanName;

    public ClanDisbandEvent(String clanName) {
        this.clanName = clanName;
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
