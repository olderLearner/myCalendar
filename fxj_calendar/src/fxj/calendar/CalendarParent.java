package fxj.calendar;


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import fxj.calendar.interfaces.CalendarElement;

public class CalendarParent implements CalendarElement
{
	protected Activity activity;
	protected View view;
	protected Paint paint = new Paint();
	protected float borderMargin;		
	protected float weekNameMargin;
	protected float weekNameSize;
	protected int sundaySaturdayColor;
	

    public CalendarParent(Activity activity, View view)
    {    	
    	this.activity = activity;
    	this.view = view;
		borderMargin = activity.getResources().getDimension(
				R.dimen.calendar_border_margin);
        weekNameMargin = activity.getResources().getDimension(R.dimen.weekname_margin);
        weekNameSize=activity.getResources().getDimension(R.dimen.weekname_size);
        sundaySaturdayColor = activity.getResources().getColor(R.color.weekname_sun_sat);
        paint.setAntiAlias(true);
    }
	@Override
	public void draw(Canvas canvas)
	{		
		
	}

}
