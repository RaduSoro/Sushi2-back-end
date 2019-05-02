package comp1206.sushi.common;

import java.io.Serializable;
import java.util.HashMap;

public class Order extends Model implements Serializable {

	private String status = "Incomplete";
	private HashMap<Dish, Number> order;
	private User sushiEater;

	public HashMap<Dish, Number> getOrderDishes() {
		return this.order;
	}

	public Order(HashMap<Dish, Number> order, User sushiEater) {
		this.order = new HashMap<>();
		this.order.putAll(order);
		this.sushiEater = sushiEater;
	}

	public Number getDistance() {
		return sushiEater.getDistance();
	}

	public String getName() {
		return this.sushiEater.toString();
	}
	public void setSushiEater(User user){
		this.sushiEater = user;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	public User getUser() {
		return this.sushiEater;
	}

	public Number getPrice() {
		return this.order.keySet().stream().mapToInt(dish -> dish.getPrice().intValue() * order.get(dish).intValue()).sum();
	}

}
