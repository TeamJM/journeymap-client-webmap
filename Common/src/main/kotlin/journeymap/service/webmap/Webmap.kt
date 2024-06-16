package journeymap.service.webmap

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.staticfiles.Location
import journeymap.client.Constants
import journeymap.client.JourneymapClient
import journeymap.client.io.FileHandler
import journeymap.common.Journeymap
import journeymap.service.webmap.kotlin.routes.*
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.net.ServerSocket


object Webmap {
    val logger: Logger = Journeymap.getLogger("webmap")

    var port: Int = 0
    var started: Boolean = false
    var app: Javalin? = null
    fun start() {
        if (!started) {
            findPort()
            initialise()
            started = true
            logger.info("Webmap is now listening on port $port.")
        }
    }

    private fun initialise() {
        try {
            app = Javalin.create {
                val assetsRootProperty: String? = System.getProperty("journeymap.webmap.assets_root", null)
                val testFile = File("../src/main/resources" + FileHandler.ASSETS_WEBMAP)

                when {
                    assetsRootProperty != null -> {
                        logger.info("Detected 'journeymap.webmap.assets_root' property, serving static files from: $assetsRootProperty")
                        it.staticFiles.add(assetsRootProperty, Location.EXTERNAL)
                    }

                    testFile.exists() -> {
                        val assets = testFile.canonicalPath
                        logger.info("Development environment detected, serving static files from the filesystem.: $assets")
                        it.staticFiles.add(testFile.canonicalPath, Location.EXTERNAL)
                    }

                    else -> {
                        val dir = File(FileHandler.getMinecraftDirectory(), Constants.WEB_DIR)
                        if (!dir.exists()) {
                            logger.info("1Attempting to copy web content to {}", File(Constants.JOURNEYMAP_DIR, "web"))
                            val created =
                                FileHandler.copyResources(
                                    dir,
                                    ResourceLocation(journeymap.Constants.MOD_ID, "web"),
                                    "",
                                    false
                                )
                            logger.info("Web content copied successfully: {}", created)
                        }

                        if (dir.exists()) {
                            logger.info("Loading web content from local: {}", dir.path)
                            it.staticFiles.add(dir.path, Location.EXTERNAL)
                        } else {
                            logger.info("Loading web content from jar: {}", FileHandler.ASSETS_WEBMAP)
                            it.staticFiles.add(FileHandler.ASSETS_WEBMAP, Location.CLASSPATH)
                        }
                    }
                }
            }.before { ctx ->
                ctx.header("Access-Control-Allow-Origin", "*")
                ctx.header("Cache-Control", "no-cache")
            }
            get("/waypoint/{id}/icon", ::iconGet)
            get("/data/{type}", ::dataGet)
            get("/logs", ::logGet)
            get("/polygons", ::polygonsGet)
            get("/resources", ::resourcesGet)
            get("/skin/{uuid}", ::skinGet)
            get("/status", ::statusGet)
            get("/tiles/tile.png", ::tilesGet)
            app?.start(port)

        } catch (e: Exception) {
            logger.error("Failed to start server: $e")
            stop()
        }
    }

    fun stop() {
        if (started) {
            app?.stop()
            started = false
            logger.info("Webmap stopped.")
        }
    }

    private fun findPort(tryCurrentPort: Boolean = true) {
        if (port == 0) {
            // the client may be null due to class loading issues in dev
            // this just suppresses the exception in dev. it does not fix the class loading issue or the webmap
            if (JourneymapClient.getInstance() == null || JourneymapClient.getInstance().webMapProperties == null) {
                port = 8080
            } else {
                // We set this here because we need to get it again if the user changes the setting
                port = JourneymapClient.getInstance().webMapProperties.port.asInteger ?: 0
                logger.info("port found, set to $port")
            }
        }

        if (tryCurrentPort) {
            try {
                val socket = ServerSocket(port)
                port = socket.localPort
                socket.close()
            } catch (e: IOException) {
                logger.warn("Configured port $port could not be bound: $e")
                findPort(false)
            }

            logger.info("Configured port $port is available.")
        } else {
            val socket = ServerSocket(0)
            port = socket.localPort
            socket.close()

            logger.info("New port $port assigned by ServerSocket.")
        }
    }
}
