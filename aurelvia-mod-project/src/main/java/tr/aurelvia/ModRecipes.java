package tr.aurelvia;

import net.fabricmc.fabric.api.recipe.v1.RecipeManagerEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/**
 * Crafting tarifleri
 */
public class ModRecipes {
    
    public static void register() {
        AurelviaMachinesMod.LOGGER.info("Registering Aurelvia recipes");
        
        RecipeManagerEvents.MODIFY.register(recipeManager -> {
            // Bilgisayar tarifi: 3 demir + 1 altın + 1 bakır
            // Pattern:
            // IAI
            // ICI
            // III
            // I = Iron Ingot, A = Gold Ingot, C = Copper Ingot
            addShapedRecipe(recipeManager, "computer",
                "IAI",
                "ICI",
                "III",
                new ItemStack(AurelviaMachinesMod.COMPUTER_ITEM),
                'I', Ingredient.of(Items.IRON_INGOT),
                'A', Ingredient.of(Items.GOLD_INGOT),
                'C', Ingredient.of(Items.COPPER_INGOT)
            );
            
            // Konveyör tarifi: 2 kayış + 2 çubuk + 1 demir blok
            // Pattern:
            // L L
            // S S
            // B
            // L = Leather (kayış), S = Stick (çubuk), B = Iron Block
            addShapedRecipe(recipeManager, "conveyor",
                "L L",
                "S S",
                " B ",
                new ItemStack(AurelviaMachinesMod.CONVEYOR_ITEM),
                'L', Ingredient.of(Items.LEATHER),
                'S', Ingredient.of(Items.STICK),
                'B', Ingredient.of(Items.IRON_BLOCK)
            );
            
            // Kimlik makinesi tarifi: 1 bilgisayar + 1 konveyör + 5 demir blok
            // Pattern:
            // C  
            // KKK
            // IKI
            // C = Computer, K = Conveyor, I = Iron Block
            addShapedRecipe(recipeManager, "id_machine",
                "C  ",
                "KKK",
                "IKI",
                new ItemStack(AurelviaMachinesMod.ID_MACHINE_ITEM),
                'C', Ingredient.of(AurelviaMachinesMod.COMPUTER_ITEM),
                'K', Ingredient.of(AurelviaMachinesMod.CONVEYOR_ITEM),
                'I', Ingredient.of(Items.IRON_BLOCK)
            );
        });
    }
    
    private static void addShapedRecipe(net.minecraft.world.item.crafting.RecipeManager recipeManager, 
                                        String name, 
                                        String pattern1, String pattern2, String pattern3,
                                        ItemStack result,
                                        Object... ingredients) {
        // Build ingredient map
        java.util.Map<Character, Ingredient> ingredientMap = new java.util.HashMap<>();
        for (int i = 0; i < ingredients.length; i += 2) {
            char key = (char) ingredients[i];
            Ingredient value = (Ingredient) ingredients[i + 1];
            ingredientMap.put(key, value);
        }
        
        java.util.List<Ingredient> ingredientList = new java.util.ArrayList<>();
        String fullPattern = pattern1 + pattern2 + pattern3;
        for (char c : fullPattern.toCharArray()) {
            if (c == ' ') {
                ingredientList.add(Ingredient.EMPTY);
            } else {
                ingredientList.add(ingredientMap.getOrDefault(c, Ingredient.EMPTY));
            }
        }
        
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(
            3, 3,
            ingredientList,
            Optional.empty()
        );
        
        ShapedRecipe recipe = new ShapedRecipe(
            "",
            CraftingBookCategory.MISC,
            shapedPattern,
            result
        );
        
        RecipeHolder<ShapedRecipe> holder = new RecipeHolder<>(
            ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, name),
            recipe
        );
        
        recipeManager.addRecipe((RecipeHolder) holder);
    }
}
