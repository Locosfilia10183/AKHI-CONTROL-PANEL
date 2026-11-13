package de.akhi.control.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class AKHIControlKeybind implements ClientModInitializer {
    private static KeyBinding openMenu;

    @Override public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.akhi_control_keybind.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.akhi_control_keybind"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.player != null && client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendChatCommand("menu");
                }
            }
        });
    }
}
