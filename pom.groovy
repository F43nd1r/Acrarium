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
    packaging 'pom'
    groupId 'com.faendir'
    artifactId 'parent'
    version '0.9.0-SNAPSHOT'
    scm 'scm:git:ssh://github.com:F43nd1r/Acrarium.git'

    modules {
        module 'message-generator-maven-plugin'
        module 'acrarium'
    }

    build {
        plugins {
            plugin {
                artifactId 'maven-release-plugin'
                version '2.5.3'
                configuration {
                    autoVersionSubModules true
                    tagNameFormat 'v@{project.version}'
                    releaseProfile 'release'
                }
            }
        }
    }
}