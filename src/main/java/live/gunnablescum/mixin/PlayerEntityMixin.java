package live.gunnablescum.mixin;

import live.gunnablescum.configuration.ConfigurationHandler;
import live.gunnablescum.configuration.configdatatypes.ArmorStandDesign;
import live.gunnablescum.data.GraveData;
import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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

import java.util.Map;
import java.util.Optional;

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

        ArmorStandDesign armorStandDesign = ConfigurationHandler.getArmorStandDesign().deserialize();
        applyPose(armorStand, armorStandDesign.getPose());

        NbtCompound data = new NbtCompound();
        armorStand.writeCustomDataToNbt(data);
        data.put("DisabledSlots", NbtInt.of(4144959));
        armorStand.readCustomDataFromNbt(data);

        applyEquipment(armorStand, armorStandDesign.getEquipment());

        GraveData.setOwnerUniqueId((IArmorStandEntityDataSaver) armorStand, player.getUuidAsString());
        GraveData.setInventory((IArmorStandEntityDataSaver) armorStand, player.getInventory().writeNbt(new NbtList()));

        Optional<NbtCompound> equipmentData = playerData.getCompound("equipment");
        equipmentData.ifPresent(nbtCompound -> GraveData.setEquipment((IArmorStandEntityDataSaver) armorStand, nbtCompound));
        world.spawnEntity(armorStand);
        ci.cancel();
    }

    @Unique
    private void applyEquipment(ArmorStandEntity armorStand, Map<String, ItemStack> equipment) {
        for(Map.Entry<String, ItemStack> entry : equipment.entrySet()) {
            String part = entry.getKey();
            ItemStack itemStack = entry.getValue();

            switch (part) {
                case "head" -> armorStand.equipStack(EquipmentSlot.HEAD, itemStack);
                case "chestplate" -> armorStand.equipStack(EquipmentSlot.CHEST, itemStack);
                case "leggings" -> armorStand.equipStack(EquipmentSlot.LEGS, itemStack);
                case "boots" -> armorStand.equipStack(EquipmentSlot.FEET, itemStack);
                case "mainHand" -> armorStand.equipStack(EquipmentSlot.MAINHAND, itemStack);
            }
        }
    }

    @Unique
    private void applyPose(ArmorStandEntity armorStand, Map<String, EulerAngle> pose) {
        for(Map.Entry<String, EulerAngle> entry : pose.entrySet()) {
            String part = entry.getKey();
            EulerAngle angle = entry.getValue();

            switch (part) {
                case "head" -> armorStand.setHeadRotation(angle);
                case "body" -> armorStand.setBodyRotation(angle);
                case "leftArm" -> armorStand.setLeftArmRotation(angle);
                case "rightArm" -> armorStand.setRightArmRotation(angle);
                case "leftLeg" -> armorStand.setLeftLegRotation(angle);
                case "rightLeg" -> armorStand.setRightLegRotation(angle);
            }
        }
    }

    @Unique
    private MutableText createGraveText(String playerName) {
        MutableText text = Text.literal(playerName + "'s Grave");
        text.fillStyle(text.getStyle().withBold(true).withItalic(true).withFormatting(Formatting.DARK_RED));
        return text;
    }
}
