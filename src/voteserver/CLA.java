package voteserver;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

public class CLA //implements Runnable
{
    //2. Must check to see if the client has voted already. If they haven't voted yet, then give them a validation number.
    
	//TODO make singleton
	
        static String algorithm = "RSA";
   static RSAPrivateKey privKey;
    static PublicKey pubKey;
    static String publicKeyString = "public_key_Client";
   static String privateKeyString = "private_key_Server";
   static String desAlgorithm = "DES";
   static String keyString = "des_key";
    static SecretKey secretKey;
    private static CLA instance = null;
    private String clientId;
    private String password;
    private String validationNumber;
    private String clientList = "keyFile";

    private boolean checkUserInfo(String clientId, String password) throws Exception{
       
        return false;
    }
    
    private boolean checkIfTheyHaveVoted() {
        
        return false;
    }
    
    private CLA() {
        
    }
    
    public static CLA getInstance() {
        
        if (instance == null) {
            instance = new CLA();
        }
        return instance;
    }
    
    public ArrayList<String> getUAV() {
        
        ArrayList<String> toCTF = new ArrayList();
        toCTF.add(this.clientId);
        toCTF.add(this.password);
        toCTF.add(this.validationNumber);
        
        return toCTF;
    }
    
    private static void getKeysFromFiles(){ //Gets the RSA keys.
                    
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
    
    	//TODO 
    	//return E(pubKey, secretKey)||E(secretKey, validation number)
        public ArrayList<byte[]> getAuthenticatedSessionKey(byte[] encryptedString) { //Generates a new DES key and sends that to the client.
            
            if (pubKey == null && privKey == null) {
            	getKeysFromFiles();
            } if (secretKey == null) {
           
                try {
                secretKey = JEncrypDES.desKeyGen();
                
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
            
         String fromClient = new String(JEncryptRSA.decrypt(privKey, encryptedString, algorithm)); 
         this.clientId = fromClient.substring(0, fromClient.indexOf("@"));
         this.password = fromClient.substring(fromClient.indexOf("@") + 1, fromClient.length());
         boolean checked = false;
         
         try {
         checked = checkUserInfo(clientId, password);
         } catch (Exception e) {
             System.out.println(e.toString());
         }
         
         boolean haveTheyVotedYet = true;
         if (checked) {
             haveTheyVotedYet = checkIfTheyHaveVoted();
         } else {
             return null;
         }
         
         ArrayList<byte[]> toClient = new ArrayList();
         if (!haveTheyVotedYet) {
             byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
             byte[] validationNumber = JEncrypDES.encryptDES("1", secretKey);
            toClient.add(encryptedDESKey);
            toClient.add(validationNumber);
                        return toClient;
         } else {
             byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
             byte[] validationNumber = JEncrypDES.encryptDES("0", secretKey);
            toClient.add(encryptedDESKey);
            toClient.add(validationNumber);
                        return toClient;
         }

        }
}

