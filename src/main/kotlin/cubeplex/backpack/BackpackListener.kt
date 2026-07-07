package cubeplex.backpack

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import com.destroystokyo.paper.profile.ProfileProperty

class BackpackListener(private val plugin: cubeplex.backpack.BackpackPlugin) : Listener {

    companion object {
        val BACKPACK_KEY = NamespacedKey("cubeplex", "backpack")
        val ENTITY_TAG = "cubeplex_backpack"
        val BACKPACK_ROOT_KEY = NamespacedKey("cubeplex", "backpack_root")

        private val activeBackpacks = mutableMapOf<UUID, UUID>()

        private data class HeadPart(
            val textureHash: String,
            val tx: Float, val ty: Float, val tz: Float,
            val lrx: Float, val lry: Float, val lrz: Float, val lrw: Float,
            val sx: Float, val sy: Float, val sz: Float,
            val rrx: Float, val rry: Float, val rrz: Float, val rrw: Float
        )

        private val BACKPACK_PARTS = listOf(
            HeadPart("8300af781ff29c33907a433fe259b6e401204d712d11b0a82a46f59e744dc90", 0.25f, 0.2441f, -0.2188f, 0.5f, 0f, 0f, 0.25f, 1f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("22ea09770d435db83ce7700ebda15f0221f8f0823f177929d7a32e643bb0ec57", 0.25f, 0.2441f, -0.2188f, 1f, 0f, 0f, -0.125f, 1f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("248a247bf36b1f4d0a33b54578976e9e30e85b12d7509b296d3d31078c908f55", -0.125f, 0.3691f, -0.2188f, 1f, 0f, 0f, -0.125f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("25fef749dc614f4761e6299ba0242998298b1d1d04b5930d669f010535f1327", 0.25f, 0.3691f, -0.2188f, 0.5f, 0f, 0f, 0.25f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("7c88694436d048b9c586c27a1256e5f7a7969f44280cd78682451abd34c824f", -0.125f, 0.2441f, 0.1563f, 1f, 0f, 0f, -0.125f, 0.25f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("980743bfc36390c76f14feb2ac8e47330b89a1c937e0b2795a283f3678a15970", -0.125f, 0.2441f, 0.0625f, 1f, 0f, 0f, -0.125f, 0.125f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("caf7e57316b83b0102fd9e7b979ea9cb3b28287696948d1c0175f96365bdcab88", 0.25f, 0.2441f, 0.0625f, 0.5f, 0f, 0f, 0.25f, 0.125f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("6d9ca7cc757f07383d92ac5e2244874344e3718c24fedcf1c0610688bcbbc68", 0.25f, 0.2441f, 0.1563f, 0.5f, 0f, 0f, 0.25f, 0.25f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("dcabbe305ade8d97dc2047ad682bb72e3e6beefce67413d9d2392f4fb3bc239", -0.125f, 0.8691f, -0.0313f, 1f, 0f, 0f, -0.125f, 1f, 1f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("1eed6a54ff36ec9e05f64ce0bd393616e09199b199b47e8ed5ec15b788dbd67", -0.125f, 0.3691f, 0.0625f, 1f, 0f, 0f, -0.125f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("1eed6a54ff36ec9e05f64ce0bd393616e09199b199b47e8ed5ec15b788dbd67", 0.25f, 0.3691f, 0.0625f, 0.5f, 0f, 0f, 0.25f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c4ac0d3d9f6c96557fca177a2be93b43024bfa6a66f06bb441e84f9cc8d37fec", -0.125f, 0.3691f, 0.1563f, 1f, 0f, 0f, -0.125f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("e4c8c116521b1bf35fdd256999ddc480a1971f52018d79a363f0c1d81e9cb794a", 0.25f, 0.3691f, 0.1563f, 0.5f, 0f, 0f, 0.25f, 1f, 0.25f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("31d00df9ed43b4bcde8f63729084a8632c7748297a5287ee7a5429907ce85506", 0.25f, 0.8691f, -0.0313f, 0.5f, 0f, 0f, 0.25f, 1f, 1f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c2dc7162dc0f2d2721f8a7f17c6f79212fd779332d810af372616fc4d987713", -0.1875f, 0.6191f, -0.4063f, 1f, 0f, 0f, -0.1875f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("fb21acf3afcc6594c433075116907da5fd3d72597437ac01fbf0534a86d13acc0", 0.1875f, 0.6191f, -0.4063f, 0.5f, 0f, 0f, 0.1875f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("bb4f9adef7ad6869997d06b2062b90826bcb0b7e45ea6e35bd3cba59e8fbb10", 0.375f, 0.6191f, -0.4063f, 0.25f, 0f, 0f, 0.375f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("e0caf5131acf886ceee9b027e9094595f0415094f577f9250a7029fa1081d96", -0.3125f, 0.7441f, 0.4063f, 0.25f, 0f, 0f, -0.3125f, 1f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("2b4377a0ef638ae3756acb047e109c88962180d402379ca07da72b5a0e5b4651", 0.3125f, 0.7441f, 0.4063f, 0.25f, 0f, 0f, 0.3125f, 1f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("d95dca40a1013345e27f627c26ee51d2de7e798d391fb340e495928c", -0.3125f, 0.2441f, 0.4063f, 0.25f, 0f, 0f, -0.3125f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("d95dca40a1013345e27f627c26ee51d2de7e798d391fb340e495928c", 0.3125f, 0.2441f, 0.4063f, 0.25f, 0f, 0f, 0.3125f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c9cf68efcea1ed1198354d4a832637d31d823a7d648038d2100e7f5cbfd9c36fc", -0.4375f, 0.2441f, -0.0313f, 0.25f, 0f, 0f, -0.4375f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("2a837e14dc721120518015916fbba09ab548fcff00282aee21358406cef50de", -0.4375f, 0.3691f, -0.0313f, 0.25f, 0f, 0f, -0.4375f, 0.25f, 0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("fd558080a36be529d2613381dadeda061871f71cba30faa5969e437c383ea07ef2", -0.4375f, 0.2441f, 0.125f, 0.25f, 0f, 0f, -0.4375f, 0.5f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("2c1710ab54cdd3037823c467968e7f4d5f8dca4f5b3eedd5face4ab966d74bbc", -0.4375f, 0.3691f, 0.125f, 0.25f, 0f, 0f, -0.4375f, 0.25f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("5df288102e9a1f0dfed04a98068b85fedc4bca732c413f9d54881a1c0884b6f3", 0.3125f, 0.2441f, 0.25f, 0.25f, 0f, 0f, 0.3125f, 0.5f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c811ce53a30df2805517af77ac10ae813444652468fb8706adb681a3f3da9a0", 0.3125f, 0.7441f, 0.25f, 0.25f, 0f, 0f, 0.3125f, 1f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("5df288102e9a1f0dfed04a98068b85fedc4bca732c413f9d54881a1c0884b6f3", -0.3125f, 0.2441f, 0.25f, 0.25f, 0f, 0f, -0.3125f, 0.5f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c811ce53a30df2805517af77ac10ae813444652468fb8706adb681a3f3da9a0", -0.3125f, 0.7441f, 0.25f, 0.25f, 0f, 0f, -0.3125f, 1f, 0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("c9cf68efcea1ed1198354d4a832637d31d823a7d648038d2100e7f5cbfd9c36fc", 0.4375f, 0.2441f, 0.0254f, -0.25f, 0f, 0f, 0.4375f, 0.5f, -0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("2a837e14dc721120518015916fbba09ab548fcff00282aee21358406cef50de", 0.4375f, 0.3691f, 0.0254f, -0.25f, 0f, 0f, 0.4375f, 0.25f, -0.5f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("fd558080a36be529d2613381dadeda061871f71cba30faa5969e437c383ea07ef2", 0.4375f, 0.2441f, -0.1309f, -0.25f, 0f, 0f, 0.4375f, 0.5f, -0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("2c1710ab54cdd3037823c467968e7f4d5f8dca4f5b3eedd5face4ab966d74bbc", 0.4375f, 0.3691f, -0.1309f, -0.25f, 0f, 0f, 0.4375f, 0.25f, -0.125f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("26bab9a9febeb97459eb9385a7b77a6770d1f8ec279a78fe02281875d56f11e53c", 0.1875f, -0.0044f, -0.4688f, 0.25f, 0f, 0f, 0.1875f, 0.6563f, 0.0031f, 0f, 0f, 0f, 0f, 1f),
            HeadPart("26bab9a9febeb97459eb9385a7b77a6770d1f8ec279a78fe02281875d56f11e53c", -0.1875f, -0.0044f, -0.4688f, 0.25f, 0f, 0f, -0.1875f, 0.6563f, 0.0031f, 0f, 0f, 0f, 0f, 1f)
        )

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

        fun removeAllBackpacks() {
            activeBackpacks.values.forEach { id ->
                Bukkit.getEntity(id)?.remove()
            }
            activeBackpacks.clear()
        }
    }

    private fun spawnBackpackVisual(player: Player): Entity? {
        val world = player.world
        val location = player.location.clone()

        val root = world.spawn(location, BlockDisplay::class.java) { display ->
            display.addScoreboardTag(ENTITY_TAG)
            display.isPersistent = true
        }

        for (part in BACKPACK_PARTS) {
            val skull = createPlayerHead(part.textureHash)

            val itemDisplay = world.spawn(location, ItemDisplay::class.java) { display ->
                display.setItemStack(skull)
                display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
                display.isPersistent = true

                val translation = Vector3f(part.tx, part.ty, part.tz)
                val leftRotation = Quaternionf(part.lrx, part.lry, part.lrz, part.lrw)
                val scale = Vector3f(part.sx, part.sy, part.sz)
                val rightRotation = Quaternionf(part.rrx, part.rry, part.rrz, part.rrw)

                display.transformation = Transformation(translation, leftRotation, scale, rightRotation)
            }
            root.addPassenger(itemDisplay)
        }

        val interactionLoc = location.clone().add(0.0, 0.5, 0.0)
        val interaction = world.spawn(interactionLoc, Interaction::class.java) { inter ->
            inter.addScoreboardTag(ENTITY_TAG)
            inter.isPersistent = true
            inter.persistentDataContainer.set(BACKPACK_ROOT_KEY, PersistentDataType.STRING, root.uniqueId.toString())
        }

        activeBackpacks[player.uniqueId] = root.uniqueId
        return root
    }

    private fun createPlayerHead(textureHash: String): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(textureHash.toByteArray()), null)
        profile.setProperty(ProfileProperty("textures", textureHash))
        meta.setPlayerProfile(profile)
        item.itemMeta = meta
        return item
    }

    private fun removeBackpackEntity(player: Player) {
        val entityId = activeBackpacks.remove(player.uniqueId)
        if (entityId != null) {
            Bukkit.getEntity(entityId)?.remove()
        }
    }

    private fun updateBackpack(player: Player) {
        val chestplate = player.inventory.chestplate
        if (isBackpackItem(chestplate)) {
            if (!activeBackpacks.containsKey(player.uniqueId)) {
                spawnBackpackVisual(player)
            }
        } else {
            removeBackpackEntity(player)
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (!event.rightClicked.scoreboardTags.contains(ENTITY_TAG)) return
        event.isCancelled = true

        val clicked = event.rightClicked
        if (clicked is Interaction) {
            plugin.backpackManager.openInventory(event.player)
            event.player.sendMessage(Component.text("\u00A7a\u00A1Mochila abierta!"))
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.slot != 38) return
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
        removeBackpackEntity(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        removeBackpackEntity(event.player)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            updateBackpack(event.player)
        })
    }
}
