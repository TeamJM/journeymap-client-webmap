package journeymap_webmap.service.webmap.kotlin.routes

import com.mojang.blaze3d.platform.NativeImage
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.texture.TextureCache
import java.nio.channels.Channels


internal fun iconGet(ctx: Context) {
    val id = ctx.pathParam("id")

    val img: NativeImage? = TextureCache.getColorizedWaypointIcon(id)?.pixels!!

    ctx.contentType(ContentType.IMAGE_PNG)
    img?.writeToChannel(Channels.newChannel(ctx.outputStream()))
    ctx.outputStream().flush()
}

