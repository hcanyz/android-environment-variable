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

import static com.hcanyz.environmentvariable.base.ConstantKt.VARIANT_PRESET_CUSTOMIZE;
import static com.hcanyz.environmentvariable.base.ConstantKt.VARIANT_PRESET_DEFAULT;

public final class EnvironmentVariableTemplate {

    public static final String KEY_SERVERURL = "ServerUrl";

    public static final String VARIANT_SERVERURL_DEV = "dev";

    public static final String VARIANT_SERVERURL_UAT = "uat";

    public static final String VARIANT_SERVERURL_RELEASE = "release";

    public static final String KEY_H5BASEURL = "H5BaseUrl";

    public static final String VARIANT_H5BASEURL_DEV = "dev";

    public static final String VARIANT_H5BASEURL_UAT = "uat";

    public static final String VARIANT_H5BASEURL_RELEASE = "release";

    public static final String VARIANT_H5BASEURL_TMP = "tmp";

    private static final Set<String> fullVariants = new HashSet<>();

    /**
     * map-key: "$key.$variant"
     */
    private static final Map<String, String> variantValueMap = new HashMap<>();

    /**
     * map-key: "$key"
     */
    private static final Map<String, String> currentVariantMap = new HashMap<>();

    static {
        fullVariants.add(VARIANT_PRESET_DEFAULT);
        fullVariants.add("dev");
        fullVariants.add("uat");
        fullVariants.add("release");
        fullVariants.add("tmp");


        variantValueMap.put(KEY_SERVERURL + "." + VARIANT_SERVERURL_DEV, "https://dev.hcanyz.com");
        variantValueMap.put(KEY_SERVERURL + "." + VARIANT_SERVERURL_UAT, "https://uat.hcanyz.com");
        variantValueMap.put(KEY_SERVERURL + "." + VARIANT_SERVERURL_RELEASE, "https://hcanyz.com");
        variantValueMap.put(KEY_SERVERURL + "." + VARIANT_PRESET_DEFAULT, "https://dev.hcanyz.com");
        variantValueMap.put(KEY_SERVERURL + "." + VARIANT_PRESET_CUSTOMIZE, "");


        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_H5BASEURL_DEV, "https://h5-dev.hcanyz.com");
        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_H5BASEURL_UAT, "https://h5-uat.hcanyz.com");
        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_H5BASEURL_RELEASE, "https://h5.hcanyz.com");
        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_H5BASEURL_TMP, "https://h5-tmp.hcanyz.com");
        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_PRESET_DEFAULT, "https://h5-dev.hcanyz.com");
        variantValueMap.put(KEY_H5BASEURL + "." + VARIANT_PRESET_CUSTOMIZE, "");

    }

    public static Set<String> getFullVariants() {
        return fullVariants;
    }

    public static List<EvHolder> getEvHolders(@NonNull Context context) {
        List<EvHolder> evHolders = new ArrayList<>();
        evHolders.add(new EvHolder(context, KEY_SERVERURL, currentVariantMap, variantValueMap));
        evHolders.add(new EvHolder(context, KEY_H5BASEURL, currentVariantMap, variantValueMap));
        return evHolders;
    }
}
