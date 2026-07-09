package com.banghoi.support;

import com.banghoi.BangHoi;
import com.banghoi.util.MessageUtil;

public class DiscordSupport {

    String SOFT_DEPEND_DISCORDWEBHOOK_URL;

    public DiscordSupport(String webHookUrl) {
        SOFT_DEPEND_DISCORDWEBHOOK_URL = webHookUrl;
    }

    public void sendMessage(String message) {
        DiscordWebhook discordWebhook = new DiscordWebhook(SOFT_DEPEND_DISCORDWEBHOOK_URL);
        if (SOFT_DEPEND_DISCORDWEBHOOK_URL == null || SOFT_DEPEND_DISCORDWEBHOOK_URL.equals("")) return;

        BangHoi.support.getFoliaLib().getScheduler().runAsync(wrappedTask -> {
            discordWebhook.addEmbed(new DiscordWebhook.EmbedObject().setDescription(message));
            try {
                discordWebhook.execute();
            } catch (Exception exception) {
                MessageUtil.throwErrorMessage("[Discord Web Hook] Occur an error while trying to connect to discord web hook! (" + exception.getMessage() + ")");
            }
        });
    }

    public void sendMessage(DiscordWebhook.EmbedObject embedObject) {
        DiscordWebhook discordWebhook = new DiscordWebhook(SOFT_DEPEND_DISCORDWEBHOOK_URL);
        if (SOFT_DEPEND_DISCORDWEBHOOK_URL == null || SOFT_DEPEND_DISCORDWEBHOOK_URL.equals("")) return;

        BangHoi.support.getFoliaLib().getScheduler().runAsync(wrappedTask -> {
            discordWebhook.addEmbed(embedObject);
            try {
                discordWebhook.execute();
            } catch (Exception exception) {
                MessageUtil.throwErrorMessage("[Discord Web Hook] Occur an error while trying to connect to discord web hook! (" + exception.getMessage() + ")");
            }
        });
    }
}
