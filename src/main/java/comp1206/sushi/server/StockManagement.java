package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Restaurant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockManagement {
    private HashMap<Ingredient, Number> ingredientStockLevels = new HashMap<>();
    private HashMap<Dish, Number> dishStockLevels = new HashMap<>();
    private Restaurant restaurant = null;
    private Server server;

    public StockManagement(Server server) {
        this.server = server;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void addToCurrentStock(Ingredient ingredient) {

    }

    /**
     * getter for the map with ingredients mapped to their stock
     *
     * @return @Map<Ingredient,Number> </Ingredient,Number>
     */
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return ingredientStockLevels;
    }

    /**
     * @synchronized to make sure only one thread can acces the stock values at a time
     *
     * @return @Map<Dish,Number></Dish,Number> to acces the dish stock
     * getter for the map with dishes mapped to their stock
     */
    public synchronized Map<Dish, Number> getDishStockLevels() {
        return dishStockLevels;
    }

    /**
     * adds the ingredient to the ingridient map, by default with 0 stock
     * @param ingredient
     */
    public void addIngredientToTracking(Ingredient ingredient) {
        this.ingredientStockLevels.put(ingredient, 0);
    }

    /**
     * adds the dish to the stock levels, by default with 0 stock
     * @param dish
     */
    public void addDishToTracking(Dish dish) {
        this.dishStockLevels.put(dish, 0);
    }

    /**
     * @param dish
     * @return @Number (the value of the dish key from map)
     */
    public Number getCurrentStockDish(Dish dish) {
        return dishStockLevels.get(dish);
    }

    /**
     * @param ingredient
     * @return @Number (the value od the ingredieny key from the map)
     */
    public Number getCurrentStockIngredient(Ingredient ingredient) {
        return ingredientStockLevels.get(ingredient);
    }

    /**
     * @param dish the dish you want to modify the stock lever
     * @param number the new stock level
     */
    public void setDishStock(Dish dish, Number number) {
        dishStockLevels.replace(dish, number);
    }

    /**
     * @param ingredient the dish you want to modify the stock lever
     * @param number the new stock level
     */
    public void setIngredientStock(Ingredient ingredient, Number number) {
        ingredientStockLevels.replace(ingredient, number);
    }

    public List<Order> getReadyOrders() {
        return server.getOrders();
    }
}
