package journeymap_webmap.service.webmap.kotlin.routes

import com.mojang.blaze3d.platform.NativeImage
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.JourneymapClient
import journeymap.client.data.WorldData
import journeymap.client.io.FileHandler
import journeymap.client.io.RegionImageHandler
import journeymap.client.model.MapType
import journeymap.client.render.map.RegionTile
import journeymap.common.Journeymap
import journeymap.common.helper.DimensionHelper
import net.minecraft.client.Minecraft
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import org.apache.logging.log4j.Logger
import org.eclipse.jetty.io.EofException
import java.io.File
import java.io.OutputStream
import java.nio.channels.Channels
import javax.imageio.IIOException
import kotlin.math.pow
import kotlin.math.roundToInt


private val logger: Logger = Journeymap.getLogger("webmap/routes/tiles")


internal fun tilesGet(ctx: Context) {
    val x: Int = ctx.queryParam("x")?.toInt() ?: 0
    var y: Int? = ctx.queryParam("y")?.toInt() ?: 0
    val z: Int = ctx.queryParam("z")?.toInt() ?: 0

    val dimension: String = ctx.queryParam("dimension") ?: "minecraft:overworld"
    val mapTypeString: String = ctx.queryParam("mapTypeString") ?: MapType.Name.day.name
    val zoom: Int = ctx.queryParam("zoom")?.toInt() ?: 0

    val minecraft: Minecraft? = Minecraft.getInstance()
    val level: Level? = minecraft?.level

    if (level == null) {
        logger.warn("Tiles requested before world loaded")
        ctx.status(400)
        ctx.result("World not loaded")
        return
    }

    if (!JourneymapClient.getInstance().isMapping) {
        logger.warn("Tiles requested before JourneyMap started")
        ctx.status(400)
        ctx.result("JourneyMap is still starting")
        return
    }

    val worldDir: File = FileHandler.getJMWorldDir(minecraft)

    try {
        if (!worldDir.exists() || !worldDir.isDirectory) {
            logger.warn("JM world directory not found")
            ctx.status(404)
            ctx.result("World not found")
            return
        }
    } catch (e: NullPointerException) {
        logger.warn("NPE occurred while locating JM world directory")
        ctx.status(404)
        ctx.result("World not found")
        return
    }

    val mapTypeName: MapType.Name?

    try {
        mapTypeName = MapType.Name.valueOf(mapTypeString)
    } catch (e: IllegalArgumentException) {
        logger.warn("Invalid map type supplied during tiles request: $mapTypeString")
        ctx.status(400)
        ctx.result("Invalid map type: $mapTypeString")
        return
    }

    if (mapTypeName != MapType.Name.underground) {
        y = null  // Only underground maps have elevation
    }

    if (mapTypeName == MapType.Name.underground && WorldData.isHardcoreAndMultiplayer()) {
        logger.debug("Blank tile returned for underground view on a hardcore server")
        val output: OutputStream = ctx.outputStream()

        ctx.contentType(ContentType.IMAGE_PNG)
        output.write(RegionImageHandler.getBlank512x512ImageFile().readBytes())
        output.flush()

    }

    // TODO: Test out this math with Leaflet

    val scale: Int = 2.0.pow(zoom).roundToInt()
    val distance: Int = 32 / scale

    val minChunkX: Int = x * distance
    val minChunkY: Int = z * distance

    val maxChunkX = minChunkX + distance - 1
    val maxChunkY = minChunkY + distance - 1

    val startCoord = ChunkPos(minChunkX, minChunkY)
    val endCoord = ChunkPos(maxChunkX, maxChunkY)

    // TODO: Show Grid could be a URL parameter
    val showGrid: Boolean = JourneymapClient.getInstance().fullMapProperties.showGrid.get()
    val mapType = MapType(mapTypeName, y, DimensionHelper.getWorldKeyForName(dimension))

    val img: NativeImage = RegionImageHandler.getMergedChunks(
        worldDir, startCoord, endCoord, mapType, true, null,
        RegionTile.TILE_SIZE, RegionTile.TILE_SIZE, false, showGrid
    )

    val output: OutputStream = ctx.outputStream()

    try {
        ctx.contentType(ContentType.IMAGE_PNG)
        img.writeToChannel(Channels.newChannel(output))
        output.flush()

    } catch (e: EofException) {
        logger.info("Connection closed while writing image response. Webmap probably reloaded.")
        ctx.status(404)
    } catch (e: IIOException) {
        logger.info("Connection closed while writing image response. Webmap probably reloaded.")
        ctx.status(404)
    }
    img.close()
    // TODO: Profiling, as in the original TileService
}
