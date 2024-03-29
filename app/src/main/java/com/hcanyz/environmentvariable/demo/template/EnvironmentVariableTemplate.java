package com.hcanyz.environmentvariable.demo.template;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hcanyz.environmentvariable.EvHandler;
import com.hcanyz.environmentvariable.IEvManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hcanyz.environmentvariable.base.ConstantKt.EV_VARIANT_PRESET_CUSTOMIZE;
import static com.hcanyz.environmentvariable.base.ConstantKt.EV_VARIANT_PRESET_DEFAULT;

public final class EnvironmentVariableTemplate implements IEvManager {
    public static final String EV_GROUP_NAME = "EvHttp";

    public static final String EV_ITEM_SERVERURL = "ServerUrl";

    private static final String EV_VARIANT_SERVERURL_DEV = "dev";

    private static final String EV_VARIANT_SERVERURL_UAT = "uat";

    private static final String EV_VARIANT_SERVERURL_RELEASE = "release";

    public static final String EV_ITEM_H5BASEURL = "H5BaseUrl";

    private static final String EV_VARIANT_H5BASEURL_DEV = "dev";

    private static final String EV_VARIANT_H5BASEURL_UAT = "uat";

    private static final String EV_VARIANT_H5BASEURL_RELEASE = "release";

    private static final String EV_VARIANT_H5BASEURL_TMP = "tmp";

    private final Set<String> intersectionVariants = new LinkedHashSet<String>();

    /**
     * map-key: "$evItemName.$variant"
     */
    private final Map<String, String> variantValueMap = new LinkedHashMap<String, String>();

    /**
     * map-key: "$evItemName"
     */
    private final Map<String, String> currentVariantMap = new LinkedHashMap<String, String>();

    private final List<EvHandler> evHandlers = new ArrayList<EvHandler>();

    {
        intersectionVariants.add("dev");
        intersectionVariants.add("uat");
        intersectionVariants.add("release");
        intersectionVariants.add(EV_VARIANT_PRESET_DEFAULT);


        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_DEV), "https://dev.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_UAT), "https://uat.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_RELEASE), "https://hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_PRESET_DEFAULT), "https://dev.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_PRESET_CUSTOMIZE), "http://172.16.53.1");


        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_DEV), "https://h5-dev.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_UAT), "https://h5-uat.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_RELEASE), "https://h5.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_TMP), "https://h5-tmp.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_PRESET_DEFAULT), "https://h5-dev.hcanyz.com");
        variantValueMap.put(EvHandler.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_PRESET_CUSTOMIZE), "");

    }

    private EnvironmentVariableTemplate() {
    }

    public static EnvironmentVariableTemplate getSingleton(@NonNull Context context) {
        EnvironmentVariableTemplate instance = EnvironmentVariableTemplate.Inner.instance;
        instance.getEvHandlers(context.getApplicationContext());
        return instance;
    }

    @Override
    @Nullable
    public String getEvItemCurrentValue(@NonNull String evItemName) {
        String variant = currentVariantMap.get(evItemName);
        return variantValueMap.get(EvHandler.Companion.joinVariantValueKey(evItemName, variant != null ? variant : EV_VARIANT_PRESET_DEFAULT));
    }

    @Override
    @NonNull
    public Set<String> getIntersectionVariants() {
        return intersectionVariants;
    }

    @Override
    @NonNull
    public synchronized List<EvHandler> getEvHandlers(@NonNull Context context) {
        if (evHandlers.isEmpty()) {
            evHandlers.add(new EvHandler(context, EV_ITEM_SERVERURL, currentVariantMap, variantValueMap));
            evHandlers.add(new EvHandler(context, EV_ITEM_H5BASEURL, currentVariantMap, variantValueMap));
        }
        return evHandlers;
    }

    private static class Inner {
        private static final EnvironmentVariableTemplate instance = new EnvironmentVariableTemplate();
    }
}
