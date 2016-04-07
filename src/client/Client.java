package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;


public class Client {
	
	public enum designation{server, client};
	
	private String hostToConnect, alias;
	private int port, myPublicKey, myPrivateKey, mySessionKey;
	private ServerSocket sSocket;
	private Socket mySocket = null;
	private DataOutputStream dataOutStream;
	private DataInputStream dataInStream;
	private BufferedReader  bR;
	private  BufferedWriter bW;
//	private SecretKey sharedKey;	
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
	
	
	public void openPort() throws IOException
	{
		sSocket = new ServerSocket(port);
		sSocket.setSoTimeout(10000);		
	}
	
	private void closeApp()
	{
		try {
			dataOutStream.close();
			dataInStream.close();
			bR.close();
			bW.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	private void chat()
	{
		do{
			try{
				byte[] byteMessage = readByteStream();
			//	String receivedMessage = decryptDESEncryptedMessage(byteMessage, this.sharedKey, expectedNounce);
				
			//	writeMessageToChatWindow("[ " + ((this.myDesignation == designation.server) ? "client" : "server")  +"][decrypted]" + receivedMessage);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}while(true);
	}
	
	
	private byte[] readByteStream() throws IOException
	{
		byte[] message = null;
		//set public key of opposite machine
		
		int messageLength = dataInStream.readInt();
		if (messageLength > 0)
		{
			message = new byte[messageLength];
			dataInStream.readFully(message, 0, messageLength);
			//this.writeMessageToChatWindow("[Received Encrypted]" + Arrays.toString(message));
		}
		
		return message;
	}

	/**
	 * Sends raw byte array to secondary application at target socket
	 * 
	 * @param length - length of message
	 * @param message - byte array of message
	 */
	public void sendRawMessage(int length, byte[] message)
	{
		try {
			//send size of message
			dataOutStream.writeInt(length);
			//send message
			dataOutStream.write(message);
		} catch (IOException e) {
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
				dataOutStream = new DataOutputStream(mySocket.getOutputStream());
				dataInStream = new DataInputStream(mySocket.getInputStream());
				bR = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
				bW = new BufferedWriter(new OutputStreamWriter(mySocket.getOutputStream()));
				
			//	handshake();
				this.expectedNounceCreatedByServer = (byte) 22;
				this.expectedNounceCreatedByClient = (byte) 22;
			//	chat();
				
				closeApp();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args)
	{
		CLA.getInstance().startListener();
		Client client = new Client("localhost", 10901, "Arjun");
	//	ChatApp client = new ChatApp("localhost", 10901, "client", "keyGenerationSeed", ChatApp.designation.client);
		
		//server.start();
		client.run();
	}
	

	
	
}
