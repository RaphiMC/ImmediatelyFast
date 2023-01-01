package net.raphimc.immediatelyfast;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ImmediatelyFastPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
    }

}
