# COEN 445 Proj
## Requirements
 - a JRE system library is needed we developed with Java SE_15


 
## Team Members
 - Gabriel Juteau ID: 40057854
 - Ewan McNeil ID: 40021787
 - Gabriel Grégoire-Bergevin ID: 40067645

 

 
## Jar Creation Guide
_This project has been successfully compiled on both IOS and Windows operating systems_
 - In order to run the project the jar files must be exported from Eclipse by 
 - Doing Right-click on project > Runnable JAR file 
 - Setting the launch configuration to either RSS-SERVER or RSS-CLIENT
 - Selecting the preferred export destinations



## Running a Server

_in the directory of the Client jar file issue the following command on mac_

Java -jar Server.jar 10011 1 localhost 10012



_in windows issue this command_



## Running a Client
_in the directory of the Client jar file issue the following command on mac_

Java -jar Client.jar localhost 54443 localhost 10011 localhost 10012

_Where the IP and ports are in the format_
ClientIP ClientPort ServerOneIP ServerOnePort ServerTwoIP ServerTwoPort

_in windows issue this command_



## interacting with the project

_after starting up the client it will prompt you to either register or update following are a list of client commands_

|   Command                   |                 Description                                                                                                                                   |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| REGISTER   |  console will prompt you to enter your name and then will register you with the server      |
| UPDATE  |   console will prompt you to enter your name and then will update your client if the server recognizes your name  |
| SUBJECTS  | console will prompt you to enter the subjects you will to subscribe to |                                              
| PUBLISH   | console will prompt you to enter the subject you want to talk on and then ask for the message to send|
| HELP   | Lists the commands in console|                                                                       | ECHO | sends a ping to the server (used for testing purposes)|

