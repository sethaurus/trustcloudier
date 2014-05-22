import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.util.*;

/*
	Concepts:
		- An identity within our system is represented by a name.
		- The public/private keys associated with an identity are stored as files.
		- Public keys are stored inside X509 certificates (stored as <identity>.cer)
		- Private keys are stored as unencrypted DER-encoded <identity>.jkey files,
		  which exist only on the client.
		- Certificates are all assumed to be internally self-signed.
		- Externally, identities can sign each other by vouching for certificates.
		- Vouching entails creating an encrypted hash of a file, using an identity's
		  private key.
		- Vouchings are themselves stored as .sig files, with the filename scheme
		  somefile.signed-by.someidentity.sig
		- The server cannot generate these vouchings, but it can verify them using
		  previously-uploaded certificates.
		- Anyone can upload a 'faked' signature or certificate, even overwriting an
		  existing one, but they cannot masquerade as someone else without access to
		  the private key. They can invalidate an existing ring of trust, but not
		  falsify one. When a user vouches for a file, they vouch for their *local* 
		  copy - including certificates. So if an attacker overwrites a certificate
		  on the server, nobody will vouch for it by accident, and any previous
		  vouchings of it will be invalidated.
		- To determine the size of a ring of trust protecting a particular file, we 
		  identify who has signed for that file (by scanning for valid .sigs), then
		  we determine the largest ring those identities' certificates participate in
		  (also by scanning for valid .sigs). We treat .sig files as graph edges
		  which describe an endorsement from an identity to a file (and to another
		  identity, in the case of a .cer file).

*/

class TrustManager {

	/* Begin Public Interface */

	TrustManager(){
		// No internal state is maintained.
	}


	public boolean checkCircumference(String fileName, int expectedCircumference) {
		// Determine whether a file is protected by a ring-of-trust with
		// at least the specified circumference.
		return getCircumference(fileName) >= expectedCircumference;
	}

	public int getCircumference(String fileName) {
		// We iterate over the signatories of a file, computing the largest
		// ring-of-trust that each participates in, the returning the maximum.
		int maxSeen = 0;
		for (String identityName : getFileSignatories(new File(fileName))) {
			int circumference = findLargestRing(identityName);
			if (circumference > maxSeen) {
				maxSeen = circumference;
			}
		}
		return maxSeen;
	}

	public File createSignature(File inputFile, String identityName) {
		// computes a signature from an input file and a private key (determined by name),
		// then saves this signature as a file.
		// The filename of the resulting signature determines which file+key combination
		// it represents, which will be verified upon reading.
		File outputFile = new File(inputFile.getName() + ".signed-by." + identityName + ".sig");
		try {
			// May fail here if we are unable to create the file
			outputFile.createNewFile();
			OutputStream outputStream = new FileOutputStream(outputFile);
			// -----
			PrivateKey privateKey = loadPrivateKey(identityName);
			byte[] fileData = loadFileAsBytes(inputFile.getName());

			
			Signature instance = Signature.getInstance("SHA1withRSA");
			instance.initSign(privateKey);
			instance.update(fileData);
			byte[] signature = instance.sign();
			outputStream.write(signature);
			outputStream.close();
			// -----
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return outputFile;
	}

	/* End Public Interface */

	private HashSet<String> findParentKeys(String identityName) {
		// Given the name of an identity, we determine a list of identities who have
		// vouched for it (by vouching for its certificate).

		// In our system, endorsement between identities is performed by vouching for
		// a certificate, just like any other file gets vouched-for.
		// So: to find the other identities who have vouched for an identity, we just 
		// determine who has vouched for its certificate file.
		File certFile = new File(identityName + ".crt");
		return getFileSignatories(certFile);
	}

	private int findLargestRing(String rootIdentityName) {
		// Given an identity name, we find all 'paths' to other identities via vouchings,
		// Stopping when we see a cycle. We keep the paths where the 'root' identity
		// Is also the final identity, ie a ring-of-trust which the root participates in.
		// We return the length of the longest such ruing.

		// 'finished' paths are those which form a ring with the root identity
		ArrayList<ArrayList<String>> unfinishedPaths = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> finishedPaths = new ArrayList<ArrayList<String>>();

		// Initial path lists are the individual parents of the root.
		for (String parentKey: findParentKeys(rootIdentityName)) {
			ArrayList<String> basePath = new ArrayList<String>(Arrays.asList(parentKey));
			unfinishedPaths.add(basePath);
		}
		
		// We keep building identity paths until either they form a loop, or there are no
		// parents. 
		while (unfinishedPaths.size() > 0) {
			ArrayList<ArrayList<String>> nextGeneration = new ArrayList<ArrayList<String>>();

			for (ArrayList<String> path: unfinishedPaths) {
				String lastKey = path.get(path.size() - 1);


				for (String parentKey: findParentKeys(lastKey)) {
					// We bifurcate the path when there are multiple parents
					ArrayList<String> newPath = (ArrayList<String>) path.clone();

					newPath.add(parentKey);

					if (parentKey.equals(rootIdentityName)) {
						// We have found a cycle involving the root
						finishedPaths.add(newPath);
					} else if (! path.contains(parentKey)) {
						// We have NOT found a cycle which excludes the node
						nextGeneration.add(newPath);
					}
				}
				unfinishedPaths = nextGeneration;	
			}
		}

		// Find the largest length of a cycle path
		int largestRingSeen = 0;
		for (ArrayList<String> path: finishedPaths) {
			if (path.size() > largestRingSeen) {
				largestRingSeen = path.size();
			}
		}

		return largestRingSeen;
	}

	private HashSet<String> getFileSignatories(File file) {
		// Returns a list of NAMES of signatories of this file.
		// Invalid signatures are discarded here.
		HashSet<String> result = new HashSet<String>();
		for (File signature: findPossibleSignatures(file)) {
			if (verifySignature(signature)) {
				String identityName = getNameOfSignatory(signature);
				result.add(identityName);
			}
		}
		return result;
	}

	private String getNameOfSignatory(File sigFile) {
		// Returns the name of the identity who allegedly made this signature
		return sigFile.getName().split(".signed-by.")[1].split(".sig")[0];
	}

	private String getNameOfSignee(File sigFile) {
		// Returns the alleged subject filename of this signature
		return sigFile.getName().split(".signed-by.")[0];
	}


	private boolean verifySignature(File sigFile) {
		// Given a signature file, we determine who it is claiming to be created by,
		// and what file it is claiming to sign.
		// We then check whether the signature is cryptographically valid, ie whether
		// it really corresponds to the file in question and the public key of the
		// certificate of the identity in question.

		// NB: if the file being vouched-for, or the certificate of its associated identity,
		// are changed/ overwritted, the signature is invalidated. This is by design.

		boolean result = false;
		try {
			String inputFileName = getNameOfSignee(sigFile);
			String identityName = getNameOfSignatory(sigFile);

			// Loading this public key will fail if the identity's certificate is invalid.
			PublicKey publicKey = loadPublicKey(identityName);

			byte[] fileData = loadFileAsBytes(inputFileName);
			byte[] allegedSignature = loadFileAsBytes(sigFile.getName());

			// Parse the *public* key and subject file into a signature-check,
			// and verify that it matches this alleged signature
			Signature instance = Signature.getInstance("SHA1withRSA");
			instance.initVerify(publicKey);
			instance.update(fileData);
			result = instance.verify(allegedSignature);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	private ArrayList<File> findPossibleSignatures(File subjectFile) {
		// We find and return all files which appear to be signatures of a given file.
		// They must be verified before use.
		ArrayList<File> result = new ArrayList<File>();
		File[] allFiles = new File(".").listFiles();
		for (File file : allFiles) {
			String fileName = file.getName();
			if (fileName.endsWith(".sig") && fileName.startsWith(subjectFile.getName())) {
				result.add(file);
			}
		}
		return result;
	}

	private PrivateKey loadPrivateKey(String identityName) {
		// Find a private-key file corresponding to the given identity name, and return
		// it as a PrivateKey object.
		// These files will be available only on the client.
		PrivateKey result;
		try {
			// Load a private key file
			File keyFile = new File(identityName + ".jkey");
			DataInputStream dataStream = new DataInputStream(new FileInputStream(keyFile));

			// Read the raw contents of the file
			byte[] rawKey = new byte[(int) keyFile.length()];
			dataStream.readFully(rawKey);
			dataStream.close();

			// Parse it into an object, to return.
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(rawKey);
			result = KeyFactory.getInstance("RSA").generatePrivate(spec);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	private PublicKey loadPublicKey(String identityName) {
		// Load a public key from a certificate, via its identity name
		try {
			X509Certificate cert = loadCertificate(identityName);
			return cert.getPublicKey();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private X509Certificate loadCertificate(String identityName) throws Exception {
		// Given an identity name, locate and load its certificate as an object
		File file = new File(identityName + ".crt");
		FileInputStream input = new FileInputStream(file);
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) factory.generateCertificate(input);
	}

	private byte[] loadFileAsBytes(String filePath) {
		// Load a file into a raw array of bytes.
		try {
			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			byte[] result = new byte[(int)file.length()];
			file.read(result); 
			return result;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		} 
	}

	/* Begin Tests */

	public static void main(String[] unusedArgs) {
		// If this class is run as a standalone program, we instantiate its inner
		// testing class, which will in turn instatiate the class and run some
		// tests agains its instance.
		new TrustManager.Test();
	}

	static class Test {
		TrustManager manager;

		Test() {
			// NB: These tests generate actual vouchings between files and identities.
			// We assume that the following files exist:
			//  - kittens.jpg
			//  - kittens3.jpg (a modified version of kittens.jpg)
			//  - kittens3.jpg.signed-by.alice.sig (an intentially invalid signature)
			// The following X509 certificates:
			//  - alice.crt
			//  - bob.crt
			//  - frank.crt
			//  - john.crt
			// The following DER-encoded private keys:
			//  - alice.jkey
			//  - bob.jkey
			//  - frank.jkey
			//  - john.jkey


			manager = new TrustManager();

			test_loadCertificate();
			test_findPossibleSignatures();
			test_loadPrivateKey();
			test_keys_match();
			test_verifySignature();
			test_getFileSignatories();
			test_getCircumference();
			constructRingOfTrust();
		}

		void printByteArray(byte[] byteArray) {
			Formatter formatter = new Formatter();
			for (byte letter : byteArray) {
			  formatter.format("%02x", letter);
			}
			System.out.println(formatter.toString());
		}


		void test_loadCertificate(){
			try {
				X509Certificate cert = manager.loadCertificate("alice");
				System.out.println("Public key for alice:");
				System.out.println(cert.getPublicKey());
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		
		void test_findPossibleSignatures(){
			try {
				File kittens = new File("kittens.jpg");
				ArrayList<File> result = manager.findPossibleSignatures(kittens);
				System.out.println("Signatures of kittens.jpg found: " + result.size());
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		void test_verifySignature(){
			try {
				File kittens = new File("kittens.jpg");
				File kittens3 = new File("kittens3.jpg");

				File sign = manager.createSignature(kittens, "alice");

				boolean success = manager.verifySignature(sign);
				System.out.println("Does a good sig verify? " + success);
				// this file is known to be a faked signature
				File badSign = new File("kittens3.jpg.signed-by.alice.sig");
				success = manager.verifySignature(badSign);
				System.out.println("Does a BAD sig verify? " + success);

			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		void test_loadPrivateKey() {
			
			System.out.println(manager.loadPrivateKey("alice"));
		}

		void test_keys_match() {
			try {
				RSAPrivateKey priv = (RSAPrivateKey) manager.loadPrivateKey("alice");
				X509Certificate cert = manager.loadCertificate("alice");
				RSAPublicKey pub = (RSAPublicKey) cert.getPublicKey();
				boolean they_match = (priv.getModulus() == pub.getModulus());
				System.out.println("Do the keys match for alice?");
				System.out.println(pub.getModulus().equals(priv.getModulus()));
				cert.verify(pub);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		void test_getFileSignatories(){
			try {
				File kittens = new File("kittens.jpg");
				HashSet<String> signatories = manager.getFileSignatories(kittens);
				System.out.println("Signatories for " + kittens.getName() + ": " + signatories);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		void constructRingOfTrust() {
			manager.createSignature(new File("bob.crt"), "alice");
			manager.createSignature(new File("frank.crt"), "bob");
			manager.createSignature(new File("john.crt"), "frank");
			manager.createSignature(new File("alice.crt"), "john");
			manager.createSignature(new File("alice.crt"), "bob");
			System.out.println(manager.findParentKeys("alice"));
		}

		void test_getCircumference() {
			String fileName = "kittens.jpg";
			int circumference = manager.getCircumference(fileName);
			System.out.println("Ring circumference for " + fileName + ": " + circumference);
		}
		
	}

}
