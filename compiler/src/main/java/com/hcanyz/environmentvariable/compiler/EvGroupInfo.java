package com.hcanyz.environmentvariable.compiler;

import java.util.ArrayList;
import java.util.List;

public class EvGroupInfo {
    public String name;
    public String packageName;
    public String defaultVariant;
    public final List<EvItemInfo> evItemInfos = new ArrayList<>();

    public EvGroupInfo(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public static class EvItemInfo {
        public String name;
        public String defaultVariant;
        public final List<EvVariantInfo> evVariantInfos = new ArrayList<>();

        public EvItemInfo(String name) {
            this.name = name;
        }
    }

    public static class EvVariantInfo {
        public String name;
        public String value;
        public String desc;
        public boolean isDefault;

        public EvVariantInfo(String name, String value, String desc, boolean isDefault) {
            this.name = name;
            this.value = value;
            this.desc = desc;
            this.isDefault = isDefault;
        }
    }
}
