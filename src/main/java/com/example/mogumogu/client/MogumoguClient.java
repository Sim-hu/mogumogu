package com.example.mogumogu.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MogumoguClient implements ClientModInitializer {
    public static final String MOD_ID = "mogumogu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final MogumoguConfig CONFIG = new MogumoguConfig();

    // 自動食事システムの状態管理
    private enum AutoEatState {
        MONITORING,    // 満腹度監視中
        EATING,        // 食事実行中
        WAIT_FOR_SERVER, // サーバー反映待ち（追加）
        COMPLETING     // 食事完了確認中
    }
    
    private AutoEatState currentState = AutoEatState.MONITORING;
    private int tickCounter = 0;
    private int eatCooldown = 0;
    
    // 食事実行時の状態保存
    private int originalHotbarSlot = -1;
    private int eatingSlot = -1;
    private int hungerBeforeEating = -1;
    private int eatingTicks = 0;
    private int itemCountBeforeEating = -1;
    private boolean hasStartedEating = false;
    private static final int MAX_EATING_TICKS = 200; // 最大10秒間食事を待機
    private int waitForServerTicks = 0; // サーバー反映待ちtick
    private static final int MAX_WAIT_FOR_SERVER_TICKS = 3; // 3tick待機
    private boolean isHoldingUseKey = false; // 右クリック長押し状態

    @Override
    public void onInitializeClient() {
        CONFIG.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        LOGGER.info("もぐもぐmod initialized!");
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.isPaused()) {
            if (currentState != AutoEatState.MONITORING) {
                resetToMonitoring();
            }
            return;
        }

        if (eatCooldown > 0) {
            eatCooldown--;
        }

        // 3秒おきにチェック
        tickCounter++;
        if (tickCounter >= 60) { // 60ティック = 3秒
            tickCounter = 0;
            if (CONFIG.isEnabled() && eatCooldown == 0) {
                processAutoEat(client);
            }
        }
    }

    private void processAutoEat(MinecraftClient client) {
        PlayerEntity player = client.player;
        
        switch (currentState) {
            case MONITORING:
                handleMonitoring(client, player);
                break;
            case EATING:
                handleEating(client, player);
                break;
            case WAIT_FOR_SERVER:
                handleWaitForServer(client, player);
                break;
            case COMPLETING:
                handleCompleting(client, player);
                break;
        }
    }

    private void handleMonitoring(MinecraftClient client, PlayerEntity player) {
        // 満腹度がしきい値以下になったかチェック
        if (player.getHungerManager().getFoodLevel() <= CONFIG.getHungerThreshold()) {
            LOGGER.info("満腹度がしきい値以下になりました: {} <= {}", 
                player.getHungerManager().getFoodLevel(), CONFIG.getHungerThreshold());
            
            // 食事可能状態かチェック
            if (!canEatNow(player)) {
                LOGGER.info("現在食事できません（他アクション中）");
                return;
            }
            
            // ホットバーに食べられる食べ物があるかチェック
            int foodSlot = findEatableFoodInHotbar(player);
            if (foodSlot != -1) {
                startEating(client, foodSlot);
            } else {
                LOGGER.info("ホットバーに食べられる食べ物がありません");
            }
        }
    }

    private boolean canEatNow(PlayerEntity player) {
        // 基本的な食事不可能状態をチェック
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        
        // 他アクション中の食事許可設定をチェック
        if (!CONFIG.isEatWhileAction()) {
            // 設定がオフの場合は、アクション中は食事不可
            if (player.isSprinting() || player.isSneaking() || player.isUsingItem()) {
                return false;
            }
        } else {
            // 設定がオンの場合は、食事中以外は食事可能
            if (player.isUsingItem()) {
                return false;
            }
        }
        
        return true;
    }

    private void handleEating(MinecraftClient client, PlayerEntity player) {
        eatingTicks++;
        
        // 右クリック長押し開始
        if (!isHoldingUseKey) {
            client.options.useKey.setPressed(true);
            isHoldingUseKey = true;
            LOGGER.info("右クリック長押し開始");
        }

        // 満腹度またはアイテム数の変化を監視
        ItemStack currentStack = player.getInventory().getStack(eatingSlot);
        int currentHunger = player.getHungerManager().getFoodLevel();
        if ((itemCountBeforeEating != -1 && currentStack.getCount() < itemCountBeforeEating)
            || (currentHunger > hungerBeforeEating)) {
            LOGGER.info("食事完了: 満腹度またはアイテム消費を検知");
            client.options.useKey.setPressed(false);
            isHoldingUseKey = false;
            currentState = AutoEatState.COMPLETING;
            eatingTicks = 0;
            return;
        }

        // タイムアウトチェック
        if (eatingTicks >= MAX_EATING_TICKS) {
            LOGGER.warn("食事がタイムアウトしました");
            client.options.useKey.setPressed(false);
            isHoldingUseKey = false;
            resetToMonitoring();
        }
    }

    private void handleWaitForServer(MinecraftClient client, PlayerEntity player) {
        waitForServerTicks++;
        ItemStack currentStack = player.getInventory().getStack(eatingSlot);
        int currentHunger = player.getHungerManager().getFoodLevel();
        // アイテム消費または満腹度変化を検知
        if ((itemCountBeforeEating != -1 && currentStack.getCount() < itemCountBeforeEating)
            || (currentHunger > hungerBeforeEating)) {
            LOGGER.info("サーバー反映: アイテム消費または満腹度変化を検知");
            currentState = AutoEatState.COMPLETING;
            eatingTicks = 0;
            return;
        }
        if (waitForServerTicks > MAX_WAIT_FOR_SERVER_TICKS) {
            LOGGER.info("サーバー反映待ちタイムアウト。食事を再実行します");
            hasStartedEating = false;
            currentState = AutoEatState.EATING;
        }
    }

    private void handleCompleting(MinecraftClient client, PlayerEntity player) {
        eatingTicks++;
        
        // 満腹度がしきい値を超えたかチェック
        int currentHunger = player.getHungerManager().getFoodLevel();
        if (currentHunger > CONFIG.getHungerThreshold()) {
            LOGGER.info("食事完了: 満腹度 {} > {}", currentHunger, CONFIG.getHungerThreshold());
            finishEating(client);
        } else if (eatingTicks >= 60) { // 3秒待っても満腹度が上がらない場合
            LOGGER.warn("満腹度が回復しませんでした: {}", currentHunger);
            resetToMonitoring();
        }
    }

    private int findEatableFoodInHotbar(PlayerEntity player) {
        // ホットバー（0-8）をチェック
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && canEat(stack, player)) {
                LOGGER.info("食べられる食べ物を発見: スロット {}, アイテム {}", i, stack.getItem().getName().getString());
                return i;
            }
        }
        return -1;
    }

    private void startEating(MinecraftClient client, int slot) {
        PlayerEntity player = client.player;
        
        // 食事実行状態に移行
        currentState = AutoEatState.EATING;
        originalHotbarSlot = player.getInventory().selectedSlot;
        eatingSlot = slot;
        hungerBeforeEating = player.getHungerManager().getFoodLevel();
        itemCountBeforeEating = player.getInventory().getStack(slot).getCount();
        eatingTicks = 0;
        hasStartedEating = false;
        
        // ホットバーのスロットを選択
        player.getInventory().selectedSlot = slot;
        
        LOGGER.info("食事開始: スロット {}, 満腹度 {}, アイテム数 {}", slot, hungerBeforeEating, itemCountBeforeEating);
    }

    private void finishEating(MinecraftClient client) {
        if (client.player != null && originalHotbarSlot != -1) {
            client.player.getInventory().selectedSlot = originalHotbarSlot;
        }
        // 念のため右クリック解除
        client.options.useKey.setPressed(false);
        isHoldingUseKey = false;
        // 監視モードに戻る
        resetToMonitoring();
        eatCooldown = 60; // 3秒のクールダウン
    }

    private void resetToMonitoring() {
        currentState = AutoEatState.MONITORING;
        originalHotbarSlot = -1;
        eatingSlot = -1;
        hungerBeforeEating = -1;
        itemCountBeforeEating = -1;
        eatingTicks = 0;
        hasStartedEating = false;
        isHoldingUseKey = false;
    }

    private boolean canEat(ItemStack stack, PlayerEntity player) {
        Item item = stack.getItem();
        if (CONFIG.isBlacklisted(item)) {
            return false;
        }
        FoodComponent food = item.getFoodComponent();
        if (food == null) {
            return false;
        }
        return player.canConsume(food.isAlwaysEdible());
    }

    public static MogumoguConfig getConfig() {
        return CONFIG;
    }
}
