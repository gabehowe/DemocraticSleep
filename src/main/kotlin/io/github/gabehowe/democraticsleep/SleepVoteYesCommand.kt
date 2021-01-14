package io.github.gabehowe.democraticsleep

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SleepVoteYesCommand(private val democraticSleep: DemocraticSleep) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can do that command!")
            return true
        }
        if(!democraticSleep.someoneSlept) {
            sender.sendMessage("§cNo one has slept yet!")
            return true
        }
        if(democraticSleep.noUUIDs.contains(sender.uniqueId)
            || democraticSleep.yesUUIDs.contains(sender.uniqueId)) {
            sender.sendMessage("§cYou already voted tonight!")
            return true
        }
        democraticSleep.yesUUIDs.add(sender.uniqueId)
        democraticSleep.totalVotes.inc()
        sender.sendMessage("§6You voted §aYES §6to skipping the night")
        if(democraticSleep.totalVotes >= democraticSleep.allUUIDs.size) {
            democraticSleep.attemptNightSkip()
        }
        return true
    }

}