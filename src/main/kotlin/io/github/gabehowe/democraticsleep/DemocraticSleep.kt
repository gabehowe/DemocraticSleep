package io.github.gabehowe.democraticsleep

import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class DemocraticSleep : JavaPlugin() {
    var someoneSlept : Boolean = false
    var yesUUIDs = mutableListOf<UUID>()
    var noUUIDs = mutableListOf<UUID>()
    var allUUIDs = mutableListOf<UUID>()
    val voteTime : Number
        get() {
            return (config.get("vote-time") as Number?)!!
        }

    override fun onEnable() {
        // Plugin startup logic
        someoneSlept = false
        saveDefaultConfig()
        getCommand("sleepvoteno")?.setExecutor(SleepVoteNoCommand(this))
        getCommand("sleepvoteyes")?.setExecutor(SleepVoteYesCommand(this))
        server.pluginManager.registerEvents(DemocraticSleepEvents(this),this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}