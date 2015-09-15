package fxj.calendar;

import java.io.Serializable;
import java.util.ArrayList;

import fxj.calendar.interfaces.CalendarElement;
import android.app.Activity;
import android.graphics.Canvas; 
import android.view.View;

public class Calendar extends CalendarParent 
{
	private ArrayList<CalendarElement> elements = new ArrayList<CalendarElement>();
    public Grid grid;
	
    /**
     * ��������Ԫ�� border week grid ��ӵ�list��
     * @param activity
     * @param view
     */
    public Calendar(Activity activity, View view)
	{	
		super(activity,view);
		//elements.add(new Border(activity, view));
		//elements.add(new Week(activity, view));
		grid = new Grid(activity, view);
		elements.add(grid);
	}

	@Override
	// ����list�е�ÿһ��Ԫ��
	public void draw(Canvas canvas)
	{
		for (CalendarElement ce : elements)
			ce.draw(canvas);
	}

}
