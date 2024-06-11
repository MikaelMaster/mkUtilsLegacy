package com.mikael.mkutilslegacy.api.db

/**
 * @see DatabaseAPI
 */
class DBInfo(

    var isEnabled: Boolean = false,

    var host: String = "localhost",

    var port: Int = 3306,

    var dbEngine: DBEngine = DBEngine.MYSQL,

    var dbName: String = "mine_database",

    var user: String = "mikael",

    var pass: String = "dummy"

)
