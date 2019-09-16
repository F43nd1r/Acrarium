/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
project {
    modelVersion '4.0.0'
    groupId 'com.faendir'
    artifactId 'acrarium'
    version '0.9.0-SNAPSHOT'
    name 'Acrarium'
    packaging 'war'

    properties {
        'maven.compiler.source' '1.8'
        'maven.compiler.target' '1.8'
        'project.build.sourceEncoding' 'UTF-8'
        'project.reporting.outputEncoding' 'UTF-8'
        'drivers.dir' '${project.basedir}/drivers'
        'drivers.downloader.phase' 'pre-integration-test'
        'vaadin.version' '14.0.4'
        'querydsl.version' '4.2.1'
    }

    parent {
        groupId 'org.springframework.boot'
        artifactId 'spring-boot-starter-parent'
        version '2.1.8.RELEASE'
    }

    pluginRepositories {
        pluginRepository {
            id 'central'
            url 'https://repo1.maven.org/maven2/'
            snapshots {
                enabled false
            }
        }
    }

    repositories {
        repository {
            id 'central'
            url 'https://repo1.maven.org/maven2/'
            snapshots {
                enabled false
            }
        }
        repository {
            id 'Vaadin Directory'
            url 'https://maven.vaadin.com/vaadin-addons'
            snapshots {
                enabled false
            }
        }
        repository {
            id 'Google'
            url 'https://maven.google.com'
            snapshots {
                enabled false
            }
        }
        repository {
            id 'jcenter'
            url 'https://jcenter.bintray.com'
            snapshots {
                enabled false
            }
        }
        /*repository {
            id 'maven.oracle.com'
            url 'https://maven.oracle.com'
            layout 'default'
            snapshots {
                enabled false
            }
        }*/
    }

    dependencyManagement {
        dependencies {
            dependency {
                groupId 'com.vaadin'
                artifactId 'vaadin-bom'
                version '${vaadin.version}'
                type 'pom'
                scope 'import'
            }
        }
    }

    dependencies {
        dependency {
            groupId 'com.vaadin'
            artifactId 'vaadin'
            exclusions {
                exclusion {
                    artifactId '*'
                    groupId 'com.vaadin.webjar'
                }
                exclusion {
                    artifactId '*'
                    groupId 'org.webjars.bowergithub.insites'
                }
                exclusion {
                    artifactId '*'
                    groupId 'org.webjars.bowergithub.polymer'
                }
                exclusion {
                    artifactId '*'
                    groupId 'org.webjars.bowergithub.polymerelements'
                }
                exclusion {
                    artifactId '*'
                    groupId 'org.webjars.bowergithub.vaadin'
                }
                exclusion {
                    artifactId '*'
                    groupId 'org.webjars.bowergithub.webcomponents'
                }
            }
        }
        dependency {
            groupId 'com.vaadin'
            artifactId 'vaadin-spring-boot-starter'
            exclusions {
                exclusion {
                    artifactId 'vaadin-core'
                    groupId 'com.vaadin'
                }
            }
        }
        dependency 'org.springframework.boot:spring-boot-starter-data-jpa'
        dependency 'mysql:mysql-connector-java'
        dependency 'org.springframework.boot:spring-boot-starter-security'
        dependency 'org.springframework.boot:spring-boot-starter-mail'
        dependency 'org.liquibase:liquibase-core'
        dependency 'org.yaml:snakeyaml'
        dependency 'com.querydsl:querydsl-jpa:${querydsl.version}'
        dependency 'com.querydsl:querydsl-sql:${querydsl.version}'
        dependency 'com.querydsl:querydsl-apt:${querydsl.version}:provided'
        dependency 'org.jfree:jfreechart:1.5.0'
        dependency 'org.apache.xmlgraphics:batik-svggen:1.10'
        dependency 'ch.acra:acra-javacore:5.3.0'
        dependency 'com.faendir.vaadin:jfreechart-flow:1.1.6'
        dependency 'org.codeartisans:org.json:20161124'
        dependency 'org.apache.commons:commons-text:1.6'
        dependency 'commons-io:commons-io:2.5'
        dependency 'org.xbib:time:1.0.0'
        dependency 'com.faendir.proguard:retrace:1.3'
        dependency 'javax.xml.bind:jaxb-api:2.3.1'
        dependency 'com.github.ziplet:ziplet:2.3.0'
        dependency 'me.xdrop:fuzzywuzzy:1.2.0'
        dependency 'com.talanlabs:avatar-generator:1.1.0'
        dependency 'org.ektorp:org.ektorp.spring:1.5.0'
        dependency 'com.github.appreciated:apexcharts:2.0.0.beta4'
        dependency 'javax.servlet:javax.servlet-api:4.0.1'
        dependency {
            groupId 'org.springframework.boot'
            artifactId 'spring-boot-starter-test'
            scope 'test'
        }
        dependency {
            groupId 'org.springframework.security'
            artifactId 'spring-security-test'
            scope 'test'
        }
        dependency {
            groupId 'com.h2database'
            artifactId 'h2'
            scope 'test'
        }
        /*dependency {
            groupId 'com.oracle.weblogic'
            artifactId 'ojdbc7'
            version '12.1.3-0-0'
            scope 'test'
        }*/
    }

    build {
        defaultGoal 'spring-boot:run'
        plugins {
            plugin {
                groupId 'org.springframework.boot'
                artifactId 'spring-boot-maven-plugin'
            }
            plugin {
                groupId 'com.vaadin'
                artifactId 'vaadin-maven-plugin'
                version '${vaadin.version}'
                executions {
                    execution {
                        goals {
                            goal 'prepare-frontend'
                        }
                    }
                }
            }
            plugin {
                groupId 'com.mysema.maven'
                artifactId 'apt-maven-plugin'
                version '1.1.3'
                executions {
                    execution {
                        goals 'process'
                        configuration {
                            outputDirectory '${project.build.directory}/generated-sources/java'
                            processor 'com.querydsl.apt.jpa.JPAAnnotationProcessor'
                        }
                    }
                }
            }
            plugin {
                groupId 'org.codehaus.mojo'
                artifactId 'build-helper-maven-plugin'
                executions {
                    execution {
                        id 'add-source'
                        phase 'generate-sources'
                        goals 'add-source'
                        configuration {
                            sources {
                                source '${project.build.directory}/generated-sources/java'
                            }
                        }
                    }
                }
            }
            plugin {
                groupId 'com.faendir'
                artifactId 'message-generator-maven-plugin'
                version '1.0-SNAPSHOT'
                executions {
                    execution {
                        goals 'generate'
                    }
                }
                configuration {
                    inputDirectory 'src/main/resources/i18n'
                    packageName 'com.faendir.acra.i18n'
                }
            }
        }
    }

    profiles {
        profile {
            id 'production'
            build {
                plugins {
                    plugin {
                        groupId 'org.springframework.boot'
                        artifactId 'spring-boot-maven-plugin'
                        configuration {
                            jvmArguments '-Dvaadin.productionMode'
                        }
                    }
                    plugin {
                        groupId 'com.vaadin'
                        artifactId 'vaadin-maven-plugin'
                        executions {
                            execution {
                                phase 'compile'
                                goals {
                                    goal 'build-frontend'
                                }
                            }
                        }
                    }
                }
            }
            properties {
                'vaadin.productionMode' 'true'
            }
            dependencies {
                dependency {
                    groupId 'com.vaadin'
                    artifactId 'flow-server-production-mode'
                }
            }
        }
    }
}