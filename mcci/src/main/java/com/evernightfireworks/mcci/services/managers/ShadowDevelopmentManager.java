package com.evernightfireworks.mcci.services.managers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Set;

public class ShadowDevelopmentManager {
    private final Logger logger = LogManager.getFormatterLogger(ShadowDevelopmentManager.class.getName());
    private final PlayerEntity player;
    private static final int MAX_EXPERIENCE_LEVEL = 40;

    public ShadowDevelopmentManager(PlayerEntity player){
        this.player = player;
    }

    private int getExperienceFactor() {
        return (int)(Math.sqrt(this.player.experienceLevel / (double)MAX_EXPERIENCE_LEVEL * 100) * 10);
    }

    private int getExploreStatusFactor() {
        try {
            Field recipeBookField = this.player.getClass().getDeclaredField("recipeBook");
            recipeBookField.setAccessible(true);
            RecipeBook recipeBook = (RecipeBook) recipeBookField.get(this.player);
            Field recipeField = RecipeBook.class.getDeclaredField("recipes");
            recipeField.setAccessible(true);
            Set<Identifier> recipes = (Set<Identifier>)recipeField.get(recipeBook);
            return (int)(Math.sqrt(((double) 100 * recipes.size() / this.player.world.getRecipeManager().values().size()))*10);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.logger.error("can not get your recipes, return 0", e);
            return 0;
        }
    }

    public int run() {
        int ef = this.getExperienceFactor();
        int esf = this.getExploreStatusFactor();
        int developmentFactor = (int)Math.ceil((ef+esf)/2.0);
        this.logger.info("player development factor is " + developmentFactor);
        return developmentFactor;
    }
}
