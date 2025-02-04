package me.eigenraven.lwjgl3ify.mixins.fml;

import com.google.common.base.Throwables;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
        targets = {"cpw.mods.fml.common.registry.ItemStackHolderRef"},
        remap = false)
public class ItemStackHolderRef {

    @Shadow(remap = false)
    private Field field;

    @Shadow(remap = false)
    private String itemName;

    @Shadow(remap = false)
    private int meta;

    @Shadow(remap = false)
    private String serializednbt;

    private static MethodHandle fieldSetter;

    /**
     * @author eigenraven
     * @reason Simple helper function
     */
    @Overwrite(remap = false)
    private static void makeWritable(Field f) {
        try {
            f.setAccessible(true);
            fieldSetter = MethodHandles.lookup().unreflectSetter(f);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * @author eigenraven
     * @reason Logic has to be significantly altered
     */
    @Overwrite
    public void apply() {
        ItemStack is;
        try {
            is = GameRegistry.makeItemStack(itemName, meta, 1, serializednbt);
        } catch (RuntimeException e) {
            FMLLog.getLogger()
                    .log(
                            Level.ERROR,
                            "Caught exception processing itemstack {},{},{} in annotation at {}.{}",
                            itemName,
                            meta,
                            serializednbt,
                            field.getClass().getName(),
                            field.getName());
            throw e;
        }
        try {
            fieldSetter.invoke(is);
        } catch (Throwable e) {
            FMLLog.getLogger()
                    .log(
                            Level.WARN,
                            "Unable to set {} with value {},{},{}",
                            this.field,
                            this.itemName,
                            this.meta,
                            this.serializednbt);
        }
    }
}
