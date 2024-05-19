# mkUtilsLegacy - by Mikael, Vinícius, and Eduard

An utility plugin/library for Minecraft Kotlin developers, targeting legacy Minecraft versions (Paper 1.8.8-R0.1).

## Features

mkUtilsLegacy offers a variety of APIs and utility systems, including:

- **MKPlugin** (All)
- **GlobalExtensions** (All)
- **MineCooldown** (All)
- **RedisAPI** (All)
- **RedisBungeeAPI** (All)
- **StorageAPI** (All)
- **BungeeExtensions** (Proxy-side)  
  Contains many useful methods such as `ProxiedPlayer.title()`, `ProxiedPlayer.sendMessages(vararg String)`, `ProxiedPlayer.clearChat()`, and more.
- **ProxyCommand** (Proxy-side)  
  Facilitates the easy creation of subcommands.
- **ProxyListener** (Proxy-side)
- **SpigotExtensions** (Spigot-side)  
  Contains many useful methods such as `Entity.enableAI()`, `Chest.open()`, `Block.setDamage()`, and more.
- **MineCommand** (Spigot-side)  
  Facilitates the easy creation of subcommands.
- **MineListener** (Spigot-side)
- **MineItem** (Spigot-side)
- **MineMenu** (Spigot-side)  
  One of the more exciting APIs offered by mkUtils. Allows for the creation of menus with animated buttons, auto-pages, per-player settings, auto updates, and more.
- **MineHologram** (Spigot-side)
- **MineBook** (Spigot-side)
- **MineScoreboard** (Spigot-side)
- **PlayerNPCAPI** (Spigot-side)  
  A simple API to create human NPCs using NMS.
- **Vault** (Spigot-side)  
  Integration with the Vault plugin.
- **LocationUtil** (Spigot-side)
- **MineNBT** (Spigot-side)
- **Custom Events** (Spigot-side, Extra)  
  Many custom events that complement the Bukkit API.

## Usage

To use mkUtilsLegacy for development, download `mkUtilsLegacy-version.jar` and `EduardAPI-1.0-all.jar` and add them as dependencies in your project. 

Since this is not just a library but also a plugin, you need to run the plugin shadow jar (`mkUtilsLegacy-version-all.jar`) on your server along with other plugins that depend on it.

## Authors and Acknowledgements

This plugin/library is developed by:
- Mikael (aka MikaelMaster, TheSrMK, SrMK)
- Vinícius (aka Koddy, KoddyDev)

This project utilizes various utility methods and the DB/SQL Manager from MineToolkit, developed by Eduard (aka EduardMaster).

For more information about EduardAPI, visit [MineToolKit on GitHub](https://github.com/EduardMaster/MineToolkit).

*Last updated by Mikael on May 2024.*
