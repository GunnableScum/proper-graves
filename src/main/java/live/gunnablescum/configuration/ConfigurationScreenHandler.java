package live.gunnablescum.configuration;

import live.gunnablescum.configuration.enums.GlowingMode;
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
                getStatusLore(
                        ConfigurationHandler.getGlowingMode(),
                        "Toggle this option to change the glowing of Graves."
                )
        ));
        content[11].applyChanges(changes.build());

        content[15] = Items.NETHERITE_SHOVEL.getDefaultStack();
        changes = ComponentChanges.builder();
        changes.add(DataComponentTypes.ITEM_NAME, Text.literal("Graverobbing").formatted(Formatting.GOLD));
        changes.add(DataComponentTypes.LORE, new LoreComponent(
                getTBDLore(
                        "Toggle this option to enable or disable",
                        "robbing the graves of other players."
                )
        ));
        content[15].applyChanges(changes.build());

        for(int i = 0; i < content.length; i++) {
            this.slots.get(i).setStack(content[i]);
        }
    }

    private List<Text> getStatusLore(GlowingMode status, String... loreText) {
        List<Text> lore = new ArrayList<>();
        for(String str : loreText) {
            lore.add(Text.literal(str).formatted(Formatting.GRAY));
        }

        lore.add(Text.literal("Status:").formatted(Formatting.GRAY));

        MutableText enabled = Text.literal("Enabled").formatted(Formatting.GREEN);
        enabled.fillStyle(enabled.getStyle().withUnderline(status == GlowingMode.ENABLED));
        lore.add(enabled);

        MutableText grave_owner_only = Text.literal("Owner Only").formatted(Formatting.YELLOW);
        grave_owner_only.fillStyle(grave_owner_only.getStyle().withUnderline(status == GlowingMode.OWNER_ONLY));
        lore.add(grave_owner_only);

        MutableText disabled = Text.literal("Disabled").formatted(Formatting.RED);
        disabled.fillStyle(disabled.getStyle().withUnderline(status == GlowingMode.DISABLED));
        lore.add(disabled);

        return lore;
    }

    private List<Text> getTBDLore(String... loreText) {
        List<Text> lore = new ArrayList<>();
        for(String str : loreText) {
            lore.add(Text.literal(str).formatted(Formatting.GRAY));
        }

        MutableText disabled = Text.literal("To be implemented!").formatted(Formatting.RED);
        disabled.fillStyle(disabled.getStyle().withUnderline(true));
        lore.add(disabled);

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
            if(operator.getPermissionLevel() != 4) continue;
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
        if(player.getPermissionLevel() != 4) {
            return;
        }

        switch (slotId) {
            case 11:
                GlowingMode newMode = ConfigurationHandler.getGlowingMode() == GlowingMode.ENABLED ? GlowingMode.OWNER_ONLY : ConfigurationHandler.getGlowingMode() == GlowingMode.OWNER_ONLY ? GlowingMode.DISABLED : GlowingMode.ENABLED;
                ConfigurationHandler.setGlowingMode(newMode);
                break;
            case 15:
                player.sendMessage(Text.literal("Not available yet!").fillStyle(Style.EMPTY.withFormatting(Formatting.RED)), false);
                break;
        }
        updateInventory();
    }
}
