package comp1206.sushi.server;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Restaurant;

import java.util.HashMap;
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

    /**
     * Decreases the stock of the dish by the number you give it.
     *
     * @param dish   dish object you want to acces
     * @param number the change in value for that dish.
     */
    public void decreaseDishStockByNumber(Dish dish, Number number) {
        dishStockLevels.replace(dish, dishStockLevels.get(dish).intValue() - number.intValue());
    }

    /**
     * @param dishFromClient the dish object from client
     * @return Dish
     * from server with the same name. Combats the null pointer execption from client dish
     */
    public Dish clientDishToServerDish(Dish dishFromClient) {
        return server.getDishes().stream().filter(dish -> dish.getName().equals(dishFromClient.getName())).findFirst().orElse(null);
    }

    /**
     * Searches the server for the first incomplete order, sets status to Delivering and sends it to
     * the drone.
     *
     * @return Order
     */
    public Order getReadyOrders() {
        Order orderToReturn = getFirstReadyOrder();
        if (orderToReturn != null) {
            for (Map.Entry<Dish, Number> entry : orderToReturn.getBufferOrder().entrySet()) {
                Dish dish = entry.getKey();
                Number number = entry.getValue();
                Dish serverSideDish = clientDishToServerDish(dish);
                //if the ammount in server is lower than the order total move everything in order to trigger restocking
                if (dishStockLevels.get(serverSideDish).intValue() <= number.intValue()) {
                    orderToReturn.getBufferOrder().replace(dish, number.intValue() - dishStockLevels.get(serverSideDish).intValue());
                    setDishStock(serverSideDish, 0);
                } else if (dishStockLevels.get(serverSideDish).intValue() > number.intValue()) {
                    decreaseDishStockByNumber(serverSideDish, number);
                    orderToReturn.getBufferOrder().replace(dish, 0);
                }
            }

        }
        if (orderToReturn != null && orderHasAllDishesReady(orderToReturn)) return orderToReturn;
        else {
            if (orderToReturn != null) orderToReturn.setStatus("Incomplete");
            return null;
        }
    }

    public synchronized Order getFirstReadyOrder() {
        Order orderToReturn = server.getOrders().stream().filter(order -> order.getStatus().equals("Incomplete")).findAny().orElse(null);
        if (orderToReturn != null) orderToReturn.setStatus("Checking ingredients");
        return orderToReturn;
    }

    public synchronized boolean orderHasAllDishesReady(Order order) {
        for (Map.Entry<Dish, Number> entry : order.getBufferOrder().entrySet()) {
            Dish dish = entry.getKey();
            Number number = entry.getValue();
            if (number.intValue() != 0) {
                return false;
            }
        }
        order.setStatus("Delivering");
        return true;
    }
}
