package journeymap;

import journeymap.api.services.WebMapService;
import journeymap.service.webmap.Webmap;

public class WebMapServiceProvider implements WebMapService
{
    @Override
    public void start()
    {
        Webmap.INSTANCE.start();
    }

    @Override
    public void stop()
    {
        Webmap.INSTANCE.stop();
    }

    @Override
    public int getPort()
    {
        return Webmap.INSTANCE.getPort();
    }
}
