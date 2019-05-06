package comp1206.sushi.common;

import java.io.Serializable;

public class Ingredient extends Model implements Serializable {

	private String name;
	private String unit;
	private Supplier supplier;
	private Number restockThreshold;
	private Number restockAmount;
	private Number futureValue;
	private Number weight;

	public Ingredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount, Number weight) {
		this.setName(name);
		this.setUnit(unit);
		this.setSupplier(supplier);
		this.setRestockThreshold(restockThreshold);
		this.setRestockAmount(restockAmount);
		this.setWeight(weight);
		this.futureValue = 0;
	}

	public String getName() {
		return name;
	}

	public synchronized Number getFutureValue() {
		return this.futureValue;
	}

	public void setFutureValue(Number futureValue) {
		this.futureValue = futureValue;
	}

	public synchronized void increaseFutureValue(Number number) {
		this.futureValue = this.futureValue.intValue() + number.intValue();
	}

	public synchronized void decreaseFutureValue(Number number) {
		this.futureValue = this.futureValue.intValue() - number.intValue();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public Number getRestockThreshold() {
		return restockThreshold;
	}

	public void setRestockThreshold(Number restockThreshold) {
		this.restockThreshold = restockThreshold;
	}

	public Number getRestockAmount() {
		return restockAmount;
	}

	public void setRestockAmount(Number restockAmount) {
		this.restockAmount = restockAmount;
	}

	public Number getWeight() {
		return weight;
	}

	public void setWeight(Number weight) {
		this.weight = weight;
	}
}
