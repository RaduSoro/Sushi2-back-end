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
    private Number distance;
    private Number procent;
	private Thread thread;

	public Drone(Number speed) {
		this.setSpeed(speed);
		this.setCapacity(1);
		this.setBattery(100);
		this.status = "Idle";
        distance = 0;
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
			distance = order.getDistance().doubleValue() * 2;
			this.setStatus("Flying");
			server.notifyUpdate();
			double distanceCovered = 0;
            server.setOrderStatus(order, "Delivering");
			moveDrone(distance.doubleValue(), distanceCovered, distance.doubleValue() / 2);
			//should be completed
			server.notifyUpdate();
			server.setOrderStatus(order, "Complete");
			this.source = destination;
			this.destination = stockManagement.getRestaurant().getLocation();
			this.setStatus("Flying");
			moveDrone(distance.doubleValue(), distance.doubleValue() / 2, distance.doubleValue());
			this.setProgress(null);
			this.destination = null;
			this.source = null;

			this.setStatus("Idle");
			server.notifyUpdate();
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
                break;
            }
        }
        if (ingredientToRestock != null) {
            restockIngredient(ingredientToRestock);
        }
    }

	public void moveDrone(double totalDistance, double distranceCovered, double DistanceToMove) {
		while (distranceCovered <= DistanceToMove) {
			procent = distranceCovered / totalDistance * 100;
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
		distance = destination.getDistance().doubleValue() * 2;
		this.setStatus("Flying");
        double distanceCovered = 0;
        //moves the drone by  speed every second
		moveDrone(distance.doubleValue(), distanceCovered, distance.doubleValue() / 2);
		this.setStatus("Flying");
        this.source = destination;
        this.destination = stockManagement.getRestaurant().getLocation();
		moveDrone(distance.doubleValue(), distance.doubleValue() / 2, distance.doubleValue());
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
