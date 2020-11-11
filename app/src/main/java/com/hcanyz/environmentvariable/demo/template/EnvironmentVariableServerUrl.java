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

public final class EnvironmentVariableServerUrl {

    public static final String KEY_SERVER_URL = "ServerUrl";
    public static final String VARIANT_SERVER_URL_DEV = "dev";
    public static final String VARIANT_SERVER_URL_SIT = "sit";
    public static final String VARIANT_SERVER_URL_UAT = "uat";
    public static final String VARIANT_SERVER_URL_RELEASE = "release";

    private static final Set<String> fullVariants = new HashSet<>();
    // map-key: $key.$env
    private static final Map<String, String> variantValueMap = new HashMap<>();
    // map-key: $key
    private static final Map<String, String> currentVariantMap = new HashMap<>();

    static {
        fullVariants.add(VARIANT_PRESET_DEFAULT);
        fullVariants.add("dev");
        fullVariants.add("sit");
        fullVariants.add("uat");
        fullVariants.add("release");

        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_SERVER_URL_DEV, "");
        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_SERVER_URL_SIT, "");
        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_SERVER_URL_UAT, "");
        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_SERVER_URL_RELEASE, "");
        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_PRESET_DEFAULT, "");
        variantValueMap.put(KEY_SERVER_URL + "." + VARIANT_PRESET_CUSTOMIZE, "");
    }

    public static Set<String> getFullVariants() {
        return fullVariants;
    }

    @NonNull
    public static List<EvHolder> getCurrentVariantServerUrl(@NonNull Context context) {
        List<EvHolder> evHolders = new ArrayList<>();
        evHolders.add(new EvHolder(context, KEY_SERVER_URL, currentVariantMap, variantValueMap));
        return evHolders;
    }
}
