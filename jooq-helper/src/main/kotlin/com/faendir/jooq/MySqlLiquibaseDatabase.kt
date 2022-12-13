package com.faendir.jooq

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.jooq.meta.mysql.MySQLDatabase
import org.jooq.tools.jdbc.JDBCUtils
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.sql.Connection
import java.util.*

@Suppress("unused")
class MySqlLiquibaseDatabase : MySQLDatabase() {
    private lateinit var mySQLContainer: MySQLContainer<*>
    private var conn: Connection? = null
    private var ctx: DSLContext? = null
    override fun create0(): DSLContext {
        if (conn == null) {
            val scripts = Objects.requireNonNull(properties.getProperty("scripts"))
            val mySqlVersion = properties.getProperty("mySqlVersion", "8.0.31")
            val dbName = properties.getProperty("databaseName", "test")
            try {
                mySQLContainer = MySQLContainer(DockerImageName.parse("mysql:$mySqlVersion"))
                    .withDatabaseName(dbName)
                    .withCommand("--log-bin-trust-function-creators=1")
                mySQLContainer.start()
                conn = mySQLContainer.createConnection("")
                ctx = DSL.using(conn)
                val scriptFile = File(scripts)
                val database = DatabaseFactory
                    .getInstance()
                    .findCorrectDatabaseImplementation(JdbcConnection(conn))
                val liquibase = Liquibase(
                    scriptFile.name,
                    DirectoryResourceAccessor(scriptFile.parentFile),
                    database
                )
                liquibase.update(Contexts(), LabelExpression())
            } catch (e: Exception) {
                log.error("Error while preparing schema for code generation", e)
                throw DataAccessException("Error while preparing schema for code generation", e)
            }
        }
        return ctx!!
    }

    override fun close() {
        JDBCUtils.safeClose(conn)
        conn = null
        if (::mySQLContainer.isInitialized) {
            mySQLContainer.close()
        }
        ctx = null
        super.close()
    }

    companion object {
        private val log = LoggerFactory.getLogger(MySqlLiquibaseDatabase::class.java)
    }
}