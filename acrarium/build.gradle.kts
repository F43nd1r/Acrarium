import com.vaadin.gradle.vaadin
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Property

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion(libs.versions.jooq.get())
        }
    }
}
plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.vaadin)
    war
    alias(libs.plugins.jooq)
}

dependencies {
    implementation(libs.orgSpringframeworkBoot.springBootStarterWeb)
    implementation(libs.orgSpringframeworkBoot.springBootStarterSecurity)
    implementation(libs.orgSpringframeworkBoot.springBootStarterMail)
    implementation(libs.orgSpringframeworkBoot.springBootStarterActuator)
    implementation(libs.orgSpringframeworkBoot.springBootStarterJooq)
    implementation(libs.comVaadin.vaadin)
    implementation(libs.comVaadin.vaadinSpringBootStarter)
    implementation(libs.comMysql.mysqlConnectorJ)
    implementation(libs.orgLiquibase.liquibaseCore)
    implementation(libs.ektorp)
    implementation(libs.acraJava)
    implementation(libs.orgJson)
    implementation(libs.commonsText)
    implementation(libs.xbibTime)
    implementation(libs.retrace)
    implementation(libs.fuzzywuzzy)
    implementation(libs.orgHibernateValidator.hibernateValidator)
    implementation(libs.ziplet)
    implementation(libs.univocityParser)
    implementation(libs.avatarGenerator)
    implementation(libs.apexCharts)
    implementation(libs.vaadin.paperMenuButton)
    implementation(libs.kotlin.coroutines)
    implementation(libs.comFasterxmlJacksonModule.jacksonModuleKotlin)
    implementation(libs.kotlin.logging)
    implementation(libs.springdoc)
    developmentOnly(libs.orgSpringframeworkBoot.springBootDevtools)
    testImplementation(libs.orgSpringframeworkBoot.springBootStarterTest)
    testImplementation(libs.orgSpringframeworkSecurity.springSecurityTest)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.strikt)
    testImplementation(libs.testContainers.mysql)
    testImplementation(libs.testContainers.junit)
    testImplementation(libs.karibu.testing)
    testImplementation(libs.karibu.spring)
    jooqGenerator(libs.comMysql.mysqlConnectorJ)
    jooqGenerator(libs.orgYaml.snakeyaml)
    jooqGenerator(projects.jooqHelper)
}

val messagesOutput = file("${layout.buildDirectory.get()}/generated/source/gradle/main")

sourceSets {
    main {
        java {
            srcDir(messagesOutput)
        }
    }
}

val generateMessageClasses by tasks.creating(com.faendir.acra.gradle.I18nClassGenerator::class) {
    inputDirectory = fileTree("src/main/resources/i18n/com/faendir/acra")
    outputDirectory = messagesOutput
    packageName = "com.faendir.acra.i18n"
    className = "Messages"
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateMessageClasses)
}

springBoot {
    mainClass.set("com.faendir.acra.BackendApplicationKt")
    buildInfo()
}

allOpen {
    annotation("com.vaadin.testbench.elementsbase.Element")
    annotation("org.acra.annotation.OpenAPI")
}

noArg {
    annotation("com.faendir.acra.util.NoArgConstructor")
}

tasks.withType<Test> {
    project.properties["vaadinProKey"]?.let { systemProperty("vaadin.proKey", it) }
    useJUnitPlatform()
}

vaadin {
    // productionMode = true
}

val changelogPath = "src/main/resources/db/db.changelog-master.yml"
jooq {
    version.set(libs.versions.jooq.get())
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
                    url = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "com.faendir.jooq.MySqlLiquibaseDatabase"
                        includes = ".*"
                        excludes = "DATABASECHANGELOG|DATABASECHANGELOGLOCK"
                        inputSchema = "acrarium"
                        properties = listOf(
                            Property().withKey("databaseName").withValue(inputSchema),
                            Property().withKey("scripts").withValue(changelogPath),
                        )
                        forcedTypes = listOf(
                            ForcedType().apply {
                                userType = "com.faendir.acra.persistence.app.AppId"
                                converter = "com.faendir.acra.persistence.app.AppIdConverter"
                                includeExpression = ".*\\.app_id|app.id"
                            },
                            ForcedType().apply {
                                userType = "com.faendir.acra.persistence.bug.BugId"
                                converter = "com.faendir.acra.persistence.bug.BugIdConverter"
                                includeExpression = ".*\\.bug_id|bug.id"
                            },
                            ForcedType().apply {
                                userType = "java.time.Instant"
                                converter = "com.faendir.acra.persistence.jooq.InstantConverter"
                                includeTypes = "DATETIME"
                            },
                            ForcedType().apply {
                                name = "BOOLEAN"
                                includeTypes = "(?i:TINYINT\\(1\\))"
                            }
                        )
                    }
                    target.apply {
                        packageName = "com.faendir.acra.jooq.generated"
                        directory = "${layout.buildDirectory.get()}/generated/source/jooq/main"
                    }
                    generate.apply {
                        isImmutableInterfaces = true
                        isKotlinNotNullInterfaceAttributes = true

                        isKotlinNotNullRecordAttributes = true
                    }
                }
            }
        }
    }
}

tasks.getByName<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    inputs.file("$projectDir/$changelogPath")
    allInputsDeclared.set(true)
}
