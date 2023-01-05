package journeymap.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.JourneymapClient
import journeymap.client.io.FileHandler
import journeymap.client.io.MapSaver
import journeymap.client.model.MapType
import journeymap.client.task.multi.MapRegionTask
import journeymap.client.task.multi.SaveMapTask
import journeymap.common.Journeymap
import journeymap.common.helper.DimensionHelper
import net.minecraft.client.Minecraft
import net.minecraft.world.level.Level

import org.apache.logging.log4j.Logger
import java.io.File


private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
private val logger: Logger = Journeymap.getLogger("webmap/routes/action")


internal fun actionGet(ctx: Context) {
    val minecraft: Minecraft = Minecraft.getInstance()
    val level: Level? = minecraft.level

    if (level == null) {
        logger.warn("Action requested before world loaded")
        ctx.status(400)
        ctx.result("World not loaded") // TODO: Handle world being unloaded
        return
    }

    if (!JourneymapClient.getInstance().isMapping) {
        logger.warn("Action requested before Journeymap started")
        ctx.status(400)
        ctx.result("JourneyMap is still starting") // TODO: Handle JM not being started
        return
    }

    val type = ctx.pathParam("type")

    when (type) {
        "automap" -> autoMap(ctx, minecraft, level)
        "savemap" -> saveMap(ctx, minecraft, level)

        else -> {
            logger.warn("Unknown action type '$type'")
            ctx.status(400)

            ctx.result("Unknown action type '$type'")
            return
        }
    }
}

internal fun saveMap(ctx: Context, minecraft: Minecraft, level: Level) {
    val worldDir: File = FileHandler.getJMWorldDir(minecraft)

    if (!worldDir.exists() || !worldDir.isDirectory) {
        logger.warn("JM world directory not found")
        ctx.status(500)
        ctx.result("Unable to find JourneyMap world directory")
    }

    val dimension: String = ctx.queryParam("dim") ?: "minecraft:overworld"
    val mapTypeString: String = ctx.queryParam("mapType") ?: MapType.Name.day.name

    var vSlice: Int? = ctx.queryParam("depth")?.toInt() ?: 0
    val mapTypeName: MapType.Name

    try {
        mapTypeName = MapType.Name.valueOf(mapTypeString)
    } catch (e: IllegalArgumentException) {
        logger.warn("Invalid map type '$mapTypeString'")
        ctx.status(400)
        ctx.result("Invalid map type '$mapTypeString'")
        return
    }

    if (mapTypeName != MapType.Name.underground) {
        vSlice = null
    }

    val hardcore: Boolean = level.levelData.isHardcore
    val mapType: MapType = MapType.from(mapTypeName, vSlice, DimensionHelper.getWorldKeyForName(dimension))

    if (mapType.isUnderground && hardcore) {
        logger.warn("Cave mapping is not allowed on hardcore servers")
        ctx.status(400)
        ctx.result("Cave mapping is not allowed on hardcore servers")
        return
    }

    val mapSaver = MapSaver(worldDir, mapType)

    if (!mapSaver.isValid) {
        logger.info("No image files to save")
        ctx.status(400)
        ctx.result("No image files to save")
        return
    }

    JourneymapClient.getInstance().toggleTask(SaveMapTask.Manager::class.java, true, mapSaver)

    val data = mutableMapOf<String, Any>()

    data["filename"] = mapSaver.saveFileName

    ctx.contentType(ContentType.APPLICATION_JSON)
    ctx.result(GSON.toJson(data))
}

internal fun autoMap(ctx: Context, minecraft: Minecraft, level: Level) {
    val data = mutableMapOf<String, Any>()
    val enabled: Boolean = JourneymapClient.getInstance().isTaskManagerEnabled(MapRegionTask.Manager::class.java)
    val scope: String = ctx.queryParam("scope") ?: "stop"

    if (scope == "stop" && enabled) {
        JourneymapClient.getInstance().toggleTask(MapRegionTask.Manager::class.java, false, false)
        data["message"] = "automap_complete"
    } else if (!enabled) {
        val doAll: Boolean = scope == "all"
        JourneymapClient.getInstance().toggleTask(MapRegionTask.Manager::class.java, true, doAll)
        data["message"] = "automap_started"
    } else {
        data["message"] = "automap_already_started"
    }

    ctx.contentType(ContentType.APPLICATION_JSON)
    ctx.result(GSON.toJson(data))
}
