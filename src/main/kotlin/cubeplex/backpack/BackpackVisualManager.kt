package cubeplex.backpack

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class BackpackVisualManager(private val plugin: BackpackPlugin) {

    data class ActiveVisual(
        val rootDisplayId: UUID,
        val spawnTag: String
    )

    private val activeVisuals = mutableMapOf<UUID, ActiveVisual>()

    fun hasVisual(playerId: UUID): Boolean = activeVisuals.containsKey(playerId)

    fun spawn(player: Player) {
        val existing = activeVisuals[player.uniqueId]
        if (existing != null) {
            val root = Bukkit.getEntity(existing.rootDisplayId) as? BlockDisplay
            if (root != null && root.isValid) {
                mountOnPlayer(player, root)
                return
            }
            activeVisuals.remove(player.uniqueId)
        }

        if (!isBackpackEquipped(player)) return

        val spawnTag = "cubeplex_bp_${player.uniqueId.toString().replace("-", "")}"
        spawnViaApi(player, spawnTag)
    }

    private fun spawnViaApi(player: Player, spawnTag: String): BlockDisplay? {
        val world = player.world
        val location = player.location

        val root = world.spawn(location, BlockDisplay::class.java) { display ->
            display.addScoreboardTag(BackpackListener.ENTITY_TAG)
            display.addScoreboardTag(spawnTag)
            configureDisplay(display)
            display.transformation = BackpackModel.ROOT_OFFSET
        }

        for (part in BackpackModel.PARTS) {
            val itemDisplay = world.spawn(location, ItemDisplay::class.java) { display ->
                display.addScoreboardTag(BackpackListener.ENTITY_TAG)
                display.setItemStack(createPlayerHead(part.textureValue))
                display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
                configureDisplay(display)
            }
            root.addPassenger(itemDisplay)
            itemDisplay.transformation = BackpackModel.transformationFromSummon(part.matrix)
        }

        configureDisplayTree(root)
        mountOnPlayer(player, root)
        addInteraction(player, root)

        activeVisuals[player.uniqueId] = ActiveVisual(root.uniqueId, spawnTag)
        return root
    }

    private fun mountOnPlayer(player: Player, root: BlockDisplay) {
        if (root.vehicle == player) return
        root.leaveVehicle()
        player.addPassenger(root)
    }

    private fun addInteraction(player: Player, root: BlockDisplay) {
        val interaction = player.world.spawn(player.location, Interaction::class.java) { inter ->
            inter.addScoreboardTag(BackpackListener.ENTITY_TAG)
            inter.isPersistent = false
            inter.interactionWidth = 0.9f
            inter.interactionHeight = 1.1f
            inter.persistentDataContainer.set(
                BackpackListener.BACKPACK_ROOT_KEY,
                PersistentDataType.STRING,
                root.uniqueId.toString()
            )
        }
        root.addPassenger(interaction)
    }

    private fun configureDisplayTree(root: Entity) {
        if (root is Display) configureDisplay(root)
        for (passenger in root.passengers) {
            configureDisplayTree(passenger)
        }
    }

    private fun configureDisplay(display: Display) {
        display.isPersistent = false
        display.teleportDuration = 0
        display.interpolationDuration = 0
        display.interpolationDelay = 0
        display.billboard = Display.Billboard.FIXED
    }

    fun remove(playerId: UUID) {
        val visual = activeVisuals.remove(playerId) ?: return
        val entity = Bukkit.getEntity(visual.rootDisplayId)
        if (entity != null) {
            removeEntityTree(entity)
            return
        }

        Bukkit.getOnlinePlayers().forEach { player ->
            player.world.entities
                .filter { it.scoreboardTags.contains(visual.spawnTag) }
                .forEach { removeEntityTree(it) }
        }
    }

    fun removeAll() {
        val copy = activeVisuals.toMap()
        activeVisuals.clear()
        copy.keys.forEach { remove(it) }
    }

    private fun isBackpackEquipped(player: Player): Boolean {
        return BackpackListener.isBackpackItem(player.inventory.chestplate)
    }

    private fun createPlayerHead(textureValue: String): ItemStack {
        val item = ItemStack(org.bukkit.Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.setProperty(ProfileProperty("textures", textureValue))
        meta.setPlayerProfile(profile)
        item.itemMeta = meta
        return item
    }

    private fun removeEntityTree(entity: Entity) {
        entity.passengers.toList().forEach { removeEntityTree(it) }
        entity.remove()
    }
}