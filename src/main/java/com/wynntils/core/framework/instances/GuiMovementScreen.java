/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.instances;

import com.wynntils.McIf;
import com.wynntils.core.framework.enums.wynntils.WynntilsConflictContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class GuiMovementScreen extends Screen {
    protected boolean allowMovement = true;

    protected GuiMovementScreen() {
        super(StringTextComponent.EMPTY);
    }

   // @Override
    public void handleInput() throws IOException {
        if (!allowMovement) {
         //   super.handleInput();
            return;
        }
        // FIXME: Should probably hijack keyPresse() instead
        /*
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
                this.handleMouseInput();
            }
        }

        if (Keyboard.isCreated()) {
            while (Keyboard.next()) {

                for (KeyBinding key : McIf.mc().options.keyBindings) {
                    if (key.getKey().getValue() != Keyboard.getEventKey() || key.getKeyConflictContext() != WynntilsConflictContext.ALLOW_MOVEMENTS) continue;

                    KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
                    KeyBinding.onTick(Keyboard.getEventKey());
                    return;
                }

                this.handleKeyboardInput();
            }
        }

         */
    }

}
