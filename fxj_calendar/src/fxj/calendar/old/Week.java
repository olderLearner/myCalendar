package fxj.calendar.old;

import fxj.calendar.CalendarParent;
import fxj.calendar.R;
import fxj.calendar.R.array;
import fxj.calendar.R.color;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;


public class Week extends CalendarParent
{
	private String[] weekNames;
	private int weekNameColor;
	
	
	public Week(Activity activity, View view)
	{
		super(activity, view);
		weekNameColor = activity.getResources().getColor(R.color.weekname_color);
		weekNames = activity.getResources().getStringArray(R.array.week_name);
		paint.setTextSize(weekNameSize);//18dp
	}

	@Override
	public void draw(Canvas canvas)
	{

		// borderMargin:10dp 
		float left = borderMargin;
		float top = borderMargin;
		// view 的宽度减 margin*2 除以7得：平均的7份
		float everyWeekWidth = (view.getMeasuredWidth() -  borderMargin * 2)/ 7;
		float everyWeekHeight = everyWeekWidth;
		
		// 仿黑体，设置字体
		paint.setFakeBoldText(true);
		for (int i = 0; i < weekNames.length; i++)
		{
			if(i == 0 || i == weekNames.length - 1)
				paint.setColor(sundaySaturdayColor);
			else
				paint.setColor(weekNameColor);

			left = borderMargin + everyWeekWidth * i
					+ (everyWeekWidth - paint.measureText(weekNames[i])) / 2;
			
			// 设置文字在origin的左面
			//paint.setTextAlign(Paint.Align.RIGHT); 
			
			// 文字开始画的 x y 坐标原点，paint中是从左下角开始绘制
			canvas.drawText(weekNames[i], left, top + paint.getTextSize()+weekNameMargin, paint);
			
			// y为top，则文字在横线之上
			//canvas.drawText(weekNames[i], left, top, paint);
		}

	}

}
