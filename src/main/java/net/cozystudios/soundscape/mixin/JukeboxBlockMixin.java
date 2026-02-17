package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.registry.SoundscapeScreenHandlers;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.block.Block;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
//?} else {
/*import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.PacketByteBuf;
*///?}

//? if >=1.21 {
import net.minecraft.component.DataComponentTypes;
//?} else {
/*import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.Hand;
*///?}


@Mixin(JukeboxBlock.class)
public class JukeboxBlockMixin {

    private static final Text TITLE = Text.translatable("soundscape.jukebox.title");

    @Unique
    private static boolean soundscape$isDisc(ItemStack stack) {
        //? if >=1.21 {
        return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE);
        //?} else {
        /*return stack.getItem() instanceof MusicDiscItem;
        *///?}
    }

    //? if >=1.21 {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void soundscape$openJukeboxScreen(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldItem = player.getMainHandStack();
    //?} else {
    /*@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void soundscape$openJukeboxScreen(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldItem = player.getStackInHand(hand);
    *///?}

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) {
            return;
        }

        boolean holdingDisc = soundscape$isDisc(heldItem);
        //? if >=1.21 {
        boolean hasDisc = !jukebox.getStack(0).isEmpty();
        //?} else {
        /*boolean hasDisc = !jukebox.getStack().isEmpty();
        *///?}

        if (player.isSneaking()) {
            return;
        }

        if (holdingDisc && !hasDisc) {
            return;
        }

        if (!holdingDisc) {
            if (!world.isClient()) {
                //? if >=1.20.5 {
                ExtendedScreenHandlerFactory<BlockPos> factory = new ExtendedScreenHandlerFactory<>() {
                    @Override
                    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
                        return pos;
                    }

                    @Override
                    public Text getDisplayName() {
                        return TITLE;
                    }

                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return SoundscapeScreenHandlers.createJukeboxScreenHandler(syncId, playerInventory, pos, jukebox);
                    }
                };
                //?} else {
                /*ExtendedScreenHandlerFactory factory = new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                        buf.writeBlockPos(pos);
                    }

                    @Override
                    public Text getDisplayName() {
                        return TITLE;
                    }

                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return SoundscapeScreenHandlers.createJukeboxScreenHandler(syncId, playerInventory, pos, jukebox);
                    }
                };
                *///?}
                player.openHandledScreen(factory);
            }
            //? if >=1.21 {
            cir.setReturnValue(ActionResult.SUCCESS);
            //?} else {
            /*cir.setReturnValue(ActionResult.success(world.isClient));
            *///?}
            return;
        }
    }

    //? if <1.21.5 {
    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void soundscape$dropQueueItems(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (state.isOf(newState.getBlock())) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                ShuffleQueue queue = provider.soundscape$getShuffleQueue();
                if (queue != null) {
                    for (int i = 0; i < queue.size(); i++) {
                        ItemStack stack = queue.getStack(i);
                        if (!stack.isEmpty()) {
                            Block.dropStack(world, pos, stack);
                            queue.setStack(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
    //?}

    @Unique
    private static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 32, 16);

    //? if >=1.21.2 {
    /*protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    *///?} else {
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    //?}
        return OUTLINE_SHAPE;
    }

    //? if >=1.21.2 {
    /*protected VoxelShape getCullingShape(BlockState state) {
    *///?} else {
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
    //?}
        return VoxelShapes.empty();
    }

    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void soundscape$addFacingProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Properties.HORIZONTAL_FACING);
        builder.add(net.cozystudios.soundscape.Soundscape.JUKEBOX_PLAYING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return ((Block)(Object)this).getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
