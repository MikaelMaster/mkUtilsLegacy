package com.mikael.mkutilslegacy.api.mkplugin.language

@Suppress("WARNINGS")
enum class Translation(var en_us: List<String>, var pt_br: List<String>) {

    // mkUtils onEnable messages
    LOADING_STARTING(
        listOf("§eStarting loading..."),
        listOf("§eIniciando carregamento...")
    ),
    LOADING_DIRECTORIES(
        listOf("§eLoading directorires..."),
        listOf("§eCarregando diretórios...")
    ),
    LOADING_EXTRAS(
        listOf("§eLoading extras..."),
        listOf("§eCarregando extras...")
    ),
    LOADING_APIS(
        listOf("§eLoading APIs..."),
        listOf("§eCarregando APIs...")
    ),
    LOADING_SYSTEMS(
        listOf("§eLoading systems..."),
        listOf("§eCarregando sistemas...")
    ),
    LOADING_COMPLETE( // Placeholders: %time_taken%
        listOf("§aPlugin loaded with success! (Time taken: §f%time_taken%ms§a)"),
        listOf("§aPlugin carregado com sucesso! (Tempo levado: §f%time_taken%ms§a)")
    ),

    // mkUtils onDisable messages
    UNLOADING_DISCONNECTING_PLAYERS(
        listOf("§eDisconnecting players..."),
        listOf("§eDisconectando jogadores...")
    ),
    UNLOADING_REDISBUNGEEAPI_DISABLING(
        listOf("§6[RedisBungeeAPI] §eMarking server as disabled on Redis..."),
        listOf("§6[RedisBungeeAPI] §eMarcando servidor como desligado no Redis...")
    ),
    UNLOADING_APIS(
        listOf("§eUnloading APIs..."),
        listOf("§eDescarregando APIs...")
    ),
    UNLOADING_SYSTEMS(
        listOf("§eUnloading systems..."),
        listOf("§eDescarregando sistemas...")
    ),
    UNLOADING_COMPLETE(
        listOf("§cPlugin unloaded!"),
        listOf("§cPlugin descarregado!")
    )

}