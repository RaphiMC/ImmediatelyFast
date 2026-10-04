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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.compat;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

public final class KnownHudShaders {

    private static final String ENTITY_TRANSLUCENT_CULL = "rendertype_entity_translucent_cull";

    // Call of Yucutan 1.0.13 adds a per-fragment emissive-alpha test to this shader.
    // It uses the same uniforms and interpolated inputs as vanilla, without depending
    // on draw boundaries or vertex IDs. Also require the complete vanilla companion
    // program: an unchanged fragment alone says nothing about replaced imports.
    private static final Map<Identifier, String> ENTITY_TRANSLUCENT_CULL_FILES = Map.of(
        new Identifier("shaders/core/" + ENTITY_TRANSLUCENT_CULL + ".fsh"), "d85c135b47898228b977d25c1ca0a59495c9e5e2e05de60276ce6764e8ff2770",
        new Identifier("shaders/core/" + ENTITY_TRANSLUCENT_CULL + ".vsh"), "0c0d3bcb9395458875a28b02eed04756560f928a54c4fe23a8c1ba210dd22206",
        new Identifier("shaders/core/" + ENTITY_TRANSLUCENT_CULL + ".json"), "65bddb8c6a1aa46072bffb781fbfa05557118918a940d80bd299679e243e7a8b",
        new Identifier("shaders/include/fog.glsl"), "c599820ddec8e4019a43e6720b02d3e7c7e6dbc97aa50df76340bd2001c12647",
        new Identifier("shaders/include/light.glsl"), "de5cc7083938a7eb72b571df4b90ce947b1ac2d2eb37498406207f3548032d3d"
    );

    private KnownHudShaders() {
    }

    public static boolean isCompatible(final String programName, final String vertexShaderName, final String fragmentShaderName, final ResourceFactory factory) {
        if (!ENTITY_TRANSLUCENT_CULL.equals(programName) || !ENTITY_TRANSLUCENT_CULL.equals(vertexShaderName) || !ENTITY_TRANSLUCENT_CULL.equals(fragmentShaderName)) {
            return false;
        }

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] buffer = new byte[4096];
            for (Map.Entry<Identifier, String> entry : ENTITY_TRANSLUCENT_CULL_FILES.entrySet()) {
                final Optional<Resource> resource = factory.getResource(entry.getKey());
                if (resource.isEmpty()) {
                    return false;
                }
                try (InputStream input = resource.get().getInputStream()) {
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        digest.update(buffer, 0, length);
                    }
                }
                if (!entry.getValue().equals(HexFormat.of().formatHex(digest.digest()))) {
                    return false;
                }
            }
            return true;
        } catch (IOException | NoSuchAlgorithmException | SecurityException ignored) {
            return false;
        }
    }

}
