package com.hcanyz.environmentvariable.compiler;

import com.google.auto.service.AutoService;
import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class EvAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }

        Map<Symbol, Set<Element>> groupMap = new HashMap<>();

        // group by file
        for (Element element : roundEnvironment.getElementsAnnotatedWith(EvItem.class)) {
            if (!(element instanceof Symbol.ClassSymbol)) {
                continue;
            }
            Symbol.ClassSymbol symbol = (Symbol.ClassSymbol) element;

            Symbol owner = symbol.owner;
            Set<Element> elements = groupMap.get(owner);
            if (elements == null) {
                elements = new HashSet<>();
                groupMap.put(owner, elements);
            }
            elements.add(element);
        }

        for (Map.Entry<Symbol, Set<Element>> entry : groupMap.entrySet()) {
            generateEvGroup(entry.getKey(), entry.getValue());
        }
        return true;
    }

    private void generateEvGroup(Symbol group, Set<Element> items) {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(group.name.toString() + "Manager")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        JavaFile javaFile = JavaFile.builder(group.packge().fullname.toString(), classBuilder.build()).build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EvGroup.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
