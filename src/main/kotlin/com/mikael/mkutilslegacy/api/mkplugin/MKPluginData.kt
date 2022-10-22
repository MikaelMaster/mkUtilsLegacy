package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.utilsmanager
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.database.api.DatabaseElement

interface MKPluginData : DatabaseElement {

    override val sqlManager: SQLManager
        get() = utilsmanager.sqlManager

}