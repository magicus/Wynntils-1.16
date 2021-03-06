/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.events.custom;

import net.minecraftforge.eventbus.api.Event;

public class WynnTerritoryChangeEvent extends Event {

    String oldTerritory, newTerritory;

    public WynnTerritoryChangeEvent(String oldTerritory, String newTerritory) {
        this.oldTerritory = oldTerritory; this.newTerritory = newTerritory;
    }

    public String getNewTerritory() {
        return newTerritory;
    }

    public String getOldTerritory() {
        return oldTerritory;
    }

}
