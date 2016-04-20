package voteserver;

import javax.crypto.SecretKey;

import encryption.JEncrypDES;
import voteserver.VoteRequest.voteID;

//TODO hard code file names for public/private key parsing

public class CTF {

	private static CTF instance = null;
	private final String desAlgorithm = "DES";
	private final String keyString = "des_key";
	private SecretKey secretKey;
	
	private CTF(){};
	//TODO make constructor
		//get instance of CLA
		//get list of eligible votes
	public static CTF getInstance()
	{
		if (instance == null)
		{
			instance = new CTF();
		}
		
		return instance;
	}
	
	public byte[] acceptVoteRequest(byte[] voteRequest)
	{
		String returnMessage = "default message";
		VoteRequest decryptedVoteRequest = decryptVoteRequest();
		boolean isValid = validateVoteRequest();
		
		//get the secret key from CLA
		
		if (isValid)
		{
			logVote(decryptedVoteRequest.getMyVote());
			returnMessage = "Vote logged. Your vote was " + decryptedVoteRequest.getMyVote().toString();
		}
		else
		{
			returnMessage = "Unable to log your vote. Your vote request was invalid.";
		}	
		
		return JEncrypDES.encryptDES(returnMessage, secretKey);
	}
		
	private boolean validateVoteRequest() {
		// TODO Auto-generated method stub
		return false;
	}
	private VoteRequest decryptVoteRequest() {
		// TODO Auto-generated method stub
		return null;
	}
	public void logVote(voteID voteID)
	{
		//get handle on logging file
		
		
		
	}
	
	//TODO method to return response


}
