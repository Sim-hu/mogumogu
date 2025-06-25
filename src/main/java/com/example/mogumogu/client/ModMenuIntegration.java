package com.example.mogumogu.client;

import com.example.mogumogu.client.gui.ButtonEntry;
import com.example.mogumogu.client.gui.FoodListScreen;
import com.example.mogumogu.client.gui.HungerBarEntry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MogumoguConfig config = MogumoguClient.getConfig();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.mogumogu.title"));

            builder.setSavingRunnable(config::save);
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // --- 一般設定カテゴリ ---
            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.mogumogu.category.general"));

            // 1. 機能のオン/オフ
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("config.mogumogu.enabled"), config.isEnabled())
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.mogumogu.enabled.tooltip"))
                    .setSaveConsumer(config::setEnabled)
                    .build());

            // 2. 他アクション中の食事
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("config.mogumogu.eat_while_action"), config.isEatWhileAction())
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.mogumogu.eat_while_action.tooltip"))
                    .setSaveConsumer(config::setEatWhileAction)
                    .build());

            // 3. 満腹度しきい値
            IntegerSliderEntry hungerSlider = entryBuilder.startIntSlider(
                            Text.translatable("config.mogumogu.hunger_threshold"), config.getHungerThreshold(), 0, 20)
                    .setDefaultValue(6)
                    .setTooltip(Text.translatable("config.mogumogu.hunger_threshold.tooltip"))
                    .setSaveConsumer(config::setHungerThreshold)
                    .build();

            // 満腹ゲージとスライダーをリンクさせる
            general.addEntry(new HungerBarEntry(hungerSlider::getValue));
            general.addEntry(hungerSlider);

            // 4. ブラックリスト設定ボタン
            general.addEntry(new ButtonEntry(
                    Text.translatable("config.mogumogu.blacklist.button"),
                    button -> MinecraftClient.getInstance().setScreen(new FoodListScreen(builder.build()))
            ));

            return builder.build();
        };
    }
}
