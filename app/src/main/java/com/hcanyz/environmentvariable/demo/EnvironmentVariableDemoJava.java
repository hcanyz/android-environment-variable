package com.hcanyz.environmentvariable.demo;

import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.hcanyz.environmentvariable.base.annotations.EvVariant;

@EvGroup
public class EnvironmentVariableDemoJava {

    @EvItem
    class Variable2 {

        @EvVariant(desc = "I am variant3")
        String variant3 = "variant3 value";

        @EvVariant(isDefault = true, desc = "I am variant4")
        String variant4 = "variant4 value";
    }
}
