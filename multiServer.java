import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

public class multiServer{
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Enter the port # you would like to connect to...");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server is listening on port " + port);

            while(true){
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println("New user has connected: " + socket.getInetAddress());

                    Thread clientHandler = new Thread(new ClientHandler(socket));
                    clientHandler.start();
                } catch(IOException e){
                    System.out.println("Waiting for next command...");
                    break;
                }
            }
        } catch(IOException e){
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static class ClientHandler extends Thread{
        private final Socket clientSocket;

        public ClientHandler(Socket socket){
            this.clientSocket = socket;
        }

        @Override
        public void run(){
            try (
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ){
                    int choice = 0;
                    do{
                        try{
                            choice = Integer.parseInt(reader.readLine());

                            switch(choice){
                                case 1: //Date and Time
                                    Calendar obj = Calendar.getInstance();
                                    Date date = obj.getTime();
                                    writer.println(date);
                                    break;


                                case 2: //Uptime
                                    RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
                                    writer.println("Uptime: " + rb.getUptime() + "ms");
                                    break;


                                case 3: //Memory Use
                                    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

                                    long heapMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
                                    long nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage().getUsed();
                                    long totalMemory = heapMemory + nonHeapMemory;

                                    writer.println("Total Memory Usage: " + totalMemory + " bytes\n");
                                    break;


                                case 4: //Netstat
                                    Process netProcess = Runtime.getRuntime().exec("netstat");
                                    BufferedReader netReader = new BufferedReader(new InputStreamReader(netProcess.getInputStream()));

                                    String netLine;
                                    while((netLine = netReader.readLine()) != null){
                                        writer.println(netLine);
                                    }

                                    netReader.close();
                                    break;


                                case 5: //Current Users
                                    Process userProcess = Runtime.getRuntime().exec("who");
                                    BufferedReader userReader = new BufferedReader(new InputStreamReader(userProcess.getInputStream()));
                                    
                                    String userLine;
                                    writer.println("Connected Users: ");
                                    while((userLine = userReader.readLine()) != null){
                                        writer.println(userLine);
                                    }
                                    
                                    userReader.close();
                                    break;


                                case 6: //Running Processes
                                    Process runProcess = Runtime.getRuntime().exec("ps -aux");
                                    BufferedReader runReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                                    String runLine;

                                    writer.println("Running Processes: \n");
                                
                                    while((runLine = runReader.readLine()) != null){
                                        writer.println(runLine);
                                    }
                                    break;

                                
                                case 7: //Exiting
                                    writer.println("Exiting...");
                                    break;


                                default:
                                    writer.println("Invalid command. Please choose another number.");
                                    break;
                            }
                        } catch(SocketException e){
                            System.out.println("Connection reset by client: " + clientSocket.getInetAddress());
                            break;
                        }

                        reader.close();
                        writer.close();
                    } while(choice != 7); 

                    System.out.println("Closing connection with client: " + clientSocket.getInetAddress());
                } catch(IOException e){
                    e.printStackTrace();
                } finally{
                    try{
                        clientSocket.close();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }
        }
    }
}