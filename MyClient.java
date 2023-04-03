import java.net.*;
import java.io.*;

public class MyClient {
    // create a socket
    Socket mySocket;
    // initialise input and output streams
    DataOutputStream outStream;
    BufferedReader inputStream;
    // initialise server and job ID variables
    int serverID = 0;
    int jobID = 0;
    int nRecs = 0;
    String serverMessage = "empty";
    String lastServerMessage = "empty";
    String tempString = "empty";
    int numServers = 1;
    String largestServerType;
    int largestCore = 0;
    int loop = 0;

    public void send(String message) throws Exception {
        this.outStream.write((message + "\n").getBytes("UTF-8"));
    }

    public MyClient(String address, int port) throws Exception {
        mySocket = new Socket(address, port);
        outStream = new DataOutputStream(mySocket.getOutputStream());
        inputStream = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        MyClient c = new MyClient("127.0.0.1", 50000);
        c.byClient();
        // close socket
        c.mySocket.close();
        c.inputStream.close();
        c.outStream.close();
    }

    public void byClient() throws Exception {
        // send HELO to server
        send("HELO");
        System.out.println("Client message: HELO");
        // receive OK
        System.out.println("Server message: " + this.inputStream.readLine());
        // authorise user
        String authName = System.getProperty("user.name");
        System.out.println("Server prompt: LOG IN " + authName);
        send("AUTH " + authName);
        // receive OK
        System.out.println("Server Message: " + this.inputStream.readLine());

        while (!(lastServerMessage.equals("NONE"))) {

            // jobs 1-n

            // send REDY
            send("REDY"); // when we send ready server sends us an update

            // Receive message: JOBN, JCPL, NONE
          
            tempString = this.inputStream.readLine();

            

            String[] jobStatus = tempString.split(" ");

            lastServerMessage = jobStatus[0];
            jobID = Integer.parseInt(jobStatus[2]);

            System.out.println("Server Message: " + lastServerMessage);

            if (loop == 0) {

                send("GETS All"); // send GETS message

                // receive DATA nRecs recSize e.g. DATA 5 124
              

                serverMessage = this.inputStream.readLine();

                System.out.println("Server message " + serverMessage);
                // send OK
                send("OK");

                String[] words = serverMessage.split(" ");

                String record;

                for (int i = 0; i < (Integer.parseInt(words[1])); i++) {
                    // receive each record
                    record = this.inputStream.readLine();

                    String[] splitRecord = record.split(" ");

                    int currCore = Integer.parseInt(splitRecord[4]);

                    // keep track of largest server type and number of servers of that type
                    if (currCore > largestCore) {
                        largestCore = currCore;
                        largestServerType = splitRecord[0];
                    }

                    if (largestServerType.equals(splitRecord[0])) {
                        numServers++;
                    }

                }

                // send OK
                send("OK");
                // receive .
                System.out.println("Server Message: " + this.inputStream.readLine());

            }

            loop++;


            if (lastServerMessage.equals("JOBN")) {
                
                    send("SCHD " + jobID + " " + largestServerType + " " + serverID%numServers);
                  
                
            }
           
        }
        System.out.println("Server says: " + this.inputStream.readLine());
        // send QUIT
        send("QUIT");
        // receive QUIT
        System.out.println("Server says: " + this.inputStream.readLine());
    }

}