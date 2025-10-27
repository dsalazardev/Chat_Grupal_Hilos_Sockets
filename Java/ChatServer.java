import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static final int PORT = 8070;
    private List<PrintWriter> clients = new ArrayList<>();

    public ChatServer() {
        System.out.println("Iniciando servidor en el puerto: " + PORT);
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Esperando una conexión...");
                Socket socket = serverSocket.accept();
                
                ClientHandler clientHandler = new ClientHandler(socket, this);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    public synchronized void addClient(PrintWriter writer) {
        this.clients.add(writer);
    }

    public synchronized void removeClient(PrintWriter writer) {
        this.clients.remove(writer);
    }

    public synchronized void broadcast(String message, PrintWriter sender) {
        for (PrintWriter writer : clients) {
            if (writer != sender) { 
                 writer.println(message);
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.run();
    }
}

class ClientHandler implements Runnable {

    private Socket socket;
    private ChatServer server;
    private String nombreUsuario;
    private PrintWriter output;
    private BufferedReader input;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            this.nombreUsuario = input.readLine();
            if (this.nombreUsuario == null || this.nombreUsuario.trim().isEmpty()) {
                output.println("Error: El nombre de usuario no puede estar vacío.");
                socket.close();
                return; 
            }

            server.addClient(this.output);
            System.out.println("[NUEVA CONEXIÓN] " + socket.getRemoteSocketAddress() + " se identificó como: " + this.nombreUsuario);
            String bienvenida = "--- " + this.nombreUsuario + " se ha unido al chat ---";
            server.broadcast(bienvenida, this.output);

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                String mensajeBroadcast = "[" + this.nombreUsuario + "]: " + mensaje;
                System.out.print(mensajeBroadcast + "\n");
                server.broadcast(mensajeBroadcast, this.output);
            }

        } catch (Exception e) {
            if (this.nombreUsuario != null) {
                System.out.println("Error/Desconexión del cliente " + this.nombreUsuario + ": " + e.getMessage());
            } else {
                 System.out.println("Error/Desconexión de " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            }
        } finally {
            if (this.nombreUsuario != null) {
                System.out.println("[CONEXIÓN CERRADA] " + this.nombreUsuario + " se ha desconectado.");
                String salida = "--- " + this.nombreUsuario + " ha salido del chat ---";
                server.broadcast(salida, this.output);
            }
            if (this.output != null) {
                server.removeClient(this.output); 
            }
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar recursos del cliente: " + e.getMessage());
            }
        }
    }
}