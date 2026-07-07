package cubeplex.backpack

import org.bukkit.plugin.java.JavaPlugin

class BackpackPlugin : JavaPlugin() {

    lateinit var backpackManager: BackpackManager
        private set

    lateinit var visualManager: BackpackVisualManager
        private set

    override fun onEnable() {
        backpackManager = BackpackManager(this)
        visualManager = BackpackVisualManager(this)
        getCommand("mochila")?.setExecutor(BackpackCommand(this))
        server.pluginManager.registerEvents(BackpackListener(this), this)
        logger.info("CubeplexBackpack activado!")
    }

    override fun onDisable() {
        visualManager.removeAll()
        backpackManager.saveAll()
        logger.info("CubeplexBackpack desactivado!")
    }
}