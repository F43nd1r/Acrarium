plugins {
    kotlin
    org.jetbrains.kotlin.kapt
    org.jetbrains.kotlin.plugin.allopen
    org.jetbrains.kotlin.plugin.spring
    org.jetbrains.kotlin.plugin.noarg
    org.jetbrains.kotlin.plugin.jpa
    org.springframework.boot
    com.vaadin
    war
    com.palantir.docker
}

val base: Configuration by configurations.creating
configurations {
    implementation { extendsFrom(base) }
    getByName("kapt") { extendsFrom(base) }
    developmentOnly { extendsFrom(base) }
}

dependencies {
    libs.bundles.bom.get().map { base(platform(it)) }
    implementation(libs.orgSpringframeworkBoot.springBootStarterSecurity)
    implementation(libs.orgSpringframeworkBoot.springBootStarterMail)
    implementation(libs.orgSpringframeworkBoot.springBootStarterActuator)
    implementation(libs.orgSpringframeworkBoot.springBootStarterDataJpa)
    kapt(libs.orgSpringframeworkBoot.springBootConfigurationProcessor)
    implementation(libs.comVaadin.vaadin)
    implementation(libs.comVaadin.vaadinSpringBootStarter)
    implementation(libs.mysql.mysqlConnectorJava)
    implementation(libs.orgLiquibase.liquibaseCore)
    implementation(libs.liquibaseKotlinDsl)
    implementation(libs.comQuerydsl.querydslJpa)
    kapt(libs.comQuerydsl.querydslApt) { artifact { classifier = "jpa" } }
    kapt(libs.comQuerydsl.querydslKotlinCodegen)
    implementation(libs.ektorp)
    implementation(libs.acraJava)
    implementation(libs.orgJson)
    implementation(libs.commonsText)
    implementation(libs.xbibTime)
    implementation(libs.retrace)
    implementation(libs.fuzzywuzzy)
    implementation(libs.javaxServlet.javaxServletApi)
    implementation(libs.orgHibernateValidator.hibernateValidator)
    implementation(libs.ziplet)
    implementation(libs.univocityParser)
    implementation(libs.avatarGenerator)
    implementation(libs.apexCharts)
    implementation(libs.gridLayout)
    implementation(libs.vaadin.paperMenuButton)
    implementation(libs.kotlin.coroutines)
    implementation(libs.comFasterxmlJacksonModule.jacksonModuleKotlin)
    implementation(libs.kotlin.logging)
    implementation(libs.autoService.annotations)
    kapt(libs.autoService.processor)
    developmentOnly(libs.orgSpringframeworkBoot.springBootDevtools)
    testImplementation(libs.orgSpringframeworkBoot.springBootStarterTest)
    testImplementation(libs.orgSpringframeworkSecurity.springSecurityTest)
    testImplementation(libs.comVaadin.vaadinTestbench)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.strikt)
    testImplementation(libs.testContainers.junit)
    testImplementation(libs.testContainers.selenium)
    testImplementation(libs.selenium.remote)
    testImplementation(libs.selenium.chrome)
    testImplementation(libs.selenium.firefox)
    testImplementation(libs.mariadb4j)
    testImplementation(libs.mariadbClient)
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
    dependsOn(generateMessageClasses)
}

springBoot {
    mainClass.set("com.faendir.acra.BackendApplicationKt")
    buildInfo()
}

allOpen {
    annotation("com.vaadin.testbench.elementsbase.Element")
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
