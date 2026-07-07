package cubeplex.backpack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.UUID

class BackpackManager(private val plugin: cubeplex.backpack.BackpackPlugin) {

    companion object {
        private val BACKPACK_KEY = NamespacedKey("cubeplex", "backpack")
        private const val INVENTORY_SIZE = 27
        private const val INVENTORY_TITLE = "⭐ Mochila"
    }

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFolder = File(plugin.dataFolder, "backpacks").also { it.mkdirs() }
    private val playerInventories = mutableMapOf<UUID, Inventory>()

    fun getOrCreateInventory(player: Player): Inventory {
        return playerInventories.getOrPut(player.uniqueId) {
            loadInventory(player) ?: createNewInventory(player)
        }
    }

    private fun createNewInventory(player: Player): Inventory {
        val inventory = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text(INVENTORY_TITLE))
        saveInventory(player.uniqueId, inventory.contents)
        return inventory
    }

    fun openInventory(player: Player) {
        val inventory = getOrCreateInventory(player)
        player.openInventory(inventory)
    }

    fun saveInventory(playerId: UUID, contents: Array<ItemStack?>) {
        val file = File(dataFolder, "$playerId.json")
        val serializable = contents.map { item ->
            item?.serialize()?.toMap() ?: mapOf("type" to "AIR")
        }.toList()
        file.writeText(gson.toJson(serializable))
    }

    private fun loadInventory(player: Player): Inventory? {
        val file = File(dataFolder, "${player.uniqueId}.json")
        if (!file.exists()) return null

        return try {
            val json = file.readText()
            val type = object : TypeToken<List<Map<String, Any>?>>() {}.type
            val serialized: List<Map<String, Any>?> = gson.fromJson(json, type)

            val inventory = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text(INVENTORY_TITLE))
            val contents = serialized.map { map ->
                if (map == null || map["type"] == "AIR") null
                else ItemStack.deserialize(map)
            }.toTypedArray()
            inventory.contents = contents
            inventory
        } catch (e: Exception) {
            plugin.logger.warning("Error cargando mochila de ${player.name}: ${e.message}")
            null
        }
    }

    fun saveAll() {
        playerInventories.forEach { (playerId, inventory) ->
            saveInventory(playerId, inventory.contents)
        }
        plugin.logger.info("Mochilas guardadas: ${playerInventories.size}")
    }

    fun closeInventory(player: Player) {
        val inventory = playerInventories[player.uniqueId] ?: return
        saveInventory(player.uniqueId, inventory.contents)
    }
}
