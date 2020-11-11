package com.hcanyz.environmentvariable.demo;

import com.hcanyz.environmentvariable.base.annotations.EvGroup;
import com.hcanyz.environmentvariable.base.annotations.EvItem;
import com.hcanyz.environmentvariable.base.annotations.EvVariant;

@EvGroup
public class Ev3rdPushInfo {

    @EvItem
    class HuaWeiAppId {

        @EvVariant(isDefault = true, desc = "xxx")
        final String dev = "xxx";

        @EvVariant(desc = "xxx")
        final String uat = "xxx";
    }

    @EvItem
    class HuaWeiBusinessId {

        @EvVariant(isDefault = true, desc = "xxx")
        final String dev = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";

        @EvVariant(desc = "xxx")
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }

    @EvItem
    class Mi {

        @EvVariant(isDefault = true, desc = "xxx")
        final String dev = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";

        @EvVariant(desc = "xxx")
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }

    @EvItem
    class MiBusinessId {

        @EvVariant(isDefault = true, desc = "xxx")
        final String dev = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";

        @EvVariant(desc = "xxx")
        final String uat = "F4xLySeieMETFpL78zttB1ccGHoFd9Le";
    }
}
