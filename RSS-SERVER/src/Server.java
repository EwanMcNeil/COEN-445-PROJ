import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

      
	  ArrayList<String> clients;
	  final int port;

      public Server(int port) throws SocketException {
       
    	  clients = new ArrayList<>();
    	  this.port = port;
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
    	  DatagramSocket socket = new DatagramSocket(port);
          while (true) {
        	  DatagramPacket requestPacket = null;
        	  try 
              { 
        	
        		  
        	int count = 0;
              
              byte [] clientMessage = new byte[50000];
              requestPacket = new DatagramPacket(clientMessage, clientMessage.length);
              
              socket.receive(requestPacket);
              String clientName = formatMessage(clientMessage).toString();
    		 
              System.out.println("incoming data");
              if(!(clients.contains(clientName))){
            	  clients.add(clientName);
            	  
              Thread t = new ClientHandler(socket, requestPacket, count); 
              
              // Invoking the start() method 
              t.start(); 
              }
              
              } 
              catch (Exception e){ 
                  socket.close(); 
                  e.printStackTrace(); 
              }
              
            
          }
      }

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


//ClientHandler class 
class ClientHandler extends Thread  
{ 
 
 //final DataInputStream dis; 
 //final DataOutputStream dos; 
 final DatagramSocket s; 
   
 final DatagramPacket request;
 final int count;

 // Constructor 
 public ClientHandler(DatagramSocket s, DatagramPacket request, int count)  
 { 
     this.s = s; 
     this.request = request;
     this.count = count;
     
 } 

 @Override
 public void run()  
 { 
     String received; 
     String toreturn; 
     String clientName;
     System.out.println("Started new Client Thread");

     //after this we start a client thread 

     String message = "Initial Message received: " + formatMessage(request.getData());
     System.out.println(message);
     
     clientName = formatMessage(request.getData()).toString();
     byte[] buffer = message.getBytes();

     
     
     // server can get the info about the client
     InetAddress clientAddress = request.getAddress();
     int clientPort = request.getPort();

     DatagramPacket response = new DatagramPacket(buffer,buffer.length, clientAddress, clientPort);
     try {
		s.send(response);
	} catch (IOException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}



     
     while (true)  
     { 
    	 try {
    		 s.receive(request);
    		 message = formatMessage(request.getData()).toString();
    		 
    		 String messageOut = "Message : " + message +  " ClientName: "+ clientName;
             System.out.println(messageOut);
    		 
             System.out.println(message.equals(clientName));
    		 if(message.equals(clientName)) {
    			
    		 
             //after this we start a client thread 

        	 message = "client " + count +  " " + formatMessage(request.getData());
             System.out.println(message);

             buffer = message.getBytes();

       
             response = new DatagramPacket(buffer,buffer.length, clientAddress, clientPort);
             s.send(response);
        	 }
           
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