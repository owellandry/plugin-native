# CubeplexBackpack

Plugin de Minecraft Paper 1.21.11 que añade una mochila portable equipable en el slot de pechera.

## Objetivo

Crear una mochila que funcione de manera similar a las elitras: se equipa en el slot de pechera y tiene un inventario propio de 27 slots (3 filas). Al hacer clic derecho o usar el comando `/mochila`, se abre el inventario de la mochila (separado del ender chest).

## Estado Actual

- [x] Item equipable (elytra con nombre y lore personalizado)
- [x] Visual 3D en la espalda del jugador (display entities)
- [x] Inventario propio (27 slots) con nombre "⭐ Mochila"
- [x] Persistencia en archivos JSON (`plugins/backpacks/[uuid].json`)
- [x] Comando `/mochila` - abre el inventario
- [x] Comando `/mochila give` - obtener una mochila
- [x] Click derecho en la entidad visual - abre el inventario
- [ ] El visual 3D se ve correctamente <-- en progreso

## Cómo funciona

### Equipar la mochila
```
/mochila give
```
Te da una elytra llamada "⭐ Mochila". Equipala en el slot de pechera (38).

### Usar la mochila
- `/mochila` - abre tu inventario de mochila
- Click derecho en la mochila visual (cuando esté visible)

### Inventario propio
El inventario de la mochila es **totalmente separado** del ender chest. Se guarda automáticamente al cerrar el inventario o al desconectarse.

## Technical Details

### Stack
- **Paper** 1.21.11 (build 107)
- **Kotlin** JVM 1.9.22
- **Java** 21 (Temurin)
- **Gradle** 8.7 con Shadow plugin (Kotlin embebido)

### Arquitectura
```
plugin/
├── src/main/kotlin/cubeplex/backpack/
│   ├── BackpackPlugin.kt      # Clase principal, onEnable/onDisable
│   ├── BackpackCommand.kt    # Comando /mochila [give]
│   ├── BackpackListener.kt    # Eventos, spawn de entidades visuales
│   └── BackpackManager.kt    # Gestión de inventarios y persistencia
├── src/main/resources/
│   └── plugin.yml            # Metadata del plugin
└── build.gradle.kts          # Build config con shadowJar
```

### Identificación
- Item: `PersistentDataType.BOOLEAN` con key `cubeplex:backpack`
- Entidad visual: scoreboard tag `cubeplex_backpack`

## Compilar

```bash
cd plugin
./gradlew clean shadowJar
```

El JAR con Kotlin embebido estará en `build/libs/`.

## Problemas Conocidos

1. **El visual 3D no se ve**: Las texturas de player heads via Minecraft.net no cargan correctamente en 1.21+. Se está investigando usar un resource pack en vez de texturas remotas.

## TODO

- [ ] Arreglar el visual 3D de la mochila
- [ ] Posiblemente usar un resource pack para las texturas
- [ ] Optimizar el spawn de entidades (solo crear cuando es necesario)
- [ ] Posibilidad de color/textura personalizada de mochila
