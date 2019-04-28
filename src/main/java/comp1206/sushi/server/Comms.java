package comp1206.sushi.server;


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
            thread = new Thread() {
                @Override
                public void run() {
                    Wrapper wrapper = new Wrapper(this, socket);
                    socketThreadHashMap.put(socket, wrapper);
                    readObject(socket, wrapper.getObjectInputStream());
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
        while (true) {
            try {
                Object o = objectInputStream.readObject();
                System.out.println(o + " given  by " + socket.getRemoteSocketAddress() + "3");
            } catch (SocketException se) {
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
        socketThreadHashMap.keySet().forEach((socket) -> {
            sendObject(o, socket);
        });
    }


class Wrapper {
    Thread thread;
    User user;
    Socket socket;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    public Wrapper(Thread thread, Socket socket) {
        this.thread = thread;
        this.user = null;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Thread getThread() {
        return this.thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
}