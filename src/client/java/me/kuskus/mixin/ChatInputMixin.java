package me.kuskus.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatScreen.class)
public class ChatInputMixin {
    // Chat interception is registered through Fabric ClientSendMessageEvents in KusKusKlient.
}
