package com.hcanyz.environmentvariable.demo

import com.hcanyz.environmentvariable.base.annotations.EvGroup
import com.hcanyz.environmentvariable.base.annotations.EvItem
import com.hcanyz.environmentvariable.base.annotations.EvVariant

@EvGroup
class EnvironmentVariableDemo {

    @EvItem
    class Variable1 {

        @EvVariant(desc = "I am variant1")
        val variant1: String = "variant1 value"

        @EvVariant(isDefault = true, desc = "I am variant2")
        val variant2: String = "variant2 value"
    }
}