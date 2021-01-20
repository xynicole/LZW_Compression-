/*************************************************************************
 * Compilation: javac LZWmod.java Execution: java LZWmod - < input.txt
 * (compress) Execution: java LZWmod + < input.txt (expand) Dependencies:
 * BinaryStdIn.java BinaryStdOut.java
 *
 * Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/

public class LZWmod {
	private static final int R = 256; // number of input chars
	// private static final int L = 4096; // number of codewords = 2^W
	// private static final int W = 12; // codeword width
	private static final int MIN_W = 9;
	private static final int MIN_L = 512; // number of codewords = 2^W
	private static final int MAX_W = 16;
	private static final int MAX_L = 65536;
	private static int W = MIN_W;
	private static int L = MIN_L;
	private static boolean reset = false;
    private static double unCompressed = 0.0;
    private static double compressed = 0.0;
    private static double oldRatio = 0.0;
    private static double newRatio = 0.0;
    private static final double trigger = 1.1; //compression ratio => codewords all used =>reset when ratio degrades to trigger
    private static String flag; //check monitor
    
	public static void compress() {
		// TODO:Modify TSTmod so that the key is a
		// StringBuilder instead of String
		TSTmod<Integer> st = new TSTmod<Integer>();
		for (int i = 0; i < R; i++)
			st.put(new StringBuilder("" + (char) i), i);
		int code = R + 1; // R is codeword for EOF
		
		//flag, 1 bit => store the reset/ do nothing mode => for expand to check
		//before decompression, program will read this flag and determine whether or not 
		//to reset the dictionary when running out of codewords.
		if (reset == true) {
			BinaryStdOut.write(1, 1);
		}else {
			if(flag.equals("m")) { //read monitor as 2 bits
				BinaryStdOut.write(11, 2); 
			}else {
				BinaryStdOut.write(0, 1);
			}
				
		}
			

		// initialize the current string
		StringBuilder current = new StringBuilder();
		// read and append the first char
		char c = BinaryStdIn.readChar();
		current.append(c);
		Integer codeword = st.get(current);

		while (!BinaryStdIn.isEmpty()) {
			codeword = st.get(current);
			// TODO: read and append the next char to current
			c = BinaryStdIn.readChar();
			current.append(c);

			if (!st.contains(current)) {
				// write codewrd to the file
				BinaryStdOut.write(codeword, W);
				
				
				unCompressed += current.length() * 8; // uncompressed data = string length * char bits (8)
				compressed += W;  //compressed data = number of w bits codeword added

				// Add to symbol table if not full
				if (code < L) {
					st.put(current, code++);
					//compression ratio= uncompressed data generated  /compressed data generated
					oldRatio = unCompressed/compressed; 

					// if codebook is full
				} else if (code >= L) {
					// increase codeword width if w not reach to the max=16
					if (W < MAX_W) {
						W++;
						L *= 2;
						st.put(current, code++);

						// if w reach to the max
					} else if (W >= MAX_W) {
						// if arg is reset then reset codebook
						if (reset == true) {
							W = MIN_W;
							L = MIN_L;

							TSTmod<Integer> temp = new TSTmod<Integer>();
							for (int i = 0; i < R; i++)
								temp.put(new StringBuilder("" + (char) i), i);

							st = temp;
							code = R + 1; // R is codeword for EOF
							st.put(current, code++);
						
							//monitor mode
						}else if(flag.equals("m")) {
							newRatio = unCompressed/compressed;	
							W = MIN_W;
							L = MIN_L;
							
							//reset old reatio
							oldRatio = 0.0; 
							
							//if a ratio of compression ratios [(old ratio)/(new ratio)] reach to trigger
							if((oldRatio/newRatio)>trigger){
								//reset
								TSTmod<Integer> temp = new TSTmod<Integer>();
								for (int i = 0; i < R; i++)
									temp.put(new StringBuilder("" + (char) i), i);

								st = temp;
								code = R + 1; // R is codeword for EOF
		                        st.put(current, code++);
		                        }
		              
								
						}
					}
				}

				// TODO: reset current
				current = new StringBuilder();
				current.append(c);

			}
		}

		// TODO: Write the codeword of whatever remains
		// in current
		codeword = st.get(current);
		BinaryStdOut.write(codeword, W);

		BinaryStdOut.write(R, W); // Write EOF
		BinaryStdOut.close();
	}

	public static void expand() {
		String[] st = new String[L];
		int i; // next available codeword value

		// initialize symbol table with all 1-character strings ASCII chars
		for (i = 0; i < R; i++)
			st[i] = "" + (char) i;
		st[i++] = ""; // (unused) lookahead for EOF
		
		
		// read if is reset or do nothing 
		boolean mode = BinaryStdIn.readBoolean();
		if(mode == true) {
			reset = true;
		}else {
			reset = false;
		}
		//read if is monitor
		int monitorMode = BinaryStdIn.readInt(2);
		if(monitorMode == 11) {
			flag = "m";
		}
		
		int codeword = BinaryStdIn.readInt(W);
		String val = st[codeword];

		while (true) {
			//monitor
			unCompressed += val.length()*8; 
            compressed += W;  
			
			// if codewrod is reached to the current codebook
			if (i >= L) {
				// increase codeword width if w not reach to the max=16
				if (W < MAX_W) {
					W++;
					L *= 2;
					
					//ArrayIndexOutOfBoundsException
					//updates symbol table L in order to fill up all i codeword
					//resize st array and copy and transfer 
					String[] temp = new String[L];
	                for (int j = 0; j < st.length; j++) { 
	                	temp[j] = st[j]; 
	                }
	                st = temp;

					// if w reach to the max
				} else if (W >= MAX_W) {
					// if arg is reset then reset codebook
					if (reset == true) {
						W = MIN_W;
						L = MIN_L;

						String[] temp = new String[L];
						for (i = 0; i < R; i++)
							temp[i] = "" + (char) i;
						temp[i++] = ""; // (unused) lookahead for EOF
		                st = temp;
		                i = R+1;
		                
		            	//monitor mode
					}else if(flag.equals("m")) {
						newRatio = unCompressed/compressed;	
						
						W = MIN_W;
						L = MIN_L;
						
						//reset old reatio
						oldRatio = 0.0; 

						
						// if ratio of ratio reach to trigger 
						if((oldRatio/newRatio)>trigger){
							String[] temp = new String[L];
							for (i = 0; i < R; i++)
								temp[i] = "" + (char) i;
							temp[i++] = ""; // (unused) lookahead for EOF
			                st = temp;
			                i = R+1;
						}
					}
				}
			}
			
			
			BinaryStdOut.write(val);
			codeword = BinaryStdIn.readInt(W);
			if (codeword == R)
				break;
			String s = st[codeword];
			if (i == codeword)
				s = val + val.charAt(0); // special case hack
			if (i < L)
				st[i++] = val + s.charAt(0);
			val = s;

		}
		BinaryStdOut.close();
	}

	public static void main(String[] args) {
		// if (args[0].equals("-")) compress();
		if (args[0].equals("-")) {
			if (args[1].equals("n")) {
				reset = false;
				compress();
			} else if (args[1].equals("r")) {
				reset = true;
				compress();
			} else if (args[1].equals("m")) {
				flag = "m";
				reset = false;
				compress();
				
			}else {
				reset = false;
				compress();
			}
			// }else throw new IllegalArgumentException("Illegal command line argument");
		} else if (args[0].equals("+"))
			expand();
		else
			throw new RuntimeException("Illegal command line argument");
	}

}
