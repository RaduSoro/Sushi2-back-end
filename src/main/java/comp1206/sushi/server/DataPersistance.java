package comp1206.sushi.server;

import comp1206.sushi.common.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

public class DataPersistance {
    public ArrayList<String> stuffToWriteInFile;
    public ArrayList<Object> objectList;
    private Server server;


    public DataPersistance(Server server) {
        this.server = server;
        stuffToWriteInFile = new ArrayList<>();
        objectList = new ArrayList<>();
    }

    public void clearArrays() {
        this.stuffToWriteInFile.clear();
        this.objectList.clear();
    }

    public void addStuffToObjectList(Object o) {
        objectList.add(o);
    }

    public synchronized void writeToFile() {
        File tmpFile = new File("E:\\sushi2\\Sushi2-back-end\\src\\main\\java\\comp1206\\sushi\\server\\save.txt");
        try {
            Files.deleteIfExists(tmpFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        objectList.forEach(o -> {
            stuffToWriteInFile.add(addToArray(o));
        });
        try {
            FileWriter writer = new FileWriter(tmpFile);
            stuffToWriteInFile.forEach(s -> {
                try {

                    if (s != null) {
                        writer.write(s);
                        writer.write(System.getProperty("line.separator"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String addToArray(Object o) {
        //case restaurant
        if (o instanceof Restaurant) {
            return "RESTAURANT:" + ((Restaurant) o).getName() + ":" + ((Restaurant) o).getLocation();
        } else if (o instanceof Postcode) {
            return "POSTCODE:" + ((Postcode) o).getName();
        } else if (o instanceof Supplier) {
            return "SUPPLIER:" + ((Supplier) o).getName() + ":" + ((Supplier) o).getPostcode();
        } else if (o instanceof Ingredient) {
            return "INGREDIENT:" + ((Ingredient) o).getName() + ":" + ((Ingredient) o).getUnit() + ":" + ((Ingredient) o).getSupplier() + ":" + ((Ingredient) o).getRestockThreshold() + ":" + ((Ingredient) o).getRestockAmount() + ":" + ((Ingredient) o).getWeight();
        } else if (o instanceof Dish) {
            String stringToReturn = "DISH:" + ((Dish) o).getName() + ":" + ((Dish) o).getDescription() + ":" + ((Dish) o).getPrice() + ":" + ((Dish) o).getRestockThreshold() + ":" + ((Dish) o).getRestockAmount() + ":";
            Dish dish = (Dish) o;
            for (Map.Entry<Ingredient, Number> entry : dish.getRecipe().entrySet()) {
                Ingredient ingredient = entry.getKey();
                Number number = entry.getValue();
                stringToReturn = stringToReturn + number + " * " + ingredient + ",";
            }
            stringToReturn = stringToReturn.substring(0, stringToReturn.length() - 1);
            return stringToReturn;
        } else if (o instanceof User) {
            return "USER:" + ((User) o).getName() + ":" + ((User) o).getPassword() + ":" + ((User) o).getAddress() + ":" + ((User) o).getPostcode();
        } else if (o instanceof Staff) {
            return "STAFF:" + ((Staff) o).getName();
        } else if (o instanceof Drone) {
            return "DRONE:" + ((Drone) o).getSpeed();
        } else if (o instanceof Order) {
            String stringToReturn = "ORDER:" + ((Order) o).getUser() + ":";
            Order order = (Order) o;
            for (Map.Entry<Dish, Number> entry : order.getOrderDishes().entrySet()) {
                Dish dish = entry.getKey();
                Number number = entry.getValue();
                stringToReturn = stringToReturn + number + " * " + dish + ",";
            }
            stringToReturn = stringToReturn.substring(0, stringToReturn.length() - 1);
            return stringToReturn;
        } else if (o instanceof StockManagement) {
            StockManagement sm = (StockManagement) o;
            Map<Ingredient, Number> ingredientNumberMap = sm.getIngredientStockLevels();
            Map<Dish, Number> dishNumberMap = sm.getDishStockLevels();
            if (ingredientNumberMap != null) ingredientNumberMap.forEach((ingredient, number) -> {
                stuffToWriteInFile.add("STOCK:" + ingredient + ":" + number);
            });
            if (dishNumberMap != null) dishNumberMap.forEach((dish, number) -> {
                stuffToWriteInFile.add("STOCK:" + dish + ":" + number);
            });
            return null;
        }
        return null;
    }

}
