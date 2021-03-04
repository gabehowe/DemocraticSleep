package io.github.gabehowe.democraticsleep

import org.bukkit.Bukkit
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.world.TimeSkipEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class DemocraticSleep : JavaPlugin() {
    var sleeping = mutableSetOf<UUID>()
    var votes = mutableMapOf<UUID, Boolean>()
    var sleepRunnableId: Int? = null

    val voteTimeout: Number
        get() {
            return (config.get("vote-timeout") as Number?)!!
        }
    val requiredVoteRatio: Double
        get() {
            return (config.get("required-vote-percentage") as Double)
        }

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        getCommand("sleepvoteno")?.setExecutor(SleepVoteCommand(this))
        getCommand("sleepvoteyes")?.setExecutor(SleepVoteCommand(this))
        server.pluginManager.registerEvents(DemocraticSleepEvents(this), this)
    }

    fun cancelVote() {
        votes.clear()
        if (sleepRunnableId != null) {
            Bukkit.getScheduler().cancelTask(sleepRunnableId!!)
        }
    }

    fun attemptSkipNight() {
        val nmsWorld = (Bukkit.getWorld("world")!! as CraftWorld).handle
        if (!nmsWorld.gameRules.getBoolean(net.minecraft.server.v1_16_R3.GameRules.DO_DAYLIGHT_CYCLE)) {
            cancelVote()
            Bukkit.getServer()
                .broadcastMessage("§4Sleep vote cancelled; gamerule doDaylightCycle prevents skipping night")
            return
        }

        if (!nmsWorld.isNight) {
            cancelVote()
            Bukkit.getServer().broadcastMessage("§6Sleep vote cancelled; it's daytime")
            return
        }

        if (sleeping.isEmpty()) {
            Bukkit.getServer().broadcastMessage("§6Sleep vote cancelled; nobody is sleeping")
        }

        if (voteRatio() < requiredVoteRatio) {
            cancelVote()
            Bukkit.getServer().broadcastMessage("§6Sleep vote failed")
            return
        }

        broadcastMessage("§6Sleep vote passed, skipping night")

        // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/nms-patches/WorldServer.patch#167-177
        val l = nmsWorld.dayTime + 24000L
        val event =
            TimeSkipEvent(nmsWorld.world, TimeSkipEvent.SkipReason.NIGHT_SKIP, (l - l % 24000L) - nmsWorld.dayTime)

        server.pluginManager.callEvent(event)
        if (!event.isCancelled) {
            nmsWorld.dayTime = (nmsWorld.dayTime + event.skipAmount)
        }
    }

    private fun voteRatio(): Double {
        // Double check everything
        val onlineSleeping = sleeping.filter { isPlayerOnlineAndInOverworld(it) }
        val onlineYesVotes =
            votes.filter { isPlayerOnlineAndInOverworld(it.key) && !onlineSleeping.contains(it.key) && it.value }
        val totalVoteCount = Bukkit.getWorld("world")!!.players.size
        val yesVoteCount = onlineSleeping.size + onlineYesVotes.size
        return yesVoteCount.toDouble() / totalVoteCount
    }

    fun successfulVote(): Boolean {
        return voteRatio() >= requiredVoteRatio
    }

    private fun isPlayerOnlineAndInOverworld(uuid: UUID): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        return player.isOnline && player.world.name == "world"
    }

    fun stopPlayerSleeping(player: Player) {
        if (!sleeping.contains(player.uniqueId)) {
            return
        }

        sleeping.remove(player.uniqueId)
        if (sleeping.isEmpty()) {
            Bukkit.getServer().broadcastMessage("§6Sleep vote cancelled; nobody is sleeping")
            cancelVote()
        }
    }
}