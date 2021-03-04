package io.github.gabehowe.democraticsleep

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SleepVoteCommand(private val democraticSleep: DemocraticSleep) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§4You must be an in-game player to use this command.")
            return true
        }
        if (democraticSleep.sleeping.isEmpty()) {
            sender.sendMessage("§4No one has slept yet.")
            return true
        }
        if (sender.world.name != "world") {
            sender.sendMessage("§4You must be in the overworld to vote.")
            return true
        }
        if (democraticSleep.sleeping.contains(sender.uniqueId)) {
            sender.sendMessage("§4You've already voted by sleeping.")
            return true
        }

        val vote = when (label) {
            "sy", "sleepyes", "sleepvoteyes" -> true
            "sn", "sleepno", "sleepvoteno" -> false
            else -> return false
        }

        if (democraticSleep.votes.containsKey(sender.uniqueId) && vote) {
            val voteNegator = if (!vote) {
                " not"
            } else {
                ""
            }
            sender.sendMessage("§4You already voted$voteNegator to skip the night.")
            return true
        }

        democraticSleep.votes[sender.uniqueId] = vote

        sender.sendMessage("§6Your vote has been counted.")

        if (democraticSleep.successfulVote()) {
            democraticSleep.attemptSkipNight()
        }

        return true
    }

}