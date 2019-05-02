package comp1206.sushi.client;

import comp1206.sushi.common.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Comms implements Runnable {
    private Client client;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private Thread thread;
    private boolean foundHost = false;

    public Comms(Client client) {
        this.client = client;
        thread = new Thread(this, "client");
        thread.start();
        connection();
    }

    public void run() {
        while (thread.isAlive()) {
            receiveObject();
        }
    }

    private void connection() {
        while (!this.foundHost) {
            try {
                socket = new Socket("127.0.0.2", 5000);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                foundHost = true;
            } catch (UnknownHostException e) {
                System.out.println("Waiting for server...");
                foundHost = false;
            } catch (IOException i) {
                System.out.println("Waiting for server...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
                foundHost = false;
            }
        }
    }

    public void sendObject(Object object) {
        if (foundHost) {
            try {
                System.out.println("Trying to send object " + object);
                this.objectOutputStream.writeObject(object);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    public void receiveObject() {
        while (foundHost) {
            try {
                Object o = objectInputStream.readObject();
                handleInput(o);
            } catch (EOFException eof){
                foundHost=false;
            }
            catch (SocketException b) {
                foundHost = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleInput(Object o){
        if (o instanceof Dish){
            //if the element is not in the client update the value else add it
            Dish dishInClient = client.getDishes().stream().filter(dish -> dish.getName().equals(((Dish) o).getName())).findFirst().orElse(null);
            if (dishInClient == null) client.getDishes().add((Dish) o);
            else dishInClient = (Dish) o;
        }else if (o instanceof Restaurant){
            client.setRestaurant((Restaurant) o);
        }else if(o instanceof Postcode){
            Postcode postcodeInClient = client.getPostcodes().stream().filter(postcode -> postcode.getName().equals(((Postcode) o).getName())).findFirst().orElse(null);
            if (postcodeInClient == null) {
                client.getPostcodes().add((Postcode) o);
            } else postcodeInClient = (Postcode) o;

        }else if(o instanceof User){
            User userInClient = client.users.stream().filter(user -> user.getName().equals(((User) o).getName())).findFirst().orElse(null);
            if (userInClient == null) client.users.add((User) o);
            else userInClient = (User) o;
        }else if (o instanceof Order){
            Order orderInClient = client.orders.stream().filter(order -> order.getName().equals(((Order) o).getName()) && !(order.getStatus().equals(((Order) o).getStatus()))).findFirst().orElse(null);
            if (orderInClient == null) client.orders.add((Order) o);
            else orderInClient = (Order) o;
        } else{
            System.out.println("Received unknown client " + o);
        }
    }
}
