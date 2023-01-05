package journeymap;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID)
public class ForgeClient
{
}
