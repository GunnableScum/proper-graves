package live.gunnablescum.data;

import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GraveData {

    @Nullable
    public static String getOwnerUniqueId(IArmorStandEntityDataSaver armorStand) {
        NbtCompound data = armorStand.getPersistentData();
        Optional<String> optionalOwner = data.getString("OwnerUUID");
        return optionalOwner.orElse(null);
    }

    public static void setOwnerUniqueId(IArmorStandEntityDataSaver armorStand, String ownerUniqueId) {
        NbtCompound data = armorStand.getPersistentData();
        data.putString("OwnerUUID", ownerUniqueId);
    }

    public static NbtList getInventory(IArmorStandEntityDataSaver armorStand) {
        NbtCompound data = armorStand.getPersistentData();
        Optional<NbtList> optionalInventory = data.getList("Inventory");
        return optionalInventory.orElseGet(NbtList::new);
    }

    public static void setInventory(IArmorStandEntityDataSaver armorStand, NbtList inventory) {
        NbtCompound data = armorStand.getPersistentData();
        data.put("Inventory", inventory);
    }

    public static NbtCompound getEquipment(IArmorStandEntityDataSaver armorStand) {
        NbtCompound data = armorStand.getPersistentData();
        Optional<NbtCompound> optionalInventory = data.getCompound("Equipment");
        return optionalInventory.orElseGet(NbtCompound::new);
    }

    public static void setEquipment(IArmorStandEntityDataSaver armorStand, NbtCompound player) {
        NbtCompound data = armorStand.getPersistentData();
        data.put("Equipment", player);
    }

}
