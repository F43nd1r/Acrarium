/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.gradle

import com.google.common.base.CaseFormat
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.plugins.ide.idea.IdeaPlugin

import static javax.lang.model.element.Modifier.*
/**
 * @author lukas
 * @since 15.08.18
 */
@CacheableTask
class I18nClassGenerator extends DefaultTask {
    @SkipWhenEmpty
    @InputDirectory
    File inputDirectory
    @OutputDirectory
    File outputDirectory
    String packageName
    String className

    I18nClassGenerator() {
        project.afterEvaluate {
            project.sourceSets.main.java.srcDirs outputs.files
            project.plugins.withType(IdeaPlugin.class, { IdeaPlugin idea -> idea.model.module.generatedSourceDirs += outputs.files })
        }
    }

    @TaskAction
    void exec() {
        Set<String> keys = new HashSet<>()
        for (File file : project.fileTree(inputDirectory)) {
            try {
                Properties properties = new Properties()
                properties.load(file.newReader())
                keys.addAll(properties.keys() as Collection<String>)
            } catch (ignored){
            }
        }
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL)
        for (String key : keys) {
            classBuilder.addField(FieldSpec.builder(String, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key), PUBLIC, STATIC, FINAL).initializer('$S', key).build())
        }
        JavaFile.builder(packageName, classBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build()
                .writeTo(outputDirectory)
    }

}
