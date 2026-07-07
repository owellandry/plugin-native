package cubeplex.backpack

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BackpackCommand(private val plugin: cubeplex.backpack.BackpackPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Solo jugadores pueden usar este comando.")
            return true
        }

        if (args.isNotEmpty() && args[0].equals("give", ignoreCase = true)) {
            val item = BackpackListener.createBackpackItem()
            sender.inventory.addItem(item).also { remaining ->
                remaining.values.forEach { sender.world.dropItem(sender.location, it) }
            }
            sender.sendMessage(Component.text("\u00A7a\u00A1Has recibido una mochila!"))
            return true
        }

        plugin.backpackManager.openInventory(sender)
        sender.sendMessage(Component.text("\u00A7a\u00A1Mochila abierta!"))
        return true
    }
}
