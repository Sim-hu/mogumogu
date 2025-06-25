package com.example.mogumogu.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MogumoguConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mogumogu.json");

    private boolean enabled = true; // 機能のオン/オフ（デフォルトはオン）
    private int hungerThreshold = 6; // デフォルトは満腹度6以下で食べる
    private Set<String> blacklistedItems = new HashSet<>();
    private boolean eatWhileAction = false; // 他の動作（スプリントなど）中に食べるか

    public MogumoguConfig() {
        // デフォルトのブラックリスト（腐った肉など）
        blacklistedItems.add("minecraft:rotten_flesh");
        blacklistedItems.add("minecraft:spider_eye");
        blacklistedItems.add("minecraft:poisonous_potato");
        blacklistedItems.add("minecraft:pufferfish");
    }

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) {
                    this.enabled = data.enabled;
                    this.hungerThreshold = data.hungerThreshold;
                    if (data.blacklistedItems != null) {
                        this.blacklistedItems = new HashSet<>(data.blacklistedItems);
                    }
                    if (data.eatWhileAction != null) {
                        this.eatWhileAction = data.eatWhileAction;
                    }
                }
            } catch (IOException e) {
                MogumoguClient.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // デフォルト設定を保存
        }
    }

    public void save() {
        try {
            ConfigData data = new ConfigData();
            data.enabled = this.enabled;
            data.hungerThreshold = this.hungerThreshold;
            data.blacklistedItems = new HashSet<>(this.blacklistedItems);
            data.eatWhileAction = this.eatWhileAction;

            String json = GSON.toJson(data);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            MogumoguClient.LOGGER.error("Failed to save config", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getHungerThreshold() {
        return hungerThreshold;
    }

    public void setHungerThreshold(int threshold) {
        this.hungerThreshold = Math.max(0, Math.min(20, threshold));
    }

    public boolean isEatWhileAction() {
        return eatWhileAction;
    }

    public void setEatWhileAction(boolean eatWhileAction) {
        this.eatWhileAction = eatWhileAction;
    }

    public boolean isBlacklisted(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return blacklistedItems.contains(id.toString());
    }

    public void addToBlacklist(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        blacklistedItems.add(id.toString());
    }

    public void removeFromBlacklist(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        blacklistedItems.remove(id.toString());
    }

    public void toggleBlacklist(Item item) {
        if (isBlacklisted(item)) {
            removeFromBlacklist(item);
        } else {
            addToBlacklist(item);
        }
    }

    public Set<String> getBlacklistedItems() {
        return new HashSet<>(blacklistedItems);
    }

    public void setBlacklistedItems(Set<String> items) {
        this.blacklistedItems = new HashSet<>(items);
    }

    private static class ConfigData {
        public boolean enabled = true;
        public int hungerThreshold = 6;
        public Set<String> blacklistedItems;
        public Boolean eatWhileAction;
    }
}
