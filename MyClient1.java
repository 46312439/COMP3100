import java.net.*;
import java.io.*;

public class MyClient1 {
    // create a socket
    Socket mySocket;
    // initialise input and output streams
    DataOutputStream outStream;
    BufferedReader inputStream;
    // initialise server and job ID variables
    int serverID = 0;
    int jobID = 0;
    int nRecs;
    String serverMessage = "empty";
    String lastServerMessage = "empty";
    String tempString = "empty";
    int numServers = 0;
    int coreCount = 0;
    int firstCapableServerID;
    String firstCapableServerType;
    int loop = 0;

    public void send(String message) throws Exception {
        this.outStream.write((message + "\n").getBytes("UTF-8"));
    }

    public MyClient1(String address, int port) throws Exception {
        mySocket = new Socket(address, port);
        outStream = new DataOutputStream(mySocket.getOutputStream());
        inputStream = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        MyClient1 c = new MyClient1("127.0.0.1", 50000);
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

            System.out.println("Server Message: " + lastServerMessage);

            if (lastServerMessage.equals("JOBN")) {

                jobID = Integer.parseInt(jobStatus[2]);

                send("GETS Avail " + jobStatus[4] + " " + jobStatus[5] + " " + jobStatus[6]); // send GETS Avail to get available servers according to job criteria

                // receive DATA nRecs recSize e.g. DATA 5 124
                tempString = this.inputStream.readLine();

                String[] tempArray = tempString.split(" ");
                nRecs = Integer.parseInt(tempArray[1]);

                if (nRecs > 0) {
                    // send("GETS Avail " + jobStatus[4] + " " + jobStatus[5] + " " + jobStatus[6]);
                    // // send GETS message

                    // // receive DATA nRecs recSize e.g. DATA 5 124
                    // tempString = this.inputStream.readLine();

                    // String[] tempArray = tempString.split(" ");

                    // nRecs = Integer.parseInt(tempArray[1]);

                    // send OK
                    send("OK");

                    serverMessage = this.inputStream.readLine();

                    String[] serverInfo = serverMessage.split(" ");

                    // Store the first capable server information
                    firstCapableServerID = Integer.parseInt(serverInfo[1]);
                    firstCapableServerType = serverInfo[0];

                    for (int i = 0; i < nRecs - 1; i++) {
                        serverMessage = this.inputStream.readLine();
                    }

                    // send OK
                    send("OK");
                    // receive .
                    serverMessage = this.inputStream.readLine();

                    send("SCHD " + jobID + " " + firstCapableServerType + " " + firstCapableServerID);

                    // OK
                    System.out.println("Server Message: " + this.inputStream.readLine());
                }

                else {

                    send("OK");
                    serverMessage = this.inputStream.readLine();
                    send("GETS Capable " + jobStatus[4] + " " + jobStatus[5] + " " + jobStatus[6]); // send GETS capable to get servers capable of job criteria

                    // receive DATA nRecs recSize e.g. DATA 5 124
                    tempString = this.inputStream.readLine();

                    tempArray = tempString.split(" ");
                    // String[] tempArray = tempString.split(" ");

                    nRecs = Integer.parseInt(tempArray[1]);

                    // send OK
                    send("OK");

                    serverMessage = this.inputStream.readLine();

                    String[] serverInfo = serverMessage.split(" ");

                    // Store the first capable server information

                    firstCapableServerID = Integer.parseInt(serverInfo[1]);
                    firstCapableServerType = serverInfo[0];

                    for (int i = 0; i < nRecs - 1; i++) {
                        serverMessage = this.inputStream.readLine();
                    }

                    // send OK
                    send("OK");
                    // receive .
                    System.out.println("Server Message: " + this.inputStream.readLine());

                    send("SCHD " + jobID + " " + firstCapableServerType + " " + firstCapableServerID);

                    // OK
                    System.out.println("Server Message: " + this.inputStream.readLine());

                }

            }

        }

        // send QUIT
        send("QUIT");
        // receive QUIT
        System.out.println("Server says: " + this.inputStream.readLine());

    }

}