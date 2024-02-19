import java.io.*;
import java.net.*;
import java.util.Random;

public class TempSensor {
    public static void main(String[] args) {
        try {
            // Create a new socket connection to the gateway
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Continuously generate and send temperature data
            while (true) {
                // Generate a random temperature between 20 and 30
                int temp = 20 + new Random().nextInt(11);

                // Send the temperature data and the current timestamp to the gateway
                out.println("TEMP " + temp + " " + System.currentTimeMillis());

                // Wait for 1 second before sending the next data
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // Print any exceptions that occur
            e.printStackTrace();
        }
    }
}
