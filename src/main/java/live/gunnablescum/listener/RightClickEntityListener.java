package live.gunnablescum.listener;

import live.gunnablescum.configuration.ConfigurationHandler;
import live.gunnablescum.configuration.configdatatypes.PermissableAction;
import live.gunnablescum.data.GraveData;
import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class RightClickEntityListener {

    public static void registerRightClickEvent() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(world.isClient) return ActionResult.PASS;
            if(entity instanceof ArmorStandEntity armorStand) {
                IArmorStandEntityDataSaver armorStandData = (IArmorStandEntityDataSaver) armorStand;
                String ownerUniqueId = GraveData.getOwnerUniqueId(armorStandData);
                if(ownerUniqueId != null) {
                    if (!ownerUniqueId.equals(player.getUuidAsString())) {
                        PermissableAction graveRobbingMode = ConfigurationHandler.getGraveRobbingMode();
                        switch (graveRobbingMode) {
                            case DENY:
                                player.sendMessage(Text.literal("This is not your grave!").withColor(0xFF0000), true);
                                return ActionResult.FAIL;
                            case SERVER_OPERATOR_ONLY:
                                if (!player.hasPermissionLevel(4)) {
                                    player.sendMessage(Text.literal("This is not your grave!").withColor(0xFF0000), true);
                                    return ActionResult.FAIL;
                                }
                            case ALLOW:
                                if (!player.isSneaking()) {
                                    player.sendMessage(Text.literal("This is not your grave, if you want to rob it, sneak and try again.").withColor(0x3498DB), true);
                                    return ActionResult.FAIL;
                                }
                        }
                    }
                    claimInventory(player, world, armorStand, armorStandData);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void claimInventory(PlayerEntity player, World world, ArmorStandEntity armorStand, IArmorStandEntityDataSaver armorStandData) {
        NbtList items = GraveData.getInventory(armorStandData);
        player.dropInventory((ServerWorld) world);
        player.getInventory().readNbt(items);
        NbtCompound equipment = GraveData.getEquipment(armorStandData);
        NbtCompound packed = new NbtCompound();
        packed.put("equipment", equipment);
        RegistryOps<NbtElement> registryOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);
        player.equipment.copyFrom(packed.get("equipment", EntityEquipment.CODEC, registryOps).orElseGet(EntityEquipment::new));
        armorStand.remove(Entity.RemovalReason.DISCARDED);
    }

}
