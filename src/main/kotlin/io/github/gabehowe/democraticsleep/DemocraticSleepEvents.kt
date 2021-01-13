package io.github.gabehowe.democraticsleep

import org.bukkit.Bukkit
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
        if(event.player.world.time !in 12543..23998) {
            return
        }
        if(democraticSleep.someoneSlept) {
            democraticSleep.yesUUIDs.add(event.player.uniqueId)
        }
        if(!democraticSleep.someoneSlept) {
            democraticSleep.someoneSlept = true
            democraticSleep.yesUUIDs.add(event.player.uniqueId)

            Bukkit.getServer().broadcastMessage("§6${event.player.displayName} is sleeping, do /sn or /sy to vote to skip the night")

            Bukkit.getServer().scheduler.scheduleSyncDelayedTask(democraticSleep, {
                if(democraticSleep.someoneSlept) {
                    democraticSleep.attemptNightSkip()
                }
            },(democraticSleep.voteTime.toDouble() * 20).toLong())
        }
    }
}