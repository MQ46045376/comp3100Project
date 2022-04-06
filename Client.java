/*
1: Create a socket
2: Initialise input and output streams associated with the socket
3: Connect ds-server
4: Send HELO
5: Receive OK
6: Send AUTH username
7: Receive OK
8: While the last message from ds-server is not NONE do // jobs 1 - n
9: Send REDY
10: Receive a message // typically one of the following: JOBN, JCPL and NONE
//Identify the largest server type; you may do this only once
11: Send a GETS message, e.g., GETS All
12: Receive DATA nRecs recSize // e.g., DATA 5 124
13: Send OK
14: For i = 0; i < nRecs; ++i do
15: Receive each record
16: Keep track of the largest server type and the number of servers of that type
17: End For
18: Send OK
19: Receive .
20: If the message received at Step 10 is JOBN then
21: Schedule a job // SCHD
22: End If
23: End While
24: Send QUIT
25: Receive QUIT
26: Close the socket
*/

package ds_sim_Client;

import java.io.*;
import java.net.*;

public class Client {

	private final static int SocketNumber = 50000; // change this to desired socket number
	private final static String UserName = "someUserName"; //change this to username
	
	
	public static void main(String[] args) {
		// The commented numbers are in reference to the lines in LRR sudo code
		// LRR = Largest Round Robin
		try {
			//1,2,3
			// Create socket, init in/out streams and connect to ds-server
			Socket s = new Socket("localhost", SocketNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			//4,5,6
			dout.write("HELO\n".getBytes()); //send HELO
			dout.flush(); //Design choice: flushing after every write to ensure nothing messes up in output stream
			
			String msg; //msg holds the last msg sent by server
			msg = in.readLine(); //receive OK
			System.out.println("DS-Server: " + msg); 
			
			//if (msg.contains("OK")) { //should contain OK anyway
				dout.write(("AUTH " + UserName + "\n").getBytes());
				dout.flush();
			//}
