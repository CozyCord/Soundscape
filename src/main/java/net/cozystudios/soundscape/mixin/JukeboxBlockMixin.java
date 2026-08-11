package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.registry.SoundscapeScreenHandlers;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = JukeboxBlock.class, priority = 1500)
public class JukeboxBlockMixin {

    private static final Component TITLE = Component.translatable("soundscape.jukebox.title");

    @Unique
    private static boolean soundscape$isDisc(ItemStack stack) {
        return stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void soundscape$openJukeboxScreen(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemStack heldItem = player.getMainHandItem();

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) {
            return;
        }

        boolean holdingDisc = soundscape$isDisc(heldItem);
        boolean hasDisc = !jukebox.getTheItem().isEmpty();

        if (player.isShiftKeyDown()) {
            return;
        }

        if (holdingDisc && !hasDisc) {
            return;
        }

        if (!holdingDisc) {
            if (!world.isClientSide()) {
                ExtendedMenuProvider<BlockPos> factory = new ExtendedMenuProvider<BlockPos>() {
                    @Override
                    public BlockPos getScreenOpeningData(ServerPlayer player) {
                        return pos;
                    }

                    @Override
                    public Component getDisplayName() {
                        return TITLE;
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerEntity) {
                        return SoundscapeScreenHandlers.createJukeboxScreenHandler(syncId, playerInventory, pos, jukebox);
                    }
                };
                player.openMenu(factory);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }


    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void soundscape$addFacingProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
        builder.add(net.cozystudios.soundscape.Soundscape.JUKEBOX_PLAYING);
    }

}
