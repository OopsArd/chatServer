import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private String listUsers = "USERS`";

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                System.out.println("New client connected: " + socket);

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        Main chatServer = new Main();
        chatServer.start(port);
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String user;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    String[] mess = inputLine.split("`");
                    if(mess[0].equals("USERS")){
                        listUsers+= mess[1] + "`";
                        broadcastMessage(listUsers);

                        user = mess[1];
                    }else{
                        broadcastMessage(inputLine);
                    }

                }
            } catch (IOException e) {
                System.out.println(user + " " +"disconnected");
                String dis = user + "`";
                listUsers = listUsers.replace(dis,"");

                broadcastMessage(listUsers);
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {

            out.println(message);
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
