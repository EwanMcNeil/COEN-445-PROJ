import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Vector;

public class Server {

      
	  ArrayList<String> clients;
	  Vector<ClientHandler> clientHandlers = new <ClientHandler>Vector();
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
              
              String message = formatMessage(clientMessage).toString();
              
              String splitMessage[] = message.split(" ");
    		 
              System.out.println("incoming data");
    		 
              
              if(splitMessage[0].equals("REGISTER")) {
            	  if(!(clients.contains(splitMessage[2]))){
            		  clients.add(splitMessage[2]);
            		  ClientHandler t = new ClientHandler(socket, requestPacket, count, splitMessage[2]); 
              
              
            		  // Invoking the start() method 
            		  t.start(); 
              
              
            		  clientHandlers.add(t);
            	  }
            	  else{
            		
            		  //REGISTER-DENIED RQ# Reason
            		  message = "REGISTER_DENIED" + " " + splitMessage[1] + " " +  "NAME_IN_USE";
                      System.out.println(message);

                      byte[] buffer = message.getBytes();
            		 
            		  DatagramPacket response = new DatagramPacket(buffer,buffer.length, requestPacket.getAddress(), requestPacket.getPort());
                      socket.send(response);
                      
            	  }
              }
              else {
            	  for(int i = 0; i < clientHandlers.size(); i++)
            	  {
            		
            		  if(clientHandlers.get(i).getName().equals(splitMessage[2])) {
            			  clientHandlers.get(i).newPacket(requestPacket);
            		 }
            	      
            	  
              }
              
              } }
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
 

 final DatagramSocket s; 
 Boolean startUp;
 DatagramPacket request;
 final int count;
 int RQ;

 // Constructor 
 public ClientHandler(DatagramSocket s, DatagramPacket request, int count, String Name)  
 { 
     this.s = s; 
     this.request = request;
     this.count = count;
     setName(Name);
     this.RQ = 0;
     startUp = true;
     
 } 

 @Override
 public void run()  
 { 
     String received; 
     String toreturn; 
     System.out.println("Started new Client Thread");

    
   
     
     
     // server can get the info about the client
     InetAddress clientAddress = request.getAddress();
     int clientPort = request.getPort();

  

     
     while (true)  
     { 
    	 try {
    		
    		
    		 String message = formatMessage(request.getData()).toString();
             String splitMessage[] = message.split(" ");
    		
   
    		if(startUp) {
    			//REGISTERED RQ#
    			 message = "REGISTERED " + RQ; 
                 byte[] buffer = message.getBytes();
                 DatagramPacket response = new DatagramPacket(buffer,buffer.length, clientAddress, clientPort);
                 s.send(response);
                 
                 startUp = false;
                 RQ += 1;
    		}
    		else {
	    		if(splitMessage[2].equals(this.getName())) {
	     			int check = Integer.parseInt(splitMessage[1]);
	     			
	     		if(RQ == check) {
	    			
	    		 
	             //after this we start a client thread 
	
	        	 message = "client " + count +  " " + formatMessage(request.getData());
	             System.out.println(message);
	
	             byte[] buffer = message.getBytes();
	       
	             DatagramPacket response = new DatagramPacket(buffer,buffer.length, clientAddress, clientPort);
	             s.send(response);
	             
	             RQ += 1;
	        	 }
    		}
    		}
           
         } catch (IOException e) { 
             e.printStackTrace(); 
         } }
     } 
       
     

		public void newPacket( DatagramPacket input) {
			System.out.println("new packet is called" + getName());
			this.request = input;
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