package cubeplex.backpack

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent

class BackpackListener(private val plugin: BackpackPlugin) : Listener {

    companion object {
        val BACKPACK_KEY = NamespacedKey("cubeplex", "backpack")
        val ENTITY_TAG = "cubeplex_backpack"
        val BACKPACK_ROOT_KEY = NamespacedKey("cubeplex", "backpack_root")

        fun createBackpackItem(): ItemStack {
            val item = ItemStack(Material.ELYTRA)
            val meta = item.itemMeta
            meta.displayName(Component.text("\u00A7e\u2B50 Mochila"))
            meta.lore(listOf(
                Component.text("\u00A77Ponte esta mochila en el pecho!"),
                Component.text("\u00A78Slot de pechera")
            ))
            meta.persistentDataContainer.set(BACKPACK_KEY, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return item
        }

        fun isBackpackItem(item: ItemStack?): Boolean {
            if (item == null || item.type != Material.ELYTRA) return false
            return item.itemMeta?.persistentDataContainer?.get(BACKPACK_KEY, PersistentDataType.BOOLEAN) ?: false
        }
    }

    private fun updateBackpack(player: Player) {
        val chestplate = player.inventory.chestplate
        if (isBackpackItem(chestplate)) {
            if (!plugin.visualManager.hasVisual(player.uniqueId)) {
                plugin.visualManager.spawn(player)
            }
        } else {
            plugin.visualManager.remove(player.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (!event.rightClicked.scoreboardTags.contains(ENTITY_TAG)) return
        event.isCancelled = true

        if (event.rightClicked is Interaction) {
            plugin.backpackManager.openInventory(event.player)
            event.player.sendMessage(Component.text("\u00A7a\u00A1Mochila abierta!"))
        }
    }

    @EventHandler
    fun onArmorChange(event: PlayerArmorChangeEvent) {
        if (event.slot != org.bukkit.inventory.EquipmentSlot.CHEST) return
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.player)
        })
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.slot != 38 && event.rawSlot != 38) return
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.whoClicked as? Player ?: return@Runnable)
        })
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (!event.inventorySlots.contains(38)) return
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.whoClicked as? Player ?: return@Runnable)
        })
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val title = PlainTextComponentSerializer.plainText().serialize(event.view.title())
        if (title.contains("Mochila")) {
            plugin.backpackManager.closeInventory(player)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        plugin.visualManager.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.visualManager.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!isBackpackItem(player.inventory.chestplate)) return
        if (!plugin.visualManager.hasVisual(player.uniqueId)) return

        val from = event.from
        val to = event.to ?: return
        if (from.x == to.x && from.y == to.y && from.z == to.z) return

        plugin.visualManager.syncPosition(player)
    }

    @EventHandler
    fun onToggleSneak(event: PlayerToggleSneakEvent) {
        if (!isBackpackItem(event.player.inventory.chestplate)) return
        Bukkit.getScheduler().runTask(plugin, Runnable {
            plugin.visualManager.refreshOffset(event.player)
        })
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.player)
        })
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.player)
        })
    }
}