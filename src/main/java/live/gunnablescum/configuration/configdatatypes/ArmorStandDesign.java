package live.gunnablescum.configuration.configdatatypes;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.EulerAngle;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorStandDesign {

    private final Map<String, ItemStack> equipment;
    private final Map<String, EulerAngle> pose;

    public ArmorStandDesign(Map<String, ItemStack> equipment, Map<String, EulerAngle> pose) {
        this.equipment = equipment;
        this.pose = pose;
    }

    public static ArmorStandDesign getDefault() {
        Map<String, EulerAngle> pose = Map.of(
                "head", new EulerAngle(37.0f, 0.0f, 0.0f),
                "body", new EulerAngle(0.0f, 0.0f, 0.0f),
                "leftArm", new EulerAngle(311.0f, 37.0f, 0.0f),
                "rightArm", new EulerAngle(336.0f, 323.0f, 0.0f),
                "leftLeg", new EulerAngle(0, 0, 0),
                "rightLeg", new EulerAngle(0, 0, 0)
        );
        ItemStack[] defaultEquipment = getDefaultGraveEquipment();
        Map<String, ItemStack> equipment = Map.of(
                "head", defaultEquipment[0],
                "chestplate", defaultEquipment[1],
                "leggings", defaultEquipment[2],
                "boots", defaultEquipment[3],
                "mainHand", Items.NETHERITE_HOE.getDefaultStack()
        );
        MutableText name = Text.literal("%s's Grave");
        name.fillStyle(name.getStyle().withBold(true).withItalic(true).withFormatting(Formatting.DARK_RED));
        return new ArmorStandDesign(equipment, pose);
    }

    public SerializedArmorStandDesign serialize() {
        Map<String, String> serializedEquipment = new HashMap<>();
        Map<String, float[]> serializedPose = new HashMap<>();

        for (Map.Entry<String, ItemStack> entry : equipment.entrySet()) {
            String key = entry.getKey();
            ItemStack itemStack = entry.getValue();
            Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
            ComponentMap components = itemStack.getComponents();
            String extraData = "";
            if(components.get(DataComponentTypes.DYED_COLOR) != null) {
                DyedColorComponent color = components.get(DataComponentTypes.DYED_COLOR);
                extraData += "dyed_color=" + color.rgb() + ",";
            }
            if(components.get(DataComponentTypes.PROFILE) != null) {
                ProfileComponent profile = components.get(DataComponentTypes.PROFILE);
                String textures = profile.properties().asMap().get("textures").stream().findFirst().orElse(null).value();
                extraData += "texture_url=" + base64toURL(textures) + ",";
            }

            String finalSerializedItemStack = itemId.toString();
            if(!extraData.isEmpty()) {
                finalSerializedItemStack += "[" + extraData.substring(0, extraData.length() - 1) + "]";
            }

            serializedEquipment.put(key, finalSerializedItemStack);
        }

        for (Map.Entry<String, EulerAngle> entry : pose.entrySet()) {
            String key = entry.getKey();
            EulerAngle angle = entry.getValue();
            serializedPose.put(key, new float[]{angle.pitch(), angle.yaw(), angle.roll()});
        }

        return new SerializedArmorStandDesign(serializedEquipment, serializedPose);
    }

    private static ItemStack[] getDefaultGraveEquipment() {
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
        componentChanges.add(DataComponentTypes.PROFILE, GetDefaultHeadProfile());
        headItem.applyChanges(componentChanges.build());
        return new ItemStack[] {headItem, armor[0], armor[1], armor[2]};
    }

    public Map<String, EulerAngle> getPose() {
        return pose;
    }

    public Map<String, ItemStack> getEquipment() {
        return equipment;
    }

    private static ProfileComponent GetDefaultHeadProfile() {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Death");
        byte[] encoded = ("{\"textures\":{\"SKIN\":{\"url\":\"" + "http://textures.minecraft.net/texture/32aace22fc9aa11307bcdb5bbc4b6c15e1fe2d2e81ce7e0c81fd9c6b15448f79" + "\"}}}").getBytes();
        profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(encoded)));
        return new ProfileComponent(profile);
    }

    public static String base64toURL(String textures) {
        byte[] decoded = Base64.getDecoder().decode(textures.getBytes());
        String url = new String(decoded);
        url = url.substring(28).split("\"")[0];
        return url;
    }

}