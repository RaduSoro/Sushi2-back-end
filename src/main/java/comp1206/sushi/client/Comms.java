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
    }

    public void run() {
        while (thread.isAlive()) {
            connection();
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
            client.dishes.add((Dish) o);
            client.notifyUpdate();
            System.out.println(((Dish) o).getName());
        }else if (o instanceof Restaurant){
            client.setRestaurant((Restaurant) o);
        }else if(o instanceof Postcode){
            client.postcodes.add((Postcode) o);
        }else if(o instanceof User){
            client.users.add((User) o);
        }else if (o instanceof Order){
            client.orders.add((Order) o);
        } else if (o instanceof ComplexMessage) {
            //converts the order from server tot the one in client
            Object object = ((ComplexMessage) o).getObject();
            String instruction = ((ComplexMessage) o).getInstruction();
            switch (instruction) {
                case "update status":
                    if (object instanceof Order) {
                        Order orderInClient = client.orders.stream().filter(order -> order.getOrderNumber().equals(((Order) object).getOrderNumber())).findFirst().orElse(null);
                        if (orderInClient != null) orderInClient.setStatus(((Order) object).getStatus());
                        client.notifyUpdate();
                    }
                    break;
                case "delete":
                    if (object instanceof Order) {
                        Order orderInClient = client.orders.stream().filter(order -> order.getOrderNumber().equals(((Order) object).getOrderNumber())).findFirst().orElse(null);
                        if (orderInClient != null) {
                            orderInClient.setStatus(((Order) object).getStatus());
                            orderInClient.getUser().getOrderHistory().remove(orderInClient);
                        }
                    } else if (object instanceof Dish) {
                        Dish dishInClient = client.getDishes().stream().filter(dish -> dish.getName().equals(((Dish) object).getName())).findFirst().orElse(null);
                        if (dishInClient != null) {
                            client.getDishes().remove(dishInClient);
                            System.out.println(client.getDishes());
                            client.notifyUpdate();
                        }
                    }
                    break;
            }
        } else {
            System.out.println("Unknown object " + o + o.getClass());
        }
    }
}
