package net.cozystudios.soundscape.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class JukeboxBlockOverrideMixin {

    @Unique
    private static final VoxelShape SOUNDSCAPE$SHAPE_NS = Block.createCuboidShape(-3, 0, 2, 19, 29, 14);
    @Unique
    private static final VoxelShape SOUNDSCAPE$SHAPE_EW = Block.createCuboidShape(2, 0, -3, 14, 29, 19);
    @Unique
    private static final VoxelShape SOUNDSCAPE$COLLISION_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 32, 16);

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(state.getBlock() instanceof JukeboxBlock)) return;
        if (!state.contains(Properties.HORIZONTAL_FACING)) return;
        Direction facing = state.get(Properties.HORIZONTAL_FACING);
        cir.setReturnValue((facing == Direction.EAST || facing == Direction.WEST) ? SOUNDSCAPE$SHAPE_EW : SOUNDSCAPE$SHAPE_NS);
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(SOUNDSCAPE$COLLISION_SHAPE);
        }
    }

    //? if >=1.21.4 {
    /*@Inject(method = "getCullingShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxCullingShape(BlockState state, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }
    *///?} else {
    @Inject(method = "getCullingShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxCullingShape(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }
    //?}

    @Inject(method = "hasSidedTransparency", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxSidedTransparency(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(true);
        }
    }
}
