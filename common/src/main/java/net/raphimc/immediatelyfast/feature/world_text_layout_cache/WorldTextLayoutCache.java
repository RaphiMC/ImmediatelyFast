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
package net.raphimc.immediatelyfast.feature.world_text_layout_cache;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorldTextLayoutCache {

    private static final int MAX_ENTRIES = 4096;
    private static final long MAX_UNUSED_FRAMES = 200L;

    private static final Map<Key, Entry> ENTRIES = new HashMap<>();
    private static final Fingerprint FINGERPRINT = new Fingerprint();
    private static final ClassValue<Field[]> CAPTURED_ARGUMENTS = new ClassValue<>() {
        @Override
        protected Field[] computeValue(final Class<?> type) {
            if (type.getNestHost() != FormattedCharSequence.class) {
                return null;
            }
            try {
                final Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                }
                return fields;
            } catch (Throwable t) {
                return null;
            }
        }
    };
    private static long frame;
    private static long lastEvictionFrame = -1L;

    public static Font.PreparedText prepareText(final Font font, final FormattedCharSequence text, final float x, final float y, final int color, final boolean dropShadow, final int backgroundColor) {
        return getOrPrepare(new Key(font, text, x, y, color, dropShadow, backgroundColor, false));
    }

    public static Font.PreparedText prepare8xTextOutline(final Font font, final FormattedCharSequence text, final float x, final float y, final int outlineColor) {
        return getOrPrepare(new Key(font, text, x, y, outlineColor, false, 0, true));
    }

    public static void onFrameStart() {
        frame++;
        if (frame % MAX_UNUSED_FRAMES == 0) {
            ENTRIES.values().removeIf(entry -> frame - entry.lastUsedFrame > MAX_UNUSED_FRAMES);
        }
    }

    public static void clear() {
        ENTRIES.clear();
    }

    private static Font.PreparedText getOrPrepare(final Key key) {
        if (key.text instanceof UncachedText || !RenderSystem.isOnRenderThread()) {
            return prepare(key);
        }
        final Entry entry = ENTRIES.get(key);
        if (entry == null) {
            if (hasSpace()) {
                ENTRIES.put(key, new Entry(frame));
            }
            return prepare(key);
        }
        entry.lastUsedFrame = frame;
        if (entry.immutable) {
            return entry.preparedText != null ? entry.preparedText : prepare(key);
        }
        final boolean cacheable = FINGERPRINT.compute(key.text);
        if (!entry.checkedImmutable) {
            entry.checkedImmutable = true;
            entry.immutable = isImmutable(key.text);
        }
        if (!cacheable) {
            return prepare(key);
        }
        final long fingerprint = FINGERPRINT.hash;
        if (entry.preparedText != null && entry.fingerprint == fingerprint) {
            return entry.preparedText;
        }
        entry.preparedText = prepare(key);
        entry.fingerprint = fingerprint;
        return entry.preparedText;
    }

    private static Font.PreparedText prepare(final Key key) {
        if (key.outline) {
            return key.font.prepare8xTextOutline(key.text, key.x, key.y, key.color);
        } else {
            return key.font.prepareText(key.text, key.x, key.y, key.color, key.dropShadow, false, key.backgroundColor);
        }
    }

    private static boolean isImmutable(final Object value) {
        if (value instanceof String || value instanceof Style || value instanceof Integer) {
            return true;
        }
        if (value instanceof ImmutableList<?> list) {
            for (Object element : list) {
                if (!isImmutable(element)) {
                    return false;
                }
            }
            return true;
        }
        if (!(value instanceof FormattedCharSequence)) {
            return false;
        }
        final Field[] capturedArguments = CAPTURED_ARGUMENTS.get(value.getClass());
        if (capturedArguments == null) {
            return false;
        }
        try {
            for (Field field : capturedArguments) {
                if (!Modifier.isStatic(field.getModifiers()) && !isImmutable(field.get(value))) {
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasSpace() {
        if (ENTRIES.size() >= MAX_ENTRIES && lastEvictionFrame != frame) {
            lastEvictionFrame = frame;
            ENTRIES.values().removeIf(entry -> frame - entry.lastUsedFrame > 1);
        }
        return ENTRIES.size() < MAX_ENTRIES;
    }

    public record UncachedText(FormattedCharSequence delegate) implements FormattedCharSequence {

        @Override
        public boolean accept(final FormattedCharSink sink) {
            return this.delegate.accept(sink);
        }

    }

    private record Key(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, int backgroundColor, boolean outline) {
    }

    private static final class Entry {

        private boolean checkedImmutable;
        private boolean immutable;
        private Font.PreparedText preparedText;
        private long fingerprint;
        private long lastUsedFrame;

        private Entry(final long lastUsedFrame) {
            this.lastUsedFrame = lastUsedFrame;
        }

    }

    private static final class Fingerprint implements FormattedCharSink {

        private long hash;
        private Style lastStyle;
        private int lastStyleHash;

        private boolean compute(final FormattedCharSequence text) {
            this.hash = 0L;
            this.lastStyle = null;
            final boolean cacheable = text.accept(this);
            this.lastStyle = null;
            return cacheable;
        }

        @Override
        public boolean accept(final int position, final Style style, final int codePoint) {
            if (style != this.lastStyle) {
                if (style.isObfuscated() || !(style.getFont() instanceof FontDescription.Resource)) {
                    return false;
                }
                this.lastStyle = style;
                int styleHash = (style.isBold() ? 1 : 0) | (style.isItalic() ? 2 : 0) | (style.isUnderlined() ? 4 : 0) | (style.isStrikethrough() ? 8 : 0);
                styleHash = 31 * styleHash + Objects.hashCode(style.getColor());
                styleHash = 31 * styleHash + Objects.hashCode(style.getShadowColor());
                this.lastStyleHash = 31 * styleHash + style.getFont().hashCode();
            }
            this.hash = (this.hash ^ ((long) position << 32 | codePoint)) * 0x9E3779B97F4A7C15L;
            this.hash = (this.hash ^ this.lastStyleHash) * 0x9E3779B97F4A7C15L;
            this.hash ^= this.hash >>> 32;
            return true;
        }

    }

}
