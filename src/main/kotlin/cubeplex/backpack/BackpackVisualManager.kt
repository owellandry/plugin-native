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
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class BackpackVisualManager(private val plugin: BackpackPlugin) {

    data class ActiveVisual(
        val rootDisplayId: UUID,
        val spawnTag: String,
        var lastBodyYaw: Float = Float.NaN,
        var lastSneaking: Boolean = false,
        var lastHeight: Double = -1.0
    )

    private val activeVisuals = mutableMapOf<UUID, ActiveVisual>()
    private var syncTask: BukkitTask? = null

    fun hasVisual(playerId: UUID): Boolean = activeVisuals.containsKey(playerId)

    fun spawn(player: Player) {
        val existing = activeVisuals[player.uniqueId]
        if (existing != null) {
            val root = Bukkit.getEntity(existing.rootDisplayId) as? BlockDisplay
            if (root != null && root.isValid) {
                syncPosition(player)
                ensureSyncTask()
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
            display.transformation = BackpackModel.ROOT_TRANSFORMATION
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
        addInteraction(player, root)

        val visual = ActiveVisual(root.uniqueId, spawnTag)
        activeVisuals[player.uniqueId] = visual
        syncPosition(player)
        ensureSyncTask()
        return root
    }

    /** Llamado desde PlayerMoveEvent — mismo tick que el jugador, sin delay de scheduler. */
    fun syncPosition(player: Player) {
        val visual = activeVisuals[player.uniqueId] ?: return
        val root = Bukkit.getEntity(visual.rootDisplayId) as? BlockDisplay ?: return
        if (!root.isValid || !player.isOnline) return

        applyPosition(player, root)
        visual.lastBodyYaw = BackpackModel.bodyYaw(player)
        visual.lastSneaking = player.isSneaking
        visual.lastHeight = player.height
    }

    fun refreshOffset(player: Player) {
        syncPosition(player)
    }

    private fun applyPosition(player: Player, root: BlockDisplay) {
        root.leaveVehicle()
        val anchor = BackpackModel.backAnchorLocation(player)
        root.teleport(anchor)
        root.setRotation(BackpackModel.bodyYaw(player), BackpackModel.bodyPitch(player))
        root.transformation = BackpackModel.ROOT_TRANSFORMATION
    }

    /**
     * Solo para cuando el cuerpo gira en el sitio (sin mover XYZ).
     * El movimiento lo cubre PlayerMoveEvent en el mismo tick.
     */
    private fun ensureSyncTask() {
        if (syncTask != null) return
        syncTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (activeVisuals.isEmpty()) {
                stopSyncTask()
                return@Runnable
            }
            for ((playerId, visual) in activeVisuals.toMap()) {
                val player = Bukkit.getPlayer(playerId) ?: continue
                if (!player.isOnline || !isBackpackEquipped(player)) continue
                val root = Bukkit.getEntity(visual.rootDisplayId) as? BlockDisplay ?: continue
                if (!root.isValid) continue

                val bodyYaw = BackpackModel.bodyYaw(player)
                val sneaking = player.isSneaking
                val height = player.height
                val poseChanged = visual.lastBodyYaw.isNaN()
                    || kotlin.math.abs(bodyYaw - visual.lastBodyYaw) > 0.01f
                    || sneaking != visual.lastSneaking
                    || kotlin.math.abs(height - visual.lastHeight) > 0.001

                if (poseChanged) {
                    applyPosition(player, root)
                    visual.lastBodyYaw = bodyYaw
                    visual.lastSneaking = sneaking
                    visual.lastHeight = height
                } else {
                    root.setRotation(bodyYaw, BackpackModel.bodyPitch(player))
                }
            }
        }, 1L, 1L)
    }

    private fun stopSyncTask() {
        syncTask?.cancel()
        syncTask = null
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
        if (activeVisuals.isEmpty()) stopSyncTask()
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
        stopSyncTask()
        val copy = activeVisuals.toMap()
        activeVisuals.clear()
        copy.values.forEach { visual ->
            Bukkit.getEntity(visual.rootDisplayId)?.let { removeEntityTree(it) }
        }
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