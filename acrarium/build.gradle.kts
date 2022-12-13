import com.vaadin.gradle.vaadin
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Property

plugins {
    java
    org.jetbrains.kotlin.jvm
    org.jetbrains.kotlin.plugin.allopen
    org.jetbrains.kotlin.plugin.spring
    org.jetbrains.kotlin.plugin.noarg
    org.jetbrains.kotlin.plugin.jpa
    org.springframework.boot
    com.vaadin
    war
    com.palantir.docker
    id("nu.studer.jooq") version "8.0"
}

repositories {
    mavenCentral()
    google()
    maven { setUrl("https://maven.vaadin.com/vaadin-addons") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { setUrl("https://repo.spring.io/milestone") }
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
    maven { setUrl("https://repository.apache.org/content/repositories/snapshots") }
    mavenLocal()
}

dependencies {
    implementation(libs.orgSpringframeworkBoot.springBootStarterWeb)
    implementation(libs.orgSpringframeworkBoot.springBootStarterSecurity)
    implementation(libs.orgSpringframeworkBoot.springBootStarterMail)
    implementation(libs.orgSpringframeworkBoot.springBootStarterActuator)
    implementation(libs.orgSpringframeworkBoot.springBootStarterJooq)
    implementation(libs.comVaadin.vaadin)
    implementation(libs.comVaadin.vaadinSpringBootStarter)
    implementation(libs.mysql.mysqlConnectorJava)
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
    testImplementation(libs.mariadb4j)
    testImplementation(libs.mariadbClient)
    jooqGenerator(libs.mysql.mysqlConnectorJava)
    jooqGenerator(libs.orgYaml.snakeyaml)
    jooqGenerator(projects.jooqHelper)
}

val messagesOutput = file("$buildDir/generated/source/gradle/main")

sourceSets {
    main {
        java {
            srcDir(messagesOutput)
        }
    }
}

val generateMessageClasses by tasks.creating(com.faendir.acra.gradle.I18nClassGenerator::class) {
    inputDirectory = file("src/main/resources/i18n/com/faendir/acra")
    outputDirectory = messagesOutput
    packageName = "com.faendir.acra.i18n"
    className = "Messages"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
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

docker {
    name = "f43nd1r/acrarium"
    tag("hubLatest", "$name:latest")
    tag("hubVersion", "$name:$version")
    tag("ghLatest", "ghcr.io/$name:latest")
    tag("ghVersion", "ghcr.io/$name:$version")
    files(tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

tasks.withType<Test> {
    project.properties["vaadinProKey"]?.let { systemProperty("vaadin.proKey", it) }
    useJUnitPlatform()
}

vaadin {
    productionMode = true
}

val changelogPath = "src/main/resources/db/db.changelog-master.yml"
jooq {
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
                    //name = "org.jooq.codegen.KotlinGenerator" TODO enable this once nonnull pojos are supported in kotlin
                    database.apply {
                        name = "com.faendir.jooq.MySqlLiquibaseDatabase"
                        includes = ".*"
                        excludes = "DATABASECHANGELOG|DATABASECHANGELOGLOCK"
                        inputSchema = "acrarium"
                        properties = listOf(
                            Property().withKey("databaseName").withValue(inputSchema),
                            Property().withKey("scripts").withValue(changelogPath),
                        )
                        forcedTypes.apply {
                            add(ForcedType().apply {
                                userType = "com.faendir.acra.persistence.app.AppId"
                                converter = "com.faendir.acra.persistence.app.AppIdConverter"
                                includeExpression = ".*\\.app_id|app.id"
                            })
                            add(ForcedType().apply {
                                userType = "com.faendir.acra.persistence.bug.BugId"
                                converter = "com.faendir.acra.persistence.bug.BugIdConverter"
                                includeExpression = ".*\\.bug_id|bug.id"
                            })
                            add(ForcedType().apply {
                                userType = "java.time.Instant"
                                converter = "com.faendir.acra.persistence.jooq.InstantConverter"
                                includeTypes = "DATETIME"
                            })
                        }
                    }
                    target.apply {
                        packageName = "com.faendir.acra.jooq.generated"
                        directory = "$buildDir/generated/source/jooq/main"
                    }
                    generate.apply {
                        isSpringAnnotations = true
                        isNonnullAnnotation = true
                        isNullableAnnotation = true
                        nonnullAnnotationType = "org.springframework.lang.NonNull"
                        nullableAnnotationType = "org.springframework.lang.Nullable"
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
