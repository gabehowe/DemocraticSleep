package io.github.gabehowe.democraticsleep

import org.bukkit.Bukkit
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class DemocraticSleep : JavaPlugin() {
    var someoneSlept : Boolean = false
    var totalVotes : Int = 0
    var yesUUIDs = mutableListOf<UUID>()
    var noUUIDs = mutableListOf<UUID>()
    var allUUIDs = mutableListOf<UUID>()
    val voteTime : Number
        get() {
            return (config.get("vote-time") as Number?)!!
        }
    val votePercentage : Double
        get() {
            return (config.get("vote-percentage") as Double)
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
    fun attemptNightSkip() {
        if((yesUUIDs.size - noUUIDs.size) > (allUUIDs.size * votePercentage)) {
            for(player in getOnlinePlayers()) {
                if(player.world.time !in 12543..23998) {
                    continue
                }
                player.world.time = 0
            }
            broadcastMessage("§6Night skipped")
        }
        else {
            Bukkit.getServer().broadcastMessage("§6Vote Failed")
        }
        noUUIDs.clear()
        yesUUIDs.clear()
        someoneSlept = false
        totalVotes = 0
    }
}