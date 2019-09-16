project {
    modelVersion '4.0.0'
    groupId 'com.faendir'
    artifactId 'message-generator-maven-plugin'
    version '1.0-SNAPSHOT'
    packaging 'maven-plugin'
    name 'message-generator-maven-plugin'

    dependencies {
        dependency 'org.apache.maven:maven-plugin-api:3.0'
        dependency 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.4:provided'
        dependency 'com.squareup:javapoet:1.11.1'
        dependency 'com.google.guava:guava:26.0-jre'
    }

    properties {
        'maven.compiler.source' '1.8'
        'maven.compiler.target' '1.8'
        'project.build.sourceEncoding' 'UTF-8'
        'project.reporting.outputEncoding' 'UTF-8'
    }

    build {
        plugins {
            plugin {
                groupId 'org.apache.maven.plugins'
                artifactId 'maven-plugin-plugin'
                version '3.6.0'
                executions {
                    execution {
                        id 'mojo-descriptor'
                        goals 'descriptor'
                    }
                }
            }
            plugin {
                groupId 'org.apache.maven.plugins'
                artifactId 'maven-javadoc-plugin'
                version '3.1.1'
                executions {
                    execution {
                        id 'attach-javadocs'
                        goals 'jar'
                        configuration {
                            additionalparam '-Xdoclint:none'
                        }
                    }
                }
            }
        }
    }
}
