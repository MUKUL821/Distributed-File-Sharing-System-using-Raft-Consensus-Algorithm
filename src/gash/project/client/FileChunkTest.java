package gash.project.client;
import java.lang.*;
public class FileChunkTest {




	   public static void main(String[] args) {

	   int arr1[] = { 10, 1, 2, 3, 4, 6 };
	   int arr2[] = { 5, 10, 20, 30, 40, 50 };
	    int add[]=new int[14];
	   // copies an array from the specified source array
	  System.arraycopy(arr1, 0, add,0, arr1.length);
	    System.arraycopy(arr2, 0, add, arr1.length, arr2.length);
	   System.out.print("array2 = ");
	   System.out.print(arr2[0] + " ");
	   System.out.print(arr2[1] + " ");
	   System.out.print(arr2[2] + " ");
	   System.out.print(arr2[3] + " ");
	   System.out.print(arr2[4] + " ");
	   for(int i=0;i<add.length;i++)
		   System.out.println(add[i]);
		   
		   System.out.println("new");
		   byte[] ciphertext = { (byte) 204, 29, (byte) 207, (byte) 217 };
		   byte[] mac = { (byte) 25, 99, (byte) 7, (byte) 66 };
		   byte[] mac2= { (byte) 252, 9, (byte) 27, (byte) 26 };
		   byte[] destination = new byte[ciphertext.length + mac.length];

		// copy ciphertext into start of destination (from pos 0, copy ciphertext.length bytes)
		System.arraycopy(ciphertext, 0, destination, 0, ciphertext.length);

		// copy mac into end of destination (from pos ciphertext.length, copy mac.length bytes)
		System.arraycopy(mac, 0, destination, ciphertext.length, mac.length);
		
		for(int i=0;i<destination.length;i++)
			   System.out.println(destination[i]);
	   }
	  
	}


