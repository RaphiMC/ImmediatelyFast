/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.raphimc.immediatelyfast.apiimpl.ApiAccessImpl;
import net.raphimc.immediatelyfast.compat.IrisCompat;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignTextCache;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;
import org.lwjgl.opengl.GL11C;
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
        if (config != null) return;

        ImmediatelyFast.loadConfig();

        if (!config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            if (config.hud_batching && PlatformCode.getModVersion("slight-gui-modifications").isPresent()) {
                LOGGER.warn("Slight GUI Modifications detected. Force disabling HUD and Screen Batching optimization.");
                config.hud_batching = false;
                config.experimental_screen_batching = false;
            }
        }

        if (config.experimental_screen_batching && !config.hud_batching) {
            LOGGER.warn("Screen Batching is enabled but HUD Batching is disabled. Disabling Screen Batching.");
            config.experimental_screen_batching = false;
        }

        ImmediatelyFast.createRuntimeConfig();
        ImmediatelyFastApi.setApiImpl(new ApiAccessImpl());

        VERSION = PlatformCode.getModVersion("immediatelyfast").orElseThrow(NullPointerException::new);
        PlatformCode.checkModCompatibility();

        //System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }

    public static void windowInit() {
        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
        LOGGER.info("Initializing ImmediatelyFast " + VERSION + " on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);

        Objects.requireNonNull(config, "Config not loaded yet");
        Objects.requireNonNull(runtimeConfig, "Runtime config not created yet");

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            PlatformCode.getModVersion("iris").or(() -> PlatformCode.getModVersion("oculus")).ifPresent(version -> {
                ImmediatelyFast.LOGGER.info("Found Iris/Oculus " + version + ". Enabling compatibility.");
                IrisCompat.init();
            });
        }
    }

    public static void lateInit() {
        if (config.experimental_sign_text_buffering) {
            signTextCache = new SignTextCache();
            ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(signTextCache);
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
