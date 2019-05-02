package comp1206.sushi.server;


import comp1206.sushi.common.Order;
import comp1206.sushi.common.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;


public class Comms implements Runnable {
    DataOutputStream out = null;
    private ServerInterface server;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private Thread thread;
    private HashMap<Socket, ClientHandler> socketThreadHashMap = new HashMap<>();

    public Comms(ServerInterface server) {
        this.server = server;
        thread = new Thread(this, "server");
        thread.start();
    }

    public void sendInitialData(Socket socket){
        server.getPostcodes().forEach(postcode -> {
            sendObject(postcode,socket);
        });

        sendObject(server.getRestaurant(),socket);

        server.getDishes().forEach(dish -> {
            sendObject(dish,socket);
        });

        server.getUsers().forEach(user -> {
            sendObject(user,socket);
        });

        server.getOrders().forEach(order -> {
            sendObject(order,socket);
        });
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
            thread = new Thread() {
                @Override
                public void run() {
                    //creates the handler with the I/O object stream
                    ClientHandler clientHandler = new ClientHandler(this, socket, server);
                    socketThreadHashMap.put(socket, clientHandler);
                    sendInitialData(socket);
                    readObject(socket, clientHandler.getObjectInputStream());

                    clientHandler = null;
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.interrupt();
                }
            };
            thread.start();
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    public void readObject(Socket socket, ObjectInputStream objectInputStream) {
        ClientHandler clientHandler = socketThreadHashMap.get(socket);
        while (!clientHandler.getExitStatus()) {
            try {
                Object o = objectInputStream.readObject();
                handleClient(o,socket);
                System.out.println(o + " given  by " + socket.getRemoteSocketAddress() + "3");
            } catch (SocketException se) {
                clientHandler.closeStreams();
                clientHandler.closeThread();
                clientHandler.setExitStatus(true);
                System.gc();
                System.out.println("Connection with client lost");
            } catch (IOException i) {
                i.printStackTrace();
            } catch (ClassNotFoundException cne) {
                cne.printStackTrace();
            }
        }
    }
    public void sendObject(Object o, Socket socket) {
        try {
            ObjectOutputStream objectOutputStream = socketThreadHashMap.get(socket).getObjectOutputStream();
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public void broadcast(Object o) {
        if (!socketThreadHashMap.isEmpty()){
            socketThreadHashMap.keySet().forEach((socket) -> {
                sendObject(o, socket);
            });
        }
    }

    public void handleClient(Object o, Socket socket){
        if(o instanceof User){
            socketThreadHashMap.get(socket).setUser((User) o);
        }else if (o instanceof Order){
            server.getOrders().add((Order) o);
        }
    }
}

class ClientHandler {
    Thread thread;
    User user;
    Socket socket;
    private boolean status = false;
    ServerInterface server;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    public ClientHandler(Thread thread, Socket socket, ServerInterface server) {
        this.thread = thread;
        this.user = null;
        this.socket = socket;
        this.server = server;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getExitStatus(){
        return this.status;
    }
    public void setExitStatus(boolean status){
        this.status = status;
    }

    public void closeStreams(){
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeThread(){
        this.thread = null;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return this.objectInputStream;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}