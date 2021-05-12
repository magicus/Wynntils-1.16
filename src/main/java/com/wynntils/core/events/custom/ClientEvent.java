/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.events.custom;

import net.minecraftforge.eventbus.api.Event;

public class ClientEvent extends Event {

    /**
     * Called when the client is successfully loaded
     */
    public static class Ready extends ClientEvent {

    }

}
