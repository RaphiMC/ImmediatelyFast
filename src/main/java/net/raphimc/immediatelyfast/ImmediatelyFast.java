/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.raphimc.immediatelyfast.compat.IrisCompat;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastRuntimeConfig;
import net.raphimc.immediatelyfast.feature.fast_buffer_upload.PersistentMappedStreamingBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.nio.file.Files;

public class ImmediatelyFast implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("ImmediatelyFast");
    public static final Unsafe UNSAFE = getUnsafe();
    public static String VERSION;
    public static ImmediatelyFastConfig config;
    public static ImmediatelyFastRuntimeConfig runtimeConfig;

    public static PersistentMappedStreamingBuffer persistentMappedStreamingBuffer;

    @Override
    public void onInitializeClient() {
        VERSION = FabricLoader.getInstance().getModContainer("immediatelyfast").orElseThrow(NullPointerException::new).getMetadata().getVersion().getFriendlyString();
        LOGGER.info("Loading ImmediatelyFast " + VERSION);

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            FabricLoader.getInstance().getModContainer("iris").ifPresent(modContainer -> {
                LOGGER.info("Found Iris " + modContainer.getMetadata().getVersion().getFriendlyString() + ". Enabling compatibility.");
                IrisCompat.init();
            });
        }

        RenderSystem.recordRenderCall(() -> {
            final GLCapabilities cap = GL.getCapabilities();
            final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
            final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
            final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
            LOGGER.info("Initializing IF on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);

            final boolean isNvidia = gpuVendor != null && gpuVendor.toLowerCase().startsWith("nvidia corporation");
            final boolean isAmd = gpuVendor != null && gpuVendor.toLowerCase().startsWith("ati technologies");
            final boolean isIntel = gpuVendor != null && gpuVendor.toLowerCase().startsWith("intel");

            if (ImmediatelyFast.config.fast_buffer_upload) {
                if (cap.GL_ARB_direct_state_access && cap.GL_ARB_buffer_storage && cap.glMemoryBarrier != 0) {
                    if (isAmd && !ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling) {
                        // Explicit flush causes AMD GPUs to stall the pipeline a lot.
                        LOGGER.warn("AMD GPU detected. Enabling coherent buffer mapping.");
                        ImmediatelyFast.config.fast_buffer_upload_explicit_flush = false;
                    }

                    persistentMappedStreamingBuffer = new PersistentMappedStreamingBuffer(config.fast_buffer_upload_size_mb * 1024 * 1024);
                } else {
                    LOGGER.warn("Your GPU doesn't support ARB_direct_state_access and/or ARB_buffer_storage and/or glMemoryBarrier. Falling back to legacy fast buffer upload method.");
                    if (!isNvidia && !ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling) {
                        // Legacy fast buffer upload causes a lot of graphical issues on non NVIDIA GPUs.
                        LOGGER.warn("Non NVIDIA GPU detected. Force disabling fast buffer upload optimization.");
                    } else {
                        ImmediatelyFast.runtimeConfig.legacy_fast_buffer_upload = true;
                    }
                }
            }
        });

        //System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }

    public static void loadConfig() {
        final File configFile = FabricLoader.getInstance().getConfigDir().resolve("immediatelyfast.json").toFile();
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
