package com.mikael.mkutilslegacy.api

import net.eduard.api.lib.database.SQLManager

class UtilsManager {
    companion object {
        lateinit var instance: UtilsManager
    }

    init {
        instance = this@UtilsManager
    }

    val dbManager get() = sqlManager.dbManager
    lateinit var sqlManager: SQLManager
    lateinit var mkUtilsVersion: String

}