package com.mikael.mkutilslegacy.api

import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.database.annotations.TableName

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