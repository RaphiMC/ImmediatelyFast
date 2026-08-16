/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
import com.mojang.blaze3d.systems.DeviceInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignTextCache;
import net.raphimc.immediatelyfast.service.PlatformService;
import org.lwjgl.system.MathUtil;
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
        if (ImmediatelyFast.config != null) {
            return;
        }
        ImmediatelyFast.loadConfig();
        ImmediatelyFast.createRuntimeConfig();
        VERSION = PlatformService.INSTANCE.getModVersion("immediatelyfast").orElseThrow(NullPointerException::new);

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            if (ImmediatelyFast.config.experimental_sign_text_buffering) {
                if (PlatformService.INSTANCE.getModVersion("iris").isPresent()) {
                    LOGGER.warn("Iris detected. Force disabling sign text buffering optimization.");
                    ImmediatelyFast.config.experimental_sign_text_buffering = false;
                }
            }
        }

        //System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }

    public static void onRenderSystemInit() {
        final DeviceInfo deviceInfo = RenderSystem.getDevice().getDeviceInfo();
        final String gpuVendor = deviceInfo.vendorName();
        final String gpuModel = deviceInfo.name();
        final String backendName = deviceInfo.backendName();
        final String backendVersion = deviceInfo.driverInfo();
        LOGGER.info("Initializing ImmediatelyFast " + VERSION + " on " + gpuModel + " (" + gpuVendor + ") with " + backendName + " " + backendVersion);

        final String gpuVendorLower = gpuVendor.toLowerCase();
        final boolean isIntel = gpuVendorLower.startsWith("intel");
        final boolean isApple = gpuVendorLower.startsWith("apple");

        Objects.requireNonNull(ImmediatelyFast.config, "Config not loaded yet");
        Objects.requireNonNull(ImmediatelyFast.runtimeConfig, "Runtime config not created yet");

        ImmediatelyFast.runtimeConfig.fix_slow_buffer_upload_on_apple_gpu &= isApple && backendName.equals("OpenGL") && !(deviceInfo.underlyingExtensions().contains("GL_ARB_direct_state_access") || deviceInfo.underlyingExtensions().contains("GL_ARB_buffer_storage"));
        if (ImmediatelyFast.runtimeConfig.avoid_redundant_framebuffer_switching && !ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling && isIntel && backendName.equals("OpenGL") && (gpuModel.contains("UHD Graphics") || gpuModel.contains("Xe Graphics"))) {
            LOGGER.warn("Intel UHD Graphics or Intel Xe Graphics detected. Force disabling redundant framebuffer switching optimization.");
            ImmediatelyFast.runtimeConfig.avoid_redundant_framebuffer_switching = false;
        }
    }

    public static void lateInit() {
        if (ImmediatelyFast.config.experimental_sign_text_buffering) {
            ImmediatelyFast.signTextCache = new SignTextCache();
            if (PlatformService.INSTANCE.getModVersion("neoforge").isEmpty()) { // NeoForge uses an event. Handled in ImmediatelyFastNeoForge
                ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(ImmediatelyFast.signTextCache);
            }
        }
    }

    public static void onLevelChange() {
        if (ImmediatelyFast.signTextCache != null) {
            ImmediatelyFast.signTextCache.clearCache();
        }
    }

    public static void loadConfig() {
        final File configFile = PlatformService.INSTANCE.getConfigDirectory().resolve("immediatelyfast.json").toFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ImmediatelyFast.config = new Gson().fromJson(reader, ImmediatelyFastConfig.class);
            } catch (Throwable e) {
                LOGGER.error("Failed to load ImmediatelyFast config. Resetting it.", e);
            }
        }
        if (ImmediatelyFast.config == null) {
            ImmediatelyFast.config = new ImmediatelyFastConfig();
        }

        if (!MathUtil.mathIsPoT(ImmediatelyFast.config.font_atlas_size)) {
            LOGGER.warn("Font atlas size " + ImmediatelyFast.config.font_atlas_size + " is not a power of two! Rounding up to the next power of two.");
            ImmediatelyFast.config.font_atlas_size = MathUtil.mathRoundPoT(ImmediatelyFast.config.font_atlas_size);
        }
        if (!MathUtil.mathIsPoT(ImmediatelyFast.config.map_atlas_size)) {
            LOGGER.warn("Map atlas size " + ImmediatelyFast.config.map_atlas_size + " is not a power of two! Rounding up to the next power of two.");
            ImmediatelyFast.config.map_atlas_size = MathUtil.mathRoundPoT(ImmediatelyFast.config.map_atlas_size);
        }
        if (!MathUtil.mathIsPoT(ImmediatelyFast.config.experimental_sign_atlas_size)) {
            LOGGER.warn("Sign atlas size " + ImmediatelyFast.config.experimental_sign_atlas_size + " is not a power of two! Rounding up to the next power of two.");
            ImmediatelyFast.config.experimental_sign_atlas_size = MathUtil.mathRoundPoT(ImmediatelyFast.config.experimental_sign_atlas_size);
        }

        try {
            Files.writeString(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(ImmediatelyFast.config));
        } catch (Throwable e) {
            LOGGER.error("Failed to save ImmediatelyFast config.", e);
        }
    }

    public static void createRuntimeConfig() {
        ImmediatelyFast.runtimeConfig = new ImmediatelyFastRuntimeConfig(ImmediatelyFast.config);
    }

}
