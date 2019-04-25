package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.util.HashMap;
import java.util.Map;

public class StockManagement {
    private HashMap<Ingredient, Number> ingredientStockLevels = new HashMap<>();
    private HashMap<Dish, Number> dishStockLevels = new HashMap<>();

    public StockManagement() {

    }

    public Map<Ingredient, Number> getIngredientStockLevels() {
        return ingredientStockLevels;
    }

    public synchronized Map<Dish, Number> getDishStockLevels() {
        return dishStockLevels;
    }

    public void addIngredientToTracking(Ingredient ingredient) {
        this.ingredientStockLevels.put(ingredient, 0);
    }

    public void addDishToTracking(Dish dish) {
        this.dishStockLevels.put(dish, 0);
    }

    public Number getCurrentStockDish(Dish dish) {
        return dishStockLevels.get(dish);
    }

    public Number getCurrentStockIngredient(Ingredient ingredient) {
        return ingredientStockLevels.get(ingredient);
    }

    public void setDishStock(Dish dish, Number number) {
        dishStockLevels.replace(dish, number);
    }

    public void setIngredientStock(Ingredient ingredient, Number number) {
        ingredientStockLevels.replace(ingredient, number);
    }
}
