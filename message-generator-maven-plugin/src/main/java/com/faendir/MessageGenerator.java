package com.faendir;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MessageGenerator extends AbstractMojo {
    @Parameter(property = "inputDirectory", defaultValue = "src/main/resources")
    private File inputDirectory;
    @Parameter(property = "outputDirectory", defaultValue = "target/generated-sources/java")
    private File outputDirectory;
    @Parameter(property = "packageName", defaultValue = "com.faendir.i18n")
    private String packageName;
    @Parameter(property = "className", defaultValue = "Messages")
    private String className;

    public void execute() throws MojoExecutionException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        Set<String> keys = new HashSet<>();
        try {
            Files.walk(inputDirectory.toPath())
                    .peek(file -> System.out.println("Walking " + file))
                    .filter(file -> file.toString().endsWith(".properties"))
                    .peek(file -> System.out.println("Found " + file))
                    .forEach(file -> {
                        try {
                            Properties properties = new Properties();
                            properties.load(new FileReader(file.toFile()));
                            keys.addAll(Collections.list(properties.keys()).stream().map(Object::toString).collect(Collectors.toList()));
                        } catch (Exception ignored) {
                        }
                    });
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            for (String key : keys) {
                classBuilder.addField(FieldSpec.builder(String.class, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", key)
                        .build());
            }
            JavaFile.builder(packageName, classBuilder.build())
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build()
                    .writeTo(outputDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
