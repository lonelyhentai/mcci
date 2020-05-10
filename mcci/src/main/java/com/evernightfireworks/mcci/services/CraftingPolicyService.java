package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.services.managers.CraftingManager;
import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.interfaces.CGraphContainer;
import com.evernightfireworks.mcci.services.parsers.LootParser;
import com.evernightfireworks.mcci.services.parsers.RecipeParser;
import com.evernightfireworks.mcci.services.parsers.TagParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class CraftingPolicyService {
    public static HashSet<LootManager> lootManagers = new HashSet<>();
    private static final String WEBVIEW_PATH = "webview/crafting-graph.html";

    public static void registerMain() {
        LootTableLoadingCallback.EVENT.register((
                (resourceManager, lootManager, identifier, fabricLootSupplierBuilder, lootTableSetter)
                        -> CraftingPolicyService.lootManagers.add(lootManager)));
    }
    private final Logger logger = LogManager.getFormatterLogger(CraftingPolicyService.class.getName());
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

    public void generateGlobalCraftingGraph(World world) {
        synchronized(this) {
            if(this.generated) {
                this.logger.info("use crafting graph caches");
                return;
            }
            this.logger.info("start generating crafting graph...");
            RecipeManager recipeManager = world.getRecipeManager();
            RegistryTagManager tagManager = world.getTagManager();
            this.manager.createBasicNodes();
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
            this.generateGlobalCraftingGraph(world);
        }
        var res = this.manager.getSubgraphOfGlobal(item);
        String itemId = Registry.ITEM.getId(item).toString();
        this.logger.info(String.format("crafting sub graph of item '%s' has generated", itemId));
        return res;
    }

    public static String serializeSubgraph(CGraph subgraph) {
        return subgraph.serialize((g, n)->5+(int)Math.pow(1.6, g.maxPriority-g.getPriority(n)));
    }

    @Environment(EnvType.CLIENT)
    public static URL getWebViewGraphURL() throws MalformedURLException {
        return ResourceSystemManager.getRuntimeResourceAbsPath(WEBVIEW_PATH).toUri().toURL();
    }

    @Environment(EnvType.CLIENT)
    public void refreshWebViewGraph(CGraphContainer container, Item item, World world) {
        this.logger.info(String.format("try to update sub graph of '%s'...",Registry.ITEM.getId(item).toString()));
        if (container.getCGraph().shouldUpdate(item)) {
            this.logger.info("should update");
            container.setCGraph(this.getSubCraftingGraph(world, item));
            String serializedGraph = serializeSubgraph(container.getCGraph());
            try {
                var templateStream = ResourceSystemManager.getSourceResourceAsStream(WEBVIEW_PATH);
                String content = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);
                String output = content.replaceAll("\\{/\\*data\\*/}", serializedGraph);
                ResourceSystemManager.writeRuntimeResource(WEBVIEW_PATH, output);
                this.logger.info(String.format("succeed to write serialized graph to %s", WEBVIEW_PATH));
            } catch (Exception e) {
                this.logger.error( String.format("failed to load resource of %s", WEBVIEW_PATH), e);
            }
        } else {
            this.logger.info("item not change, should not update");
        }
    }
}