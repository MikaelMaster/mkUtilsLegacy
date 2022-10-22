package com.mikael.mkutilslegacy.api.mkplugin

import net.eduard.api.lib.plugin.IPluginInstance

interface MKPlugin : IPluginInstance {

     val isFree: Boolean
     fun log(msg: String)
}