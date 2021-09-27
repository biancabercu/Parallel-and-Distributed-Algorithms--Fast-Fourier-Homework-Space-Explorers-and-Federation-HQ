import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Set;
import java.nio.charset.StandardCharsets;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {
	Integer hashCount;
	volatile Set<Integer> discovered;
	CommunicationChannel channel;
	int counter=0;
	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */
	//SpaceExplorer(numberOfHashes, solved, channel)
	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount=hashCount;
		this.discovered=discovered;
		this.channel=channel;
	}

	@Override
	public void run() {
		while (true) {
			//verify if there is any message from HQ
			Message firstMessage = channel.getMessageHeadQuarterChannel();
			Message secondMessage = channel.getMessageHeadQuarterChannel();

			if (firstMessage == null) {
				// continue;
				System.exit(0);
			}
			//verify if this is the end
			if(firstMessage.getData()==HeadQuarter.END) {
				continue;
			}
			if(secondMessage.getCurrentSolarSystem()!=-1 &&
				!(discovered.contains(secondMessage.getCurrentSolarSystem()))){
				Integer parentSyst=firstMessage.getCurrentSolarSystem();
				Integer currentSyst=secondMessage.getCurrentSolarSystem();
				discovered.add(currentSyst); //adaugam si pe lista asta discovered?
		
				String dataToDecrypt=secondMessage.getData();
				String encryptedData=encryptMultipleTimes(dataToDecrypt, hashCount);
				
				Message messagetoHQ= new Message(parentSyst, currentSyst, encryptedData);
				// Message messagetoHQ=null;
				channel.putMessageSpaceExplorerChannel(messagetoHQ);
			}
			
		}
	}
	
	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
