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
package net.raphimc.immediatelyfast.neoforge;

import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.raphimc.immediatelyfast.ImmediatelyFast;

@Mod("immediatelyfast")
@EventBusSubscriber(modid = "immediatelyfast", bus = EventBusSubscriber.Bus.MOD)
public class ImmediatelyFastNeoForge {

    private static final Identifier SIGN_TEXT_CACHE_RELOAD_LISTENER_ID = Identifier.of("immediatelyfast", "sign_text_cache_reload_listener");

    @SubscribeEvent
    private static void onAddClientReloadListeners(final AddClientReloadListenersEvent event) {
        if (ImmediatelyFast.config.experimental_sign_text_buffering) {
            event.addListener(SIGN_TEXT_CACHE_RELOAD_LISTENER_ID, (SynchronousResourceReloader) manager -> ImmediatelyFast.signTextCache.reload(manager));
        }
    }

}
