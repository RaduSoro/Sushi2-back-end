package comp1206.sushi.server;


import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.User;

import java.io.*;
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
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String input = "";
    private HashMap<Socket, Wrapper> socketThreadHashMap = new HashMap<>();

    public Comms(ServerInterface server) {
        this.server = server;
        thread = new Thread(this, "server");
        thread.start();
    }

    public synchronized void run() {
        System.out.println("Running thread server" + Thread.currentThread());
        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) try {
            socket = serverSocket.accept();
            System.out.println(socket);
            System.out.println("passed 1st");
            //inputStream = new DataInputStream(this.socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("passed 2nd");
            thread = new Thread() {
                @Override
                public void run() {
                    sendObject(this, socket);
                    readObject(this, socket, objectInputStream);
                }
            };
            thread.start();
            socketThreadHashMap.put(socket, new Wrapper(thread));
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void read(Thread thread, Socket socket, DataInputStream dis) {
        while (true) {
            try {
                input = dis.readUTF();
                System.out.println(input);
                Thread.sleep(500);
            } catch (IOException i) {
                i.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void readObject(Thread thread, Socket socket, ObjectInputStream objectInputStream) {
        while (true) {
            try {
                Dish dish = (Dish) objectInputStream.readObject();
                System.out.println(dish);
                // Thread.sleep(500);
            } catch (IOException i) {
                i.printStackTrace();
//            }catch (InterruptedException ie){
//                ie.printStackTrace();
            } catch (ClassNotFoundException cne) {
                cne.printStackTrace();
            }
        }
    }

    public void sendObject(Thread thread, Socket socket) {

        try {
            objectOutputStream.writeObject(new Postcode("So16 3ZE"));
            objectOutputStream.flush();
            // Thread.sleep(500);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void broadcast(String input) {
        socketThreadHashMap.forEach((socket, thread) -> {
            try {
                System.out.println("first output stream");
                out = new DataOutputStream(socket.getOutputStream());
                System.out.println("first outObject stream");
                out.writeUTF(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void broadCastObject(Object o) {
        socketThreadHashMap.forEach((socket, thread) -> {
            try {
                System.out.println("trying to broadcast object" + o);
                objectOutputStream = new ObjectOutputStream((new BufferedOutputStream(socket.getOutputStream())));
                this.objectOutputStream.writeObject(o);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

class Wrapper {
    Thread thread;
    User user;

    public Wrapper(Thread thread) {
        this.thread = thread;
        this.user = null;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Thread getThread() {
        return this.thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}

class ClientHandler {

}