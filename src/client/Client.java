package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import voteserver.CLA;
import voteserver.VoteRequest;


public class Client {
	
	public enum designation{server, client};
	
	private String hostToConnect, alias;
	private int port, myPublicKey, myPrivateKey, mySessionKey;
	private Socket mySocket = null;
	private ObjectInputStream oInStream = null;
	private ObjectOutputStream oOutStream = null;	
	private byte expectedNounceCreatedByServer, expectedNounceCreatedByClient;
	

	public Client (String hostToConnect, int portToConnect, String clientID)
	{
		if (hostToConnect == null || Integer.toString(portToConnect) == null)
		{
			throw new IllegalArgumentException("Input values are invalid");
		}
		this.hostToConnect = hostToConnect;
		this.port = portToConnect;

	}
	
	
	private void closeApp()
	{
		try {
			this.mySocket.close();
			if (oInStream != null)
				this.oInStream.close();
			if (oOutStream != null)
				this.oOutStream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public void run()
	{	
	
		try {
			
				
		
			System.out.println("[Client] Connecting to host:" + this.hostToConnect + " on port:"  + port);
			mySocket =  new Socket(this.hostToConnect, this.port);
			System.out.println("[Client] Connected to host:" + this.hostToConnect + " on port:"  + port);
		
			
			//setup streams 
			oOutStream = new ObjectOutputStream(new BufferedOutputStream(mySocket.getOutputStream()));
			//oInStream = new ObjectInputStream(new BufferedInputStream(mySocket.getInputStream()));
			
			VoteRequest vr = new VoteRequest("me", "password", 1, VoteRequest.voteID.Anmol);
			oOutStream.writeObject(vr);
			oOutStream.flush();
			closeApp();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args)
	{
		CLA.getInstance().startListening();
		Client client = new Client("localhost", 10901, "Arjun");
	
		client.run();
	}
	

	
	
}
