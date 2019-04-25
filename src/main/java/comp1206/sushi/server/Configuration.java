package comp1206.sushi.server;

import comp1206.sushi.common.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Pattern;

class Configuration {
    private String fileLocation;
    private File cfgFile;
    private BufferedReader reader = null;
    private String currentLine = null;
    private Server server;
    private String[] identifierArray;

    /**
     * @param fileLocation the location of the file to read the config from
     *                     creates a new private file and assigns the File fileLocation
     *                     and BufferedReader to private variables
     * @example new Configuration("file.txt")
     */
    public Configuration(String fileLocation, Server server) {
        this.fileLocation = fileLocation;
        this.cfgFile = new File(this.fileLocation);
        this.server = server;
        createBufferedReader();
        loadConfig();

    }

    /**
     * Creates a new @BufferedReader, throws exception if the file is not found
     */
    private void createBufferedReader() {
        try {
            this.reader = new BufferedReader(new FileReader(this.cfgFile));
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }


    public void loadConfig() {
        try {
            currentLine = reader.readLine();
            while (currentLine != null) {
                identifyLine();
                currentLine = reader.readLine();
            }
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    private void identifyLine() {
        //identifierArray gets the contentet of the currentline splited individualy after :
        //each string in a different index
        identifierArray = currentLine.split(":");
        //case @Postcode
        if (identifierArray[0].toLowerCase().matches("postcode")) server.addPostcode(identifierArray[1]);

            //case RESTAURANT
        else if (identifierArray[0].toLowerCase().matches("restaurant")) {
            String restaurantName = identifierArray[1];
            String restaurantPostcodeString = identifierArray[2];
            Postcode restaurantPostcode = stringToPostcode(restaurantPostcodeString);
            server.restaurant = new Restaurant(restaurantName, restaurantPostcode);
        }
        //case @Supplier
        else if (identifierArray[0].toLowerCase().matches("supplier")) {
            String supplierName = identifierArray[1];
            String supplerPostcodeString = identifierArray[2];
            Postcode supplerPostcode = stringToPostcode(supplerPostcodeString);
            server.addSupplier(supplierName, supplerPostcode);
        }
        //case @Ingredient
        else if (identifierArray[0].toLowerCase().matches("ingredient")) {
            String ingredientName = identifierArray[1];
            String ingredientUnit = identifierArray[2];
            String supplierString = identifierArray[3];
            Number restockThreshold = Integer.valueOf(identifierArray[4]);
            Number restockAmount = Integer.valueOf(identifierArray[5]);
            Number weight = Integer.valueOf(identifierArray[6]);
            Supplier supplierObject = stringToSupplier(supplierString);
            server.addIngredient(ingredientName, ingredientUnit, supplierObject, restockThreshold, restockAmount, weight);
        }
        //case @Dish
        else if (identifierArray[0].toLowerCase().matches("dish")) {
            Number dishPrice = Integer.valueOf(identifierArray[3]);
            Number dishRestockThreshold = Integer.valueOf(identifierArray[4]);
            Number dishRestockAmount = Integer.valueOf(identifierArray[5]);

            Dish dish = server.addDish(identifierArray[1], identifierArray[2], dishPrice, dishRestockThreshold, dishRestockAmount);

            String[] comaSpliter = identifierArray[6].split(",");
            for (String starSplitter : comaSpliter) {
                String[] otherSplit = Pattern.compile(" \\* ").split(starSplitter);
                server.addIngredientToDish(dish, stringToIngredient(otherSplit[1]), Integer.valueOf(otherSplit[0]));
            }
        }
        //case @User
        else if (identifierArray[0].toLowerCase().matches("user"))
            server.users.add(new User(identifierArray[1], identifierArray[2], identifierArray[3], stringToPostcode(identifierArray[4])));

            //case @Order
        else if (identifierArray[0].toLowerCase().matches("order")) {
            User user = stringToUser(identifierArray[1]);
            HashMap<Dish, Number> order = new HashMap<>();
            String[] comaSplitter = identifierArray[2].split(",");
            for (String starSplitter : comaSplitter) {
                String[] otherSplit = Pattern.compile(" \\* ").split(starSplitter);
                order.put(stringToDish(otherSplit[1]), Integer.valueOf(otherSplit[0]));
            }
            server.orders.add(new Order(order, user));
        }
        //case Stock
        else if (identifierArray[0].toLowerCase().matches("stock")) {
            if (stringToIngredient(identifierArray[1]) != null)
                server.setStock(stringToIngredient(identifierArray[1]), Integer.valueOf(identifierArray[2]));
            else if (stringToDish(identifierArray[1]) != null)
                server.setStock(stringToDish(identifierArray[1]), Integer.valueOf(identifierArray[2]));
        }
        //case @Staff
        else if (identifierArray[0].toLowerCase().matches("staff")) server.addStaff(identifierArray[1]);

            //case drone
        else if (identifierArray[0].toLowerCase().matches("drone"))
            server.addDrone(Integer.valueOf(identifierArray[1]));

        server.notifyUpdate();
    }


    /**
     * @param @String entry
     * @return @Postcode refference towards the object in the server
     */
    private Postcode stringToPostcode(String input) {
        //gets the correct postcode object to make sure there are no duplicate objects
        return server.getPostcodes().stream().filter(postcode -> postcode.toString().equals(input)).findFirst().orElse(null);
    }

    /**
     * @param @String entry
     * @return @Supplier refference towards the object in the server
     */
    private Supplier stringToSupplier(String input) {
        return server.getSuppliers().stream().filter(supplier -> supplier.getName().equals(input)).findFirst().orElse(null);
    }

    /**
     * @param input the string value of the ingredient
     * @return @Ingredient object that is stored in server or null otherwise
     */
    private Ingredient stringToIngredient(String input) {
        return server.getIngredients().stream().filter(ingredient -> ingredient.getName().equals(input)).findFirst().orElse(null);
    }

    /**
     * @param input the string value of the user
     * @return @User object that is stored in server or null otherwise
     */
    private User stringToUser(String input) {
        return server.getUsers().stream().filter(user -> user.getName().equals(input)).findFirst().orElse(null);
    }

    /**
     * @param input the string value of the dish
     * @return @Dish object that is stored in server or null otherwise
     * @
     */
    private Dish stringToDish(String input) {
        return server.getDishes().stream().filter(dish -> dish.getName().equals(input)).findFirst().orElse(null);
    }
}