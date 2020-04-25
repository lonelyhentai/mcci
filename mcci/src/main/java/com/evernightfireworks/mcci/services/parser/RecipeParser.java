package com.evernightfireworks.mcci.services.parser;

import com.evernightfireworks.mcci.services.core.CNode;
import com.evernightfireworks.mcci.services.core.CNodeType;
import com.evernightfireworks.mcci.services.core.CraftingManager;
import com.evernightfireworks.mcci.services.util.TriConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

public class RecipeParser {
    CraftingManager manager;
    final HashMap<RecipeType<?>, TriConsumer<RecipeParser, Recipe<?>, Identifier>> parseMapping = new HashMap<>() {
        {
            parseMapping.put(RecipeType.BLASTING, (p, r, i) -> p.parseBlasting((BlastingRecipe) r, i));
            parseMapping.put(RecipeType.CRAFTING, (p, r, i) -> p.parseCrafting((CraftingRecipe) r, i));
            parseMapping.put(RecipeType.CAMPFIRE_COOKING, (p, r, i) -> p.parseCampfireCooking((CampfireCookingRecipe) r, i));
            parseMapping.put(RecipeType.SMELTING, (p, r, i) -> p.parseSmelting((SmeltingRecipe) r, i));
            parseMapping.put(RecipeType.SMOKING, (p, r, i) -> p.parseSmoking((SmokingRecipe) r, i));
            parseMapping.put(RecipeType.STONECUTTING, (p, r, i) -> p.parseStoneCutting((StonecuttingRecipe) r, i));
        }
    };

    public RecipeParser(CraftingManager manager) {
        this.manager = manager;
    }

    CNode parseInGradientItem(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            return this.manager.getOrCreateNode(new Identifier(stack.getTag().toString()), CNodeType.tag);
        } else {
            return this.manager.getOrCreateNode(Registry.ITEM.getId(stack.getItem()), CNodeType.item);
        }
    }

    void parseInGradients(Recipe<?> recipe, Identifier recipeId) {
        ItemStack outputItem = recipe.getOutput();
        CNode outputNode = this.manager.getOrCreateNode(Registry.ITEM.getId(outputItem.getItem()), CNodeType.item);
        var inputs = recipe.getPreviewInputs();
        for (Ingredient i : inputs) {
            ItemStack[] entries = i.getMatchingStacksClient();
            for (ItemStack e : entries) {
                CNode node = this.parseInGradientItem(e);
                this.manager.createSingleLink(outputNode, node, recipe, recipeId);
            }
        }
    }

    void parseCraftingShapeless(ShapelessRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    // @TODO
    void parseCraftingSpecial(SpecialCraftingRecipe recipe, Identifier recipeId) {
    }

    void parseCraftingShaped(ShapedRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    void parseCrafting(CraftingRecipe recipe, Identifier recipeId) {
        if (recipe.isIgnoredInRecipeBook()) {
            this.parseCraftingSpecial((SpecialCraftingRecipe) recipe, recipeId);
        } else if(recipe instanceof ShapedRecipe) {
            this.parseCraftingShaped((ShapedRecipe)recipe, recipeId);
        } else {
            this.parseCraftingShapeless((ShapelessRecipe) recipe, recipeId);
        }
    }

    void parseBlasting(BlastingRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    void parseCampfireCooking(CampfireCookingRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    void parseSmelting(SmeltingRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    void parseSmoking(SmokingRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    void parseStoneCutting(StonecuttingRecipe recipe, Identifier recipeId) {
        this.parseInGradients(recipe, recipeId);
    }

    public void parseRecipes(RecipeManager recipes) {
        recipes
                .values()
                .forEach((r) -> {
                    var fun = this.parseMapping.get(r.getType());
                    if (fun != null) {
                        fun.accept(this, r, r.getId());
                    }
                });
    }
}
