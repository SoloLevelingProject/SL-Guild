## SL-Guild

SL-Guild is a Minecraft guild plugin for SoloLevelingProject servers.

## Requirements

- Spigot/Paper/Folia compatible server
- Java 16+

## Features

- Guild creation, disband, invites, leave, kick, owner/manager management
- Guild GUI menus, member lists, guild list, settings, upgrades, storage
- Guild spawn, ally management, PvP toggle, guild chat, admin chat spy
- Guild contribution by Vault money with TurtleTop point sync
- YAML, H2, and SQLite storage
- PlaceholderAPI support with `slguild` identifier
- PlayerPoints and Vault support for upgrade/create costs

## Soft Depends

- PlaceholderAPI
- Vault
- PlayerPoints
- TurtleTop, optional for point-backed guild score/contribution

## Commands

- `/clan` aliases: `/slguild`
- `/clanadmin` aliases: `/slguildadmin`, `/slguildad`

## Permissions

- `slguild.admin` for `/clanadmin`
- `slguild.setspawn`
- `slguild.seticon`
- `slguild.setpermission`
- `slguild.setcustomname`
- `slguild.setmessage`

## PlaceholderAPI

Use `%slguild_*%` placeholders.

Examples:

- `%slguild_clan_name%`
- `%slguild_clan_formatname%`
- `%slguild_clan_score%`
- `%slguild_player_rank%`
- `%slguild_top_score_name_1%`
- `%slguild_top_score_value_1%`

## API

Internal API package currently remains `com.banghoi.api` for code stability.

```java
BangHoiAPI api = Bukkit.getServicesManager().getRegistration(BangHoiAPI.class).getProvider();
```
