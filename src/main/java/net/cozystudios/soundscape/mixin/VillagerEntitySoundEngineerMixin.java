package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.registry.SoundscapeItems;
import net.cozystudios.soundscape.villager.SoundscapeTrades;
import net.cozystudios.soundscape.villager.SoundscapeVillagers;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntitySoundEngineerMixin {

    @Shadow private int levelUpTimer;
    @Shadow private boolean levelingUp;

    @Invoker("canLevelUp")
    abstract boolean soundscape$canLevelUp();

    //? if >=1.21.11 {
    /*@Invoker("levelUp")
    abstract void soundscape$levelUp(net.minecraft.server.world.ServerWorld world);
    *///?} else {
    @Invoker("levelUp")
    abstract void soundscape$levelUp();
    //?}

    @Inject(method = "afterUsing", at = @At("TAIL"))
    private void soundscape$instantLevelUp(TradeOffer offer, CallbackInfo ci) {
        VillagerEntity self = (VillagerEntity) (Object) this;
        if (!soundscape$isSoundEngineer(self)) return;
        while (soundscape$canLevelUp()) {
            //? if >=1.21.11 {
            /*soundscape$levelUp((net.minecraft.server.world.ServerWorld) self.getEntityWorld());
            *///?} else {
            soundscape$levelUp();
            //?}
        }
        this.levelUpTimer = 0;
        this.levelingUp = false;
    }

    @Inject(method = "interactMob", at = @At("HEAD"))
    private void soundscape$ensureShardsFirst(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        VillagerEntity self = (VillagerEntity) (Object) this;
        if (self.getEntityWorld().isClient()) return;
        if (!soundscape$isSoundEngineer(self)) return;
        TradeOfferList offers = self.getOffers();
        for (TradeOffer existing : offers) {
            if (existing.getSellItem().isOf(SoundscapeItems.AMBIENCE_DISC_SHARD)) return;
        }
        offers.add(0, SoundscapeTrades.createShardsOffer(self.getRandom()));
    }

    private static boolean soundscape$isSoundEngineer(VillagerEntity self) {
        //? if >=1.21.11 {
        /*return self.getVillagerData().profession().matchesKey(SoundscapeVillagers.SOUND_ENGINEER_KEY);
        *///?} else {
        return self.getVillagerData().getProfession() == SoundscapeVillagers.SOUND_ENGINEER;
        //?}
    }
}
