import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {

        private DatagramSocket socket;


      public EchoServer(int port) throws SocketException {
          socket = new DatagramSocket(port);

      }

      public static void main(String[] args) {
          if (args.length < 1) {
              System.out.println("Missing Input");
              return;
          }

          int port = Integer.parseInt(args[0]);

          try {
              EchoServer server = new EchoServer(port);
              System.out.println("Echo Server listening on port " + port);
              server.service();

          } catch (SocketException ex) {
              System.out.println("Socket error: " + ex.getMessage());
          } catch (IOException ex) {
              System.out.println("I/O error: " + ex.getMessage());
          }
      }

      private void service() throws IOException {
          while (true) {

                byte [] clientMessage = new byte[50000];
              DatagramPacket request = new
DatagramPacket(clientMessage, clientMessage.length);
              socket.receive(request);

              String message = "Message recived : " +
formatMessage(clientMessage);
              System.out.println(message);

              byte[] buffer = message.getBytes();

              // server can get the info about the client
              InetAddress clientAddress = request.getAddress();
              int clientPort = request.getPort();

              DatagramPacket response = new DatagramPacket(buffer,
buffer.length, clientAddress, clientPort);
              socket.send(response);
          }
      }


   // A method to convert the byte array data into a string representation.
      private static StringBuilder formatMessage(byte[] a)
      {
          if (a == null)
              return null;
          StringBuilder ret = new StringBuilder();
          int i = 0;
          while (a[i] != 0)
          {
              ret.append((char) a[i]);
              i++;
          }
          return ret;
      }




}