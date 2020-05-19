
/**	
 * Implementation of Delegation Forwarding with Frequency Destination utility.
 * Lampros Vatsilidis - Univesity of ioannina - 2013-14.
 */


package routing;

import java.util.*;

import core.*;

public class DelegationFw2 extends ActiveRouter {
	
	
	
	/** 
	 * UTILITY. летяаеи посес жояес сумамтгсе емас йолбос йахе емам апо тоу аккоус 
	 * */
	private Map<DTNHost, Integer> meets;
	
	public int 				ok=1;
	public int 				dot=0;
	public int 		meetZerofy=0;
	public int 				con=0;
	public int 			mapUpdate=0;
	public int 				nod=0;
	public int 				nam=0;
	public int 	oneNodeMetings=0;
	public int allNodesMetings=0;
	public int 		mapinside=0;
	public int conbetween2nodes=0;
	public int emptySendingList=0;
	public int otherRouterNotGood=0;
	public int msgAlrdExists=1;
	
	/**
	 * Constructor. Creates a new message router based on the settings in 
	 * the given Settings object.
	 * @param s The settings object
	 */
	public DelegationFw2 (Settings s) {
		super(s);
		
	}
	
	
	/**
	 * Copyconstructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected DelegationFw2 (DelegationFw2 r) {
		super(r);
		initMeets();
	}
	
	/**
	 * аявийопоигсг летягтг епажым аккым йолбым
	 */
	private void initMeets() {
		this.meets = new HashMap<DTNHost, Integer>();
		if(meetZerofy==1)
			System.out.println("аявийопоигсг HashMap");
	}

	
	/**
	 * се лиа меа сумдесг паиямеи то ID тоу аккоу йолбоу 
	 * йаи емглеяымеи том пимайа сумамтгсеым meets ле
	 * +1 ам том евеи намадеи ч ле 1 ам еимаи г пяытг жояа
	 * (йакеи тгм updateDestMetsFor)
	 * */
	@Override
	public void changedConnection(Connection con) {
		if(mapUpdate==1)
			System.out.println("L-65= емглеяысг пимайа сумамтгсеым");

		if (con.isUp()) {
			if(mapUpdate==1)
				System.out.println("L-73= CON.isup");

			DTNHost otherHost = con.getOtherNode(getHost());
			updateDestMetsFor(otherHost);
		}
	}
	
	
	/**
	 * аунамеи том летягтг +=1 циа том йолбо поу сумамтгхгйе
	 * (йакеи тгм getMeetFor) 
	 * @param host The host we just met
	 */
	private void updateDestMetsFor(DTNHost host) {
		if(mapinside==1){
			System.out.println("L-=101= аунамеи том летягтг +=1 циа том йолбо поу сумамтгхгйе");
			System.out.println(this.getHost()+"L-105= Map BEFORE ++= "+meets);
		}
		int oldValue = getMeetFor(host);
		int newValue = oldValue +1;
		meets.put(host, newValue);
		if(mapinside==1)
			System.out.println(this.getHost()+"L-105= Map AFTER ++= "+meets);
		
		/*if(map==1)
			System.out.println("L-112= Map AFTER ++=");
			System.out.println(meets);
		*/
	}
	
	
	/**
	 *епистяежеи то пкгхос сумамтгсеым циа емам йолбо 
	 *@param host The host to look the meets for
	 * @return the current meeting times
	 */
	public int getMeetFor(DTNHost host) {
		if (meets.containsKey(host)) {
			if(oneNodeMetings==1)
				System.out.print("L=128= oneNodeMetings = "+meets.get(host));
			return meets.get(host);
			
		}
		else {
			return 0;
		}
	}
		
	
	/**
	 * епистяежеи ема лая ME тис сумамтгсеис аутоу тоу йолбоу пяос тоус аккоус
	 * @return a map of this router's meetings
	 */
	/*private Map<DTNHost, Integer> getMeetings() {
		if(allNodesMetings==1)
			System.out.print("L=131= allNodesMetings");
		return this.meets;
	}*/
	
		
	/**
	 * екецвос апожуцгс дипкотупгс апостокгс стом идио йолбо
	 */
	@Override
	protected int checkReceiving(Message m) {
		int recvCheck = super.checkReceiving(m); 
		
		if (recvCheck == RCV_OK) {
			/* don't accept a message that has already traversed this node */
			if (m.getHops().contains(getHost())) {
				recvCheck = DENIED_OLD;
			}
		}
		
		return recvCheck;
	}
			
	
	@Override
	public void update() {
		super.update();
		//ам де лпояеи ма летажеяеи ч летажеяеи гдг
		if (!canStartTransfer() ||isTransferring()) {
			return; // nothing to transfer or is currently transferring 
		}
		
		// try messages that could be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return;
		}
		//стеике ока та акка лгмулата се окоус тоу сумдеделемоус йолбоус
		tryOtherMessages();		
	}
	
	
	
	/**
	 * еды циметаи г укопоигсг тоу акцояихлоу
	 * Tries to send all other messages to all connected hosts ordered by
	 * their utility 
	 * @return The return value of {@link #tryMessagesForConnected(List)}
	 */
	private String  tryOtherMessages() {
		//амажояа ле та лгмулата поу евеи о йолбос се COLLECTION
		Collection<Message> msgCollection = getMessageCollection();
		
		//ам дем упаявоум лгмулата циа апостокг се аутг тг сумдесг епестяеье
		if (messages.size() == 0) {
			if (emptySendingList==1)
				System.out.println("messages.size() == 0");
			return "";
		}
		
		// паяе тг киста ле тис сумдесеис тоу тяевомта йолбоу циа окоус тоу аккоус
		// KAI циа йахе лиа йаме та паяайаты
		for (Connection con : getConnections()) {
		
			//паяе то ID тоу аккоу йолбоу
			DelegationFw2 othRouter = (DelegationFw2)con.getOtherNode(getHost()).getRouter();
			
			//ам о аккос йолбос еимаи се йатастасг летадосгс апежуце том
			if (othRouter.isTransferring()) {
				continue;	}
			
			//циа йахе ема апо та лгмулата тоу тяевомта йолбоу
			for (Message m : msgCollection) {
				//TA UTILITY тым дуо йолбым циа том паяакгптг тоу тяевомтос лгмулатос
				int myUtility= this.getMeetFor(m.getTo());
				int otherUtility=othRouter.getMeetFor(m.getTo());
				
				//ам о аккос йолбос евеи йакутеяо UTILITY апо елема
				//дгкадг лецакутеяг пихамотгта паяадосгс
				if ( myUtility < otherUtility) {
					
					//тупысе та лгмулата йаи тым дуо пяим тгм емглеяысг
					if(ok==1){
						System.out.println("********** BIGGER ***********");
						System.out.println("THIS MESSAGGES BEFORE= "+this.messages);
						System.out.println("OTHER MESSAGGES BEFORE= "+othRouter.messages);
						System.out.println("MSG:"+m+"- FROM: "+m.getFrom()+"- To: "+m.getTo());
						System.out.println("VIA: from "+this.getHost()+"(" +myUtility+ ") -->TO--> " 
											+othRouter.getHost()+"(" +otherUtility+ ")" );
					}	
					
					//*****аккане то UTILITY тоу тяевымта йолбоу циа том пяоояисло тоу лгмулатос
					//*****ле ауто тоу аккоу йолбоу (йакутеяо)
					this.meets.put(m.getTo(), otherUtility);
					
					//тупысе то мео UTILITY & TON NEO пимайа
					if(ok==1){
						System.out.println("New value " +otherUtility);
						System.out.println(this.getHost()+" Current Router MeetTable= " 
											+this.meets+"\n");
					}
					
					
					// ам о аккос дем евеи то лгмула
					if (!othRouter.hasMessage(m.getId())) {
						//апостокг тоу лгмулатос
						if (startTransfer(m, con) == RCV_OK) {
							//йаи ам то паяакабеи ояха
							//йаме амтицяажо тоу лгмулатос сто BUFFER тоу аккоу йолбоу
							othRouter.addToMessages(m, true);
							if(ok==1)
								System.out.println("OTHER MESSAGGES AFTER= "+othRouter.messages);
							if(dot==1)
								System.out.println(".");
						}
						
					}else {
						if(msgAlrdExists==1){
							System.out.println("Message " +m.getId()+ "allready exists to other!!!");
							
						}
					}
					
				}else if (otherRouterNotGood==1){
					System.out.println("otherRouterNotGood. Other=" +otherUtility+ 
										" ****** This=" +myUtility);
				}
			}			
		}
		
		return "";
	}
	
	
	
	
	@Override
	public RoutingInfo getRoutingInfo() {
		RoutingInfo top = super.getRoutingInfo();
		RoutingInfo ri = new RoutingInfo(meets.size() + 
				" sum of meetings (s)");
		
		for (Map.Entry<DTNHost, Integer> e : meets.entrySet()) {
			DTNHost host = e.getKey();
			int value = e.getValue();
			
			ri.addMoreInfo(new RoutingInfo(String.format("%s : %.6f", 
					host, value)));
		}
		
		top.addMoreInfo(ri);
		return top;
	}
	
	@Override
	public MessageRouter replicate() {
		DelegationFw2 r = new DelegationFw2 (this);
		return r;
	}

}
