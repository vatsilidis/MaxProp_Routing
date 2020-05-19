
/**	
 * Implementation of Delegation Forwarding with Frequency Destination utility.
 * Lampros Vatsilidis - Univesity of ioannina - 2013-14.
 */


package routing;

import java.util.*;

import core.*;

public class DelegationFw2 extends ActiveRouter {
	
	
	
	/** 
	 * UTILITY. ������� ����� ����� ��������� ���� ������ ���� ���� ��� ��� ������ 
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
	 * ������������ ������� ������ ����� ������
	 */
	private void initMeets() {
		this.meets = new HashMap<DTNHost, Integer>();
		if(meetZerofy==1)
			System.out.println("������������ HashMap");
	}

	
	/**
	 * �� ��� ��� ������� ������� �� ID ��� ����� ������ 
	 * ��� ���������� ��� ������ ����������� meets ��
	 * +1 �� ��� ���� ������� � �� 1 �� ����� � ����� ����
	 * (����� ��� updateDestMetsFor)
	 * */
	@Override
	public void changedConnection(Connection con) {
		if(mapUpdate==1)
			System.out.println("L-65= ��������� ������ �����������");

		if (con.isUp()) {
			if(mapUpdate==1)
				System.out.println("L-73= CON.isup");

			DTNHost otherHost = con.getOtherNode(getHost());
			updateDestMetsFor(otherHost);
		}
	}
	
	
	/**
	 * ������� ��� ������� +=1 ��� ��� ����� ��� �����������
	 * (����� ��� getMeetFor) 
	 * @param host The host we just met
	 */
	private void updateDestMetsFor(DTNHost host) {
		if(mapinside==1){
			System.out.println("L-=101= ������� ��� ������� +=1 ��� ��� ����� ��� �����������");
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
	 *���������� �� ������ ����������� ��� ���� ����� 
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
	 * ���������� ��� ��� ME ��� ����������� ����� ��� ������ ���� ���� ������
	 * @return a map of this router's meetings
	 */
	/*private Map<DTNHost, Integer> getMeetings() {
		if(allNodesMetings==1)
			System.out.print("L=131= allNodesMetings");
		return this.meets;
	}*/
	
		
	/**
	 * ������� �������� ���������� ��������� ���� ���� �����
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
		//�� �� ������ �� ��������� � ��������� ���
		if (!canStartTransfer() ||isTransferring()) {
			return; // nothing to transfer or is currently transferring 
		}
		
		// try messages that could be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return;
		}
		//������ ��� �� ���� �������� �� ����� ��� ������������� �������
		tryOtherMessages();		
	}
	
	
	
	/**
	 * ��� ������� � ��������� ��� ����������
	 * Tries to send all other messages to all connected hosts ordered by
	 * their utility 
	 * @return The return value of {@link #tryMessagesForConnected(List)}
	 */
	private String  tryOtherMessages() {
		//������� �� �� �������� ��� ���� � ������ �� COLLECTION
		Collection<Message> msgCollection = getMessageCollection();
		
		//�� ��� �������� �������� ��� �������� �� ���� �� ������� ���������
		if (messages.size() == 0) {
			if (emptySendingList==1)
				System.out.println("messages.size() == 0");
			return "";
		}
		
		// ���� �� ����� �� ��� ��������� ��� �������� ������ ��� ����� ��� ������
		// KAI ��� ���� ��� ���� �� ��������
		for (Connection con : getConnections()) {
		
			//���� �� ID ��� ����� ������
			DelegationFw2 othRouter = (DelegationFw2)con.getOtherNode(getHost()).getRouter();
			
			//�� � ����� ������ ����� �� ��������� ��������� ������� ���
			if (othRouter.isTransferring()) {
				continue;	}
			
			//��� ���� ��� ��� �� �������� ��� �������� ������
			for (Message m : msgCollection) {
				//TA UTILITY ��� ��� ������ ��� ��� ��������� ��� ��������� ���������
				int myUtility= this.getMeetFor(m.getTo());
				int otherUtility=othRouter.getMeetFor(m.getTo());
				
				//�� � ����� ������ ���� �������� UTILITY ��� �����
				//������ ���������� ���������� ���������
				if ( myUtility < otherUtility) {
					
					//������ �� �������� ��� ��� ��� ���� ��� ���������
					if(ok==1){
						System.out.println("********** BIGGER ***********");
						System.out.println("THIS MESSAGGES BEFORE= "+this.messages);
						System.out.println("OTHER MESSAGGES BEFORE= "+othRouter.messages);
						System.out.println("MSG:"+m+"- FROM: "+m.getFrom()+"- To: "+m.getTo());
						System.out.println("VIA: from "+this.getHost()+"(" +myUtility+ ") -->TO--> " 
											+othRouter.getHost()+"(" +otherUtility+ ")" );
					}	
					
					//*****������ �� UTILITY ��� �������� ������ ��� ��� ��������� ��� ���������
					//*****�� ���� ��� ����� ������ (��������)
					this.meets.put(m.getTo(), otherUtility);
					
					//������ �� ��� UTILITY & TON NEO ������
					if(ok==1){
						System.out.println("New value " +otherUtility);
						System.out.println(this.getHost()+" Current Router MeetTable= " 
											+this.meets+"\n");
					}
					
					
					// �� � ����� ��� ���� �� ������
					if (!othRouter.hasMessage(m.getId())) {
						//�������� ��� ���������
						if (startTransfer(m, con) == RCV_OK) {
							//��� �� �� ��������� ����
							//���� ��������� ��� ��������� ��� BUFFER ��� ����� ������
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
