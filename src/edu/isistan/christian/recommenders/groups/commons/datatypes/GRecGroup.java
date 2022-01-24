package edu.isistan.christian.recommenders.groups.commons.datatypes;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import edu.isistan.christian.recommenders.sur.datatypes.SURUser;

public class GRecGroup extends ArrayList<SURUser> {

	private final static AtomicInteger count = new AtomicInteger(0);  //to avoid concurrent creation of groups with the same ID and also concurrent modifications of the static variable
	/**
	 * 
	 */
	private static final long serialVersionUID = 6999239327241128023L;
	protected String ID;
	
	public GRecGroup(){
		this.ID = "Group_";
		int number = count.getAndIncrement();
		if (number < 10)
			this.ID += "0"+number; //00,01,02,03... 09 => this is useful for sorting by id
		else
			this.ID+= number;
	}
		
	public String getID(){
		return ID;
	}

	@Override
	public String toString() {
		String s = "GRecGroup [ID=" + ID + ", Members: [";
		for (int i=0; i<this.size(); i++){
			s+= "User [id="+this.get(i).getID()+"]";
			if (i!= this.size()-1)
				s+= ", ";
		}
		s +="]";
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GRecGroup other = (GRecGroup) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		return true;
	}

	
}
