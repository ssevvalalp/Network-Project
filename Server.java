import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import com.sun.net.httpserver.*;

public class Server {
	// ConcurrentHashMaps to store temperature and humidity data
    private static ConcurrentHashMap<String, String> temperatureData = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> humidityData = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
    	// Create an HttpServer on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Create contexts for temperature and humidity
        server.createContext("/temperature", new TemperatureHandler());
        server.createContext("/humidity", new HumidityHandler());
        // Set the executor for the server
        server.setExecutor(null); // creates a default executor
        // Start the server
        server.start();

        try {
        	// Create a ServerSocket on port 6000
            ServerSocket serverSocket = new ServerSocket(6000);
            // Accept a connection from a client
            Socket socket = serverSocket.accept();

            // Receive handshake message from the gateway
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String handshakeMessage = in.readLine();
            System.out.println("Received handshake message: " + handshakeMessage);

            // Send handshake response to the gateway
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("HANDSHAKE_ACK");

            while (true) {
                String line = in.readLine();
                if (line != null) {
                    // Process the received data and store it
                    if (line.startsWith("TEMP")) {
                    	// Store temperature data with current time as key
                        temperatureData.put(Long.toString(System.currentTimeMillis()), line);
                    } else if (line.startsWith("HUMIDITY")) {
                    	// Store humidity data with current time as key
                        humidityData.put(Long.toString(System.currentTimeMillis()), line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handler for temperature data
    static class TemperatureHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	// Prepare the response with temperature data
            String response = temperatureData.toString();
            // Send the response headers
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            // Write the response and close the stream
            os.write(response.getBytes());
            os.close();
        }
    }
 
    // Handler for humidity data
    static class HumidityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	// Prepare the response with humidity data
            String response = humidityData.toString();
            // Send the response headers
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            // Write the response and close the stream
            os.write(response.getBytes());
            os.close();
        }
    }
}
