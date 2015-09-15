package fxj.calendar.util;


public class EventEmptyList implements EventListInterface{
	
	public static final int TYPE= 2;
	public int id= -1;
	public String title = " ";
	
	@Override
	public int getType() {
		return TYPE;
	}
	
	
	
}
