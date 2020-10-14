import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Client {

         public static void main(String[] args) {

                if (args.length < 2) {
                    System.out.println("Missing Input");
                    return;

                }

                String hostname = args[0]; //IPaddress
                int port = Integer.parseInt(args[1]);

                try {

                    InetAddress address = InetAddress.getByName(hostname);
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(10000); // set time out to 10s

                    while (true) {

                        // Create a Scanner object to read input.
                        Scanner console = new Scanner(System.in);
                     String message;
                        System.out.print("Enter your message ");
                        message = console.nextLine();


                        byte[] requestbuffer = message.getBytes();

                        //Send message to server
                        DatagramPacket request = new 
DatagramPacket(requestbuffer,requestbuffer.length, address, port);
                        socket.send(request);

                        //wait for response
                        byte[] buffer = new byte[512];
                        DatagramPacket response = new DatagramPacket(buffer, 
buffer.length);
                        socket.receive(response); // blocking call

                        //format and print response
                        String serverMessage = new String(buffer, 0, 
response.getLength());
                        System.out.println(serverMessage);
                        System.out.println();

                        Thread.sleep(10000);
                    }

                } catch (SocketTimeoutException ex) {
                    System.out.println("Timeout error: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (IOException ex) {
                    System.out.println("Client error: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

}