package io.github.gabehowe.democraticsleep

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SleepVoteNoCommand(private val democraticSleep: DemocraticSleep) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can do that command!")
            return true
        }
        if(!democraticSleep.someoneSlept) {
            sender.sendMessage("§cNo one has slept yet!")
            return true
        }
        if(democraticSleep.allUUIDs.contains(sender.uniqueId)
            || democraticSleep.noUUIDs.contains(sender.uniqueId)
            || democraticSleep.yesUUIDs.contains(sender.uniqueId)) {
            sender.sendMessage("§cYou already voted tonight!")
            return true
        }
        democraticSleep.noUUIDs.add(sender.uniqueId)
        democraticSleep.totalVotes.inc()
        sender.sendMessage("§6You voted §cNO §6to skipping the night")
        if(democraticSleep.totalVotes >= democraticSleep.allUUIDs.size) {
            democraticSleep.attemptNightSkip()
        }
        return true
    }

}