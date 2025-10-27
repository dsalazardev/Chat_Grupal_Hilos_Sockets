import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private static final String HOST = "25.50.189.178";
    private static final int PORT = 8070;

    public static void main(String[] args) {
        System.out.println("Conectándose a " + HOST + " puerto " + PORT);

        Socket socket = null;
        PrintWriter output = null;
        BufferedReader input = null;
        Scanner scanner = null;
        Thread receiverThread = null;

        try {
            socket = new Socket(HOST, PORT);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            
            MessageReceiver receiver = new MessageReceiver(input);
            receiverThread = new Thread(receiver);
            receiverThread.setDaemon(true); 
            receiverThread.start();

            System.out.print("Ingresa tu nombre de usuario para el chat: ");
            String nombreUsuario = scanner.nextLine();
            
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                System.out.println("El nombre no puede estar vacío. Saliendo.");
                return;
            }

            output.println(nombreUsuario); 

            System.out.println("¡Conectado! Escribe 'salir' para desconectarte.");

            while (true) {
                String mensaje = scanner.nextLine(); 

                if ("salir".equalsIgnoreCase(mensaje)) {
                    break; 
                }
                output.println(mensaje); 
            }

        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage()); 
        } finally {
            System.out.println("Cerrando conexión..."); 
             try { 
                if (scanner != null) scanner.close();
                if (output != null) output.close();
                if (input != null) input.close();
                if (socket != null) socket.close(); 
             } catch (Exception e) { 
                System.out.println("Error al cerrar recursos: " + e.getMessage());
             }
             if (receiverThread != null && receiverThread.isAlive()) {
                 receiverThread.interrupt(); 
             }
        }
    }
}

class MessageReceiver implements Runnable {
    private BufferedReader input;

    public MessageReceiver(BufferedReader input) {
        this.input = input;
    }

    @Override
    public void run() {
        try {
            String mensaje;
            while (!Thread.currentThread().isInterrupted() && (mensaje = input.readLine()) != null) {
                 if (mensaje.startsWith("Error:")) {
                     System.out.println("\n[ERROR DEL SERVIDOR] " + mensaje);
                     System.exit(1); 
                 } else {
                     System.out.println(mensaje); 
                 }
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                System.out.println("\n[DESCONECTADO] Se ha perdido la conexión con el servidor.");
            }
        } finally {
             System.exit(0);
        }
    }
}