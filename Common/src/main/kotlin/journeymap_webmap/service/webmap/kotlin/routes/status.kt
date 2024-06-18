package journeymap_webmap.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.JourneymapClient
import journeymap.client.ui.minimap.MiniMap
import journeymap_webmap.service.webmap.kotlin.enums.WebmapStatus
import net.minecraft.client.Minecraft

private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()


internal fun statusGet(ctx: Context)
{
    val data: MutableMap<String, Any> = mutableMapOf()

    var status = when
    {
        Minecraft.getInstance()?.level == null -> WebmapStatus.NO_WORLD
        !JourneymapClient.getInstance().isMapping     -> WebmapStatus.STARTING

        else                                  -> WebmapStatus.READY
    }

    if (status == WebmapStatus.READY)
    {
        val mapState = MiniMap.state()

        data["mapType"] = mapState.mapType.name()

        val allowedMapTypes: Map<String, Boolean> = mapOf(
            "cave" to (mapState.isCaveMappingAllowed && mapState.isCaveMappingEnabled),
            "surface" to mapState.isSurfaceMappingAllowed,
            "topo" to mapState.isTopoMappingAllowed
        )

        if (allowedMapTypes.filterValues { it }.isEmpty())
        {
            status = WebmapStatus.DISABLED
        }

        data["allowedMapTypes"] = allowedMapTypes
    }

    data["status"] = status.status

    ctx.contentType(ContentType.APPLICATION_JSON)
    ctx.result(GSON.toJson(data))
}
