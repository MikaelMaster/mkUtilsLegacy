package com.mikael.mkutilslegacy.api.mkplugin

import com.mikael.mkutilslegacy.api.UtilsManager
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.database.api.DatabaseElement

@Suppress("WARNINGS")
@Deprecated("EduardAPI DB System in not used in mkUtils. Exposed is used instead.")
interface MKPluginData : DatabaseElement {

    override val sqlManager: SQLManager
        get() = UtilsManager.sqlManager

}