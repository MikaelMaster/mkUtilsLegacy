package com.mikael.mkutilslegacy.api.db

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import org.jetbrains.exposed.sql.Database

@Suppress("WARNINGS")
object DatabaseAPI {

    private lateinit var _usingDBInfo: DBInfo
    var usingDBInfo
        get() = _usingDBInfo
        internal set(value) {
            _usingDBInfo = value
        }
    val usingDBEngine get() = usingDBInfo.dbEngine

    private lateinit var _exposedDB: Database
    val exposedDB get() = _exposedDB

    val isInitialized get() = this::_usingDBInfo.isInitialized && this::_exposedDB.isInitialized

    private fun log(vararg msg: String) {
        (if (isProxyServer) utilsMain else utilsBungeeMain).log(*msg)
    }

    internal fun loadDatabaseAPI(): Boolean {
        return try {
            val config = if (isProxyServer) utilsMain.config else utilsBungeeMain.config
            val dbInfo = config.get("Database", DBInfo::class.java)
            when (usingDBEngine) {
                DBEngine.MYSQL -> {
                    _exposedDB = Database.connect(
                        "jdbc:mysql://${dbInfo.host}:${dbInfo.port}/${dbInfo.dbName}",
                        "com.mysql.cj.jdbc.Driver",
                        dbInfo.user, dbInfo.pass
                    )
                }

                DBEngine.MARIADB -> {
                    _exposedDB = Database.connect(
                        "jdbc:mariadb://${dbInfo.host}:${dbInfo.port}/${dbInfo.dbName}",
                        "org.mariadb.jdbc.Driver",
                        dbInfo.user, dbInfo.pass
                    )
                }
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            log(
                "",
                "§cAn internal error occurred when trying to connect to SQL server.",
                "§cPlease check your SQL server and plugin settings and try again.",
                ""
            )
            false
        }
    }

    internal fun unloadDatabaseAPI() {
        if (!this::_exposedDB.isInitialized) return
        try {
            _exposedDB.connector().close()
        } catch (ex: Exception) {
            ex.printStackTrace()
            log(
                "",
                "§cAn internal error occurred when trying to close the connection with SQL server.",
                ""
            )
        }
    }

}