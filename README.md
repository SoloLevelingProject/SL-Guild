## SL-Guild

SL-Guild is a Minecraft guild plugin for SoloLevelingProject servers.

## Requirements

- Spigot/Paper/Folia compatible server
- Java 16+

## Features

- Guild creation, disband, invites, leave, kick, owner/manager management
- Guild GUI menus, member lists, guild list, settings, upgrades
- Guild spawn, ally management, PvP toggle, guild chat, admin chat spy
- Guild contribution by Vault money with TurtleTop point sync
- YAML, H2, and SQLite storage
- PlaceholderAPI support with `guild` identifier
- PlayerPoints and Vault support for upgrade/create costs

## Soft Depends

- PlaceholderAPI
- Vault
- PlayerPoints
- TurtleTop, optional for point-backed guild score/contribution

## Commands

- `/guild`
- `/guildadmin` aliases: `/guildad`

## Permissions

- `guild.admin` for `/guildadmin`
- `guild.setspawn`
- `guild.seticon`
- `guild.setpermission`
- `guild.setcustomname`
- `guild.setmessage`

## PlaceholderAPI

Use `%guild_*%` placeholders.

Examples:

- `%guild_name%`
- `%guild_formatname%`
- `%guild_score%`
- `%guild_player_rank%`
- `%guild_top_score_name_1%`
- `%guild_top_score_value_1%`

## API

Internal API package currently remains `com.banghoi.api` for code stability.

```java
BangHoiAPI api = Bukkit.getServicesManager().getRegistration(BangHoiAPI.class).getProvider();
```
