import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.System.in;

public class Main {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private String listUsers = "NAME`";

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
        private InputStream inputStream;
        private OutputStream outputStream;

        private String user;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                // Tạo một thư mục để lưu trữ các file nhận được từ client
                String saveDirectory = "./Share/";
                File saveDir = new File(saveDirectory);
                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }

                String messageFromClient;
                while (true)
                {
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);
                    String title = new String(buffer, 0, 4);


                    messageFromClient = new String(buffer, 0, bytesRead);

                    if(title.equals("NAME")){
                        String[] mess = messageFromClient.split("`");
                        listUsers+= mess[1] + "`";
                        broadcastMessage(listUsers);

                        user = mess[1];
                    } else if(title.equals("FILE")){
                        String[] mess = messageFromClient.split("`");

                        String fileName = mess[2];
                        int fileNameLength = fileName.length();

                        String fileContent = mess[3];
                        int fileContentLength = fileName.length();


                        File receivedFile = new File(saveDirectory + fileName);
                        FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);

                        fileOutputStream.write(buffer, 5 + fileNameLength, bytesRead);

                        String aler = "ALER`" + mess[1] + " đã chia sẻ " + fileName + ", kiểm tra trong thư mục của ứng dụng";
                        broadcastMessage(aler);

                        broadcastMessage(messageFromClient);
                    }
                    else{
                        broadcastMessage(messageFromClient);
                    }

                }
            } catch (IOException e) {
                System.out.println(user + " " +"disconnected");
                String dis = user + "`";
                listUsers = listUsers.replace(dis,"");
                broadcastMessage(listUsers);

            } finally {
                try {
                    clientSocket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            try{
                outputStream.write(message.getBytes());
            }catch(Exception ex){
                //System.out.println("------Error: " + ex.getMessage());
            }
        }
    }

    private void broadcastMessage(String message){
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
