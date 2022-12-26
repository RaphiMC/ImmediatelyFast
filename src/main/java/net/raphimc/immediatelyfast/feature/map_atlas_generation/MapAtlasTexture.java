package net.raphimc.immediatelyfast.feature.map_atlas_generation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class MapAtlasTexture implements AutoCloseable {

    public static final int ATLAS_SIZE = 4096;
    public static final int MAP_SIZE = 128;
    public static final int MAPS_PER_ATLAS = (ATLAS_SIZE / MAP_SIZE) * (ATLAS_SIZE / MAP_SIZE);

    private final int id;
    private final Identifier identifier;
    private final NativeImageBackedTexture texture;
    private int mapCount;

    public MapAtlasTexture(final int id) {
        this.id = id;

        this.identifier = new Identifier("immediatelyfast", "map_atlas/" + id);
        this.texture = new NativeImageBackedTexture(ATLAS_SIZE, ATLAS_SIZE, true);
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.identifier, this.texture);
    }

    public int getNextMapLocation() {
        if (this.mapCount >= MAPS_PER_ATLAS) {
            return -1;
        }

        final byte atlasX = (byte) (this.mapCount % (ATLAS_SIZE / MAP_SIZE));
        final byte atlasY = (byte) (this.mapCount / (ATLAS_SIZE / MAP_SIZE));
        this.mapCount++;

        return (this.id << 16) | (atlasX << 8) | atlasY;
    }

    public int getId() {
        return this.id;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public NativeImageBackedTexture getTexture() {
        return this.texture;
    }

    @Override
    public void close() {
        this.texture.close();
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.identifier);
    }

}
