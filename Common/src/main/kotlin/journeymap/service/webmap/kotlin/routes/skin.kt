package journeymap.service.webmap.kotlin.routes

import com.mojang.blaze3d.platform.NativeImage
import io.javalin.http.ContentType
import io.javalin.http.Context
import journeymap.client.texture.IgnSkin
import net.minecraft.client.Minecraft
import java.nio.channels.Channels
import java.util.*


internal fun skinGet(ctx: Context)
{
    val uuid = UUID.fromString(ctx.pathParam("uuid"))
    val profile = Minecraft.getInstance().connection?.getPlayerInfo(uuid)?.profile

    val img: NativeImage = if (profile == null)
    {
        NativeImage(24, 24, false)
    }
    else
    {
        IgnSkin.getFaceImage(profile)
    }

    ctx.contentType(ContentType.IMAGE_PNG)
    img.writeToChannel(Channels.newChannel(ctx.outputStream()))
    ctx.outputStream().flush()
    img.close()
}
