package net.raphimc.immediatelyfast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastConfig;
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
    public static ImmediatelyFastConfig config;

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer("immediatelyfast").ifPresent(modContainer -> {
            LOGGER.info("Loading ImmediatelyFast " + modContainer.getMetadata().getVersion().getFriendlyString());
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
