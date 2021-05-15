/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.ui;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.Container;

public abstract class FakeGuiContainer extends ContainerScreen {

    public FakeGuiContainer(Container container) {
        super(container);
    }

}
