/*
* This is the Front end file for the client side.
* 
* 
*/

import java.util.ArrayList;


public class trustcloud {

	private static int testToken(String tok){

		if (tok.equals("-a")) return 1;

		if (tok.equals("-c")) return 2;
				
		if (tok.equals("-f")) return 3;

		if (tok.equals("-h")) return 4;

		if (tok.equals("-l")) return 5;
		
		if (tok.equals("-u")) return 6;
		
		if (tok.equals("-v")) return 7;
			
		return -1;
	}

	private static boolean checkHostFormat(String host) {
		if (host.indexOf(":") == -1)
			return false;
		else 
			if (host.startsWith(":"))
				return false;

		return true;
	}
	
	public static void main(String args[]) {
		
		// Inicalize arrays for input

		ArrayList<String> fileUpName = new ArrayList<String>();
		ArrayList<String> fileDownName = new ArrayList<String>();
		ArrayList<String> certificate = new ArrayList<String>();
		ArrayList<String> fileVouchName = new ArrayList<String>();
		ArrayList<String> fileCertName = new ArrayList<String>();
		String serverConect = null;
		boolean listFiles = false;
		int circumferance = 0;
		
		
		// put input into arrays
		int argument = 0;
		while(argument < args.length) {
			
			if (args[argument].startsWith("-")) {
				String token = args[argument];

				switch (testToken(token)) {
					case 1: // add or replace a file to the trust cloud
							try {
								argument++;
								fileUpName.add(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error - You need to specify a filename: -a filename");
							}

							break;

					case 2: // supply the requred circumference (length) of a ring of trust
							try {
								argument++;
								circumferance = Integer.parseInt(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error - You need to specify an integer: -c number");
							}

							break;

					case 3: //fetch an existing file from the trustcloud server (simply sent to stdout)
							try {
								argument++;
								fileDownName.add(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error - You need to specify a filename: -f filename");
							}
							break;

					case 4: // provide the remote address hosting the trustcloud server
							argument++;
							try {
								if (checkHostFormat(args[argument]))
									serverConect = args[argument];
								else throw new RuntimeException("");
								} catch (Exception ex) {
									System.out.println("Hostname should be in this format - <hostname>:<port>");
								}
							break;

					case 5: //list all stored files and how they are protected
							listFiles = true;
							break;

					case 6: //upload a certificate to the trustcloud server
							try {
								argument++;
								certificate.add(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error: You need to specify the certificate: -u certificate");
							}

							break;

					case 7: //
							argument++;

							try {
								fileVouchName.add(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error: You need to specify the filename: -v filename certificate");
							}

							argument++;

							try {
								fileCertName.add(args[argument]);
							} catch (Exception e) {
								System.out.println("Input error: You need to specify the certificate: -v filename certificate");
							}

							break;
					default:
							System.out.println("Invalid input");
				}
			}
			else {
				System.out.println("Error: couldn't understand " + args[argument] + " in this context");
				break;
			}
		argument++;
		}

		// run input

		
		// conect to server
		ClientNew back = new ClientNew();

		try {
			back.openConnection(serverConect);
		}
		catch (Exception exception) {
            exception.printStackTrace();        // Print error
        }
		

		// up load certificates
		for(int i = 0; i < certificate.size(); i++){
			back.sendFile(certificate.get(i));
		}


		for(int i = 0; i < fileDownName.size(); i++){
			// download files if requirements met
			back.fetchFile(fileDownName.get(i), circumferance);
		}
		

		// upload files
		for(int i = 0; i < fileUpName.size(); i++){
			back.sendFile(fileUpName.get(i));
		}

		for(int i = 0; i < fileCertName.size(); i++){
			back.vouchForFile(fileVouchName.get(i), fileCertName.get(i));
		}

		// list all file if required
		if(listFiles){
			back.listFiles();
		}

		back.closeConnection();
	}
}
