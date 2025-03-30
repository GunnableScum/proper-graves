package live.gunnablescum.mixin;

import live.gunnablescum.dataoverride.IArmorStandEntityDataSaver;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin implements IArmorStandEntityDataSaver {

    // I thank god for KaupenJoe's tutorial series on Entity Data Saving for this one
    // I owe you one, KaupenJoe

    private NbtCompound persistentData;

    @Override
    public NbtCompound getPersistentData() {
        if(this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }
        return persistentData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        if(this.persistentData != null) {
            nbt.put("GraveData", this.persistentData);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void injectReadMethod(NbtCompound nbt, CallbackInfo ci) {
        Optional<NbtCompound> nbtCompoundOptional = nbt.getCompound("GraveData");
        this.persistentData = nbtCompoundOptional.orElseGet(NbtCompound::new);
    }

}
