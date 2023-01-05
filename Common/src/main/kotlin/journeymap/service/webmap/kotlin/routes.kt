package journeymap.service.webmap.kotlin

import io.javalin.http.Context
import journeymap.common.Journeymap
import journeymap.common.log.LogFormatter
import org.apache.logging.log4j.Logger


// Variable/value declarations
private val logger: Logger = Journeymap.getLogger("webmap/routes")


internal fun wrapForError(function: (Context) -> Any): (Context) -> Any {
    fun wrapper(ctx: Context): Any {
        return try {
            function(ctx)
        } catch (t: Throwable) {
            logger.error(LogFormatter.toString(t))
            ctx.status(500)

            t.localizedMessage
        }
    }

    return ::wrapper
}
