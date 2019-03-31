package comp1206.sushi.common;

import java.util.HashMap;

public class Order extends Model {

	private String status;
	private HashMap<Dish, Integer> order;
	private User sushiEater;

	public Order(HashMap<Dish, Integer> order, User sushiEater) {
		this.order = order;
		this.sushiEater = sushiEater;
	}

	public Number getDistance() {
		return 1;
	}

	@Override
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

}
