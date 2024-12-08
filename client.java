import java.net.*;
import java.io.*;
import java.util.*;

public class client{
    public static void main(String[] args) throws InterruptedException{
        if (args.length < 2) {
            System.out.println("Enter the <Host Name> and <Port>");
            return;
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);

        try( Socket ClientHandle = new Socket(hostName, port);
             PrintWriter writer = new PrintWriter(ClientHandle.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ClientHandle.getInputStream()));
             Scanner consoleInput = new Scanner(System.in)){

            int choice = -1;
            String operation = "";

            ArrayList<ClientHandle> requestList = new ArrayList<>();

            do{
                System.out.println();
                System.out.println("Enter the number of which command you would like to use: " +
                        "\n1) Date and Time" +
                        "\n2) Uptime" +
                        "\n3) Memory Use" +
                        "\n4) Netstat" +
                        "\n5) Current Users" +
                        "\n6) Running Processes" +
                        "\n7) Exit" +
                        "\n");

                choice = consoleInput.nextInt();

                if(choice < 1 || choice > 7){
                    System.out.println("Invalid Input... Enter a number between 1-7");
                } 
                else if(choice == 7){
                    System.out.println("Exiting...");
                    writer.println(choice);
                    break;
                } 
                else{
                    switch(choice){
                        case 1:
                            operation = "Date and Time";
                            break;

                        case 2:
                            operation = "Uptime";
                            break;

                        case 3:
                            operation = "Memory Use";
                            break;

                        case 4:
                            operation = "Netstat";
                            break;

                        case 5:
                            operation = "Current User(s)";
                            break;

                        case 6:
                            operation = "Running Processes";
                            break;

                        default:
                            operation = "Invalid Input";
                            break;
                    }
                    writer.println(choice);
                    System.out.println();
                    System.out.println("How many " + operation + " requests should be made?");
                    int numRequests = consoleInput.nextInt();
                    System.out.println();

                    ClientHandle[] requests = new ClientHandle[numRequests];

                    for(int i = 0; i < numRequests; i++){
                        requests[i] = new ClientHandle(hostName, port, choice, i + 1);
                        requests[i].start();
                    }

                    for(int i = 0; i < numRequests; i++){
                        requestList.add(requests[i]);
                    }

                    writer.close();
                    ClientHandle.close();
                    for(ClientHandle clientThread : requestList){
                        clientThread.join();
                    }

                    long totalTime = 0;
                    for (ClientHandle handle : requestList){
                        totalTime += handle.time;
                    }

                    System.out.println();
                    System.out.println("Total Turn-Around Time: " + totalTime + "ms");
                    System.out.println("Average Turn-around Time: " + (double) totalTime / requestList.size() + "ms");
                    requestList.clear();
                    System.out.println();
                }
            } while(choice != 7);
        } catch(UnknownHostException ex){
            System.out.println("Server not found: " + ex.getMessage());
        } catch(IOException ex){
            System.out.println("I/O error: " + ex.getMessage());
        }
    }



    static class ClientHandle extends Thread{
        private final String hostName;
        private final int port;
        private final int userChoice;
        private final int thread;
        private long time = 0;

        public ClientHandle(String hostName, int port, int userChoice, int thread){
            this.hostName = hostName;
            this.port = port;
            this.userChoice = userChoice;
            this.thread = thread;
        }

        @Override
        public void run(){
            try (Socket clientSocket = new Socket(hostName, port);
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){

                long timeStart = System.currentTimeMillis();
                writer.println(userChoice);

                String outputFromServer;
                while((outputFromServer = reader.readLine()) != null){
                    long timeEnd = System.currentTimeMillis();
                    time = timeEnd - timeStart;
                    System.out.println(outputFromServer + " --Thread #:" + thread + " --Time: " + time + "ms");
                }
                
                reader.close();
                writer.close();
                clientSocket.close();
            } catch(IOException ex){
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}







