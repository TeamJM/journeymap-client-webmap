package journeymap.common.kotlin.extensions

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import java.io.InputStream

fun ResourceLocation.getResourceAsStream(): InputStream {
    return Minecraft.getInstance().resourceManager.open(this)
}
