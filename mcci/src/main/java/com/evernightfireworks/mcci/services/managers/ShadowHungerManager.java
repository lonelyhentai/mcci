package com.evernightfireworks.mcci.services.managers;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Stack;

public class ShadowHungerManager {
    private final Logger logger = LogManager.getFormatterLogger(ShadowHungerManager.class.getName());

    private int foodLevel = 20;
    private float foodSaturationLevel = 5.0F;
    private float exhaustion;
    private int foodStarvationTimer;
    private final PlayerEntity player;
    private float shadowHealth;

    public ShadowHungerManager(HungerManager source, PlayerEntity player) {
        CompoundTag tag = new CompoundTag();
        source.serialize(tag);
        this.deserialize(tag);
        this.player = player;
        this.shadowHealth = player.getHealth();
    }

    private void add(int food, float f) {
        this.foodLevel = Math.min(food + this.foodLevel, 20);
        this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)food * f * 2.0F, (float)this.foodLevel);
    }

    private void eat(Item item, ItemStack itemStack) {
        if (item.isFood()) {
            FoodComponent foodComponent = item.getFoodComponent();
            this.add(foodComponent.getHunger(), foodComponent.getSaturationModifier());
        }

    }

    private boolean canFoodHeal() {
        return this.shadowHealth > 0.0F && this.shadowHealth < player.getMaximumHealth();
    }

    private void update() {
        Difficulty difficulty = this.player.world.getDifficulty();
        if (this.exhaustion > 4.0F) {
            this.exhaustion -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean bl = this.player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        if (bl && this.foodSaturationLevel > 0.0F && this.canFoodHeal() && this.foodLevel >= 20) {
            ++this.foodStarvationTimer;
            if (this.foodStarvationTimer >= 10) {
                float f = Math.min(this.foodSaturationLevel, 6.0F);
                this.heal(f / 6.0F);
                this.addExhaustion(f);
                this.foodStarvationTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18 && player.canFoodHeal()) {
            ++this.foodStarvationTimer;
            if (this.foodStarvationTimer >= 80) {
                this.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodStarvationTimer = 0;
            }
        } else if(this.foodLevel>0) {
            this.foodStarvationTimer = 0;
        }
    }

    private void deserialize(CompoundTag compoundTag) {
        if (compoundTag.contains("foodLevel", 99)) {
            this.foodLevel = compoundTag.getInt("foodLevel");
            this.foodStarvationTimer = compoundTag.getInt("foodTickTimer");
            this.foodSaturationLevel = compoundTag.getFloat("foodSaturationLevel");
            this.exhaustion = compoundTag.getFloat("foodExhaustionLevel");
        }

    }

    private void addExhaustion(float exhaustion) {
        this.exhaustion = Math.min(this.exhaustion + exhaustion, 40.0F);
    }

    private void heal(float amount) {
        float f = this.getHealth();
        if (f > 0.0F) {
            this.setHealth(f + amount);
        }

    }

    private float getHealth() {
        return this.shadowHealth;
    }

    private void setHealth(float health) {
        this.shadowHealth = MathHelper.clamp(health, 0.0F, this.player.getMaximumHealth());
    }

    public int run() {
        this.logger.info("starting to test hunger factor...");
        PlayerInventory inventory = player.inventory;
        Stack<ItemStack> foodStacks = new Stack<>();
        for(int i=0;i<inventory.getInvSize();i++) {
            Item item = inventory.getInvStack(i).getItem();
            if(item.isFood()) {
                foodStacks.add(inventory.getInvStack(i).copy());
            }
        }
        foodStacks.sort((a,b) -> {
            var aComponent = a.getItem().getFoodComponent();
            var bComponent = b.getItem().getFoodComponent();
            assert aComponent!=null;
            assert bComponent!=null;
            return bComponent.getHunger() - aComponent.getHunger();
        });
        int t = 0;
        while(this.foodLevel>0) {
            while(!foodStacks.isEmpty()) {
                var last = foodStacks.pop();
                if(Objects.requireNonNull(last.getItem().getFoodComponent()).getHunger() + this.foodLevel > 20) {
                    foodStacks.push(last);
                    break;
                }
                this.eat(last.getItem(), last);
                last.setCount(last.getCount()-1);
                if(last.getCount()>0) {
                    foodStacks.push(last);
                }
            }
            this.update();
            this.foodLevel -= 1;
            t += 1;
        }
        t = Math.min(200, t);
        int factor = (int)Math.ceil(Math.sqrt(((double) t / 2))) * 10;
        this.logger.info(String.format("hunger factor is %d", factor));
        return factor;
    }
}
