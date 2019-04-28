package comp1206.sushi.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Comms implements Runnable {
    DataOutputStream out = null;
    private ServerInterface server;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream inputStream = null;
    private Thread thread;
    private String input = "";
    private HashMap<Socket, Thread> socketThreadHashMap = new HashMap<>();

    public Comms(ServerInterface server) {
        this.server = server;
        thread = new Thread(this, "server");
        thread.start();
    }

    public void run() {
        System.out.println("Running thread server" + Thread.currentThread());
        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                System.out.println(socket);
                inputStream = new DataInputStream(this.socket.getInputStream());
                thread = new Thread() {
                    @Override
                    public void run() {
                        read(this, socket, inputStream);
                    }
                };
                thread.start();
                socketThreadHashMap.put(socket, thread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read(Thread thread, Socket socket, DataInputStream dis) {
        while (true) {
            try {
                input = dis.readUTF();
                System.out.println(input);
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }

    public void send(String input) {
        socketThreadHashMap.forEach((socket, thread) -> {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("hello from serverside println 2");

    }
}
