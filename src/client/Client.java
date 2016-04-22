package client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;
import voteserver.CLA;
import voteserver.CTF;
import voteserver.VoteRequest;


public class Client// implements Runnable
{
	private final static String algorithm = "RSA";
	private static RSAPrivateKey privKey;
	private static PublicKey pubKey;
	private final static String publicKeyString = "public_key_Server";
	private final static String privateKeyString = "private_key_Client";
	private final static String desAlgorithm = "DES";
	private final static String keyString = "des_key";
	private static SecretKey secretKey;
    private static String clientID = "c0";
    private static String password = "p0";
        
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //1. Send a request to vote to the CLA.
        //3. Get the validation number from the CLA.
        //4. Cast vote to CTF
        //6. Read response from CTF. Close program.
    	
    	// private_key_client and public_key_server keys 
        getKeysFromFiles();
        
        //handshake
        // send hard coded user details ID and PW
        String myUserInfo = clientID + "@" + password;
        
        // Encrypt User info with CLA's public key
        byte[] encryptedString = JEncryptRSA.encrypt(pubKey, myUserInfo.getBytes(), algorithm);
        
        
        //Get authenticated session key from CLA, If userID and pw Exists and are correct
        ArrayList<byte[]> claResponse = CLA.getInstance().getAuthenticatedSessionKey(encryptedString);
        
        //IF response is null the ID and pw are not legitamite 
        if (claResponse == null) {
            System.out.println("ID and password don't match.");
            System.exit(0);
        }
        
        //They are a valid user.
        // Get Session key value from CLA, decrypt using CLA private key
        byte[] sessionSecretKey = JEncryptRSA.decrypt(privKey, claResponse.get(0), algorithm);
        // initialize Secrey key object used by cryptography lib
        secretKey = new SecretKeySpec(sessionSecretKey, 0, sessionSecretKey.length, desAlgorithm);
        // decrypt validation number from CLA 
        String validationNumberString = new String(JEncrypDES.decryptDES(claResponse.get(1), secretKey));
        int validationNumber = Integer.parseInt(validationNumberString);
        
        // If the validation number was return as zero, means you have already voted
        if (validationNumber == 0) {
            System.out.println("You have already voted.");
            System.exit(0);
        }

        //They have not voted yet.
        // Create VoteRequest object to be sent to CTF
        //Validation number is for the CTF to check legitimacy, VoteRequest.voteID is the candidate being voted for
         VoteRequest voteRequest = new VoteRequest(clientID, password, validationNumber, VoteRequest.voteID.KRISHNA);   
         
         //Initialize byte stream to send to CTF
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytesOfVoteRequest;
        
        try {	
        		// send vote request to CTF, 
                out = new ObjectOutputStream(bos);   
                 out.writeObject(voteRequest);
                 bytesOfVoteRequest = bos.toByteArray(); //Encrypt the bytes of the object.
                 
        } finally {
                try {
                 if (out != null) {
                	 	// close stream
                         out.close();
                    }
              } catch (IOException ex) {
                      System.out.println(ex.toString());
             }
             try {
             bos.close();
             } catch (IOException ex) {
                  System.out.println(ex.toString());
             }
        }
        
        if (bytesOfVoteRequest == null) {
            System.out.println("Couldn't create bytes of vote request.");
            System.exit(0);
        }
        //encrypt vote request 
        byte[] voteRequestToCTF = JEncrypDES.encryptDESAsBytes(bytesOfVoteRequest, secretKey);         
        // register vote with CLA
        byte[] returnMessage = CTF.getInstance().acceptVoteRequest(voteRequestToCTF);
        // get message from CTF, decrypt using DES 
        String messageFromCTF = new String(JEncrypDES.decryptDES(returnMessage, secretKey));
        System.out.println(messageFromCTF);
        System.exit(0);
    }
	
   private  static void getKeysFromFiles(){
                    
            //Get public key from file.
       try {
            FileInputStream keyfis = new FileInputStream(publicKeyString);
            byte[] encKey = new byte[keyfis.available()];  
            keyfis.read(encKey);
            keyfis.close();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            pubKey = keyFactory.generatePublic(pubKeySpec);
            //System.out.println(pubKey);
            
            //Get private key from file.
            FileInputStream keyfis2 = new FileInputStream(privateKeyString);
            byte[] encKey2 = new byte[keyfis2.available()];  
            keyfis2.read(encKey2);
            keyfis2.close();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey2);
            KeyFactory keyFactory2 = KeyFactory.getInstance(algorithm);
             privKey = (RSAPrivateKey)keyFactory2.generatePrivate(privKeySpec);
            //System.out.println(privKey);
       } catch (Exception e) {
           System.out.println(e.toString());
       }
    }   
}
