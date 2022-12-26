package net.raphimc.immediatelyfast.injection.interfaces;

import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;

public interface IMapRenderer {

    MapAtlasTexture getMapAtlasTexture(int id);

    int getAtlasMapping(final int mapId);

}
