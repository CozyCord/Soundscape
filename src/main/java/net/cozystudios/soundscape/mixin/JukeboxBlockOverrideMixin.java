package net.cozystudios.soundscape.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class JukeboxBlockOverrideMixin {

    @Unique
    private static final VoxelShape SOUNDSCAPE$SHAPE_NS = Block.box(-3, 0, 2, 19, 29, 14);
    @Unique
    private static final VoxelShape SOUNDSCAPE$SHAPE_EW = Block.box(2, 0, -3, 14, 29, 19);
    @Unique
    private static final VoxelShape SOUNDSCAPE$COLLISION_SHAPE = Block.box(0, 0, 0, 16, 32, 16);

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(state.getBlock() instanceof JukeboxBlock)) return;
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        cir.setReturnValue((facing == Direction.EAST || facing == Direction.WEST) ? SOUNDSCAPE$SHAPE_EW : SOUNDSCAPE$SHAPE_NS);
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(SOUNDSCAPE$COLLISION_SHAPE);
        }
    }

    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxOcclusionShape(BlockState state, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    @Inject(method = "useShapeForLightOcclusion", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxLightOcclusion(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof JukeboxBlock) {
            cir.setReturnValue(true);
        }
    }
}
