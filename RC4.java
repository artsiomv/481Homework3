import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class RC4 {
 
    private char[] key;
    private int[] sbox;
    private static final int SBOX_LENGTH = 256;
    private static final int KEY_MIN_LENGTH = 5;
 
    public static void main(String[] args) {
        try {
            RC4 rc4 = new RC4("testkey");
            
            BufferedReader br = new BufferedReader(new FileReader("Bible.txt"));
            StringBuilder sb = new StringBuilder();
            try {
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            long startTime = System.currentTimeMillis();
            char[] result = rc4.encrypt(sb.toString().toCharArray());
            long endTime = System.currentTimeMillis();
            
            System.out.println("RC4 " + (endTime-startTime) + " milliseconds");
            
            //CBC AES
            String key = "Bar12345Bar12345"; // 128 bit key
            String initVector = "RandomInitVector"; // 16 bytes IV
            
            long startTime1 = System.currentTimeMillis();
            encrypt(key, initVector, sb.toString(), "CBC");
            long endTime1 = System.currentTimeMillis();
            
            System.out.println("CBC " + (endTime1-startTime1) + " milliseconds");
            
            long startTime2 = System.currentTimeMillis();
            encrypt(key, initVector, sb.toString(), "CTR");
            long endTime2 = System.currentTimeMillis();
            
            System.out.println("CTR " + (endTime2-startTime2) + " milliseconds");
//            System.out.println("encrypted string:\n" + new String(result));
//            System.out.println("decrypted string:\n"
//                    + new String(rc4.decrypt(result)));
        } catch (InvalidKeyException e) {
            System.err.println(e.getMessage());
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String encrypt(String key, String initVector, String value, String mode) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/"+mode+"/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
//            System.out.println("encrypted string: "
//                    + Base64.getEncoder().encodeToString(encrypted));//.encodeBase64String(encrypted));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
 
    public RC4(String key) throws InvalidKeyException {
        setKey(key);
    }
 
    public RC4() {
    }
 
    public char[] decrypt(final char[] msg) {
        return encrypt(msg);
    }
 
    public char[] encrypt(final char[] msg) {
        sbox = initSBox(key);
        char[] code = new char[msg.length];
        int i = 0;
        int j = 0;
        for (int n = 0; n < msg.length; n++) {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + sbox[i]) % SBOX_LENGTH;
            swap(i, j, sbox);
            int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
            code[n] = (char) (rand ^ (int) msg[n]);
        }
        return code;
    }
 
    private int[] initSBox(char[] key) {
        int[] sbox = new int[SBOX_LENGTH];
        int j = 0;
 
        for (int i = 0; i < SBOX_LENGTH; i++) {
            sbox[i] = i;
        }
 
        for (int i = 0; i < SBOX_LENGTH; i++) {
            j = (j + sbox[i] + key[i % key.length]) % SBOX_LENGTH;
            swap(i, j, sbox);
        }
        return sbox;
    }
 
    private void swap(int i, int j, int[] sbox) {
        int temp = sbox[i];
        sbox[i] = sbox[j];
        sbox[j] = temp;
    }
 
    public void setKey(String key) throws InvalidKeyException {
        if (!(key.length() >= KEY_MIN_LENGTH && key.length() < SBOX_LENGTH)) {
            throw new InvalidKeyException("Key length has to be between "
                    + KEY_MIN_LENGTH + " and " + (SBOX_LENGTH - 1));
        }
 
        this.key = key.toCharArray();
    }
 
}
