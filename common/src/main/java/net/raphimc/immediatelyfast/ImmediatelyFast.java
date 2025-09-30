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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignTextCache;
import net.raphimc.immediatelyfast.util.IrisCompat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Objects;

public class ImmediatelyFast {

    public static final Logger LOGGER = LoggerFactory.getLogger("ImmediatelyFast");
    public static final Unsafe UNSAFE = getUnsafe();
    public static String VERSION;
    public static ImmediatelyFastConfig config;
    public static ImmediatelyFastRuntimeConfig runtimeConfig;

    public static SignTextCache signTextCache;

    public static void earlyInit() {
        if (ImmediatelyFast.config != null) return;
        ImmediatelyFast.loadConfig();
        ImmediatelyFast.createRuntimeConfig();
        VERSION = PlatformCode.getModVersion("immediatelyfast").orElseThrow(NullPointerException::new);
        PlatformCode.checkModCompatibility();

        //System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }

    public static void onRenderSystemInit() {
        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
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

        Objects.requireNonNull(ImmediatelyFast.config, "Config not loaded yet");
        Objects.requireNonNull(ImmediatelyFast.runtimeConfig, "Runtime config not created yet");

        if (ImmediatelyFast.config.fix_slow_buffer_upload_on_apple_gpu && isApple && !(RenderSystem.getDevice().getEnabledExtensions().contains("GL_ARB_direct_state_access") || RenderSystem.getDevice().getEnabledExtensions().contains("GL_ARB_buffer_storage"))) {
            ImmediatelyFast.runtimeConfig.disable_fast_buffer_upload = true;
        }

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            PlatformCode.getModVersion("iris").or(() -> PlatformCode.getModVersion("oculus")).ifPresent(version -> {
                ImmediatelyFast.LOGGER.info("Found Iris/Oculus " + version + ". Enabling compatibility.");
                IrisCompat.init();
            });
        }
    }

    public static void lateInit() {
        if (ImmediatelyFast.config.experimental_sign_text_buffering) {
            ImmediatelyFast.signTextCache = new SignTextCache();
            if (PlatformCode.getModVersion("neoforge").isEmpty()) { // NeoForge uses an event. Handled in ImmediatelyFastNeoForge
                ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(ImmediatelyFast.signTextCache);
            }
        }
    }

    public static void onWorldJoin() {
        if (ImmediatelyFast.signTextCache != null) {
            ImmediatelyFast.signTextCache.clearCache();
        }
    }

    public static void loadConfig() {
        final File configFile = PlatformCode.getConfigDirectory().resolve("immediatelyfast.json").toFile();
        if (configFile.exists()) {
            try {
                ImmediatelyFast.config = new Gson().fromJson(new FileReader(configFile), ImmediatelyFastConfig.class);
            } catch (Throwable e) {
                LOGGER.error("Failed to load ImmediatelyFast config. Resetting it.", e);
            }
        }
        if (ImmediatelyFast.config == null) {
            ImmediatelyFast.config = new ImmediatelyFastConfig();
        }
        try {
            Files.writeString(configFile.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(ImmediatelyFast.config));
        } catch (Throwable e) {
            LOGGER.error("Failed to save ImmediatelyFast config.", e);
        }

        if (!MathUtil.mathIsPoT(ImmediatelyFast.config.font_atlas_size)) {
            LOGGER.warn("Font atlas size " + ImmediatelyFast.config.font_atlas_size + " is not a power of two! Rounding up to the next power of two.");
            ImmediatelyFast.config.font_atlas_size = MathUtil.mathRoundPoT(ImmediatelyFast.config.font_atlas_size);
        }
        if (!MathUtil.mathIsPoT(ImmediatelyFast.config.map_atlas_size)) {
            LOGGER.warn("Map atlas size " + ImmediatelyFast.config.map_atlas_size + " is not a power of two! Rounding up to the next power of two.");
            ImmediatelyFast.config.map_atlas_size = MathUtil.mathRoundPoT(ImmediatelyFast.config.map_atlas_size);
        }
    }

    public static void createRuntimeConfig() {
        ImmediatelyFast.runtimeConfig = new ImmediatelyFastRuntimeConfig(ImmediatelyFast.config);
    }

    private static Unsafe getUnsafe() {
        try {
            for (Field field : Unsafe.class.getDeclaredFields()) {
                if (field.getType().equals(Unsafe.class)) {
                    field.setAccessible(true);
                    return (Unsafe) field.get(null);
                }
            }
        } catch (Throwable ignored) {
        }
        throw new IllegalStateException("Unable to get Unsafe instance");
    }

}
