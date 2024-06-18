package journeymap_webmap.service.webmap.kotlin.routes

import io.javalin.http.Context
import journeymap.client.log.JMLogger
import journeymap.common.Journeymap
import org.apache.logging.log4j.Logger
import java.io.File


private val logger: Logger = Journeymap.getLogger("webmap/routes/log")


internal fun logGet(ctx: Context)
{
    val logFile: File = JMLogger.getLogFile()

    if (logFile.exists())
    {
        ctx.res().addHeader("Content-Disposition", "inline; filename=\"journeymap.log\"")
        ctx.outputStream().write(logFile.readBytes())
        ctx.outputStream().flush()

    }
    else
    {
        logger.warn("Unable to find JourneyMap logfile")
        ctx.status(404)
        ctx.result("Not found: ${logFile.path}")
    }
}
