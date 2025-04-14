package live.gunnablescum.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import live.gunnablescum.data.GraveData;
import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    // Suppressing DataFlowIssue because I know as fact that ArmorStands can be casted to IArmorStandEntityDataSaver.
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    public void dropInventory(ServerWorld world, CallbackInfo ci) {
        if(world.isClient) return;
        PlayerEntity player = (PlayerEntity) (Object) this;
        // No need to do anything if the player is not dead, since this might have been called in something like another mod.
        if(!player.isDead()) return;
        // No need to do anything if keepInventory is enabled.
        if(world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;

        NbtCompound playerData = new NbtCompound();
        player.writeCustomDataToNbt(playerData);

        PlayerInventory inventory = player.getInventory();
        if(inventory.isEmpty() && playerData.getCompound("equipment").isEmpty()) return;

        double y = player.getY();
        boolean isExtendedHeightLimit = !world.isOutOfHeightLimit(-64);
        y = Math.max(y, isExtendedHeightLimit ? -59 : 5);
        y = Math.min(y, isExtendedHeightLimit ? 314 : 250);

        ArmorStandEntity armorStand = new ArmorStandEntity(world, player.getX(), y, player.getZ());

        // Feature-Implementation for issue #1, Check GraveGlowPacketMixin.java.
        armorStand.setGlowing(false);


        armorStand.setNoGravity(true);
        armorStand.setShowArms(true);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName(createGraveText(player.getName().getLiteralString()));
        armorStand.setCustomNameVisible(true);
        armorStand.setHideBasePlate(true);
        armorStand.setLeftArmRotation(new EulerAngle(311.0f, 37.0f, 0.0f));
        armorStand.setRightArmRotation(new EulerAngle(336.0f, 323.0f, 0.0f));
        armorStand.setHeadRotation(new EulerAngle(37.0f, 0.0f, 0.0f));

        NbtCompound data = new NbtCompound();
        armorStand.writeCustomDataToNbt(data);
        data.put("DisabledSlots", NbtInt.of(4144959));
        armorStand.readCustomDataFromNbt(data);

        ItemStack[] equipment = getGraveEquipment();

        armorStand.equipStack(EquipmentSlot.HEAD, equipment[0]);
        armorStand.equipStack(EquipmentSlot.CHEST, equipment[1]);
        armorStand.equipStack(EquipmentSlot.LEGS, equipment[2]);
        armorStand.equipStack(EquipmentSlot.FEET, equipment[3]);
        armorStand.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_HOE));

        GraveData.setOwnerUniqueId((IArmorStandEntityDataSaver) armorStand, player.getUuidAsString());
        GraveData.setInventory((IArmorStandEntityDataSaver) armorStand, player.getInventory().writeNbt(new NbtList()));

        Optional<NbtCompound> equipmentData = playerData.getCompound("equipment");
        equipmentData.ifPresent(nbtCompound -> GraveData.setEquipment((IArmorStandEntityDataSaver) armorStand, nbtCompound));
        world.spawnEntity(armorStand);
        ci.cancel();
    }

    @Unique
    private MutableText createGraveText(String playerName) {
        MutableText text = Text.literal(playerName + "'s Grave");
        text.fillStyle(text.getStyle().withBold(true).withItalic(true).withFormatting(Formatting.DARK_RED));
        return text;
    }

    @Unique
    private ItemStack[] getGraveEquipment() {
        ItemStack[] armor = new ItemStack[3];
        armor[0] = new ItemStack(Items.LEATHER_CHESTPLATE);
        armor[1] = new ItemStack(Items.LEATHER_LEGGINGS);
        armor[2] = new ItemStack(Items.LEATHER_BOOTS);
        for(ItemStack itemStack : armor) {
            if(itemStack == null) continue;
            ComponentChanges.Builder componentChanges = ComponentChanges.builder();
            componentChanges.add(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x000000));
            itemStack.applyChanges(componentChanges.build());
        }
        ItemStack headItem = new ItemStack(Items.PLAYER_HEAD);
        ComponentChanges.Builder componentChanges = ComponentChanges.builder();
        componentChanges.add(DataComponentTypes.PROFILE, GetDeathHeadProfile());
        headItem.applyChanges(componentChanges.build());
        return new ItemStack[] {headItem, armor[0], armor[1], armor[2]};
    }

    @Unique
    private ProfileComponent GetDeathHeadProfile() {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Death");
        byte[] encoded = ("{\"textures\":{\"SKIN\":{\"url\":\"" + "http://textures.minecraft.net/texture/32aace22fc9aa11307bcdb5bbc4b6c15e1fe2d2e81ce7e0c81fd9c6b15448f79" + "\"}}}").getBytes();
        profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(encoded)));
        return new ProfileComponent(profile);
    }

}
