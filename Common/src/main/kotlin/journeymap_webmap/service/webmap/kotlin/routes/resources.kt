package journeymap_webmap.service.webmap.kotlin.routes

import com.mojang.blaze3d.platform.NativeImage
import io.javalin.http.Context
import journeymap.client.JourneymapClient
import journeymap.client.io.FileHandler.ASSETS_JOURNEYMAP_UI
import journeymap.client.render.draw.MobIconCache
import journeymap.client.texture.TextureCache
import journeymap.common.Journeymap
import journeymap_webmap.common.kotlin.extensions.getResourceAsStream
import journeymap_webmap.service.webmap.Webmap
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.Logger
import org.eclipse.jetty.io.EofException
import java.io.FileNotFoundException
import java.nio.channels.Channels
import javax.imageio.IIOException


private val logger: Logger = Journeymap.getLogger("webmap/routes/resources")


private val ALLOWED_EXTENSIONS: List<String> = listOf(
    // For the sake of security, we don't want the webmap giving unauthorised access to random resource files
    "png"
)


internal fun resourcesGet(ctx: Context)
{
    val img: NativeImage
    val resource = ctx.queryParam("resource")
    val resourceLocation = resource?.let { ResourceLocation.parse(it) }
    var close = false
    var extension = resource?.split('.')?.last()

    if (Minecraft.getInstance().level == null || !JourneymapClient.getInstance().isMapping) {
        ctx.result("")
        return
    }

    if (extension?.contains(":") == true) {
        // So we can actually get the filetype from extension
        extension = extension.split(":").first()
    }

    if ("fake" == resourceLocation?.namespace) {
        img = TextureCache.getTexture(resourceLocation)?.pixels!!
    } else {
        img = try {
            MobIconCache.getWebMapIcon(resourceLocation)?.pixels!!
        } catch (e: NullPointerException) {
            close = true
            NativeImage.read(resourceLocation?.getResourceAsStream()!!)
        } catch (e: FileNotFoundException) {
            logger.warn("File at resource location not found: $resource")
            ctx.status(404)
            close = true
            NativeImage.read(Webmap.javaClass.getResource("$ASSETS_JOURNEYMAP_UI/img/marker-dot-32.png").openStream())
        } catch (e: EofException) {
            logger.info("Connection closed while writing image response. Webmap probably reloaded.")
            ctx.result("")
            return
        } catch (e: IIOException) {
            logger.info("Connection closed while writing image response. Webmap probably reloaded.")
            ctx.result("")
            return
        } catch (e: Exception) {
            logger.error("Exception thrown while retrieving resource at location: $resource", e)
            ctx.status(500)
            close = true
            NativeImage.read(Webmap.javaClass.getResource("$ASSETS_JOURNEYMAP_UI/img/marker-dot-32.png").openStream())
        }
    }

    ctx.contentType("image/${extension}")
    img.writeToChannel(Channels.newChannel(ctx.outputStream()))
    ctx.outputStream().flush()
    // close images read from disk to prevent memory leaks
    if (close) {
        img.close()
    }
}
