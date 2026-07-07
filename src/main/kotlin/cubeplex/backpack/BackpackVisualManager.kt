package cubeplex.backpack

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

class BackpackVisualManager(private val plugin: BackpackPlugin) {

    data class ActiveVisual(
        val anchorId: UUID,
        val rootDisplayId: UUID,
        val interactionId: UUID
    )

    private val activeVisuals = mutableMapOf<UUID, ActiveVisual>()

    fun hasVisual(playerId: UUID): Boolean = activeVisuals.containsKey(playerId)

    fun spawn(player: Player): Boolean {
        if (activeVisuals.containsKey(player.uniqueId)) return true

        val world = player.world
        val anchorLocation = computeAnchorLocation(player)

        val anchor = world.spawn(anchorLocation, ArmorStand::class.java) { stand ->
            stand.addScoreboardTag(BackpackListener.ENTITY_TAG)
            stand.isVisible = false
            stand.isMarker = true
            stand.setGravity(false)
            stand.setBasePlate(false)
            stand.setArms(false)
            stand.isSmall = false
            stand.isPersistent = false
            stand.isCollidable = false
            stand.setRotation(anchorLocation.yaw, 0f)
        }

        val root = world.spawn(anchorLocation, BlockDisplay::class.java) { display ->
            display.addScoreboardTag(BackpackListener.ENTITY_TAG)
            display.isPersistent = false
            display.billboard = Display.Billboard.FIXED
        }
        anchor.addPassenger(root)

        for (part in BackpackModel.PARTS) {
            val skull = createPlayerHead(part.textureValue)
            val itemDisplay = world.spawn(anchorLocation, ItemDisplay::class.java) { display ->
                display.addScoreboardTag(BackpackListener.ENTITY_TAG)
                display.setItemStack(skull)
                display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
                display.isPersistent = false
                display.billboard = Display.Billboard.FIXED
                display.setTransformationMatrix(BackpackModel.matrixFromSummon(part.matrix))
            }
            root.addPassenger(itemDisplay)
        }

        val interaction = world.spawn(anchorLocation, Interaction::class.java) { inter ->
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

        activeVisuals[player.uniqueId] = ActiveVisual(
            anchor.uniqueId,
            root.uniqueId,
            interaction.uniqueId
        )
        return true
    }

    fun remove(playerId: UUID) {
        val visual = activeVisuals.remove(playerId) ?: return
        removeEntity(visual.anchorId)
    }

    fun removeAll() {
        val ids = activeVisuals.values.map { it.anchorId }.toList()
        activeVisuals.clear()
        ids.forEach { removeEntity(it) }
    }

    fun tickAll() {
        for ((playerId, visual) in activeVisuals.toMap()) {
            val player = Bukkit.getPlayer(playerId) ?: continue
            if (!player.isOnline) continue

            val anchor = Bukkit.getEntity(visual.anchorId) as? ArmorStand ?: continue
            val target = computeAnchorLocation(player)
            anchor.teleport(target)
            anchor.setRotation(target.yaw, 0f)
        }
    }

    private fun computeAnchorLocation(player: Player): Location {
        val location = player.location.clone()
        location.pitch = 0f

        val yawRad = Math.toRadians(location.yaw.toDouble())
        location.y = player.location.y + 1.0

        location.x -= sin(yawRad) * 0.35
        location.z += cos(yawRad) * 0.35

        val modelOffset = Vector(-0.5, -0.5, -0.5)
        modelOffset.rotateAroundY(Math.toRadians(-location.yaw.toDouble()))
        location.add(modelOffset)

        return location
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

    private fun removeEntity(entityId: UUID) {
        val entity = Bukkit.getEntity(entityId) ?: return
        removeEntityTree(entity)
    }

    private fun removeEntityTree(entity: Entity) {
        entity.passengers.toList().forEach { removeEntityTree(it) }
        entity.remove()
    }
}