package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.services.core.CraftingManager;
import com.evernightfireworks.mcci.services.parser.LootParser;
import com.evernightfireworks.mcci.services.parser.RecipeParser;
import com.evernightfireworks.mcci.services.parser.TagParser;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

public class CraftingPolicyService {
    public static HashSet<LootManager> lootManagers = new HashSet<>();

    public static void registerMain() {
        LootTableLoadingCallback.EVENT.register(((resourceManager, lootManager, identifier, fabricLootSupplierBuilder, lootTableSetter) -> {
            CraftingPolicyService.lootManagers.add(lootManager);
        }));
    }
    Logger logger = LogManager.getFormatterLogger("mcci:services:crafting_policy_service");
    CraftingManager manager;
    RecipeParser recipeParser;
    LootParser lootParser;
    TagParser tagParser;
    Boolean generated;

    public CraftingPolicyService() {
        manager = new CraftingManager();
        recipeParser = new RecipeParser(manager);
        lootParser = new LootParser(manager);
        tagParser = new TagParser(manager);
        generated = false;
    }

    public void generateCraftingGraph(ServerWorld world) {
        synchronized(this) {
            if(this.generated) {
                this.logger.info("use crafting graph caches");
                return;
            }
            this.logger.info("start generating crafting graph...");
            RecipeManager recipeManager = world.getRecipeManager();
            RegistryTagManager tagManager = world.getTagManager();
            this.logger.info("parsing tags crafting...");
            this.tagParser.parseTags(tagManager);
            this.logger.info("parsing recipes crafting...");
            this.recipeParser.parseRecipes(recipeManager);
            this.logger.info("parsing loots crafting...");
            for(var lootManager: CraftingPolicyService.lootManagers) {
                this.lootParser.parseLoot(lootManager);
            }
            this.logger.info("completing remained links...");
            this.manager.completeLinks();
            this.generated = true;
            this.logger.info("crafting graph generated");
        }
    }
}