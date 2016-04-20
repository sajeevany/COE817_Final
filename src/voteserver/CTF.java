package voteserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import encryption.JEncrypDES;
import voteserver.VoteRequest.voteID;

//TODO hard code file names for public/private key parsing

/**
 * @author Admin
 *
 */
public class CTF {

	private static CTF instance = null;
	private final String desAlgorithm = "DES";
	private final String keyString = "des_key";
	private final String logFile = "VoteLog.txt";
	private SecretKey secretKey;
	private SecretKey ctfCommsSecretKey;
	
	private CTF(){};
	
	public static CTF getInstance()
	{
		if (instance == null)
		{
			instance = new CTF();
		}
		
		return instance;
	}
	
	public byte[] acceptVoteRequest(byte[] voteRequest) throws ClassNotFoundException, IOException
	{
		String returnMessage = "default message";
		VoteRequest decryptedVoteRequest = decryptVoteRequest(voteRequest);
		boolean isValid = validateVoteRequest(decryptedVoteRequest);
		
		//get the secret key from CLA for client
		
		
		//test the validity of the vote request
		if (isValid)
		{
			int error = logVote(decryptedVoteRequest.getMyVote());
			
			if (error == 0) 
				returnMessage = "Vote logged. Your vote was " + decryptedVoteRequest.getMyVote().toString();
			else
				returnMessage = "Unable to log vote. See System.err";
			
		}
		else
		{
			returnMessage = "Unable to log your vote. Your vote request was invalid.";
		}	
		
		return JEncrypDES.encryptDES(returnMessage, secretKey);
	}
		
	
	/**
	 * 
	 * Returns true/false if @param decryptedVoteRequest possesses the expected values
	 * 
	 * @param decryptedVoteRequest - vote request object that has been decrypted
	 * @return - true if vote request object is valid
	 */
	private boolean validateVoteRequest(VoteRequest decryptedVoteRequest) {
		
		if (decryptedVoteRequest.getValidationNumber() == 0)
			return false;
		
		//get validation + client numbers
		
		//compare validation to expected validation number
			//username + password + validation
		ArrayList<String> uav = CLA.getInstance().getUAV();
		
		if (uav.get(0).equals(decryptedVoteRequest.getClientID()) && //validate client 
				uav.get(1).equals(decryptedVoteRequest.getPassword()) && //validate password
					Integer.parseInt(uav.get(2)) == (decryptedVoteRequest.getValidationNumber())) //validate validation number
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private VoteRequest decryptVoteRequest(byte[] voteRequest) throws ClassNotFoundException, IOException {
				
		ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(voteRequest));
		return (VoteRequest) objIn.readObject();
	}

	//assumption is that client has not already voted
	public int logVote(voteID voteID)
	{
		try {
			Files.write(Paths.get(this.logFile), voteID.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to write to VoteLog.txt");
			
			return -1;
		}
		
		return 0;
	}
	
	//TODO method to return response


}
