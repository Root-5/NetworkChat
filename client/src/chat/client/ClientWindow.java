package chat.client;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener
{

    private static final String localIp = "127.0.0.1";      //Default ip
    private static final int standartPort = 8189;      //Default port
    private static final int width = 800;
    private static final int height = 600;
    private TCPConnection connection;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNick = new JTextField("username");
    private final JTextField inputText = new JTextField();


    private ClientWindow()
    {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setDefaultLookAndFeelDecorated(true);
        setBackground(Color.black);
        setTitle("Dima's chat");

        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);
        add(fieldNick, BorderLayout.NORTH);
        add(inputText, BorderLayout.SOUTH);
        inputText.addActionListener(this);

        setVisible(true);

        try {
            connection = new TCPConnection(this, localIp, standartPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = inputText.getText();
        if (msg.equals("")) return;
        else {
            inputText.setText(null);
            connection.sendString(fieldNick.getText() + ": " + msg);
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Ready for connection!");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String string) {
        printMsg(string);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Disconnect...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception ex) {
        printMsg("Connection exeption: " + ex);
    }

    private synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
