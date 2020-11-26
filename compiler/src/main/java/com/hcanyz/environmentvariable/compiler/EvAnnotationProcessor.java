package com.hcanyz.environmentvariable.compiler;

import com.google.auto.service.AutoService;
import com.hcanyz.environmentvariable.base.ConstantKt;
import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.hcanyz.environmentvariable.base.annotations.EvVariant;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class EvAnnotationProcessor extends AbstractProcessor {

    private static final String TAG = EvAnnotationProcessor.class.getSimpleName();
    private static final String LIB_PACKAGE_NAME = "com.hcanyz.environmentvariable";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }

        // group by ev config file
        Map<Symbol, Set<Symbol.ClassSymbol>> groupMap = new LinkedHashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(EvItem.class)) {
            if (!(element instanceof Symbol.ClassSymbol)) {
                continue;
            }
            Symbol.ClassSymbol symbol = (Symbol.ClassSymbol) element;

            Symbol owner = symbol.owner;
            Set<Symbol.ClassSymbol> elements = groupMap.get(owner);
            if (elements == null) {
                elements = new HashSet<>();
                groupMap.put(owner, elements);
            }
            elements.add(symbol);
        }

        // collect info
        List<EvGroupInfo> evGroupInfos = new ArrayList<>();
        for (Map.Entry<Symbol, Set<Symbol.ClassSymbol>> entry : groupMap.entrySet()) {
            evGroupInfos.add(collectEvGroupInfo(entry.getKey(), entry.getValue()));
        }

        // generate file
        for (EvGroupInfo evGroupInfo : evGroupInfos) {
            generateEvFile(evGroupInfo);
        }
        return true;
    }

    private void generateEvFile(EvGroupInfo evGroupInfo) {
        String evGroupName = evGroupInfo.name + "Manager";
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(evGroupName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(LIB_PACKAGE_NAME, "IEvManager"));

        // private constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

        // singleton instance
        classBuilder.addType(TypeSpec.classBuilder("Inner")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addField(FieldSpec.builder(ClassName.get(evGroupInfo.packageName, evGroupName),
                        "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $L()", evGroupName).build())
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("getSingleton")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(evGroupInfo.packageName, evGroupName))
                .addCode("return Inner.instance;")
                .build());

        // static constant group name
        classBuilder.addField(FieldSpec.builder(String.class, "EV_GROUP_NAME",
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", evGroupInfo.name)
                .build());

        // static constant
        for (EvGroupInfo.EvItemInfo evItemInfo : evGroupInfo.evItemInfos) {
            String itemNameUpperCase = evItemInfo.name.toUpperCase();
            classBuilder.addField(FieldSpec.builder(String.class, "EV_ITEM_" + itemNameUpperCase,
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", evItemInfo.name)
                    .build());

            for (EvGroupInfo.EvVariantInfo evVariantInfo : evItemInfo.evVariantInfos) {
                if (evVariantInfo.name.equals(ConstantKt.EV_VARIANT_PRESET_CUSTOMIZE)) {
                    continue;
                }
                String variantNameUpperCase = evVariantInfo.name.toUpperCase();
                classBuilder.addField(FieldSpec.builder(String.class, "EV_VARIANT_" + itemNameUpperCase + "_" + variantNameUpperCase,
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", evVariantInfo.name)
                        .build());
            }
        }

        // fullVariants
        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Set.class, String.class), "fullVariants",
                Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(HashSet.class, String.class))
                .build());

        // variantValueMap
        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "variantValueMap",
                Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("map-key: \"$$evItemName.$$variant\"")
                .initializer("new $T()", ParameterizedTypeName.get(LinkedHashMap.class, String.class, String.class))
                .build());

        // currentVariantMap
        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "currentVariantMap",
                Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("map-key: \"$$evItemName\"")
                .initializer("new $T()", ParameterizedTypeName.get(LinkedHashMap.class, String.class, String.class))
                .build());

        // EvHandlers
        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(LIB_PACKAGE_NAME, "EvHandler")), "EvHandlers",
                Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get(LIB_PACKAGE_NAME, "EvHandler")))
                .build());

        // fullVariantStrSet
        Set<String> fullVariantStrSet = new LinkedHashSet<>();

        // collect variantValueMap CodeBlocks
        List<CodeBlock> fullVariantsCodeBlocks = new ArrayList<>();
        List<CodeBlock> variantValueMapCodeBlocks = new ArrayList<>();

        for (EvGroupInfo.EvItemInfo evItemInfo : evGroupInfo.evItemInfos) {
            String itemNameUpperCase = evItemInfo.name.toUpperCase();
            String defaultValue = "";
            String defaultValueFrom = "";
            String customizeValue = "";
            for (EvGroupInfo.EvVariantInfo evVariantInfo : evItemInfo.evVariantInfos) {
                if (evVariantInfo.name.equals(ConstantKt.EV_VARIANT_PRESET_CUSTOMIZE)) {
                    customizeValue = evVariantInfo.value;
                    continue;
                }
                fullVariantStrSet.add(evVariantInfo.name);

                String evVariantNameUpperCase = evVariantInfo.name.toUpperCase();
                // find every item default value
                // The closer variant are, the more priority
                if (evVariantInfo.isDefault) {
                    defaultValue = evVariantInfo.value;
                    defaultValueFrom = evVariantInfo.name + " isDefault";
                } else if (evItemInfo.defaultVariant != null && !evItemInfo.defaultVariant.isEmpty()) {
                    if (evItemInfo.defaultVariant.equals(evVariantInfo.name)) {
                        defaultValue = evVariantInfo.value;
                        defaultValueFrom = evItemInfo.name + " annotation";
                    }
                } else if (evGroupInfo.defaultVariant != null && !evGroupInfo.defaultVariant.isEmpty()) {
                    if (evGroupInfo.defaultVariant.equals(evVariantInfo.name)) {
                        defaultValue = evVariantInfo.value;
                        defaultValueFrom = evGroupInfo.name + " annotation";
                    }
                } else {
                    printMessage(Diagnostic.Kind.ERROR, String.format("%s - evGroup defaultVariant invalid,   group:%s,item:%s,variant:%s", TAG, evGroupInfo.name, evItemInfo.name, evVariantInfo.name));
                }
                variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_$L, EV_VARIANT_$L_$L), \"$L\");", "variantValueMap", itemNameUpperCase, itemNameUpperCase, evVariantNameUpperCase, evVariantInfo.value).build());
            }

            printMessage(Diagnostic.Kind.NOTE, String.format("%s - defaultValue:%s from:%s,  group:%s,item:%s", TAG, defaultValue, defaultValueFrom, evGroupInfo.name, evItemInfo.name));

            variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_$L, EV_VARIANT_PRESET_DEFAULT), \"$L\");", "variantValueMap", itemNameUpperCase, defaultValue).build());
            variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_$L, EV_VARIANT_PRESET_CUSTOMIZE), \"$L\");", "variantValueMap", itemNameUpperCase, customizeValue).build());
            variantValueMapCodeBlocks.add(CodeBlock.builder().add("\n").build());
        }

        // collect fullVariants CodeBlocks
        fullVariantsCodeBlocks.add(CodeBlock.builder().add("$L.add(EV_VARIANT_PRESET_DEFAULT);", "fullVariants").build());
        for (String variant : fullVariantStrSet) {
            fullVariantsCodeBlocks.add(CodeBlock.builder().add("$L.add(\"$L\");", "fullVariants", variant).build());
        }

        List<CodeBlock> all = new ArrayList<>(fullVariantsCodeBlocks);
        all.add(CodeBlock.builder().add("\n").build());
        all.addAll(variantValueMapCodeBlocks);

        // block init default
        classBuilder.addInitializerBlock(CodeBlock.join(all, "\n"));

        // method getEvItemCurrentValue
        classBuilder.addMethod(MethodSpec.methodBuilder("getEvItemCurrentValue")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(String.class, "evItemName")
                        .addAnnotation(ClassName.get("androidx.annotation", "NonNull"))
                        .build())
                .addCode(CodeBlock.builder().add("return $L.get(EvHandler.Companion.joinVariantValueKey(evItemName, $L.get(evItemName)));", "variantValueMap", "currentVariantMap").build())
                .returns(String.class)
                .build());

        // method getFullVariants
        classBuilder.addMethod(MethodSpec.methodBuilder("getFullVariants")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder().add("return $L;", "fullVariants").build())
                .returns(ParameterizedTypeName.get(Set.class, String.class))
                .build());

        // method getEvHandlers
        List<CodeBlock> EvHandlersCodeBlocks = new ArrayList<>();
        EvHandlersCodeBlocks.add(CodeBlock.builder().add("if (EvHandlers.isEmpty()) {").build());
        for (EvGroupInfo.EvItemInfo evItemInfo : evGroupInfo.evItemInfos) {
            EvHandlersCodeBlocks.add(CodeBlock.builder().add("  EvHandlers.add(new EvHandler(context, EV_ITEM_$L, currentVariantMap, variantValueMap));", evItemInfo.name.toUpperCase())
                    .build());
        }
        EvHandlersCodeBlocks.add(CodeBlock.builder().add("}").build());
        EvHandlersCodeBlocks.add(CodeBlock.builder().add("return EvHandlers;").build());
        classBuilder.addMethod(MethodSpec.methodBuilder("getEvHandlers")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(ClassName.get("android.content", "Context"), "context")
                        .addAnnotation(ClassName.get("androidx.annotation", "NonNull"))
                        .build())
                .addCode(CodeBlock.join(EvHandlersCodeBlocks, "\n"))
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(LIB_PACKAGE_NAME, "EvHandler")))
                .build());

        // final file
        JavaFile javaFile = JavaFile.builder(evGroupInfo.packageName, classBuilder.build())
                .addStaticImport(ConstantKt.class, "EV_VARIANT_PRESET_CUSTOMIZE", "EV_VARIANT_PRESET_DEFAULT")
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EvGroupInfo collectEvGroupInfo(Symbol group, Set<Symbol.ClassSymbol> items) {
        EvGroupInfo evGroupInfo = new EvGroupInfo(group.name.toString(), group.packge().fullname.toString());

        EvGroup evGroupAnnotation = group.getAnnotation(EvGroup.class);
        evGroupInfo.defaultVariant = evGroupAnnotation.defaultVariant();

        for (Symbol.ClassSymbol item : items) {
            String itemName = item.name.toString();
            EvGroupInfo.EvItemInfo evItemInfo = new EvGroupInfo.EvItemInfo(itemName);
            evGroupInfo.evItemInfos.add(evItemInfo);

            EvItem evItemAnnotation = item.getAnnotation(EvItem.class);
            evItemInfo.defaultVariant = evItemAnnotation.defaultVariant();

            List<? extends Element> variants = processingEnv.getElementUtils().getAllMembers(item);
            for (Element variantTmp : variants) {
                if (!variantTmp.getKind().equals(ElementKind.FIELD)) {
                    continue;
                }
                if (!variantTmp.getModifiers().contains(Modifier.FINAL)) {
                    printMessage(Diagnostic.Kind.WARNING, String.format("%s - not FINAL ignored,  group:%s,item:%s,variant:%s", TAG, evGroupInfo.name, itemName, variantTmp.getSimpleName()));
                    continue;
                }
                VariableElement variant = (VariableElement) variantTmp;
                String variantName = variant.getSimpleName().toString();
                String variantValue = variant.getConstantValue().toString();
                EvVariant variantAnnotation = variant.getAnnotation(EvVariant.class);

                evItemInfo.evVariantInfos.add(new EvGroupInfo.EvVariantInfo(variantName, variantValue, variantAnnotation.desc(), variantAnnotation.isDefault()));
            }
        }
        return evGroupInfo;
    }

    private void printMessage(Diagnostic.Kind kind, CharSequence charSequence) {
        processingEnv.getMessager().printMessage(kind, charSequence + "\r\n");
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
