package comp1206.sushi.common;

import java.util.HashMap;

public class Order extends Model {

	private String status = "Incomplete";
	private HashMap<Dish, Number> order;
	private User sushiEater;

	public Order(HashMap<Dish, Number> order, User sushiEater) {
		this.order = order;
		this.sushiEater = sushiEater;
	}

	public Number getDistance() {
		return 1;
	}

	public String getName() {
		return this.sushiEater.toString();
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
		return order.keySet().stream().mapToInt(dish -> dish.getPrice().intValue() * order.get(dish).intValue()).sum();
	}

}
