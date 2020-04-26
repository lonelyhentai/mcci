package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.core.CraftingManager;
import com.evernightfireworks.mcci.services.parser.LootParser;
import com.evernightfireworks.mcci.services.parser.RecipeParser;
import com.evernightfireworks.mcci.services.parser.TagParser;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
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
    private final Logger logger = LogManager.getFormatterLogger("mcci:services:crafting_policy_service");
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

    public void generateCraftingGraph(World world) {
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
            this.manager.completeGlobalRemainLinks();
            this.generated = true;
            this.logger.info("crafting graph generated");
        }
    }

    public CGraph getSubCraftingGraph(World world, Item item) {
        this.logger.info("start getting crafting sub graph...");
        if(!this.generated) {
            this.generateCraftingGraph(world);
        }
        var res = this.manager.getSubgraphOfGlobal(item);
        String itemId = Registry.ITEM.getId(item).toString();
        if(res==null) {
            this.logger.error(String.format("unexpected logic error, can not find target item '%s' in this manager, return null", itemId));
        } else {
            this.logger.info(String.format("crafting sub graph of item '%s' has generated", itemId));
        }
        return res;
    }
}