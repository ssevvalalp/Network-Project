import java.net.*;
import java.util.Random;

public class HumiditySensor {
    public static void main(String[] args) {
        try {
            // Create a new DatagramSocket for sending data to the gateway
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");

            // Continuously generate and send humidity data
            int counter = 0;
            while (true) {
                // Generate a random humidity between 40 and 90
                int humidity = 40 + new Random().nextInt(51);
                byte[] buf;

                // If the humidity is greater than 80 or if 3 seconds have passed, send the humidity value or an "ALIVE" message
                if (humidity > 80 || counter % 3 == 0) {
                    if (humidity > 80) {
                        buf = ("HUMIDITY " + humidity + " " + System.currentTimeMillis()).getBytes();
                    } else {
                        buf = "ALIVE".getBytes();
                    }

                    // Create a DatagramPacket to send the data
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5001);

                    // Send the packet
                    socket.send(packet);
                }

                // Wait for 1 second before generating the next data
                Thread.sleep(1000);

                // Increment the counter
                counter++;
            }
        } catch (Exception e) {
            // Print any exceptions that occur
            e.printStackTrace();
        }
    }
}
