package net.cozystudios.soundscape.boombox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

//? if >=1.21.6 {
/*import net.minecraft.server.world.ServerWorld;
*///?}

//? if >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
//? if <1.21.2 {
import net.minecraft.state.property.DirectionProperty;
//?} else {
/*import net.minecraft.state.property.EnumProperty;
*///?}
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

//? if <1.20.5 {
/*import net.minecraft.util.Hand;
*///?}

import org.jetbrains.annotations.Nullable;

public class BoomboxBlock extends BlockWithEntity {

    //? if <1.21.2 {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    //?} else {
    /*public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    *///?}

    private static final VoxelShape SHAPE_NS = VoxelShapes.union(
            Block.createCuboidShape(-6, 0, 0, 22, 15, 16),
            Block.createCuboidShape(-5, 17, 7, 21, 18, 9)
    );
    private static final VoxelShape SHAPE_EW = VoxelShapes.union(
            Block.createCuboidShape(0, 0, -6, 16, 15, 22),
            Block.createCuboidShape(7, 17, -5, 9, 18, 21)
    );

    //? if >=1.20.5 {
    private static final MapCodec<BoomboxBlock> CODEC = createCodec(BoomboxBlock::new);

    @Override
    protected MapCodec<? extends BoomboxBlock> getCodec() {
        return CODEC;
    }
    //?}

    public BoomboxBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BoomboxBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    //? if >=1.21.2 {
    /*@Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
    *///?} elif >=1.20.5 {
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, BlockHitResult hit) {
    //?} else {
    /*@Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit) {
    *///?}
        if (world.isClient()) {
            net.cozystudios.soundscape.boombox.client.BoomboxScreenOpener.open(pos);
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BoomboxBlockEntity boombox) {
            boombox.syncToPlayer((ServerPlayerEntity) player);
        }
        return ActionResult.SUCCESS;
    }

    //? if >=1.21.6 {
    /*@Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BoomboxBlockEntity boombox) {
            boombox.broadcastStop();
        }
        super.onStateReplaced(state, world, pos, moved);
    }
    *///?} elif >=1.21.2 {
    /*@Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos,
                                   BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BoomboxBlockEntity boombox) {
                    boombox.broadcastStop();
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
    *///?} else {
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BoomboxBlockEntity boombox) {
                    boombox.broadcastStop();
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
    //?}

    //? if >=1.21.2 {
    /*@Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    *///?} else {
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    //?}
        Direction facing = state.get(FACING);
        return (facing == Direction.EAST || facing == Direction.WEST) ? SHAPE_EW : SHAPE_NS;
    }

    //? if >=1.21.2 {
    /*@Override
    protected VoxelShape getCullingShape(BlockState state) {
    *///?} else {
    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
    //?}
        return VoxelShapes.empty();
    }

    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }
}
