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
    listOf(libs.spring.boot.bom, libs.vaadin.base.bom).forEach {
        implementation(it)
        testImplementation(it)
    }
    implementation(springLibs.spring.springBootStarterWeb)
    implementation(springLibs.spring.springBootStarterSecurity)
    implementation(springLibs.spring.springBootStarterMail)
    implementation(springLibs.spring.springBootStarterActuator)
    implementation(springLibs.spring.springBootStarterJooq)
    implementation(vaadinLibs.vaadin.vaadin)
    implementation(vaadinLibs.vaadin.vaadinSpringBootStarter)
    implementation(springLibs.mysql.mysqlConnectorJ)
    implementation(springLibs.liquibase.liquibaseCore)
    implementation(libs.ektorp)
    implementation(libs.acraJava)
    implementation(libs.orgJson)
    implementation(libs.commonsText)
    implementation(libs.xbibTime)
    implementation(libs.retrace)
    implementation(libs.fuzzywuzzy)
    implementation(springLibs.hibernate.hibernateValidator)
    implementation(libs.ziplet)
    implementation(libs.univocityParser)
    implementation(libs.avatarGenerator)
    implementation(libs.apexCharts)
    implementation(libs.vaadin.paperMenuButton)
    implementation(libs.kotlin.coroutines)
    implementation(springLibs.jackson.jacksonModuleKotlin)
    implementation(libs.kotlin.logging)
    implementation(libs.springdoc)
    implementation(springLibs.aspectj.aspectjweaver)
    developmentOnly(springLibs.spring.springBootDevtools)
    testImplementation(springLibs.spring.springBootStarterTest)
    testImplementation(springLibs.spring.springSecurityTest)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.strikt)
    testImplementation(libs.testContainers.mysql)
    testImplementation(libs.testContainers.junit)
    testImplementation(libs.karibu.testing)
    testImplementation(libs.karibu.spring)
    jooqGenerator(springLibs.mysql.mysqlConnectorJ)
    jooqGenerator(springLibs.yaml.snakeyaml)
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
    jvmToolchain(21)
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
    useJUnitPlatform()
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
                        forcedTypes = listOf(ForcedType().apply {
                            userType = "com.faendir.acra.persistence.app.AppId"
                            converter = "com.faendir.acra.persistence.app.AppIdConverter"
                            includeExpression = ".*\\.app_id|app.id"
                        }, ForcedType().apply {
                            userType = "com.faendir.acra.persistence.bug.BugId"
                            converter = "com.faendir.acra.persistence.bug.BugIdConverter"
                            includeExpression = ".*\\.bug_id|bug.id"
                        }, ForcedType().apply {
                            userType = "java.time.Instant"
                            converter = "com.faendir.acra.persistence.jooq.InstantConverter"
                            includeTypes = "DATETIME"
                        }, ForcedType().apply {
                            name = "BOOLEAN"
                            includeTypes = "(?i:TINYINT)"
                        })
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
