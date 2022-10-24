package com.mikael.mkutilslegacy.api.mkplugin

import net.eduard.api.lib.config.Config
import net.eduard.api.lib.plugin.IPluginInstance

interface MKPlugin : IPluginInstance {

     val isFree: Boolean

     /**
      * The [Config] lang files of the plugin.
      * Remember that the first argument must be config en-US and the second must be pt-BR
      *
      * @author KoddyDev
      */
     val langConfigs: MutableMap<String, Config>

     fun log(msg: String)
}