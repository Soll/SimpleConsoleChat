package chat.client;

import chat.Connection;
import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private volatile boolean isConnected = false;
    private static Connection connection;


    protected ClientThread getClientThread() {
        return new ClientThread();
    }

    public void sendTextMessage(String text) throws IOException {
        connection.send(new Message(MessageType.TEXT, text));
    }

    protected void run() {
        ClientThread clientThread = getClientThread();
        clientThread.setDaemon(true);
        clientThread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (Exception e) {
            ConsoleHelper.write("Error of client thread");
        }

        while (true) {

            try {
                String str = ConsoleHelper.readString();

                if (str.equalsIgnoreCase("exit"))
                    break;
                else
                    sendTextMessage(str);
            } catch (IOException e) {
                ConsoleHelper.write("Error of client thread");
                isConnected = false;
            }


        }

    }

    public static void main(String... args) {

        new Client().run();

    }

    private class ClientThread extends Thread {

        protected String getServerAddress() {
            return ConsoleHelper.readString();
        }

        protected int getServerPort() {
            return ConsoleHelper.readInt();
        }

        protected String getUserName() {
            return ConsoleHelper.readString();
        }

        protected void processIncomingMessage(String text) {
            ConsoleHelper.write(text);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.write("Пользователь " + userName + " присоединился к чату");
        }

        protected void informAboutDeletingUser(String userName) {
            ConsoleHelper.write("Пользователь " + userName + " покинул чат");
        }

        protected void notifyClientAboutNameAccepted(boolean status) {

            synchronized (Client.this) {
                Client.this.isConnected = status;
                Client.this.notify();
            }

            ConsoleHelper.write("Установлено соединение с сервером.");
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {

            while (!isConnected) {
                Message message = connection.receive();

                if (message.getType() == MessageType.USERNAME_REQUEST)
                    connection.send(new Message(MessageType.USERNAME, getUserName()));
                if (message.getType() == MessageType.USERNAME_ACCEPTED)
                    notifyClientAboutNameAccepted(true);
            }
        }

        protected void clientMainLoop() throws ClassNotFoundException {

            while (isConnected) {
                try {
                    Message message = connection.receive();

                    if (message.getType() == MessageType.USER_ADDED)
                        informAboutAddingNewUser(message.getData());
                    if (message.getType() == MessageType.USER_REMOVED)
                        informAboutDeletingUser(message.getData());
                    if (message.getType() == MessageType.TEXT)
                        processIncomingMessage(message.getData());
                } catch (IOException e) {
                    ConsoleHelper.write("Клиентский поток закрыт!");
                    isConnected = false;
                }
            }

        }

        @Override
        public void run() {

            try {
                ConsoleHelper.write("Введите адрес сервера:");
                String address = getServerAddress();

                ConsoleHelper.write("Введите адрес порта:");
                int port = getServerPort();

                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
            } catch (IOException e) {
                ConsoleHelper.write("Ошибка соединения с сервером! Попробуйте снова!");
            }

            try {
                ConsoleHelper.write("Введите имя пользователя:");
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.write("Ошибка отправки данных!");
            }

        }
    }

}
