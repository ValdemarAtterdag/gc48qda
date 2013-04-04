import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;


public class gc48qda {
	static int noCaches = 0;
	static long[] cacheIds = new long[20000];
	static String[] cacheNames = new String[20000];
	static int []cacheIndex = new int[10]; // array with references/indices of selected caches.
	static int noSolutions = 0;
	static int noSelected;

	private static int readCaches(String fileName) throws IOException {
		int noDropped = 0;
		System.out.println("Reading from file " + fileName);
		// Open the file that is the first 
		// command line parameter
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
			long cId = 0;
			boolean addCache = true;
			for (int i = 2; i<strLine.length(); i++) {
				int c = strLine.charAt(i) - 48; // ascii '0' value.

				long cValue = (long) 1 << c;
				// System.out.println(strLine.charAt(i) + " " + c + " " + cValue);
				if ((cId & cValue) != 0)
					addCache = false;
				else
					cId = cId | cValue;
			}
			if (addCache) {
				cacheIds[noCaches] = cId;
				cacheNames[noCaches++] = strLine;
				System.out.println ("Added " + strLine);
			} else {
				System.out.println ("Skipped w/ duplicate char: " + strLine);
				noDropped++;
			}
		}
		in.close();
		System.out.println("Loaded " + noCaches + " caches w/o duplicated characters (dropped " + noDropped + ")");
		return noCaches;
	}



	private static int noBits(long value) {
		return Long.bitCount(value);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// Read the caches from file given as argument
		readCaches(args[0]);	

		// Invariant: We enter the scene with a selected array of noSelected caches which is not conflicting and have less than 31 bits set.
		// We then search for the noSelected+1 cache to add starting from the cache# selected at #noSelected + 1.
		// If this is not possible, then we backtrack by throwing away caches. 
		// If not correct, then the last selected value will be advanced until there is a possible match - and then a guess will be made for the next level
		// If it is not possible to find a match by adjusting the last value, then the number of selections are backtracked.
		// to start we select the first cache.
		noSelected = 0;
		int backTrack = -1; // used to store first cache index to search. 0 means not backtracking.

		while (true) {

			// First, OR values for all selected caches, and determine number of bits set.
			long selectedBits= 0;
			for (int i=0; i<noSelected; i++) 
				selectedBits |= cacheIds[cacheIndex[i]];	    	 
			int noBits = noBits(selectedBits);

			// debug
			//System.out.print("No selected = " + noSelected + ": ");
			//for (int i=0; i<noSelected; i++)
			//	System.out.print(cacheNames[cacheIndex[i]] + "(" + noBits(cacheIds[cacheIndex[i]]) + "), ");
			//System.out.println(" total bits = " + noBits);

			// Now, let's try to place another cache. Starting from one higher than the one just selected.  
			int j;
			int searchStart;
			if (backTrack != -1)
				searchStart = backTrack + 1;
			else {
				if (noSelected == 0)
					searchStart = 0;
				else 		
					searchStart = cacheIndex[noSelected-1] + 1;
			}
			backTrack = -1;

			for (j = searchStart; j < noCaches; j++) {
				int jBits = noBits(cacheIds[j]);
				
				//System.out.print(" checking " + cacheNames[j] + " (" + jBits + ") ");
				// We need a cache with no overlaps, and which does not add too many bits
				if (((selectedBits & cacheIds[j]) == 0) &&
						(jBits + noBits < 32)) {
					// Good. Let's take this one.
					//System.out.println("good" + cacheNames[j]);
					cacheIndex[noSelected] = j;
					noSelected++;

					// Are we done? - need 31 bits set
					if (jBits + noBits == 31) {
						System.out.println("Solution # " + (++noSolutions) + ": ");
						for (int i=0; i<noSelected+1; i++) {
							System.out.print(cacheNames[cacheIndex[i]] + ", ");
						}
						System.out.println();
						
						// continue search
						continue; 
						
					} 
					break;
				}
			}
			if (j == noCaches) {
				// No matching cache to add. Must backtrack.
				noSelected--;
				if (noSelected == -1) {
					System.out.println("No more solution(s)");
					return;
				}
				backTrack = cacheIndex[noSelected]; 
			}

		}

	}
}
