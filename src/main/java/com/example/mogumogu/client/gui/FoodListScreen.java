package com.example.mogumogu.client.gui;

import com.example.mogumogu.client.MogumoguClient;
import com.example.mogumogu.client.MogumoguConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class FoodListScreen extends Screen {
    private final Screen parent;
    private FoodListWidget foodListWidget;
    private final MogumoguConfig config;

    public FoodListScreen(Screen parent) {
        super(Text.translatable("config.mogumogu.blacklist.title"));
        this.parent = parent;
        this.config = MogumoguClient.getConfig();
    }

    @Override
    protected void init() {
        super.init();

        // 食べ物リストウィジェットを作成
        this.foodListWidget = new FoodListWidget(this.client, this.width, this.height, 32, this.height - 32, 36);
        this.addSelectableChild(this.foodListWidget);

        // 全ての食べ物を追加
        List<Item> foodItems = Registries.ITEM.stream()
                .filter(item -> item.getFoodComponent() != null)
                .sorted((a, b) -> {
                    // ブラックリストに入っているものを上に
                    boolean aBlacklisted = config.isBlacklisted(a);
                    boolean bBlacklisted = config.isBlacklisted(b);
                    if (aBlacklisted != bBlacklisted) {
                        return aBlacklisted ? -1 : 1;
                    }
                    // その後名前でソート
                    return a.getName().getString().compareTo(b.getName().getString());
                })
                .collect(Collectors.toList());

        for (Item item : foodItems) {
            this.foodListWidget.addEntry(new FoodEntry(item));
        }

        // 保存して戻るボタン
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.done"),
                        button -> {
                            config.save();
                            this.client.setScreen(parent);
                        })
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.foodListWidget.render(matrices, mouseX, mouseY, delta);
        
        // タイトルを描画
        int titleWidth = this.textRenderer.getWidth(this.title);
        this.textRenderer.drawWithShadow(matrices, this.title, this.width / 2 - titleWidth / 2, 10, 16777215);

        // ヘルプテキスト
        Text helpText = Text.translatable("config.mogumogu.blacklist.help");
        int helpWidth = this.textRenderer.getWidth(helpText);
        this.textRenderer.drawWithShadow(matrices, helpText, this.width / 2 - helpWidth / 2, 20, 10526880);

        super.render(matrices, mouseX, mouseY, delta);
    }

    class FoodListWidget extends AlwaysSelectedEntryListWidget<FoodEntry> {
        public FoodListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
        }

        @Override
        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        public int addEntry(FoodEntry entry) {
            return super.addEntry(entry);
        }
    }

    class FoodEntry extends AlwaysSelectedEntryListWidget.Entry<FoodEntry> {
        private final Item item;
        private final ItemStack stack;
        private final ButtonWidget toggleButton;

        public FoodEntry(Item item) {
            this.item = item;
            this.stack = new ItemStack(item);

            this.toggleButton = ButtonWidget.builder(
                            Text.literal(""),
                            button -> {
                                config.toggleBlacklist(item);
                                updateButtonText();
                            })
                    .dimensions(0, 0, 60, 20)
                    .build();

            updateButtonText();
        }

        private void updateButtonText() {
            boolean blacklisted = config.isBlacklisted(item);
            this.toggleButton.setMessage(
                    Text.translatable(blacklisted ? "config.mogumogu.blacklisted" : "config.mogumogu.allowed")
                            .formatted(blacklisted ? Formatting.RED : Formatting.GREEN)
            );
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            MinecraftClient client = FoodListScreen.this.client;

            // アイテムアイコンを描画
            client.getItemRenderer().renderInGuiWithOverrides(matrices, stack, x + 5, y + 8);
            client.getItemRenderer().renderGuiItemOverlay(matrices, client.textRenderer, stack, x + 5, y + 8);

            // アイテム名を描画
            Text itemName = item.getName();
            client.textRenderer.draw(matrices, itemName, x + 30, y + 5, 16777215);

            // 栄養価情報を描画
            if (item.getFoodComponent() != null) {
                Text foodInfo = Text.literal(String.format("🍖 %d  🥩 %.1f",
                                item.getFoodComponent().getHunger(),
                                item.getFoodComponent().getSaturationModifier()))
                        .formatted(Formatting.GRAY);
                client.textRenderer.draw(matrices, foodInfo, x + 30, y + 16, 10526880);
            }

            // トグルボタンを描画
            this.toggleButton.setPosition(x + entryWidth - 65, y + 7);
            this.toggleButton.render(matrices, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggleButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Text getNarration() {
            return Text.literal(item.getName().getString());
        }
    }
}
