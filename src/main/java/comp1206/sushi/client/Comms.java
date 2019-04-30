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
        System.out.println(foundHost + "2nd");
    }

    public void run() {
        System.out.println("Running thread client" + Thread.currentThread());
        System.out.println("Thread current id" + Thread.currentThread().getId());
        while (thread.isAlive()) {
            //receive();
            receiveObject();
        }
    }

    private void connection() {
        System.out.println(foundHost);
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
            System.out.println("Received dish " + o);
            client.getDishes().add((Dish) o);
        }else if (o instanceof Restaurant){
            System.out.println("Received restaurant " + o);
            client.setRestaurant((Restaurant) o);
        }else if(o instanceof Postcode){
            System.out.println("Received postcode " + o);
            client.getPostcodes().add((Postcode) o);
        }else if(o instanceof User){
            client.users.add((User) o);
        }else if (o instanceof Order){
            client.orders.add((Order) o);
            System.out.println("received order " + o);
        } else{
            System.out.println("Received idk " + o);
        }
    }
}
