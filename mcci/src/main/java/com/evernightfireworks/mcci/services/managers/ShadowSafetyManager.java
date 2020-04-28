package com.evernightfireworks.mcci.services.managers;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ShadowSafetyManager {

    private final PlayerEntity player;
    private final static int MONSTER_SEARCH_SIZE = 12;
    private final static int MONSTER_SEARCH_SIZE_Z = 3;
    private final static ArrayList<EntityType<?>> MONSTER_TYPES = Registry.ENTITY_TYPE.stream()
            .filter(e->e.getCategory()==EntityCategory.MONSTER)
            .collect(Collectors.toCollection(ArrayList::new));

    public ShadowSafetyManager(PlayerEntity player) {
        this.player = player;
    }

    public void run() {
        ArrayList<MobEntity> mobs = this.shadowSpawnMobs();
    }

    private ArrayList<MobEntity> shadowSpawnMobs() {
        BlockPos playerPos = this.player.getBlockPos();
        World world = this.player.getEntityWorld();
        int playerPosX = playerPos.getX();
        int playerPosY = playerPos.getY();
        int playerPosZ = playerPos.getZ();
        ArrayList<MobEntity> entities = new ArrayList<>();
        for(int i=playerPosX-MONSTER_SEARCH_SIZE;i<=playerPosX+MONSTER_SEARCH_SIZE;i++) {
            for(int j=playerPosY-MONSTER_SEARCH_SIZE;j<=playerPosY+MONSTER_SEARCH_SIZE;j++) {
                for(int k=playerPosZ-MONSTER_SEARCH_SIZE_Z;k<=playerPosZ+MONSTER_SEARCH_SIZE_Z;k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    Block block = world.getBlockState(pos).getBlock();
                    if(!block.canMobSpawnInside()) {
                        continue;
                    }
                    float a = world.getBrightness(pos);
                    for(EntityType<?> entityType: MONSTER_TYPES) {
                        Entity e = trySpawn(entityType, world, pos,  SpawnType.NATURAL, true, false);
                        if(e!=null) {
                            entities.add((MobEntity)e);
                        }
                    }
                }
            }
        }
        return entities;
    }

    @Nullable
    private <T extends Entity> T trySpawn(
            EntityType<T> entityType, World world, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY) {
        return entityType.create(world, null, null,  null, pos, spawnType, alignPosition, invertY);
    }
}
