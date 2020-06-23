package chat.server;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener
{
    public static void main(String[] args)
    {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();


    private ChatServer()
    {
        System.out.println("Server running!\n");
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(serverSocket.accept(), this);
                } catch (IOException e) {
                    System.out.println("TCP connection has been fault :(\n" + e + "\n");
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        broadcast("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String string) {
        broadcast(string);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        broadcast("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception ex) {
        System.out.println("Exception from tcp connection: " + ex.toString());
    }

    private void broadcast(String string){
        System.out.println(string);

        for (int i = 0; i < connections.size(); ++i){
            connections.get(i).sendString(string);
        }

    }
}
