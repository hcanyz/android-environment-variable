package com.hcanyz.environmentvariable

import android.content.Context
import android.content.SharedPreferences
import com.hcanyz.environmentvariable.base.VARIANT_PRESET_CUSTOMIZE
import com.hcanyz.environmentvariable.base.VARIANT_PRESET_DEFAULT

class EvHolder(
    private val context: Context,
    private val key: String,
    private val currentVariantMap: MutableMap<String, String>,
    private val variantValueMap: MutableMap<String, String?>
) {
    init {
        currentVariantMap[key] = VARIANT_PRESET_DEFAULT

        if (containsVariantFromStorage(key)) {
            val currentVariant = readVariantFromStorage(key)
            currentVariantMap[key] = currentVariant ?: VARIANT_PRESET_DEFAULT
        }
        if (containsCustomizeValueFromStorage(key)) {
            variantValueMap["$key.$VARIANT_PRESET_CUSTOMIZE"] = readCustomizeValueFromStorage(key)
        }
    }

    fun currentVariant(): String {
        return currentVariantMap[key] ?: return VARIANT_PRESET_DEFAULT
    }

    fun currentVariantValue(): String? {
        val currentVariant: String = currentVariant()
        val value: String? = variantValueMap[currentVariant]
        if (currentVariant != VARIANT_PRESET_CUSTOMIZE && value == null) {
            throw IllegalStateException("error variant value")
        }
        return value
    }

    fun changeVariant(variant: String) {
        var variantFinal = variant
        val value: String? = variantValueMap[variantFinal]
        if (variantFinal != VARIANT_PRESET_CUSTOMIZE && value == null) {
            variantFinal = VARIANT_PRESET_DEFAULT
        }

        currentVariantMap[key] = variantFinal
        commitVariantToStorage(key, variantFinal)
    }

    fun changeVariantToCustomize(customizeValue: String) {
        currentVariantMap[key] = VARIANT_PRESET_CUSTOMIZE
        commitCustomizeValueToStorage(context, key, customizeValue)
    }

    private fun readVariantFromStorage(key: String): String? {
        return sharedPreferences(context).getString(
            "$key.variant", ""
        )
    }

    private fun containsVariantFromStorage(key: String): Boolean {
        return sharedPreferences(context).contains(
            "$key.variant"
        )
    }

    private fun readCustomizeValueFromStorage(key: String): String? {
        return sharedPreferences(context).getString(
            "$key.customizeValue", ""
        )
    }

    private fun containsCustomizeValueFromStorage(key: String): Boolean {
        return sharedPreferences(context).contains(
            "$key.customizeValue"
        )
    }

    private fun commitVariantToStorage(key: String, variant: String) {
        sharedPreferences(context).edit()
            .putString("$key.variant", variant)
            .apply()
    }

    private fun commitCustomizeValueToStorage(
        context: Context,
        key: String,
        value: String
    ) {
        sharedPreferences(context).edit()
            .putString("$key.customizeValue", value)
            .apply()
    }

    private fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + ".environment_variable",
            Context.MODE_PRIVATE
        )
    }
}