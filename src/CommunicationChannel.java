import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

// import java.util.Comparator;  
/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {
	ArrayList<Message>toSpaceExplorers;
	ArrayList<Message>toHeadQuarters;
	ArrayList<Integer> adjNodes;
	ArrayList<Message> adjNodesMessages;
	ArrayList<Integer> adjUSED;
	ArrayList<Integer> parentUSED;
	Message currentNode;
	Message prevNode;
	boolean firstNode=false;
	
	Semaphore emptyExplorer =new Semaphore(1);
	Semaphore fullExplorer =new Semaphore(0);
	Semaphore emptyHQ=new Semaphore(1);
	Semaphore fullHQ=new Semaphore(0);

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		toSpaceExplorers=new ArrayList<>();
		toHeadQuarters=new ArrayList<>();
		adjNodes=new ArrayList<>();
		adjUSED=new ArrayList<>();
		parentUSED=new ArrayList<>();
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */ 
	public void putMessageSpaceExplorerChannel(Message message) {
		try{
			emptyExplorer.acquire();
		}catch(InterruptedException e){
		}
		synchronized(toHeadQuarters){
			System.out.println("AM PUS "+message.getParentSolarSystem()+" "+message.getCurrentSolarSystem());
			this.toHeadQuarters.add(message); //adds at the end
		}
		emptyExplorer.release();
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {
		// try{
		// 	fullExplorer.acquire();
		// }catch(InterruptedException e){
		// }
		Message messageToReturn=null;
		synchronized(toHeadQuarters){
			if(toHeadQuarters.size()!=0) {
				messageToReturn=toHeadQuarters.get(0);
				toHeadQuarters.remove(0);
				System.out.println("AM LUAT "+ messageToReturn.getParentSolarSystem()+" "+messageToReturn.getCurrentSolarSystem());
			}
		}
		// fullExplorer.release();
		return messageToReturn;
		// return null;
	}
	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * Message(int currentSolarSystem, String data)
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {
		try{
			emptyHQ.acquire();
		}catch(InterruptedException e){
		}
		synchronized(toSpaceExplorers){
	
			if(message!=null &&
				message.getData()!=HeadQuarter.END &&
				!(adjNodes.contains(message.getCurrentSolarSystem()))
				) {

				if(firstNode==false) {//doar pt primul nod din lista
					firstNode=true;
					prevNode=message;
				}
				currentNode=message;
				if(!(parentUSED.contains(prevNode.getCurrentSolarSystem())) &&
					prevNode.getCurrentSolarSystem()!=
					currentNode.getCurrentSolarSystem()) { //nu e primul mes din lista
					adjNodes.add(prevNode.getCurrentSolarSystem());
					adjNodes.add(currentNode.getCurrentSolarSystem());
					toSpaceExplorers.add(prevNode);
					toSpaceExplorers.add(currentNode);
				}	
			}else 
			if(message!=null && 
				message.getData()==HeadQuarter.END &&
				message.getCurrentSolarSystem()==-1) {//am ajuns la end

				if(firstNode==true) {
					firstNode=false;
				}
				parentUSED.add(prevNode.getCurrentSolarSystem());		
				adjUSED.addAll(adjNodes);
				adjNodes.clear();
			} 
		}		
		emptyHQ.release();
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		// try{
		// 	fullHQ.acquire();
		// }catch(InterruptedException e){
		// }
		Message messageToReturn=null;
		synchronized(toSpaceExplorers){
			if(toSpaceExplorers.size()!=0) {
				messageToReturn=toSpaceExplorers.get(0);
				toSpaceExplorers.remove(0);
			}
		}
		// fullHQ.release();
		return messageToReturn;
		// return null;
	}
}
