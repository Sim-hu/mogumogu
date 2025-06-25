package com.example.mogumogu.client.gui;

import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FoodIconListEntry extends BooleanListEntry {
    private final ItemStack itemStack;

    public FoodIconListEntry(Text fieldName, ItemStack itemStack, boolean value, Text resetButtonKey, Supplier<Boolean> defaultValue, Consumer<Boolean> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, requiresRestart);
        this.itemStack = itemStack;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        // アイテムアイコンを描画
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getItemRenderer() != null) {
            client.getItemRenderer().renderInGuiWithOverrides(matrices, itemStack, x + 4, y + 2);
        }

        // テキストとチェックボックスの位置を調整
        int offsetX = x + 28; // アイコンの分だけオフセット

        // 元のrenderメソッドの内容を位置調整して実行
        super.render(matrices, index, y, offsetX, entryWidth - 24, entryHeight, mouseX, mouseY, isHovered, delta);
    }
}
