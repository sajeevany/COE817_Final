package voteserver;

public class VoteRequest {

	private String clientID = null;
	private String password = null;
	private int validationNumber = 0;
	private voteID myVote;
	
	//enum of possible candidates in election
	public enum voteID
	{
		Arjun, Sajeevan, Krishna, Anmol
	}
	
	//constructor to be used by client during first request to CLA
	public VoteRequest(String clientID, String password, voteID myVote) {
		super();
		this.clientID = clientID;
		this.password = password;
		this.validationNumber = 0;
		this.myVote = myVote;
	}
	
	//constructor to be used by CLA in response to client vote request
	public VoteRequest(String clientID, String password, int validationNumber, voteID myVote) {
		super();
		this.clientID = clientID;
		this.password = password;
		this.validationNumber = validationNumber;
		this.myVote = myVote;
	}
	

	public int getValidationNumber() {
		return validationNumber;
	}

	public void setValidationNumber(int validationNumber) {
		this.validationNumber = validationNumber;
	}

	public String getClientID() {
		return clientID;
	}

	public String getPassword() {
		return password;
	}

	public voteID getMyVote() {
		return myVote;
	}

}
