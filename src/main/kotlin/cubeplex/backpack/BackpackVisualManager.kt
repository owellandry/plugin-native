package cubeplex.backpack

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import java.util.UUID

class BackpackVisualManager(private val plugin: BackpackPlugin) {

    private val visuals = mutableMapOf<UUID, List<UUID>>()
    private val tasks  = mutableMapOf<UUID, Int>()

    fun hasVisual(playerId: UUID) = visuals.containsKey(playerId)

    fun spawn(player: Player) {
        if (hasVisual(player.uniqueId)) return

        val anchorLoc = BackpackModel.backAnchorLocation(player)
        val rootQuat  = yawQuaternion(player)

        val partEntities = BackpackModel.PARTS.map { part ->
            player.world.spawn(anchorLoc, ItemDisplay::class.java) { d ->
                d.addScoreboardTag(BackpackListener.ENTITY_TAG)
                d.isPersistent        = false
                d.billboard           = Display.Billboard.FIXED
                d.interpolationDelay  = 0
                d.interpolationDuration = 3
                d.setItemStack(createSkullItem(part.textureValue))

                val localT = BackpackModel.transformationFromSummon(part.matrix)
                d.transformation = Transformation(
                    localT.translation,
                    Quaternionf(rootQuat).mul(localT.leftRotation),
                    localT.scale,
                    localT.rightRotation
                )
            }
        }

        visuals[player.uniqueId] = partEntities.map { it.uniqueId }

        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (!player.isOnline) { remove(player.uniqueId); return@Runnable }
            syncPosition(player)
        }, 1L, 1L).taskId

        tasks[player.uniqueId] = taskId
    }

    fun syncPosition(player: Player) {
        val partIds   = visuals[player.uniqueId] ?: return
        val anchorLoc = BackpackModel.backAnchorLocation(player)
        val rootQuat  = yawQuaternion(player)

        partIds.forEachIndexed { i, id ->
            val display = player.world.entities
                .firstOrNull { it.uniqueId == id } as? ItemDisplay ?: return@forEachIndexed

            display.teleport(anchorLoc)

            val localT = BackpackModel.transformationFromSummon(BackpackModel.PARTS[i].matrix)
            display.interpolationDelay = 0
            display.transformation = Transformation(
                localT.translation,
                Quaternionf(rootQuat).mul(localT.leftRotation),
                localT.scale,
                localT.rightRotation
            )
        }
    }

    fun refreshOffset(player: Player) = syncPosition(player)

    fun remove(playerId: UUID) {
        tasks.remove(playerId)?.let { Bukkit.getScheduler().cancelTask(it) }
        val partIds = visuals.remove(playerId) ?: return
        partIds.forEach { id ->
            Bukkit.getWorlds().flatMap { it.entities }
                .firstOrNull { it.uniqueId == id }?.remove()
        }
    }

    fun removeAll() = visuals.keys.toList().forEach { remove(it) }

    private fun yawQuaternion(player: Player): Quaternionf {
        val yawRad = Math.toRadians(BackpackModel.bodyYaw(player).toDouble()).toFloat()
        val pitchDeg = BackpackModel.bodyPitch(player)
        return Quaternionf()
            .rotateY(-yawRad)
            .apply { if (pitchDeg != 0f) rotateX(Math.toRadians(pitchDeg.toDouble()).toFloat()) }
    }

    private fun createSkullItem(textureValue: String): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD)
        val meta  = skull.itemMeta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.setProperty(ProfileProperty("textures", textureValue))
        meta.playerProfile = profile
        skull.itemMeta = meta
        return skull
    }
}
