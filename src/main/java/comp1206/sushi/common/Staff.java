package comp1206.sushi.common;

import comp1206.sushi.server.StockManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Staff extends Model implements Runnable {

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
			while (thread.isAlive()) {
				if (status.toLowerCase().equals("idle")) {
					checkDishesStock();
				}
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
			int timeToWait = ThreadLocalRandom.current().nextInt(20000, 61000);
			System.out.println(timeToWait / 1000 + " seconds wating");
			try {
				Thread.sleep(timeToWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " has finished preparing dish ");
			System.out.println("old value" + stockManagement.getCurrentStockDish(dish) + " " + dish);
			stockManagement.setDishStock(dish, stockManagement.getCurrentStockDish(dish).intValue() + 1);
			System.out.println("new value " + stockManagement.getCurrentStockDish(dish) + " " + dish);
			dish.decreaseFutureValue();
			this.setStatus("Idle");
		}
	}

	private synchronized void checkDishesStock() {
		Dish dishToRestock = null;
		Boolean foundDish = false;
		for (Map.Entry<Dish, Number> entry : stockManagement.getDishStockLevels().entrySet()) {
			Dish k = entry.getKey();
			Number v = entry.getValue();
            if (k.getRestockThreshold().intValue() > v.intValue() && k.getFutureValue().intValue() + v.intValue() < k.getRestockThreshold().intValue()) {
				dishToRestock = k;
				foundDish = true;
				k.increaseFutureValue();
				this.setStatus("Prearing dish " + k);
				break;
			}
		}
		if (foundDish) {
			prepareDish(dishToRestock);
		}
	}

    private boolean hasEnoughIngredients(Dish dish) {
        boolean hasEnoughIngredients = true;
        for (Map.Entry<Ingredient, Number> entry : dish.getRecipe().entrySet()) {
            Ingredient k = entry.getKey();
            Number v = entry.getValue();
            if (stockManagement.getCurrentStockIngredient(k).intValue() < v.intValue()) {
                hasEnoughIngredients = false;
                break;
            }
        }
        return hasEnoughIngredients;
    }
}
