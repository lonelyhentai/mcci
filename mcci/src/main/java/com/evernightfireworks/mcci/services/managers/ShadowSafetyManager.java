package com.evernightfireworks.mcci.services.managers;

import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ShadowSafetyManager {

    private final Logger logger = LogManager.getFormatterLogger(ShadowSafetyManager.class.getName());
    private final PlayerEntity player;
    private final static int MONSTER_SEARCH_SIZE = 7;
    private final static int MONSTER_SEARCH_SIZE_Z = 3;
    private final static double MONSTER_GEN_SPEED = 0.01;
    private final static int MONSTER_MAX_SIZE = 8;
    private final static int MONSTER_GEN_LIGHT_LEVEL = 7;
    private final float health;
    private final float armor;
    private final float attackDamage;
    private final float attackSpeed;


    public ShadowSafetyManager(PlayerEntity player) {
        this.player = player;
        this.health = player.getHealth();
        this.armor = this.calculateArmor(player);
        this.attackDamage = this.calculateAttackDamage(player);
        this.attackSpeed = this.calculateAttackSpeed(player);
    }

    private float calculateAttackDamage(PlayerEntity player) {
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        Item item = itemStack.getItem();
        float playerAttackDamage = (float) player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue();
        this.logger.debug(String.format("attack damage of player is %f", playerAttackDamage));
        try {
            Field damageField = item.getClass().getDeclaredField("attackDamage");
            damageField.setAccessible(true);
            float itemAttackDamage = damageField.getFloat(item);
            this.logger.debug(String.format("attack damage of item %s is %f", Registry.ITEM.getId(item).toString(), itemAttackDamage));
            return playerAttackDamage + itemAttackDamage;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.logger.debug(String.format("item in hand has no attack damage of %s", Registry.ITEM.getId(item).toString()));
            return playerAttackDamage;
        }
    }

    private float calculateAttackSpeed(PlayerEntity player) {
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        Item item = itemStack.getItem();
        float playerAttackSpeed = (float) player.getAttributeInstance(EntityAttributes.ATTACK_SPEED).getValue();
        try {
            Field damageField = item.getClass().getDeclaredField("attackSpeed");
            damageField.setAccessible(true);
            float itemAttackSpeed = damageField.getFloat(item);
            this.logger.debug(String.format("attack speed of player is %f", playerAttackSpeed - itemAttackSpeed));
            this.logger.debug(String.format("attack speed of item %s is %f", Registry.ITEM.getId(item).toString(), itemAttackSpeed));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.logger.debug(String.format("attack speed of player is %f", playerAttackSpeed));
            this.logger.debug(String.format("item in hand has no attack speed of %s", Registry.ITEM.getId(item).toString()));
        }
        return playerAttackSpeed;
    }

    private float calculateArmor(PlayerEntity player) {
        this.logger.debug(String.format("armor value of player is %d", player.getArmor()));
        return player.getArmor();
    }

    public int run() {
        ArrayList<Integer> monsterGenLevels = this.shadowSpawnMobs();
        double mobProps = 0.0;
        for(var level: monsterGenLevels) {
            mobProps += MONSTER_GEN_SPEED / (level + 1);
        }
        int m = Math.min(MONSTER_MAX_SIZE, (int)Math.floor(mobProps));
        double w = ( this.attackDamage / this.attackSpeed / 5.0 );
        double h = ( this.health / 20.0 );
        double a = ( this.armor / 20.0 );
        double x = (80 - m * 10) * (h * h);
        int res = (int)Math.ceil(Math.max(0,(w+a)/2*(100-4*m-x)) + x);
        this.logger.info(String.format("player safety factor is %d", res));
        return res;
    }

    private ArrayList<Integer> shadowSpawnMobs() {
        boolean justBlockLight = this.player.world.getTimeOfDay() > 12500 || this.player.world.isThundering();
        BlockPos playerPos = this.player.getBlockPos();
        World world = this.player.getEntityWorld();
        int playerPosX = playerPos.getX();
        int playerPosY = playerPos.getY();
        int playerPosZ = playerPos.getZ();
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = playerPosX - MONSTER_SEARCH_SIZE; i <= playerPosX + MONSTER_SEARCH_SIZE; i++) {
            for (int j = playerPosY - MONSTER_SEARCH_SIZE; j <= playerPosY + MONSTER_SEARCH_SIZE; j++) {
                for (int k = playerPosZ - MONSTER_SEARCH_SIZE_Z; k <= playerPosZ + MONSTER_SEARCH_SIZE_Z; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    Block block = world.getBlockState(pos).getBlock();
                    BlockPos topPos = new BlockPos(i, j, k + 1);
                    Block topBlock = world.getBlockState(topPos).getBlock();
                    if (block.canMobSpawnInside() && topBlock.canMobSpawnInside()) {
                        int lightLevel = justBlockLight ? world.getLightLevel(LightType.BLOCK, pos) :
                                world.getLightLevel(LightType.BLOCK, pos) + world.getLightLevel(LightType.SKY,pos);
                        if (lightLevel <= MONSTER_GEN_LIGHT_LEVEL) {
                            res.add(lightLevel);
                        }
                    }
                }
            }
        }
        this.logger.debug(String.format("there have %d blocks may spawning mobs per tick at most", res.size()));
        return res;
    }
}
