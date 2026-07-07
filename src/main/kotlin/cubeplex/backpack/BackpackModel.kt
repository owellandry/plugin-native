package cubeplex.backpack

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modelo 3D de la mochila (35 piezas). Datos extraídos de mochila.md.
 */
object BackpackModel {

    data class Part(val textureValue: String, val matrix: FloatArray)

    /** Jugador de pie: 1.8 alto, agachado: 1.5. Elytra ~61% de altura desde los pies. */
    private const val ELYTRA_HEIGHT_RATIO = 0.61
    /** Distancia detrás del torso (de pie). */
    private const val BACK_DEPTH = 0.40
    /** Subida fina respecto al punto elytra. */
    private const val HEIGHT_OFFSET = 0.04
    /** Extra hacia atrás al agacharse (compensa que la inclinación la empuja al frente). */
    private const val SNEAK_EXTRA_BACK = 0.08
    /** Centro vertical del modelo respecto al block_display raíz (piezas ty ≈ -0.004…0.87). */
    private const val MODEL_CENTER_Y = 0.43
    /** Inclinación del torso al agacharse (grados), similar al modelo del jugador. */
    private const val SNEAK_BODY_PITCH = 12f

    val ROOT_TRANSFORMATION = Transformation(
        Vector3f(0f, 0f, 0f),
        Quaternionf(),
        Vector3f(1f, 1f, 1f),
        Quaternionf()
    )

    fun bodyYaw(player: Player): Float = player.bodyYaw

    fun bodyPitch(player: Player): Float = if (player.isSneaking) SNEAK_BODY_PITCH else 0f

    /**
     * Posición en la espalda (elytra). Usa bodyYaw/bodyPitch para seguir el torso, no la cabeza.
     * Al agacharse baja con player.height e inclina el torso hacia adelante.
     */
    fun backAnchorLocation(player: Player): Location {
        val yaw = bodyYaw(player)
        val pitch = bodyPitch(player)
        val loc = player.location.clone()
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())

        val depth = BACK_DEPTH + if (player.isSneaking) SNEAK_EXTRA_BACK else 0.0
        val anchorY = player.height * ELYTRA_HEIGHT_RATIO - MODEL_CENTER_Y + HEIGHT_OFFSET
        val horizontalBack = depth * cos(pitchRad)
        val liftFromLean = depth * sin(pitchRad)

        val dx = sin(yawRad) * horizontalBack
        val dz = -cos(yawRad) * horizontalBack

        loc.add(dx, anchorY + liftFromLean, dz)
        loc.yaw = yaw
        loc.pitch = pitch
        return loc
    }

    val PARTS: List<Part> = listOf(
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODMwMGFmNzgxZmYyOWMzMzkwN2E4NDMzZmUyNTliNmU0MDEyMDRkNzEyZDExYjBhODJhNDZmNTllNzRkYzkwIn19fQ==", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 1.0f, -0.2190624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlYTA5NzcwZDQzNWRiODNjZTc3MDBlYmRhMTVmMDIyMWY4ZjA4MjNmMTc3OTI5ZDdhMzJlNjQzYmIwZWM1NyJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 1.0f, -0.2190624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ4YTI0N2JmMzZiMWY0ZDBhMzNiNTQ1Nzg5NzZlOWUzMDhlNWIxMmQ3NTA5YjI5NmQzZDMxMDc4YzkwOGY1NSJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 1.0f, -0.2190624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjVmZWY3NDlkYzYxNGY0NzY3MWU2MjliYTAyNDIxOTk4Mjk4YjFkMWQwNGI1OTMwZDY2OWYwMTA1MzdmMTMyNyJ9fX0=", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 1.0f, -0.2190624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2M4ODY5NDQzNmQwNDhiOWM1ODZjMjdhMTI1NmU1ZjdhNzk2OWY0NDI4MGNkNzg2ODI0NDUxYWJkMzRjODI0ZiJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.25f, 0.1560375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgwNzQzYmZjMzYzOTBjNzZmMTRmZWIyYWM4ZTQ3MzMwYjg5YTFjOTM3ZTBiMjc5NWEyODNmMzY3OGExNTk3MCJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.125f, 0.0622375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FmN2U1NzMxNmI4M2IwMTAyZmQ5ZTdiOTc5ZWE5Y2IzYjI4Mjg3NjY5NDhkMWMwMTc1Zjk2MzY1YmRjYWI4OCJ9fX0=", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.125f, 0.0622375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ5Y2E3Y2M3NTc1ZjA3MzgzZDkyYWM1ZTIyNDQ4NzQzNDRlMzcxOGMyNGZlZGNmYzFjMDYxMDY4OGNiYmM2OCJ9fX0=", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.25f, 0.1560375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhYmJlMzA1YWRlOGQ5N2RjMjA0N2FkNjgyYmJlNzJjM2U2YmVlZmNlZTY3NDEzOWQyMzkyZjRmYjNiYzIzOSJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 1.0f, 0.0f, 0.8694046918f, 0.0f, 0.0f, 1.0f, -0.0315624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("ewogICJ0aW1lc3RhbXAiIDogMTcxNjcxMDY5NDAwNCwKICAicHJvZmlsZUlkIiA6ICIxMDg3YmUwZTEzMWQ0NGMzYTNiYTUyNzIxY2ZkNzI5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYXJpdHk4MCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZWVkNmE1NGZmMzZlYzllMDVmNjRjZTBiZDM5MzYxNmUwZTkxOTlkMTk5YjQ3ZThlZDVlYzE1Yjc4OGRiZDY3IgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZDlkODJhYjE3ZmQ5MjAyMmRiZDRhODZjZGU0YzM4MmE3NTQwZTExN2ZhZTdiOWEyODUzNjU4NTA1YTgwNjI1IgogICAgfQogIH0KfQ==", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.125f, 0.0622375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("ewogICJ0aW1lc3RhbXAiIDogMTcxNjcxMDY5NDAwNCwKICAicHJvZmlsZUlkIiA6ICIxMDg3YmUwZTEzMWQ0NGMzYTNiYTUyNzIxY2ZkNzI5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYXJpdHk4MCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZWVkNmE1NGZmMzZlYzllMDVmNjRjZTBiZDM5MzYxNmUwZTkxOTlkMTk5YjQ3ZThlZDVlYzE1Yjc4OGRiZDY3IgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZDlkODJhYjE3ZmQ5MjAyMmRiZDRhODZjZGU0YzM4MmE3NTQwZTExN2ZhZTdiOWEyODUzNjU4NTA1YTgwNjI1IgogICAgfQogIH0KfQ==", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.125f, 0.0622375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRhYzBkM2Q5ZjZjOTY1NTdmY2ExNzdhMmJlOTNiNDMwMjRiZmE2YTZmNjA2YmI0NDFlODRmOWNjOGQzN2ZlYyJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.12484375f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.25f, 0.1560375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRjOGMxMTY1MjFiMWJmMzVmZGQyNTY5OWRkYzQ4MGExOTcxZjUyMDE4ZDc5YTM2M2YwYzFkODFlOWNiNzk0YSJ9fX0=", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.25f, 0.1560375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFkMDBkZjllZDQzYjRiY2RlOGY2MzcyOTA4NGE4NjMyYzc3NDgyOTdhNTI4N2VlN2E1NDI5MDdjZTg1NTA2In19fQ==", floatArrayOf(0.5f, 0.0f, 0.0f, 0.25015625f, 0.0f, 1.0f, 0.0f, 0.8694046918f, 0.0f, 0.0f, 1.0f, -0.0315624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJkYzcxNjJkYzBmMmQyNzIxZjhhN2YxNzFjNmY3OTIxMmZkNzc5MzMyZDgxMGFmMzcyNjE2ZmM0ZDk4NzcxMyJ9fX0=", floatArrayOf(1.0f, 0.0f, 0.0f, -0.18734375f, 0.0f, 0.5f, 0.0f, 0.6194046918f, 0.0f, 0.0f, 0.5f, -0.4065624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmIyMWFjZjNhZmNjNjU5NGM0MzA3NTExNjkwN2RhNWZkM2Q3MjU5NzQzN2FjMDFmYmYwNTM0YTg2ZDEzYWNjMCJ9fX0=", floatArrayOf(0.5f, 0.0f, 0.0f, 0.18765625f, 0.0f, 0.5f, 0.0f, 0.6194046918f, 0.0f, 0.0f, 0.5f, -0.4065624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmI0ZjlhZGVmN2FkNjg2OTk3ZDA2YjIwNjJiOTA4MjZiY2IwYjdlNDVlYTZlMzViZDNjYmEyZjU5ZThmYmIxMCJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.37515625f, 0.0f, 0.5f, 0.0f, 0.6194046918f, 0.0f, 0.0f, 0.5f, -0.4065624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBjYWY1MTEzYWNmODg2Y2VlOGU5YjAyN2U5MDk0NTk1ZjA0MTUwOTRmNTc3ZjkyNTBhNzAyOWZhMTA4MWQ5NiJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.31234375f, 0.0f, 1.0f, 0.0f, 0.7444046918f, 0.0f, 0.0f, 0.5f, 0.4060375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI0Mzc3YTBlZjYzOGFlMzc1NmFjYjA0N2UxMDljODg5NjIxODBkNDAyMzc5Y2EwN2RhNzJiNWEwZTViNDY1MSJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.31265625f, 0.0f, 1.0f, 0.0f, 0.7444046918f, 0.0f, 0.0f, 0.5f, 0.4060375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk1ZGNhNDBhMTAxMzM0NWUyN2ZlNjI3YzI2ZWU1MWQyZGU3ZTk2MmJkMjdlNzk4ZDM5MWZiMzQwZTQ5NTkyYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.31234375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.5f, 0.4060375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk1ZGNhNDBhMTAxMzM0NWUyN2ZlNjI3YzI2ZWU1MWQyZGU3ZTk2MmJkMjdlNzk4ZDM5MWZiMzQwZTQ5NTkyYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.31265625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.5f, 0.4060375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljZjY4ZWZjZWExZWQxMTk4MzU0ZDRhODMyNjM3ZDMxZDgyM2E4N2Q2NDgwM2QyMTBlN2Y1Y2JmZDljMzZmYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.43734375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.5f, -0.0315624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE4MzdlMTRkYzcyMTEyMDUxMTgwMTU5MTZmYmJhMDlhYjU0OGZjZmYwMDI4MmFlZTIxMzU4NDA2Y2VmNTBkZSJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.43734375f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.5f, -0.0315624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ1NTgwODBhMzZiZTUyOWQyNjEzMzgxZGFkZGEwNjE4NzFmNzFjYmEzMGZhYTU5NjllNDM3YzgzZWEwN2VmMiJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.43734375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.125f, 0.1247375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmMxNzEwYWI1NGNkZDMwMzc4MjNjNDZlNzk2OGU3ZjRkNWY4ZGNhNGY1YjNlZWRkNWZhY2U0YWI5NjZkNzRiYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.43734375f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, 0.125f, 0.1247375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRmMjg4MTAyZTlhMWYwZGZlZDA0YTk4MDY4NmI4NWZlYzRiY2E3MzJjNDEzZjlkNTQ4ODFhMWMwODg0YjZmMyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.31265625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.125f, 0.2497375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzgxMWNlNTNhMzBkZjI4MDU1MTdhZjc3YWMxMGFlODEzNDQ0NjUyNDY4ZmI4NzA2YWRiNjgxYTNmMzNkYTlhMCJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.31265625f, 0.0f, 1.0f, 0.0f, 0.7444046918f, 0.0f, 0.0f, 0.125f, 0.2497375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRmMjg4MTAyZTlhMWYwZGZlZDA0YTk4MDY4NmI4NWZlYzRiY2E3MzJjNDEzZjlkNTQ4ODFhMWMwODg0YjZmMyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.31234375f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, 0.125f, 0.2497375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzgxMWNlNTNhMzBkZjI4MDU1MTdhZjc3YWMxMGFlODEzNDQ0NjUyNDY4ZmI4NzA2YWRiNjgxYTNmMzNkYTlhMCJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.31234375f, 0.0f, 1.0f, 0.0f, 0.7444046918f, 0.0f, 0.0f, 0.125f, 0.2497375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljZjY4ZWZjZWExZWQxMTk4MzU0ZDRhODMyNjM3ZDMxZDgyM2E4N2Q2NDgwM2QyMTBlN2Y1Y2JmZDljMzZmYyJ9fX0=", floatArrayOf(-0.25f, 0.0f, 0.0f, 0.43765625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, -0.5f, 0.0251375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE4MzdlMTRkYzcyMTEyMDUxMTgwMTU5MTZmYmJhMDlhYjU0OGZjZmYwMDI4MmFlZTIxMzU4NDA2Y2VmNTBkZSJ9fX0=", floatArrayOf(-0.25f, 0.0f, 0.0f, 0.43765625f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, -0.5f, 0.0251375083f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ1NTgwODBhMzZiZTUyOWQyNjEzMzgxZGFkZGEwNjE4NzFmNzFjYmEzMGZhYTU5NjllNDM3YzgzZWEwN2VmMiJ9fX0=", floatArrayOf(-0.25f, 0.0f, 0.0f, 0.43765625f, 0.0f, 0.5f, 0.0f, 0.2444046918f, 0.0f, 0.0f, -0.125f, -0.1311624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmMxNzEwYWI1NGNkZDMwMzc4MjNjNDZlNzk2OGU3ZjRkNWY4ZGNhNGY1YjNlZWRkNWZhY2U0YWI5NjZkNzRiYyJ9fX0=", floatArrayOf(-0.25f, 0.0f, 0.0f, 0.43765625f, 0.0f, 0.25f, 0.0f, 0.3694046918f, 0.0f, 0.0f, -0.125f, -0.1311624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZiYWI5YTlmZWJiOTc0NTllYjkzODVhN2I3N2E2NzcwZDFmOGVjMjc5YTc4ZmUwMjI4MTg3ZDU2ZjExZTUzYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, 0.18765625f, 0.0f, 0.6563f, 0.0f, -0.0040953082f, 0.0f, 0.0f, 0.0031f, -0.4690624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
        Part("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZiYWI5YTlmZWJiOTc0NTllYjkzODVhN2I3N2E2NzcwZDFmOGVjMjc5YTc4ZmUwMjI4MTg3ZDU2ZjExZTUzYyJ9fX0=", floatArrayOf(0.25f, 0.0f, 0.0f, -0.18734375f, 0.0f, 0.6563f, 0.0f, -0.0040953082f, 0.0f, 0.0f, 0.0031f, -0.4690624917f, 0.0f, 0.0f, 0.0f, 1.0f)),
    )

    /**
     * El comando /summon guarda la matriz como 4 filas:
     * [sx, 0, 0, tx], [0, sy, 0, ty], [0, 0, sz, tz], [0, 0, 0, 1]
     * Paper usa Transformation (translation, rotation, scale) — no Matrix4f crudo.
     */
    fun transformationFromSummon(values: FloatArray): Transformation {
        require(values.size == 16)
        return Transformation(
            Vector3f(values[3], values[7], values[11]),
            Quaternionf(),
            Vector3f(values[0], values[5], values[10]),
            Quaternionf()
        )
    }
}