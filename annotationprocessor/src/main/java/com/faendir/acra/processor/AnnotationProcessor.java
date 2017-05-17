package com.faendir.acra.processor;

import com.faendir.acra.annotation.AutoDiscoverView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> views = roundEnv.getElementsAnnotatedWith(AutoDiscoverView.class);
        if (!views.isEmpty()) {
            try {
                JavaFile.builder("com.faendir.acra.gen", TypeSpec.classBuilder("ViewDefinition")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(MethodSpec.methodBuilder("getViewClasses")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class))))
                                .addCode("return $T.asList($L);", TypeName.get(Arrays.class), views.stream()
                                        .filter(element -> !element.getModifiers().contains(Modifier.ABSTRACT))
                                        .map(element -> ((TypeElement) element).getQualifiedName().toString() + ".class")
                                        .reduce((s1, s2) -> s1 + ", " + s2).orElse(""))
                                .build())
                        .build())
                        .skipJavaLangImports(true)
                        .indent("    ")
                        .build()
                        .writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate classes");
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoDiscoverView.class.getName());
    }
}
