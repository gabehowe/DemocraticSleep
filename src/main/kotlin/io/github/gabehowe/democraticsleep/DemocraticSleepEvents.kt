package io.github.gabehowe.democraticsleep

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.TimeSkipEvent

class DemocraticSleepEvents(private val democraticSleep: DemocraticSleep) : Listener {
    @EventHandler
    fun onLeaveEvent(event: PlayerQuitEvent) {
        democraticSleep.votes.remove(event.player.uniqueId)
        democraticSleep.stopPlayerSleeping(event.player)
    }

    @EventHandler
    fun onTimeSkipEvent(event: TimeSkipEvent) {
        if ((event.world as CraftWorld).handle.isNight) {
            return
        }

        democraticSleep.cancelVote()
        democraticSleep.sleeping.clear()
    }

    @EventHandler
    fun onLeaveBedEvent(event: PlayerBedLeaveEvent) {
        democraticSleep.stopPlayerSleeping(event.player)
    }

    @EventHandler
    fun onSleepEvent(event: PlayerBedEnterEvent) {
        if (Bukkit.getOnlinePlayers().size <= 1) {
            return
        }
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) {
            return
        }
        if (event.player.world.environment == World.Environment.NETHER || event.player.world.environment == World.Environment.THE_END) {
            return
        }
        if ((event.player.world as CraftWorld).handle.isDay) {
            return
        }

        democraticSleep.votes.remove(event.player.uniqueId)
        democraticSleep.sleeping.add(event.player.uniqueId)

        if (democraticSleep.sleeping.size == event.player.world.players.size) {
            democraticSleep.cancelVote()
            democraticSleep.sleeping.clear()
            return
        }

        if (democraticSleep.successfulVote()) {
            democraticSleep.attemptSkipNight()
            return
        }

        if (democraticSleep.sleeping.size != 1) {
            return
        }

        democraticSleep.votes.clear()

        Bukkit.getServer()
            .broadcastMessage("§6${event.player.displayName}§r§6 is sleeping, do /sn or /sy to vote to skip the night")

        democraticSleep.sleepRunnableId = Bukkit.getServer().scheduler.scheduleSyncDelayedTask(
            democraticSleep,
            {
                if (democraticSleep.sleeping.isNotEmpty()) {
                    democraticSleep.attemptSkipNight()
                }
            }, (democraticSleep.voteTimeout.toDouble() * 20).toLong()
        )
    }
}