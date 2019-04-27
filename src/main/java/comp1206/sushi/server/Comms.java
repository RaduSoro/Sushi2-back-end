package comp1206.sushi.server;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Comms implements Runnable {
    DataOutputStream out = null;
    private ServerInterface server;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream inputStream = null;
    private Thread thread;
    private String input = "";

    public Comms(ServerInterface server) {
        this.server = server;
        thread = new Thread(this, "client");
        thread.start();
    }

    public void run() {
        System.out.println("Running thread server" + Thread.currentThread());
        while (thread.isAlive()) {
            initializate();
        }
    }

    public void initializate() {
        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Starting the server socket ");
            //listens to the socket until connection from client is made
            socket = serverSocket.accept();
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("The client was accepted ");
            //reads whatever the client inputs
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            while (!input.equals("Stop")) {
                try {
                    input = inputStream.readUTF();
                    System.out.println(input);
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
            System.out.println("Closing connection message was received");
            socket.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String input) {
        try {
            out.writeUTF(input);
            System.out.println("hello from serverside println 2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
