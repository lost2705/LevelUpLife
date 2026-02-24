package com.example.leveluplife.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shop_items")
public class ShopItem {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private String icon;
    private int price;
    private String effectType;
    private int effectValue;
    private boolean available;

    public ShopItem(String name, String description, String icon,
                    int price, String effectType, int effectValue) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.price = price;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.available = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }
    public int getEffectValue() { return effectValue; }
    public void setEffectValue(int effectValue) { this.effectValue = effectValue; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
