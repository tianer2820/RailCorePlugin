package plugin.tianer2820.railcore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager {
    public static void registerRecipes(JavaPlugin plugin) {
        // Rail Dup Recipes
        Material[] railTypes = {
                Material.RAIL,
                Material.POWERED_RAIL,
                Material.DETECTOR_RAIL,
                Material.ACTIVATOR_RAIL
        };
        for (Material railType : railTypes) {
            ItemStack result = new ItemStack(railType, 2);
            NamespacedKey key = new NamespacedKey(plugin, railType.name().toLowerCase() + "_dup");
            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            recipe.addIngredient(railType);
            Bukkit.addRecipe(recipe);
        }
        // Tool Efficiency Recipes
        Material[] toolMaterials = {
                Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
        };
        for (Material toolMaterial : toolMaterials) {
            ItemStack result = new ItemStack(toolMaterial, 1);
            result.addEnchantment(Enchantment.EFFICIENCY, 5);
            NamespacedKey key = new NamespacedKey(plugin, toolMaterial.name().toLowerCase() + "_efficiency_crafting");
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("TT", "TT");
            recipe.setIngredient('T', toolMaterial);
            Bukkit.addRecipe(recipe);
        }
    }
}
