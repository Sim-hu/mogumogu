package com.example.mogumogu.mixin;

import com.example.mogumogu.client.MogumoguClient;
import com.example.mogumogu.client.MogumoguConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemUseMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        MogumoguConfig config = MogumoguClient.getConfig();

        if (config.isBlacklisted(stack.getItem())) {
            // ブラックリストにあるアイテムは使用不可にする
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
} 
