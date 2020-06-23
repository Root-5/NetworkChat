package chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

//Одно соединение
public class TCPConnection
{
    private final Socket socket;          //Связанный сокет
    private final Thread rxThread;        //Поток для прослушивания входящих соединений
    private final TCPConnectionListener eventListener;      //Событийная модель
    private final BufferedReader in;
    private final BufferedWriter out;


    //Метод для готового сокета
    public TCPConnection(TCPConnectionListener tcpConnectionListener, String ip, int port) throws IOException {
        this(new Socket(ip, port), tcpConnectionListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException
    {
        //Запоминаем сокет
        this.socket = socket;
        //event
        this.eventListener = eventListener;
        //Получаем входящий и исходящий потоки
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));      //Указываем кодировку
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        //Создание и запуск нового потока для прослушивания входящих данных
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted())
                    {
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();

    }

    public synchronized void sendString(String string)
    {
        try {
            out.write(string + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect()
    {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e){
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
