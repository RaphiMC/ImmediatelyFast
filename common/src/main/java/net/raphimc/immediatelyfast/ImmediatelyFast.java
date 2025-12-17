/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.GlDebugInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.raphimc.immediatelyfast.apiimpl.ApiAccessImpl;
import net.raphimc.immediatelyfast.compat.IrisCompat;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignTextCache;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Objects;

public class ImmediatelyFast {

    public static final Logger LOGGER = LoggerFactory.getLogger("ImmediatelyFast");
    public static String VERSION;
    public static ImmediatelyFastConfig config;
    public static ImmediatelyFastRuntimeConfig runtimeConfig;

    public static SignTextCache signTextCache;

    public static void earlyInit() {
        if (config != null) return;

        ImmediatelyFast.loadConfig();

        if (config.experimental_screen_batching && !config.hud_batching) {
            LOGGER.warn("Screen Batching is enabled but HUD Batching is disabled. Disabling Screen Batching.");
            config.experimental_screen_batching = false;
        }

        if (!config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            if (config.experimental_sign_text_buffering) {
                if (PlatformCode.getModVersion("vulkanmod").isPresent()) {
                    LOGGER.warn("VulkanMod detected. Force disabling sign text buffering optimization.");
                    config.experimental_sign_text_buffering = false;
                }
            }
        }

        ImmediatelyFast.createRuntimeConfig();
        ImmediatelyFastApi.setApiImpl(new ApiAccessImpl());

        VERSION = PlatformCode.getModVersion("immediatelyfast").orElseThrow(NullPointerException::new);
        PlatformCode.checkModCompatibility();

        //System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }

    public static void windowInit() {
        final String gpuVendor = GlDebugInfo.getVendor();
        final String gpuModel = GlDebugInfo.getRenderer();
        final String glVersion = GlDebugInfo.getVersion();
        LOGGER.info("Initializing ImmediatelyFast " + VERSION + " on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);

        boolean isNvidia = false;
        boolean isAmd = false;
        boolean isIntel = false;
        boolean isApple = false;
        if (gpuVendor != null) {
            final String gpuVendorLower = gpuVendor.toLowerCase();

            isNvidia = gpuVendorLower.startsWith("nvidia");
            isAmd = gpuVendorLower.startsWith("ati") || gpuVendorLower.startsWith("amd");
            isIntel = gpuVendorLower.startsWith("intel");
            isApple = gpuVendorLower.startsWith("apple");
        }

        Objects.requireNonNull(config, "Config not loaded yet");
        Objects.requireNonNull(runtimeConfig, "Runtime config not created yet");

        if (config.fast_buffer_upload && isApple && !config.debug_only_and_not_recommended_disable_hardware_conflict_handling) {
            LOGGER.warn("Apple GPU detected. Disabling fast buffer upload.");
            runtimeConfig.fast_buffer_upload = false;
        }

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            PlatformCode.getModVersion("iris").ifPresent(version -> {
                ImmediatelyFast.LOGGER.info("Found Iris " + version + ". Enabling compatibility.");
                IrisCompat.init();
            });
        }
    }

    public static void lateInit() {
        if (config.experimental_sign_text_buffering) {
            signTextCache = new SignTextCache();
            if (!PlatformCode.getModVersion("neoforge").isPresent()) { // NeoForge uses an event. Handled in ImmediatelyFastNeoForge
                ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(signTextCache);
            }
        }
    }

    public static void onWorldJoin() {
        if (signTextCache != null) {
            signTextCache.clearCache();
        }
    }

    public static void loadConfig() {
        final File configFile = PlatformCode.getConfigDirectory().resolve("immediatelyfast.json").toFile();
        if (configFile.exists()) {
            try {
                config = new Gson().fromJson(new FileReader(configFile), ImmediatelyFastConfig.class);
            } catch (Throwable e) {
                LOGGER.error("Failed to load ImmediatelyFast config. Resetting it.", e);
            }
        }
        if (config == null) {
            config = new ImmediatelyFastConfig();
        }
        try {
            Files.writeString(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(config));
        } catch (Throwable e) {
            LOGGER.error("Failed to save ImmediatelyFast config.", e);
        }
    }

    public static void createRuntimeConfig() {
        runtimeConfig = new ImmediatelyFastRuntimeConfig(config);
    }

}
