package live.gunnablescum.configuration.configdatatypes;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.EulerAngle;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SerializedArmorStandDesign {
    private final Map<String, String> equipment;
    private final Map<String, float[]> pose;

    public SerializedArmorStandDesign(Map<String, String> equipment, Map<String, float[]> pose) {
        this.equipment = equipment;
        this.pose = pose;
    }

    public ArmorStandDesign deserialize() {
        Map<String, ItemStack> equipmentMap = new HashMap<>();
        Map<String, EulerAngle> poseMap = new HashMap<>();

        for(Map.Entry<String, String> entry : equipment.entrySet()) {
            String equipmentSlot = entry.getKey();
            String serializedItem = entry.getValue();

            equipmentMap.put(equipmentSlot, deserializeItem(serializedItem));
        }

        for(Map.Entry<String, float[]> entry : pose.entrySet()) {
            String posePart = entry.getKey();
            float[] serializedPose = entry.getValue();

            EulerAngle angle = new EulerAngle(
                    serializedPose[0],
                    serializedPose[1],
                    serializedPose[2]
            );
            poseMap.put(posePart, angle);
        }

        return new ArmorStandDesign(equipmentMap, poseMap);
    }

    private ItemStack deserializeItem(String serializedItem) {
        if(serializedItem == null || serializedItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String item_id;
        String item_data = "";

        if(serializedItem.contains("["))  {
            String[] data = serializedItem.split("\\[");
            item_id = data[0];
            item_data = data[1].substring(0, data[1].length() - 1);
        } else {
            item_id = serializedItem;
        }

        Identifier id = Identifier.of(item_id);
        Item item = Registries.ITEM.get(id);

        if(item_data.isEmpty()) return item.getDefaultStack();

        ComponentChanges.Builder changes = ComponentChanges.builder();

        String[] flags = item_data.split(",");
        for(String flag : flags) {
            String[] flagData = flag.split("=");
            if(flagData.length != 2) continue;

            String flagName = flagData[0];
            String flagValue = flagData[1];

            if(flagName.equalsIgnoreCase("texture_url")) {
                changes.add(DataComponentTypes.PROFILE, GetHeadProfile(flagValue));
                continue;
            }

            if(flagName.equalsIgnoreCase("dyed_color")) {
                int color = Integer.parseInt(flagValue);
                changes.add(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
            }
        }
        ItemStack itemStack = item.getDefaultStack();
        itemStack.applyChanges(changes.build());
        return itemStack;
    }

    private static ProfileComponent GetHeadProfile(String url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Death");
        byte[] encoded = ("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes();
        profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(encoded)));
        return new ProfileComponent(profile);
    }
}
