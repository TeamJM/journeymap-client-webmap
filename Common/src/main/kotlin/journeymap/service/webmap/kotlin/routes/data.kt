package journeymap.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.data.AllData
import journeymap.client.data.DataCache
import journeymap.client.data.ImagesData
import journeymap.client.model.EntityDTO
import journeymap.client.waypoint.Waypoint
import journeymap.common.Journeymap
import org.apache.logging.log4j.Logger

private val GSON: Gson =
    GsonBuilder().setPrettyPrinting().setExclusionStrategies(EntityDTO.EntityDTOExclusionStrategy()).create()
private val logger: Logger = Journeymap.getLogger("webmap/routes/data")


val dataTypesRequiringSince = listOf<String>("all", "images")

/**
 * TODO: Remove this stupid mess
 *
 * So this is a fucking awful hack that we need to use right now to work around an issue with how Forge handles
 * Java modules. Essentially, GSON sees an empty map and tries to make use of its constructor to serialize it,
 * which forge doesn't allow via the module system, so it blows up. This function exists to force an empty map
 * into a map with a single placeholder key, which is terrible, but works. Oh, and it does type coercion from `Any?`
 * too because the maps taken from the cache have their types completely erased.
 *
 * You're welcome!
 */
private fun Any?.modulesAreTerrible(): MutableMap<*, *> {
    val entry = (this as Map<*, *>?)?.toMutableMap()

    return if (entry.isNullOrEmpty()) {
        mutableMapOf("//" to null)
    } else {
        entry
    }
}

internal fun dataGet(ctx: Context)
{
    val since: Long? = ctx.queryParam("images.since")?.toLong()
    val type = ctx.pathParam("type")

    if (type in dataTypesRequiringSince && since == null) {
        logger.warn("Data type '$type' requested without 'images.since' parameter")
        ctx.status(400)
        ctx.result("Data type '$type' requires 'images.since' parameter.")
        return
    }

    val data: Any? = when (type) {
        "all" -> DataCache.INSTANCE.getAll(since!!).let {
            val map = it.toMutableMap()

            map[AllData.Key.animals] = map[AllData.Key.animals].modulesAreTerrible()
            map[AllData.Key.mobs] = map[AllData.Key.mobs].modulesAreTerrible()
            map[AllData.Key.players] = map[AllData.Key.players].modulesAreTerrible()
            map[AllData.Key.villagers] = map[AllData.Key.villagers].modulesAreTerrible()
            map[AllData.Key.waypoints] = map[AllData.Key.waypoints].modulesAreTerrible()

            map
        }

        "animals" -> DataCache.INSTANCE.getAnimals(false).modulesAreTerrible()
        "mobs" -> DataCache.INSTANCE.getMobs(false).modulesAreTerrible()
        "images" -> ImagesData(since!!)
        "messages" -> DataCache.INSTANCE.getMessages(false)
        "player" -> DataCache.INSTANCE.getPlayer(false)
        "players" -> DataCache.INSTANCE.getPlayers(false).modulesAreTerrible()
        "world" -> DataCache.INSTANCE.getWorld(false)
        "villagers" -> DataCache.INSTANCE.getVillagers(false).modulesAreTerrible()
        "waypoints" -> {
            val waypoints: Collection<Waypoint> = DataCache.INSTANCE.getWaypoints(false)
            val wpMap = mutableMapOf<String, Waypoint>()

            for (waypoint in waypoints) {
                wpMap[waypoint.id] = waypoint
            }

            wpMap.toMap().modulesAreTerrible()
        }

        else -> null
    }

    if (data == null) {
        logger.warn("Unknown data type '$type'")
        ctx.status(400)
        ctx.result( "Unknown data type '$type'")
        return
    }

    ctx.contentType(ContentType.APPLICATION_JSON)
    ctx.result(GSON.toJson(data))
}
