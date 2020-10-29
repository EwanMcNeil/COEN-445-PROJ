import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {

        private DatagramSocket socket;


      public Server(int port) throws SocketException {
          socket = new DatagramSocket(port);

      }
      
      

      
      //Main function starts up server
      public static void main(String[] args) {
          if (args.length < 1) {
              System.out.println("Missing Input");
              return;
          }

          int port = Integer.parseInt(args[0]);

          try {
              Server server = new Server(port);
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
        	  DatagramPacket requestPacket = null;
        	  try 
              { 
        		 
              
              byte [] clientMessage = new byte[50000];
              requestPacket = new DatagramPacket(clientMessage, clientMessage.length);
              
              socket.receive(requestPacket);
              
              Thread t = new ClientHandler(socket, requestPacket); 
              
              // Invoking the start() method 
              t.start(); 
              
              
              } 
              catch (Exception e){ 
                  socket.close(); 
                  e.printStackTrace(); 
              }
              
            
          }
      }


 



}


//ClientHandler class 
class ClientHandler extends Thread  
{ 
 
 //final DataInputStream dis; 
 //final DataOutputStream dos; 
 final DatagramSocket s; 
   
 final DatagramPacket request;

 // Constructor 
 public ClientHandler(DatagramSocket s, DatagramPacket request)  
 { 
     this.s = s; 
     this.request = request;
 } 

 @Override
 public void run()  
 { 
     String received; 
     String toreturn; 
     while (true)  
     { 
         try { 
        	 
        	 System.out.println("Started new Client Thread");

             //after this we start a client thread 

             String message = "Message received: " + formatMessage(request.getData());
             System.out.println(message);

             byte[] buffer = message.getBytes();

             
             
             // server can get the info about the client
             InetAddress clientAddress = request.getAddress();
             int clientPort = request.getPort();

             DatagramPacket response = new DatagramPacket(buffer,buffer.length, clientAddress, clientPort);
             s.send(response);
           
         } catch (IOException e) { 
             e.printStackTrace(); 
         } 
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