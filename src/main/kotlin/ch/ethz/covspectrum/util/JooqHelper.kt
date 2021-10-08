package ch.ethz.covspectrum.util

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection

object JooqHelper {
    fun getDSLCtx(connection: Connection?): DSLContext {
        return DSL.using(connection, SQLDialect.POSTGRES)
    }
}
