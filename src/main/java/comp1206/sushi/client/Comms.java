package comp1206.sushi.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Comms implements Runnable {
    private ClientInterface client;
    private Socket socket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private String input = "";
    private Thread thread;
    private boolean foundHost = false;

    public Comms(ClientInterface client) {
        this.client = client;
        thread = new Thread(this, "client");
        thread.start();
        connection();
    }

    public void run() {
        System.out.println("Running thread client" + Thread.currentThread());
        System.out.println("Thread current id" + Thread.currentThread().getId());
        while (thread.isAlive()) {
            receive();
        }
    }

    private void connection() {
        while (!this.foundHost) {
            try {
                socket = new Socket("127.0.0.1", 5000);
                inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                outputStream = new DataOutputStream(socket.getOutputStream());
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
