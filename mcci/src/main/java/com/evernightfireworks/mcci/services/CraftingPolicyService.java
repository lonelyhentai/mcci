package com.evernightfireworks.mcci.services;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CraftingPolicyService {
    public static void generateCraftingTree(World world, ItemStack stack) {
        var lootTables = LootTables.getAll();
        var recipes = world.getRecipeManager().values();

        var tagManager = world.getTagManager();
        var itemTags = tagManager.items().getEntries();
        var blockTags = tagManager.blocks().getEntries();
        var entityTags = tagManager.entityTypes().getEntries();
        var fluidTags = tagManager.fluids().getEntries();

    }
}