package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.UtilsManager
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.database.api.DatabaseElement

@Suppress("WARNINGS")
interface MKPluginData : DatabaseElement {

    override val sqlManager: SQLManager
        get() = UtilsManager.sqlManager

}