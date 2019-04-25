package comp1206.sushi.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User extends Model {
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private HashMap<Dish, Number> basket;
	private List<Order> orderHistory;
	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		basket = new HashMap<>();
		orderHistory = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addToBasket(Dish dish, Number quantity) {
		this.basket.put(dish, quantity);
	}

	public void UpdateBasket(Dish dish, Number quantity) {
		this.basket.replace(dish, basket.get(dish), quantity);
	}

	public void clearBasket() {
        this.basket.clear();
	}

	public HashMap<Dish, Number> getBasket() {
		return this.basket;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}

	public String getPassword() {
		return this.password;
	}

	public void addBasketToOrderHistory(Order order) {
		orderHistory.add(order);
	}

	public List<Order> getOrderHistory() {
		return this.orderHistory;
	}
}
