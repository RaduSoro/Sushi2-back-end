package comp1206.sushi.common;

import comp1206.sushi.server.StockManagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Staff extends Model implements Runnable, Serializable {

	private String name;
	private String status;
	private Number fatigue;
	private Thread thread;
	private HashMap<Dish, Number> dishList;
	private StockManagement stockManagement;
	
	public Staff(String name) {
		this.setName(name);
		this.setFatigue(0);
		thread = new Thread(this, name);
		this.status = "Idle";
		thread.start();
	}

	public void setStaffStockManagent(StockManagement stockManagent) {
		this.stockManagement = stockManagent;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	@Override
	public void run() {
		try {
			System.out.println("Running thread " + Thread.currentThread());
			Thread.sleep(50);
			while (true) {
				//sleeps if the fatigue is about
				if (getFatigue().intValue() == 100) {
					setStatus("Break");
					Thread.sleep(60000);
					this.fatigue = 0;
				}
				checkDishesStock();
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName() + "stoppped working");
		}
	}

	public void prepareDish(Dish dish) {
        if (hasEnoughIngredients(dish)) {
			dish.getRecipe().forEach((k, v) -> {
				stockManagement.setIngredientStock(k, stockManagement.getCurrentStockIngredient(k).intValue() - v.intValue());
			});
            int timeToWait = ThreadLocalRandom.current().nextInt(20000, 60001);
			try {
				Thread.sleep(timeToWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stockManagement.setDishStock(dish, stockManagement.getCurrentStockDish(dish).intValue() + dish.getRestockAmount().intValue());
			dish.decreaseFutureValue(dish.getRestockAmount());
			fatigue = fatigue.intValue() + 5;
		}
		this.setStatus("Idle");
	}

	private synchronized void checkDishesStock() {
		Dish dishToRestock = null;
		for (Map.Entry<Dish, Number> entry : stockManagement.getDishStockLevels().entrySet()) {
			Dish dish = entry.getKey();
			Number dishStock = entry.getValue();
			if (dish.getRestockThreshold().intValue() > dishStock.intValue() && dish.getFutureValue().intValue() + dishStock.intValue() < dish.getRestockThreshold().intValue()) {
				dishToRestock = dish;
				this.setStatus("Prearing dish " + dish);
				break;
			}
		}
		if (dishToRestock != null) {
			prepareDish(dishToRestock);
		}
	}

    private boolean hasEnoughIngredients(Dish dish) {
        for (Map.Entry<Ingredient, Number> entry : dish.getRecipe().entrySet()) {
            Ingredient k = entry.getKey();
            Number v = entry.getValue();
            if (stockManagement.getCurrentStockIngredient(k).intValue() < v.intValue()) {
				return false;
            }
        }
		dish.increaseFutureValue(dish.getRestockAmount());
		return true;
    }
}
