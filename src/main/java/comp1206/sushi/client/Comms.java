package comp1206.sushi.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Comms implements Runnable {
    private ClientInterface client;
    private Socket socket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String input = "";
    private Thread thread;
    private boolean foundHost = false;

    public Comms(ClientInterface client) {
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
//                inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//                outputStream = new DataOutputStream(socket.getOutputStream());
                System.out.println("CREATED OOP");
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
                System.out.println(o);
            } catch (SocketException b) {
                foundHost = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void receive() {
        while (this.foundHost) {
            try {
                input = inputStream.readUTF();
                System.out.println(input + "client receive");
            } catch (IOException e) {
                e.printStackTrace();
                foundHost = false;
            }
        }
    }
    public void send(String message) {
        if (foundHost) {
            try {
                outputStream.writeUTF(message);
                System.out.println(message + " println");
            } catch (IOException e) {
                System.out.println("Failed reaching server");
                foundHost = false;
            }
        }
    }
}
