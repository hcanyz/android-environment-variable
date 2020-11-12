package com.hcanyz.environmentvariable.demo;

import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.hcanyz.environmentvariable.base.annotations.EvVariant;

@EvGroup(defaultVariant = BuildConfig.EV_VARIANT)
public class Ev3rdPushInfo {

    @EvItem
    class HuaWeiAppId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "xxx")
        final String uat = "uat";
    }

    @EvItem
    class HuaWeiBusinessId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "xxx")
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }

    @EvItem
    class MiAppId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "xxx", isDefault = true)
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }

    @EvItem(defaultVariant = BuildConfig.EV_VARIANT)
    class MiBusinessId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "xxx", isDefault = true)
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }
}
