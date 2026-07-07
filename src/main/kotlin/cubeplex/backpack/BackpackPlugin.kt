package cubeplex.backpack

import org.bukkit.plugin.java.JavaPlugin

class BackpackPlugin : JavaPlugin() {

    lateinit var backpackManager: BackpackManager
        private set

    override fun onEnable() {
        backpackManager = BackpackManager(this)
        getCommand("mochila")?.setExecutor(BackpackCommand(this))
        server.pluginManager.registerEvents(BackpackListener(this), this)
        logger.info("CubeplexBackpack activado!")
    }

    override fun onDisable() {
        BackpackListener.removeAllBackpacks()
        backpackManager.saveAll()
        logger.info("CubeplexBackpack desactivado!")
    }
}
