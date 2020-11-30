package com.hcanyz.environmentvariable.demo;

import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.hcanyz.environmentvariable.base.annotations.EvVariant;

@EvGroup(defaultVariant = BuildConfig.EV_VARIANT, hideNonDefault = BuildConfig.EV_VARIANT == "release")
public class Ev3rdPushInfo {

    @EvItem
    class HuaWeiAppId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "uat")
        final String uat = "uat";
    }

    @EvItem(defaultVariant = "uat2")
    class HuaWeiBusinessId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "uat")
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";

        @EvVariant(desc = "uat2")
        final String uat2 = "uat2";
    }

    @EvItem
    class MiAppId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "uat", isDefault = true)
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }

    @EvItem(defaultVariant = "dev")
    class MiBusinessId {

        @EvVariant(desc = "dev")
        final String dev = "dev";

        @EvVariant(desc = "uat", isDefault = true)
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }
}
