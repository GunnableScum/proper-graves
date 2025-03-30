package live.gunnablescum.listener;

import live.gunnablescum.data.GraveData;
import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class RightClickEntityListener {

    public static void registerRightClickEvent() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(world.isClient) return ActionResult.PASS;
            if(entity instanceof ArmorStandEntity armorStand) {
                IArmorStandEntityDataSaver armorStandData = (IArmorStandEntityDataSaver) armorStand;
                String ownerUniqueId = GraveData.getOwnerUniqueId(armorStandData);
                if(ownerUniqueId != null) {
                    if(ownerUniqueId.equals(player.getUuidAsString())) {
                        NbtList items = GraveData.getInventory(armorStandData);
                        player.dropInventory((ServerWorld) world);
                        player.getInventory().readNbt(items);
                        NbtCompound equipment = GraveData.getEquipment(armorStandData);
                        NbtCompound packed = new NbtCompound();
                        packed.put("equipment", equipment);
                        RegistryOps<NbtElement> registryOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);
                        player.equipment.copyFrom(packed.get("equipment", EntityEquipment.CODEC, registryOps).orElseGet(EntityEquipment::new));
                        armorStand.remove(Entity.RemovalReason.DISCARDED);
                        return ActionResult.SUCCESS;
                    } else {
                        // TODO: Probably should make Graverobbing a configurable option, still curious about the possible PR at https://github.com/Living-Lemming/Villager-Pickup-Mod/issues/17.
                        player.sendMessage(Text.literal("This is not your grave!").withColor(0xFF0000), true);
                        return ActionResult.PASS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

}
