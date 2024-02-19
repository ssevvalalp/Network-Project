import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Gateway {
    public static void main(String[] args) {
    	// Create a thread pool with 2 threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // Execute the TempSensorHandler and HumiditySensorHandler in separate threads
        executor.execute(new TempSensorHandler());
        executor.execute(new HumiditySensorHandler());
    }
   
    // This class handles temperature sensor data
    static class TempSensorHandler implements Runnable {
        @Override
        public void run() {
            try {
                // Create a ServerSocket on port 5000
                ServerSocket serverSocket = new ServerSocket(5000);
                // Accept a connection from a client
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String lineTemp;

                // Create a new socket connection to the server
                Socket serverSocket2 = new Socket("localhost", 6000);
                PrintWriter out = new PrintWriter(serverSocket2.getOutputStream(), true);

                // Send handshake message to the server
                out.println("HANDSHAKE_REQ");

                // Receive handshake response from the server
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket2.getInputStream()));
                String handshakeResponse = serverIn.readLine();
                System.out.println("Received handshake response: " + handshakeResponse);

                long lastReceived = System.currentTimeMillis();
                while (true) {
                    if (in.ready()) {
                    	lineTemp = in.readLine();
                        lastReceived = System.currentTimeMillis();
                        // Send the received data to the server
                        out.println("TEMP " + lineTemp + " " + System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - lastReceived > 3000) {
                        out.println("TEMP SENSOR OFF");
                        lastReceived = System.currentTimeMillis();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // This class handles humidity sensor data
    static class HumiditySensorHandler implements Runnable {
        @Override
        public void run() {
            try {
                // Create a DatagramSocket on port 5001
                @SuppressWarnings("resource")
                DatagramSocket socket = new DatagramSocket(5001);
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                // Create a new socket connection to the server
                Socket serverSocket = new Socket("localhost", 6000);
                PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);

                // Send handshake message to the server
                out.println("HANDSHAKE_REQ");

                // Receive handshake response from the server
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                String handshakeResponse = serverIn.readLine();
                System.out.println("Received handshake response: " + handshakeResponse);

                long lastReceived = System.currentTimeMillis();
                while (true) {
                    socket.receive(packet);
                    String lineHumidity = new String(packet.getData(), 0, packet.getLength());

                    if (lineHumidity.equals("ALIVE")) {
                        lastReceived = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - lastReceived > 7000) {
                        out.println("HUMIDITY SENSOR OFF");
                        lastReceived = System.currentTimeMillis();
                    }

                    // Send the received data to the server
                    out.println("HUMIDITY " + lineHumidity + " " + System.currentTimeMillis());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
