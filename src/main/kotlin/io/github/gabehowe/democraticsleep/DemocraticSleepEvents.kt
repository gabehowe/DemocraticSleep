package io.github.gabehowe.democraticsleep

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class DemocraticSleepEvents(private val democraticSleep: DemocraticSleep) : Listener {
    @EventHandler
    fun onJoinEvent(event: PlayerJoinEvent) {
        democraticSleep.allUUIDs.add(event.player.uniqueId)
    }
    @EventHandler
    fun onLeaveEvent(event: PlayerQuitEvent) {
        democraticSleep.allUUIDs.remove(event.player.uniqueId)
    }
    @EventHandler
    fun onSleepEvent(event : PlayerBedEnterEvent) {
        if( democraticSleep.allUUIDs.size <= 1 ) {
            return
        }
        if(event.player.world.time !in 12543..23998) {
            return
        }
        if(event.player.world.environment == World.Environment.NETHER || event.player.world.environment == World.Environment.THE_END) {
            return
        }
        if(democraticSleep.someoneSlept) {
            if(democraticSleep.yesUUIDs.contains(event.player.uniqueId) || democraticSleep.noUUIDs.contains(event.player.uniqueId)) {
                return
            }
            democraticSleep.yesUUIDs.add(event.player.uniqueId)
            democraticSleep.totalVotes.inc()
            if(democraticSleep.totalVotes >= democraticSleep.allUUIDs.size) {
                democraticSleep.attemptNightSkip()
            }
        }
        if(!democraticSleep.someoneSlept) {
            democraticSleep.someoneSlept = true
            if(democraticSleep.yesUUIDs.contains(event.player.uniqueId) || democraticSleep.noUUIDs.contains(event.player.uniqueId)) {
                return
            }
            democraticSleep.yesUUIDs.add(event.player.uniqueId)
            democraticSleep.totalVotes.inc()
            Bukkit.getServer().broadcastMessage("§6${event.player.displayName} is sleeping, do /sn or /sy to vote to skip the night")
            Bukkit.getServer().scheduler.runTaskLater(democraticSleep, Runnable {
                if(democraticSleep.someoneSlept) {
                    democraticSleep.attemptNightSkip()

                }
            },(democraticSleep.voteTime.toDouble() * 20).toLong())
        }
    }
}