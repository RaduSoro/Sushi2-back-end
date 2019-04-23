package comp1206.sushi.client;


import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client implements ClientInterface {


    private static final Logger logger = LogManager.getLogger("Client");
    public ArrayList<Dish> dishes = new ArrayList<Dish>();
    public ArrayList<Drone> drones = new ArrayList<Drone>();
    public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
    public ArrayList<Order> orders = new ArrayList<Order>();
    public ArrayList<Staff> staff = new ArrayList<Staff>();
    public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
    public ArrayList<User> users = new ArrayList<User>();
    public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
    Postcode restaurantPostcode = new Postcode("SO17 1BJ");
    public Restaurant restaurant = new Restaurant("Mine", restaurantPostcode);
    private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
    private ClientInterface client;

	public Client() {
        logger.info("Starting up client...");
        postcodes.add(restaurantPostcode);
        Postcode postcode1 = new Postcode("SO17 1TJ");
        postcodes.add(postcode1);
        Postcode postcode2 = new Postcode("SO17 1BX");
        postcodes.add(postcode2);
        Postcode postcode3 = new Postcode("SO17 2NJ");
        postcodes.add(postcode3);
        Postcode postcode4 = new Postcode("SO17 1TW");
        postcodes.add(postcode4);
        Postcode postcode5 = new Postcode("SO17 2LB");
        postcodes.add(postcode5);

        Supplier supplier1 = new Supplier("Supplier 1", postcode1);
        suppliers.add(supplier1);
        Supplier supplier2 = new Supplier("Supplier 2", postcode2);
        suppliers.add(supplier2);
        Supplier supplier3 = new Supplier("Supplier 3", postcode3);
        suppliers.add(supplier3);

        Ingredient ingredient1 = new Ingredient("Ingredient 1", "grams", supplier1, 1, 5, 1);
        ingredients.add(ingredient1);
        Ingredient ingredient2 = new Ingredient("Ingredient 2", "grams", supplier2, 1, 5, 1);
        ingredients.add(ingredient2);
        Ingredient ingredient3 = new Ingredient("Ingredient 3", "grams", supplier3, 1, 5, 1);
        ingredients.add(ingredient3);

        Dish dish1 = new Dish("Dish 1", "Dish 1", 1, 1, 10);
        dishes.add(dish1);
        Dish dish2 = new Dish("Dish 2", "Dish 2", 2, 1, 10);
        dishes.add(dish2);
        Dish dish3 = new Dish("Dish 3", "Dish 3", 3, 1, 10);
        dishes.add(dish3);

        HashMap<Dish, Number> orderMap = new HashMap<>();
        orderMap.put(dish1, 20);
        User user1 = register("admin", "admin", "zepler", postcode1);
        Order order1 = new Order(orderMap, user1);
        orders.add(order1);
        user1.addBasketToOrderHistory(order1);

        dish1.getRecipe().put(ingredient1, 1);
        dish1.getRecipe().put(ingredient2, 2);
        dish2.getRecipe().put(ingredient2, 3);
        dish2.getRecipe().put(ingredient2, 3);
        dish3.getRecipe().put(ingredient3, 1);
        dish3.getRecipe().put(ingredient2, 3);


        staff.add(new Staff("Staff 1"));
        staff.add(new Staff("Staff 2"));
        staff.add(new Staff("Staff 3"));

        drones.add(new Drone(1));
        drones.add(new Drone(2));
        drones.add(new Drone(3));
	}
	@Override
	public Restaurant getRestaurant() {
        return this.restaurant;
	}
	
	@Override
	public String getRestaurantName() {
        return this.restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
        return this.restaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
        User mockuser = new User(username, password, address, postcode);
        users.add(mockuser);
        this.notifyUpdate();
        return mockuser;
	}

	@Override
	public User login(String username, String password) {
        return users.stream().filter(user -> user.getName().equals(username) && user.getPassword().equals(password)).findFirst().orElse(null);
	}

	@Override
	public List<Postcode> getPostcodes() {
        return this.postcodes;
	}

	@Override
	public List<Dish> getDishes() {
        return this.dishes;
	}

	@Override
	public String getDishDescription(Dish dish) {
        return dishes.get(dishes.indexOf(dish)).getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
        return dishes.get(dishes.indexOf(dish)).getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
        return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
        return user.getBasket().keySet().stream().mapToInt(dish -> dish.getPrice().intValue() * user.getBasket().get(dish).intValue()).sum();
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
        if (quantity.intValue() > 0) {
            user.addToBasket(dish, quantity);
            this.notifyUpdate();
        }
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
        user.UpdateBasket(dish, quantity);
	}

	@Override
	public Order checkoutBasket(User user) {
        Order orderToProcess = new Order(user.getBasket(), user);
        orders.add(orderToProcess);
        user.addBasketToOrderHistory(orderToProcess);
        clearBasket(user);
        this.notifyUpdate();
        return orderToProcess;
	}

	@Override
	public void clearBasket(User user) {
        user.clearBasket();
	}

	@Override
	public List<Order> getOrders(User user) {
        return user.getOrderHistory();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getOrderStatus(Order order) {
        return order.getStatus();
	}

	@Override
	public Number getOrderCost(Order order) {
        return order.getPrice();
	}

	@Override
	public void cancelOrder(Order order) {
        order.setStatus("Canceled");
        order.getUser().getOrderHistory().remove(order);
        this.notifyUpdate();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
        this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

}
