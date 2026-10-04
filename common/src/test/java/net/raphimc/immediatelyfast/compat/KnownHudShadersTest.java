package net.raphimc.immediatelyfast.compat;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KnownHudShadersTest {

    private static final String PROGRAM = "rendertype_entity_translucent_cull";
    private static final String FRAGMENT = "shaders/core/" + PROGRAM + ".fsh";
    private static final List<String> FILES = List.of(FRAGMENT, "shaders/core/" + PROGRAM + ".vsh", "shaders/core/" + PROGRAM + ".json", "shaders/include/fog.glsl", "shaders/include/light.glsl");

    @Test
    public void acceptsOnlyTheCompleteKnownProgramAndClosesStreams() throws IOException {
        final Map<Identifier, byte[]> files = compatibleFiles();
        final AtomicInteger closed = new AtomicInteger();
        final ResourceFactory factory = id -> Optional.ofNullable(files.get(id)).map(bytes -> new Resource(null, () -> new ByteArrayInputStream(bytes) {
            @Override
            public void close() throws IOException {
                closed.incrementAndGet();
                super.close();
            }
        }));
        assertTrue(KnownHudShaders.isCompatible(PROGRAM, PROGRAM, PROGRAM, factory));
        assertEquals(FILES.size(), closed.get());
    }

    @Test
    public void rejectsEachChangedOrMissingDependency() throws IOException {
        for (String path : FILES) {
            final Map<Identifier, byte[]> files = compatibleFiles();
            final Identifier id = new Identifier(path);
            files.get(id)[0] ^= 1;
            assertFalse(path, compatible(files));
            files.remove(id);
            assertFalse(path, compatible(files));
        }
    }

    @Test
    public void validatesFreshResourcesAfterEachReload() throws IOException {
        final Map<Identifier, byte[]> files = compatibleFiles();
        assertTrue(compatible(files));
        files.put(new Identifier(FRAGMENT), vanillaResource(FRAGMENT));
        assertFalse(compatible(files));
        files.putAll(compatibleFiles());
        assertTrue(compatible(files));
    }

    @Test
    public void rejectsOtherProgramAndStageNamesWithoutReadingResources() {
        final ResourceFactory unexpectedRead = id -> {
            throw new AssertionError("Unexpected resource read: " + id);
        };
        assertFalse(KnownHudShaders.isCompatible("rendertype_text", PROGRAM, PROGRAM, unexpectedRead));
        assertFalse(KnownHudShaders.isCompatible(PROGRAM, "custom", PROGRAM, unexpectedRead));
        assertFalse(KnownHudShaders.isCompatible(PROGRAM, PROGRAM, "custom", unexpectedRead));
    }

    @Test
    public void fallsBackWhenResourcesCannotBeRead() {
        final ResourceFactory unreadable = id -> Optional.of(new Resource(null, () -> {
            throw new IOException("unreadable test resource");
        }));
        assertFalse(KnownHudShaders.isCompatible(PROGRAM, PROGRAM, PROGRAM, unreadable));
        final ResourceFactory denied = id -> {
            throw new SecurityException("denied test resource");
        };
        assertFalse(KnownHudShaders.isCompatible(PROGRAM, PROGRAM, PROGRAM, denied));
    }

    private static boolean compatible(final Map<Identifier, byte[]> files) {
        final ResourceFactory factory = id -> Optional.ofNullable(files.get(id)).map(bytes -> new Resource(null, () -> new ByteArrayInputStream(bytes)));
        return KnownHudShaders.isCompatible(PROGRAM, PROGRAM, PROGRAM, factory);
    }

    private static Map<Identifier, byte[]> compatibleFiles() throws IOException {
        final Map<Identifier, byte[]> files = new HashMap<>();
        for (String path : FILES) {
            files.put(new Identifier(path), vanillaResource(path));
        }

        // Recreate the Call of Yucutan 1.0.13 compatibility fixture from the vanilla
        // resource supplied by Minecraft on the test classpath. No game assets are
        // bundled in this project. Whitespace is intentional: the check is byte-exact.
        final String vanilla = new String(files.get(new Identifier(FRAGMENT)), StandardCharsets.UTF_8);
        final String emissive = vanilla.substring(0, vanilla.indexOf("void main()"))
            + "#define EQ(a,b) (abs(a - b) < 0.002)\n\n"
            + "void main() {\n"
            + "   vec4 texColor = texture(Sampler0, texCoord0);\n"
            + "   vec4 color = texColor * vertexColor * ColorModulator;\n"
            + "   \n"
            + "   if (color.a < 0.1) {\n"
            + "      discard;\n"
            + "   }\n\n"
            + "   color = EQ(color.a, 254.0/255.0) ? texColor : color;\n"
            + "   fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);\n"
            + "}";
        files.put(new Identifier(FRAGMENT), emissive.getBytes(StandardCharsets.UTF_8));
        return files;
    }

    private static byte[] vanillaResource(final String path) throws IOException {
        try (InputStream input = KnownHudShadersTest.class.getResourceAsStream("/assets/minecraft/" + path)) {
            assertNotNull("Missing Minecraft test resource: " + path, input);
            return input.readAllBytes();
        }
    }

}
