package live.gunnablescum.configuration;

import live.gunnablescum.configuration.enums.GlowingMode;
import live.gunnablescum.configuration.enums.PermissableAction;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationScreenHandler extends GenericContainerScreenHandler {

    public ConfigurationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, new SimpleInventory(9 * 3), 3);
        updateInventory();
    }

    private void updateInventory() {
        ItemStack[] content = new ItemStack[27];
        for (int i = 0; i < content.length; i++) {
            content[i] = Items.BLACK_STAINED_GLASS_PANE.getDefaultStack();
            ComponentChanges.Builder changes = ComponentChanges.builder();
            changes.add(DataComponentTypes.CUSTOM_NAME, Text.of(""));
            content[i].applyChanges(changes.build());
        }
        content[11] = Items.LIGHT.getDefaultStack();
        ComponentChanges.Builder changes = ComponentChanges.builder();
        changes.add(DataComponentTypes.ITEM_NAME, Text.literal("Glowing").formatted(Formatting.GOLD));
        changes.add(DataComponentTypes.LORE, new LoreComponent(
                getGlowingModeLore(
                        ConfigurationHandler.getGlowingMode(),
                        "Toggle this option to change the glowing of Graves."
                )
        ));
        content[11].applyChanges(changes.build());

        content[15] = Items.NETHERITE_SHOVEL.getDefaultStack();
        changes = ComponentChanges.builder();
        changes.add(DataComponentTypes.ITEM_NAME, Text.literal("Graverobbing").formatted(Formatting.GOLD));
        changes.add(DataComponentTypes.LORE, new LoreComponent(
                getPermissableActionLore(
                        ConfigurationHandler.getGraveRobbingMode(),
                        "Toggle this option to enable or disable",
                        "robbing the graves of other players."
                )
        ));
        content[15].applyChanges(changes.build());

        for(int i = 0; i < content.length; i++) {
            this.slots.get(i).setStack(content[i]);
        }
    }

    private List<Text> getGlowingModeLore(GlowingMode status, String... loreText) {
        List<Text> lore = new ArrayList<>();
        for(String str : loreText) {
            lore.add(Text.literal(str).formatted(Formatting.GRAY));
        }

        lore.add(Text.literal("Status:").formatted(Formatting.GRAY));

        for(GlowingMode glowingMode : GlowingMode.values()) {
            MutableText mode = Text.literal(glowingMode.getName()).formatted(glowingMode.getColor());
            mode.fillStyle(mode.getStyle().withUnderline(status == glowingMode));
            lore.add(mode);
        }

        return lore;
    }

    private List<Text> getPermissableActionLore(PermissableAction status, String... loreText) {
        List<Text> lore = new ArrayList<>();
        for(String str : loreText) {
            lore.add(Text.literal(str).formatted(Formatting.GRAY));
        }

        lore.add(Text.literal("Status:").formatted(Formatting.GRAY));
        for(PermissableAction action : PermissableAction.values()) {
            MutableText mode = Text.literal(action.getName()).formatted(action.getColor());
            mode.fillStyle(mode.getStyle().withUnderline(status == action));
            lore.add(mode);
        }

        return lore;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        // Edge case - Player gets deopped while in the config screen
        if(!player.hasPermissionLevel(4)) {
            super.onClosed(player);
            return;
        }

        for(PlayerEntity operator : player.getServer().getPlayerManager().getPlayerList()) {
            if(!operator.hasPermissionLevel(4)) continue;
            operator.sendMessage(
                    Text.literal("[")
                            .append(player.getDisplayName())
                            .append(": Changed Proper-Graves Config]")
                            .fillStyle(Style.EMPTY
                                    .withFormatting(Formatting.GRAY)
                                    .withItalic(true)
                            ),
                    false
            );
        }

        ConfigurationHandler.saveConfig();
        super.onClosed(player);
    }

    @Override
    public void onSlotClick(int slotId, int button, SlotActionType actionType, PlayerEntity player) {
        // Edge case - Player gets deopped while in the config screen
        if(!player.hasPermissionLevel(4)) return;

        switch (slotId) {
            case 11:
                GlowingMode newGlowMode = ConfigurationHandler.getGlowingMode() == GlowingMode.ENABLED ? GlowingMode.OWNER_ONLY : ConfigurationHandler.getGlowingMode() == GlowingMode.OWNER_ONLY ? GlowingMode.DISABLED : GlowingMode.ENABLED;
                ConfigurationHandler.setGlowingMode(newGlowMode);
                break;
            case 15:
                PermissableAction newPermissableMode = ConfigurationHandler.getGraveRobbingMode() == PermissableAction.ALLOW ? PermissableAction.SERVER_OPERATOR_ONLY : ConfigurationHandler.getGraveRobbingMode() == PermissableAction.SERVER_OPERATOR_ONLY ? PermissableAction.DENY : PermissableAction.ALLOW;
                ConfigurationHandler.setGraveRobbingMode(newPermissableMode);
                break;
        }
        updateInventory();
    }
}
