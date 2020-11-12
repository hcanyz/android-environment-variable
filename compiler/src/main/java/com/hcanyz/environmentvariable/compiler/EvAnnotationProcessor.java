package com.hcanyz.environmentvariable.compiler;

import com.google.auto.service.AutoService;
import com.hcanyz.environmentvariable.base.ConstantKt;
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
import java.util.HashMap;
import java.util.HashSet;
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

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }

        // group by ev config file
        Map<Symbol, Set<Symbol.ClassSymbol>> groupMap = new HashMap<>();
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
        List<EvGroup> evGroups = new ArrayList<>();
        for (Map.Entry<Symbol, Set<Symbol.ClassSymbol>> entry : groupMap.entrySet()) {
            evGroups.add(collectEvGroup(entry.getKey(), entry.getValue()));
        }

        // generate file
        for (EvGroup evGroup : evGroups) {
            generateEvFile(evGroup);
        }
        return true;
    }

    private void generateEvFile(EvGroup evGroup) {
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(evGroup.name + "Manager")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // static constant
        for (EvGroup.EvItem evItem : evGroup.evItems) {
            String itemNameUpperCase = evItem.name.toUpperCase();
            classBuilder.addField(FieldSpec.builder(String.class, "KEY_" + itemNameUpperCase,
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", evItem.name)
                    .build());

            for (EvGroup.EvVariant evVariant : evItem.evVariants) {
                if (evVariant.name.equals(ConstantKt.VARIANT_PRESET_CUSTOMIZE)) {
                    continue;
                }
                String variantNameUpperCase = evVariant.name.toUpperCase();
                classBuilder.addField(FieldSpec.builder(String.class, "VARIANT_" + itemNameUpperCase + "_" + variantNameUpperCase,
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", evVariant.name)
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
                .addJavadoc("map-key: \"$$key.$$variant\"")
                .initializer("new $T()", ParameterizedTypeName.get(HashMap.class, String.class, String.class))
                .build());

        // currentVariantMap
        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, String.class), "currentVariantMap",
                Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("map-key: \"$$key\"")
                .initializer("new $T()", ParameterizedTypeName.get(HashMap.class, String.class, String.class))
                .build());

        // fullVariantStrSet
        Set<String> fullVariantStrSet = new LinkedHashSet<>();

        // collect variantValueMap CodeBlocks
        List<CodeBlock> fullVariantsCodeBlocks = new ArrayList<>();
        List<CodeBlock> variantValueMapCodeBlocks = new ArrayList<>();

        for (EvGroup.EvItem evItem : evGroup.evItems) {
            String itemNameUpperCase = evItem.name.toUpperCase();
            String defaultValue = "";
            String defaultValueFrom = "";
            String customizeValue = "";
            for (EvGroup.EvVariant evVariant : evItem.evVariants) {
                if (evVariant.name.equals(ConstantKt.VARIANT_PRESET_CUSTOMIZE)) {
                    customizeValue = evVariant.value;
                    continue;
                }
                fullVariantStrSet.add(evVariant.name);

                String evVariantNameUpperCase = evVariant.name.toUpperCase();
                // find every item default value
                // The closer variant are, the more priority
                if (evVariant.isDefault) {
                    defaultValue = evVariant.value;
                    defaultValueFrom = evVariant.name + " isDefault";
                } else if (evItem.defaultVariant != null && !evItem.defaultVariant.isEmpty()) {
                    if (evItem.defaultVariant.equals(evVariant.name)) {
                        defaultValue = evVariant.value;
                        defaultValueFrom = evItem.name + " annotation";
                    }
                } else if (evGroup.defaultVariant != null && !evGroup.defaultVariant.isEmpty()) {
                    if (evGroup.defaultVariant.equals(evVariant.name)) {
                        defaultValue = evVariant.value;
                        defaultValueFrom = evGroup.name + " annotation";
                    }
                } else {
                    printMessage(Diagnostic.Kind.ERROR, String.format("%s - evGroup defaultVariant invalid,   group:%s,item:%s,variant:%s", TAG, evGroup.name, evItem.name, evVariant.name));
                }
                variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHolder.Companion.joinVariantValueKey(KEY_$L, VARIANT_$L_$L), \"$L\");", "variantValueMap", itemNameUpperCase, itemNameUpperCase, evVariantNameUpperCase, evVariant.value).build());
            }

            printMessage(Diagnostic.Kind.NOTE, String.format("%s - defaultValue:%s from:%s,  group:%s,item:%s", TAG, defaultValue, defaultValueFrom, evGroup.name, evItem.name));

            variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHolder.Companion.joinVariantValueKey(KEY_$L, VARIANT_PRESET_DEFAULT), \"$L\");", "variantValueMap", itemNameUpperCase, defaultValue).build());
            variantValueMapCodeBlocks.add(CodeBlock.builder().add("$L.put(EvHolder.Companion.joinVariantValueKey(KEY_$L, VARIANT_PRESET_CUSTOMIZE), \"$L\");", "variantValueMap", itemNameUpperCase, customizeValue).build());
            variantValueMapCodeBlocks.add(CodeBlock.builder().add("\n").build());
        }

        // collect fullVariants CodeBlocks
        fullVariantsCodeBlocks.add(CodeBlock.builder().add("$L.add(VARIANT_PRESET_DEFAULT);", "fullVariants").build());
        for (String variant : fullVariantStrSet) {
            fullVariantsCodeBlocks.add(CodeBlock.builder().add("$L.add(\"$L\");", "fullVariants", variant).build());
        }

        List<CodeBlock> all = new ArrayList<>(fullVariantsCodeBlocks);
        all.add(CodeBlock.builder().add("\n").build());
        all.addAll(variantValueMapCodeBlocks);

        // block init default
        classBuilder.addInitializerBlock(CodeBlock.join(all, "\n"));

        // method getFullVariants
        classBuilder.addMethod(MethodSpec.methodBuilder("getFullVariants")
                .addModifiers(Modifier.PUBLIC)
                .addCode(CodeBlock.builder().add("return $L;", "fullVariants").build())
                .returns(ParameterizedTypeName.get(Set.class, String.class))
                .build());

        // method getEvHolders
        List<CodeBlock> evHoldersCodeBlocks = new ArrayList<>();
        evHoldersCodeBlocks.add(CodeBlock.builder().add("List<EvHolder> evHolders = new $T<>();", ArrayList.class).build());
        for (EvGroup.EvItem evItem : evGroup.evItems) {
            evHoldersCodeBlocks.add(CodeBlock.builder().add("evHolders.add(new EvHolder(context, KEY_$L, currentVariantMap, variantValueMap));", evItem.name.toUpperCase())
                    .build());
        }
        evHoldersCodeBlocks.add(CodeBlock.builder().add("return evHolders;").build());
        classBuilder.addMethod(MethodSpec.methodBuilder("getEvHolders")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get("android.content", "Context"), "context")
                        .addAnnotation(ClassName.get("androidx.annotation", "NonNull"))
                        .build())
                .addCode(CodeBlock.join(evHoldersCodeBlocks, "\n"))
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get("com.hcanyz.environmentvariable", "EvHolder")))
                .build());

        // final file
        JavaFile javaFile = JavaFile.builder(evGroup.packageName, classBuilder.build())
                .addStaticImport(ConstantKt.class, "VARIANT_PRESET_CUSTOMIZE", "VARIANT_PRESET_DEFAULT")
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EvGroup collectEvGroup(Symbol group, Set<Symbol.ClassSymbol> items) {
        EvGroup evGroup = new EvGroup(group.name.toString(), group.packge().fullname.toString());

        com.hcanyz.environmentvariable.base.annotations.EvGroup evGroupAnnotation = group.getAnnotation(com.hcanyz.environmentvariable.base.annotations.EvGroup.class);
        evGroup.defaultVariant = evGroupAnnotation.defaultVariant();

        for (Symbol.ClassSymbol item : items) {
            String itemName = item.name.toString();
            EvGroup.EvItem evItem = new EvGroup.EvItem(itemName);
            evGroup.evItems.add(evItem);

            EvItem evItemAnnotation = item.getAnnotation(EvItem.class);
            evItem.defaultVariant = evItemAnnotation.defaultVariant();

            List<? extends Element> variants = processingEnv.getElementUtils().getAllMembers(item);
            for (Element variantTmp : variants) {
                if (!variantTmp.getKind().equals(ElementKind.FIELD)) {
                    continue;
                }
                if (!variantTmp.getModifiers().contains(Modifier.FINAL)) {
                    printMessage(Diagnostic.Kind.WARNING, String.format("%s - not FINAL ignored,  group:%s,item:%s,variant:%s", TAG, evGroup.name, itemName, variantTmp.getSimpleName()));
                    continue;
                }
                VariableElement variant = (VariableElement) variantTmp;
                String variantName = variant.getSimpleName().toString();
                String variantValue = variant.getConstantValue().toString();
                EvVariant variantAnnotation = variant.getAnnotation(EvVariant.class);

                evItem.evVariants.add(new EvGroup.EvVariant(variantName, variantValue, variantAnnotation.desc(), variantAnnotation.isDefault()));
            }
        }
        return evGroup;
    }

    private void printMessage(Diagnostic.Kind kind, CharSequence charSequence) {
        processingEnv.getMessager().printMessage(kind, charSequence + "\r\n");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(com.hcanyz.environmentvariable.base.annotations.EvGroup.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
