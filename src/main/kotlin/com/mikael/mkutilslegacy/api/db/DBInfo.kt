package com.mikael.mkutilslegacy.api.db

/**
 * @see DatabaseAPI
 */
data class DBInfo(
    val isEnabled: Boolean = false,
    val host: String = "localhost",
    val port: Int = 3306,
    val dbEngine: DBEngine = DBEngine.MYSQL,
    val dbName: String = "mine_database",
    val user: String = "mikael",
    val pass: String = "dummy"
)
