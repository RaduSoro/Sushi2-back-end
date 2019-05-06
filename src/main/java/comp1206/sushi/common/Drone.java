package comp1206.sushi.common;

import comp1206.sushi.server.Server;
import comp1206.sushi.server.StockManagement;

import java.io.Serializable;
import java.util.Map;

public class Drone extends Model implements Serializable {

	private Number speed;
	private Number progress;
	
	private Number capacity;
	private Number battery;
	private Server server;
	private String status;
    private StockManagement stockManagement;
	private Postcode source;
	private Postcode destination;
    private Number totalDistance;
    private Number procent;
	private Thread thread;

	public Drone(Number speed) {
		this.setSpeed(speed);
		this.setCapacity(1);
		this.setBattery(100);
        totalDistance = 0;
        procent = 0;
		thread = new Thread() {
            @Override
            public void run() {
				while (true) {
					checkIngredientsStock();
					deliverOrder();
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
    }

	public void setServer(Server server) {
		this.server = server;
	}
    public void setDroneStaffManagement(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }

	public void deliverOrder() {
		Order order = stockManagement.getReadyOrders();
		if (order != null) {
			this.destination = order.getUser().getPostcode();
			this.source = stockManagement.getRestaurant().getLocation();
			totalDistance = order.getDistance().doubleValue() * 2;
			this.setStatus("Delivering order " + order);
			double distanceCovered = 0;
			server.setOrderStatus(order, "Delivering");
			moveDrone(totalDistance, distanceCovered);
			//should be completed
			server.setOrderStatus(order, "Completed");
			this.setStatus("Returning");
			distanceCovered = 0;
			this.source = destination;
			this.destination = stockManagement.getRestaurant().getLocation();
			moveDrone(totalDistance, distanceCovered);
			this.setProgress(null);
			this.destination = null;
			this.source = null;
			this.setStatus("Idle");
		}
	}

    public synchronized void checkIngredientsStock() {
        Ingredient ingredientToRestock = null;
        for (Map.Entry<Ingredient, Number> entry : stockManagement.getIngredientStockLevels().entrySet()) {
            Ingredient ingredient = entry.getKey();
            Number ingredientStock = entry.getValue();
            if (ingredient.getRestockThreshold().intValue() > ingredientStock.intValue() && ingredient.getFutureValue().intValue() + ingredientStock.intValue() < ingredient.getRestockThreshold().intValue()) {
                ingredientToRestock = ingredient;
                ingredient.increaseFutureValue(ingredient.getRestockAmount());
                this.setStatus("Restocking ingredient " + ingredient);
                ingredient.increaseFutureValue(ingredient.getRestockAmount());
                break;
            }
        }
        if (ingredientToRestock != null) {
            restockIngredient(ingredientToRestock);
        }
    }

	public void moveDrone(Number totalDistance, double distranceCovered) {
		while (distranceCovered <= totalDistance.doubleValue()) {
			procent = distranceCovered / totalDistance.doubleValue() * 100;
			this.setProgress(procent.intValue());
			this.notifyUpdate();
			distranceCovered = distranceCovered + this.getSpeed().doubleValue();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public void restockIngredient(Ingredient ingredient) {
        this.destination = ingredient.getSupplier().getPostcode();
        this.source = stockManagement.getRestaurant().getLocation();
        totalDistance = destination.getDistance().intValue() * 2;
        this.setStatus("Restocking " + ingredient.getName());
        double distranceCovered = 0;
        //moves the drone by  speed every second
		moveDrone(totalDistance, distranceCovered);
        stockManagement.setIngredientStock(ingredient, stockManagement.getCurrentStockIngredient(ingredient).intValue() + ingredient.getRestockAmount().intValue());
        ingredient.decreaseFutureValue(ingredient.getRestockAmount());
        this.setProgress(null);
        this.destination = null;
        this.source = null;
        this.setStatus("Idle");

    }

	public Number getSpeed() {
		return speed;
	}

	
	public Number getProgress() {
		return progress;
	}
	
	public void setProgress(Number progress) {
		this.progress = progress;
	}
	
	public void setSpeed(Number speed) {
		this.speed = speed;
	}
	
	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		this.destination = destination;
	}

	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
}
