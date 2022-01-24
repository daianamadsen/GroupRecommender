package edu.isistan.christian.recommenders.groups.commons.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.isistan.christian.recommenders.sur.datatypes.SURUser;

public class GroupUser extends SURUser {
	private static final String MEMBERS_IDS_KEY= "MEMBERS_IDS";
	
	private final static AtomicInteger groupsCount = new AtomicInteger(0);  //to avoid concurrent creation of groups with the same ID and also concurrent modifications of the static variable
	
	public GroupUser(GRecGroup group) {
		super(group.getID()+"_"+String.valueOf(groupsCount.getAndIncrement()));
		
		List<String> membersIDs = new ArrayList<>();
		for (SURUser u : group)
			membersIDs.add(u.getID());
		
		this.addOptionalAttribute(MEMBERS_IDS_KEY, membersIDs);
	}
	
	public GroupUser(String id, List<SURUser> members) {
		super(id);
		
		List<String> membersIDs = new ArrayList<>();
		for (SURUser u : members)
			membersIDs.add(u.getID());
		
		this.addOptionalAttribute(MEMBERS_IDS_KEY, membersIDs);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getMembers(){
		return (List<String>) this.getOptAttribute(MEMBERS_IDS_KEY);
	}
	
	public boolean hasMember (String userID){
		return this.getMembers().contains(userID);
	}

	@Override
	public String toString() {
		return "GroupUser [id=" + id + ", otherAttributes=" + otherAttributes
				+ "]";
	}

}
