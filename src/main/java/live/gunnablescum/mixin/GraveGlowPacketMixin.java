package live.gunnablescum.mixin;

import live.gunnablescum.configuration.ConfigurationHandler;
import live.gunnablescum.configuration.configdatatypes.GlowingMode;
import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerCommonNetworkHandler.class)
public class GraveGlowPacketMixin {

    @Inject(method = "send", at = @At("HEAD"))
    private void modifyGraveGlowPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if(packet instanceof EntityS2CPacket entityPacket) {

            ServerPlayNetworkHandler serverPlayNetworkHandler = (ServerPlayNetworkHandler) (Object) this;
            ServerWorld world = serverPlayNetworkHandler.getPlayer().getServerWorld();

            // Get the entity from the packet
            Entity entity = entityPacket.getEntity(world);
            if(!(entity instanceof ArmorStandEntity armorStand)) return;

            // Check if the entity is a grave
            IArmorStandEntityDataSaver graveData = (IArmorStandEntityDataSaver) armorStand;
            NbtCompound persistentData = graveData.getPersistentData();
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            if(!persistentData.contains("OwnerUUID")) return;

            // Prepare a glow packet
            EntityTrackerUpdateS2CPacket glowingPacket = new EntityTrackerUpdateS2CPacket(
                    armorStand.getId(),
                    List.of(DataTracker.SerializedEntry.of(Entity.FLAGS, (byte) 0x60))
            );

            GlowingMode glowingMode = ConfigurationHandler.getGlowingMode();

            switch (glowingMode) {
                case DISABLED:
                    return;
                case OWNER_ONLY:
                    if(persistentData.getString("OwnerUUID").isPresent()) {
                        // Get the Owner UUID from the persistent data
                        String ownerUUIDString = persistentData.getString("OwnerUUID").get();
                        if (!player.getUuidAsString().equalsIgnoreCase(ownerUUIDString)) {
                            return;
                        }
                    } else return;
                case ENABLED:
                    serverPlayNetworkHandler.sendPacket(glowingPacket);
            }
        }
    }

}
