package journeymap.service.webmap.kotlin.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.javalin.http.ContentType
import journeymap.api.client.impl.ClientAPI
import journeymap.client.api.display.Context
import journeymap.client.api.display.PolygonOverlay
import journeymap.client.cartography.color.RGB
import journeymap.client.render.draw.DrawPolygonStep
import journeymap.client.render.draw.OverlayDrawStep


private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()


internal fun polygonsGet(ctx: io.javalin.http.Context)
{
    val data = mutableListOf<Any>()
    val steps = mutableListOf<OverlayDrawStep>()
    val fullscreenState = ClientAPI.INSTANCE.getUIState(Context.UI.Fullscreen)
    val minimapState = ClientAPI.INSTANCE.getUIState(Context.UI.Minimap)

    val uiState = if (fullscreenState?.active == false && minimapState?.active == true) {
        minimapState
    } else {
        fullscreenState
    }

    ClientAPI.INSTANCE.getDrawSteps(steps, uiState!!)

    for (step in steps)
    {
        if (step is DrawPolygonStep)
        {
            val polygon = step.overlay as PolygonOverlay
            val points = mutableListOf<Map<String, Int>>()

            polygon.outerArea.points.forEach { point ->
                points.add(
                    mapOf(
                        "x" to point.x,
                        "y" to point.y,
                        "z" to point.z
                    )
                )
            }

            val holes = mutableListOf<MutableList<Map<String, Int>>>()

            if (polygon.holes != null)
            {
                for (hole in polygon.holes)
                {
                    val holePoints = mutableListOf<Map<String, Int>>()

                    for (holePoint in hole.points)
                    {
                        holePoints.add(
                            mapOf(
                                "x" to holePoint.x,
                                "y" to holePoint.y,
                                "z" to holePoint.z
                            )
                        )
                    }

                    holes.add(holePoints)
                }
            }

            data.add(
                mapOf(
                    "fillColor" to RGB.toHexString(polygon.shapeProperties.fillColor),
                    "fillOpacity" to polygon.shapeProperties.fillOpacity,
                    "strokeColor" to RGB.toHexString(polygon.shapeProperties.strokeColor),
                    "strokeOpacity" to polygon.shapeProperties.strokeOpacity,
                    "strokeWidth" to polygon.shapeProperties.strokeWidth,
                    "imageLocation" to (step.textureResource?.toString() ?: ""),
                    "texturePositionX" to polygon.shapeProperties.texturePositionX,
                    "texturePositionY" to polygon.shapeProperties.texturePositionY,
                    "textureScaleX" to polygon.shapeProperties.textureScaleX,
                    "textureScaleY" to polygon.shapeProperties.textureScaleY,

                    "holes" to holes,
                    "points" to points
                )
            )
        }
    }

    ctx.contentType(ContentType.APPLICATION_JSON)
    ctx.result(GSON.toJson(data))
}
