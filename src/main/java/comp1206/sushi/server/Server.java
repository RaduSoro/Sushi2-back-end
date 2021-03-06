package comp1206.sushi.server;

import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	private StockManagement stockManagement = new StockManagement(this);
    public Configuration cfgReader;
	public Comms communcations;
	public DataPersistance dataPersistance;

	public Server() {
        logger.info("Starting up server...");
		communcations = new Comms(this);
		File configFile = new File("Sushi2-back-end\\src\\main\\java\\comp1206\\sushi\\server\\save.txt");
		if (!Files.exists(configFile.toPath()))
			cfgReader = new Configuration("Sushi2-back-end\\src\\main\\java\\comp1206\\sushi\\server\\cfg.txt", this); //running from compiler
			//if there is a data persistance load from it.
		else cfgReader = new Configuration("Sushi2-back-end\\src\\main\\java\\comp1206\\sushi\\server\\save.txt", this);
		dataPersistance = new DataPersistance(this);
		//cfgReader = new Configuration("src\\main\\java\\comp1206\\sushi\\server\\cfg.txt", this);//running from mvn
	}

	public synchronized void addStuffToDataPersistance() {
		System.out.println("Tring to clean");
		dataPersistance.clearArrays();
		dataPersistance.addStuffToObjectList(restaurant.getLocation());
		dataPersistance.addStuffToObjectList(restaurant);
		postcodes.forEach(postcode -> {
			if (!postcode.equals(restaurant.getLocation()))
				dataPersistance.addStuffToObjectList(postcode);
		});
		suppliers.forEach(supplier -> dataPersistance.addStuffToObjectList(supplier));
		ingredients.forEach(ingredient -> dataPersistance.addStuffToObjectList(ingredient));
		dishes.forEach(dish -> dataPersistance.addStuffToObjectList(dish));
		users.forEach(user -> dataPersistance.addStuffToObjectList(user));
		orders.forEach(order -> dataPersistance.addStuffToObjectList(order));
		dataPersistance.addStuffToObjectList(stockManagement);
		staff.forEach(staff1 -> dataPersistance.addStuffToObjectList(staff1));
		drones.forEach(drone -> dataPersistance.addStuffToObjectList(drone));
		dataPersistance.writeToFile();
	}
	
	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		this.dishes.add(newDish);
		stockManagement.addDishToTracking(newDish);
		communcations.broadcast(newDish);
		this.notifyUpdate();
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		communcations.broadcast(new ComplexMessage(dish, "delete"));
		this.notifyUpdate();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		return stockManagement.getDishStockLevels();
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
        //TO DO
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
        //TO DO
	}

	@Override
	public void setStock(Dish dish, Number stock) {
		stockManagement.setDishStock(dish, stock);
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		stockManagement.setIngredientStock(ingredient, stock);
	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name,unit,supplier,restockThreshold,restockAmount,weight);
		this.ingredients.add(mockIngredient);
		stockManagement.addIngredientToTracking(mockIngredient);
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) {
        this.ingredients.remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name,postcode);
		this.suppliers.add(mock);
		this.notifyUpdate();
		return mock;
	}


	@Override
	public void removeSupplier(Supplier supplier) {
        this.suppliers.remove(supplier);
		this.notifyUpdate();
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone mock = new Drone(speed);
		mock.setDroneStaffManagement(this.stockManagement);
		this.drones.add(mock);
		mock.setServer(this);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removeDrone(Drone drone) {
        this.drones.remove(drone);
		this.notifyUpdate();
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff mock = new Staff(name);
		this.staff.add(mock);
		mock.setStaffStockManagent(stockManagement);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removeStaff(Staff staff) {
		this.staff.remove(staff);
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) {
		this.orders.remove(order);
		communcations.sendObject(new ComplexMessage(order, "delete"), communcations.userToSocket(order.getUser().getName()));
		this.notifyUpdate();
	}
	
	@Override
	public Number getOrderCost(Order order) {
		return order.getPrice();
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		return stockManagement.getIngredientStockLevels();
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
        Order mock = order;
		return mock.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if (quantity.equals(Integer.valueOf(0))) {
			removeIngredientFromDish(dish,ingredient);
		} else {
			dish.getRecipe().put(ingredient,quantity);
		}
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock = new Postcode(code);
		mock.calculateDistance(restaurant);
		if (stockManagement.getRestaurant() == null) stockManagement.setRestaurant(restaurant);
		this.postcodes.add(mock);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		this.postcodes.remove(postcode);
		this.notifyUpdate();
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}
	
	@Override
	public void removeUser(User user) {
		this.users.remove(user);
		this.notifyUpdate();
	}

	@Override
	public void loadConfiguration(String filename) {
		postcodes.clear();
		restaurant = null;
		suppliers.clear();
		ingredients.clear();
		dishes.clear();
		users.clear();
		orders.clear();
		dataPersistance.addStuffToObjectList(stockManagement);
		staff.clear();
		drones.clear();
		cfgReader = new Configuration(filename, this);
		System.gc();
		this.notifyUpdate();
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for(Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
		}
		this.notifyUpdate();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return order.getStatus().equals("Complete");
	}

	@Override
	public String getOrderStatus(Order order) {
		return order.getStatus();
	}


	public void setOrderStatus(Order order, String status) {
		order.setStatus(status);
        Socket userSocket = communcations.userToSocket(order.getUser().getName());
		if (status.equals("Complete") && userSocket != null) {
			String parsable = order.getOrderNumber() + ":" + "Complete";
            communcations.sendObject(parsable, userSocket);
		} else if (userSocket != null && !status.equals("Complete"))
            communcations.sendObject(new ComplexMessage(order, "update status"), userSocket);
	}

	@Override
	public String getDroneStatus(Drone drone) {
		return drone.getStatus();
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		return staff.getStatus();
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold(restockThreshold);
		dish.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
		try {
			addStuffToDataPersistance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
	}

	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}
}
