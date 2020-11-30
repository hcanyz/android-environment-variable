package com.hcanyz.environmentvariable.demo

import com.hcanyz.environmentvariable.base.annotations.EvGroup
import com.hcanyz.environmentvariable.base.annotations.EvItem
import com.hcanyz.environmentvariable.base.annotations.EvVariant

@EvGroup(defaultVariant = BuildConfig.EV_VARIANT)
class EvHttp {

    @EvItem
    class ServerUrl {

        @EvVariant(desc = "dev server url")
        val dev: String = "https://dev.hcanyz.com"

        @EvVariant(desc = "uat server url")
        val uat: String = "https://uat.hcanyz.com"

        @EvVariant(desc = "release server url")
        val release: String = "https://hcanyz.com"

        @EvVariant(desc = "customize server url")
        val _customize_: String = "http://172.16.53.1"
    }

    @EvItem
    class H5BaseUrl {

        @EvVariant(desc = "h5 dev server url")
        val dev: String = "https://h5-dev.hcanyz.com"

        @EvVariant(desc = "h5 uat server url")
        val uat: String = "https://h5-uat.hcanyz.com"

        @EvVariant(desc = "h5 release server url")
        val release: String = "https://h5.hcanyz.com"

        @EvVariant(desc = "h5 tmp server url")
        val tmp: String = "https://h5-tmp.hcanyz.com"
    }
}