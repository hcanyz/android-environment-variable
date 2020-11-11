package com.hcanyz.environmentvariable.compiler;

import java.util.ArrayList;
import java.util.List;

public class EvInfo {
    public String name;
    public String packageName;
    public final List<EvItem> evItems = new ArrayList<>();

    public EvInfo(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public static class EvItem {
        public String name;
        public final List<EvVariant> evVariants = new ArrayList<>();

        public EvItem(String name) {
            this.name = name;
        }
    }

    public static class EvVariant {
        public String name;
        public String value;
        public String desc;
        public boolean isDefault;

        public EvVariant(String name, String value, String desc, boolean isDefault) {
            this.name = name;
            this.value = value;
            this.desc = desc;
            this.isDefault = isDefault;
        }
    }
}
