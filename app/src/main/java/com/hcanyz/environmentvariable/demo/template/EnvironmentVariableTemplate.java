package com.hcanyz.environmentvariable.demo.template;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hcanyz.environmentvariable.EvHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hcanyz.environmentvariable.base.ConstantKt.EV_VARIANT_PRESET_CUSTOMIZE;
import static com.hcanyz.environmentvariable.base.ConstantKt.EV_VARIANT_PRESET_DEFAULT;

public final class EnvironmentVariableTemplate {

    public static final String EV_GROUP_NAME = "EvHttp";

    public static final String EV_ITEM_H5BASEURL = "H5BaseUrl";

    public static final String EV_VARIANT_H5BASEURL_DEV = "dev";

    public static final String EV_VARIANT_H5BASEURL_UAT = "uat";

    public static final String EV_VARIANT_H5BASEURL_RELEASE = "release";

    public static final String EV_VARIANT_H5BASEURL_TMP = "tmp";

    public static final String EV_ITEM_SERVERURL = "ServerUrl";

    public static final String EV_VARIANT_SERVERURL_DEV = "dev";

    public static final String EV_VARIANT_SERVERURL_UAT = "uat";

    public static final String EV_VARIANT_SERVERURL_RELEASE = "release";

    private final Set<String> fullVariants = new HashSet<>();

    /**
     * map-key: "$key.$variant"
     */
    private final Map<String, String> variantValueMap = new HashMap<>();

    /**
     * map-key: "$key"
     */
    private final Map<String, String> currentVariantMap = new HashMap<>();

    {
        fullVariants.add(EV_VARIANT_PRESET_DEFAULT);
        fullVariants.add("dev");
        fullVariants.add("uat");
        fullVariants.add("release");
        fullVariants.add("tmp");


        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_DEV), "https://h5-dev.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_UAT), "https://h5-uat.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_RELEASE), "https://h5.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_H5BASEURL_TMP), "https://h5-tmp.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_PRESET_DEFAULT), "https://h5-uat.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_H5BASEURL, EV_VARIANT_PRESET_CUSTOMIZE), "");


        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_DEV), "https://dev.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_UAT), "https://uat.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_SERVERURL_RELEASE), "https://hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_PRESET_DEFAULT), "https://dev.hcanyz.com");
        variantValueMap.put(EvHolder.Companion.joinVariantValueKey(EV_ITEM_SERVERURL, EV_VARIANT_PRESET_CUSTOMIZE), "");

    }

    public String getEvItemCurrentValue(@NonNull String key) {
        return variantValueMap.get(EvHolder.Companion.joinVariantValueKey(key, currentVariantMap.get(key)));
    }

    public Set<String> getFullVariants() {
        return fullVariants;
    }

    public List<EvHolder> getEvHolders(@NonNull Context context) {
        List<EvHolder> evHolders = new ArrayList<>();
        evHolders.add(new EvHolder(context, EV_ITEM_H5BASEURL, currentVariantMap, variantValueMap));
        evHolders.add(new EvHolder(context, EV_ITEM_SERVERURL, currentVariantMap, variantValueMap));
        return evHolders;
    }
}
