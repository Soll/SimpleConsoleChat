package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String... args) {

        ConsoleHelper.write("Введите номер порта:");
        try {
            ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt());
            ConsoleHelper.write("Сервер запущен на порту - " + serverSocket.getLocalSocketAddress());

            while (true) {
                new Handler(serverSocket.accept()).start();
            }

        } catch (IOException e) {
            ConsoleHelper.write("Ошибка создания сервера!");
        }

    }

    private static class Handler extends Thread {

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public String serverHandshake(Connection connection) {

            while (true) {
                try {
                    connection.send(new Message(MessageType.USERNAME_REQUEST));
                    Message answer = connection.receive();

                    if (answer.getType() == MessageType.USERNAME && !answer.getData().isEmpty()) {
                        connection.send(new Message(MessageType.USERNAME_ACCEPTED));
                        connectionMap.put(answer.getData(), connection);
                        return answer.getData();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    ConsoleHelper.write("Ошибка согласования с клиентом!");
                }
            }
        }

        public void sendUserList(String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName))
                    entry.getValue().send(new Message(MessageType.USER_ADDED, userName));
            }
        }

        public void sendBroadcastMessage(Message message) {

            try {
                for (Connection connection : connectionMap.values())
                    connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.write("Ошибка отправки сообщения");
            }

        }

        public void serverMainLoop(Connection connection, String userNmae) throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userNmae + ": " + message.getData()));
                } else
                    ConsoleHelper.write("Ошибка входящего сообщения!");
            }
        }

        @Override
        public void run() {

            String userName = null;

            try (Connection connection = new Connection(socket)) {

                userName = serverHandshake(connection);
                ConsoleHelper.write("Установлено соединение с удаленным хостом - " + connection.getRemoteSocketAddress());
                sendUserList(userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {

                ConsoleHelper.write("Разорвано соединение с удаленным хостом - " + socket.getRemoteSocketAddress());

            } finally {

                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
        }
    }

}
