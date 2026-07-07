package cubeplex.backpack

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class BackpackVisualManager(private val plugin: BackpackPlugin) {

    // UUID del jugador → UUID de la entidad ROOT (invisible) que hace de ancla
    private val visuals = mutableMapOf<UUID, UUID>()

    fun hasVisual(playerId: UUID) = visuals.containsKey(playerId)

    fun spawn(player: Player) {
        if (hasVisual(player.uniqueId)) return

        val world = player.world

        // 1. Entidad raíz — completamente invisible, solo sirve de "vehículo"
        //    Usamos un Interaction (sin hitbox visible) o un Marker
        val root = world.spawn(player.location, org.bukkit.entity.Interaction::class.java) { e ->
            e.interactionWidth = 0f
            e.interactionHeight = 0f
            e.addScoreboardTag(BackpackListener.ENTITY_TAG)
            e.isPersistent = false
        }

        // 2. El jugador lleva al root como pasajero → se mueve con él sin TP
        player.addPassenger(root)

        // 3. Spawnear cada pieza del modelo como pasajero del root
        BackpackModel.PARTS.forEach { part ->
            val head = world.spawn(player.location, org.bukkit.entity.ArmorStand::class.java) { stand ->
                stand.isVisible = false
                stand.isSmall = true
                stand.isMarker = true
                stand.setGravity(false)
                stand.isPersistent = false
                // Aplicar textura de skull via NMS o via SkullMeta en el helmet
                val skull = createSkullItem(part.textureValue)
                stand.equipment.helmet = skull

                // Aplicar la transformación del modelo
                val t = BackpackModel.transformationFromSummon(part.matrix)
                // Posicionar relativo al root con el offset de la espalda
                val offset = org.bukkit.util.Vector(t.translation.x.toDouble(), t.translation.y.toDouble(), t.translation.z.toDouble())
                stand.teleport(player.location.add(offset))
                stand.addScoreboardTag(BackpackListener.ENTITY_TAG)
            }
            root.addPassenger(head)
        }

        visuals[player.uniqueId] = root.uniqueId
    }

    fun remove(playerId: UUID) {
        val rootId = visuals.remove(playerId) ?: return
        val root = plugin.server.getEntity(rootId) ?: return

        // Quitar todos los pasajeros (las piezas del modelo)
        root.passengers.toList().forEach { it.remove() }
        root.remove()
    }

    fun removeAll() {
        visuals.keys.toList().forEach { remove(it) }
    }

    // syncPosition y refreshOffset ya NO son necesarios — el riding lo hace automático
    // Pero los dejamos vacíos para no romper las llamadas existentes en BackpackListener
    fun syncPosition(player: Player) { /* no-op: passenger riding se encarga */ }
    fun refreshOffset(player: Player) { /* no-op: passenger riding se encarga */ }

    private fun createSkullItem(textureValue: String): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD)
        val meta = skull.itemMeta as org.bukkit.inventory.meta.SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.setProperty(ProfileProperty("textures", textureValue))
        meta.playerProfile = profile
        skull.itemMeta = meta
        return skull
    }
}