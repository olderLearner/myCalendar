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
		// view �Ŀ�ȼ� margin*2 ����7�ã�ƽ����7��
		float everyWeekWidth = (view.getMeasuredWidth() -  borderMargin * 2)/ 7;
		float everyWeekHeight = everyWeekWidth;
		
		// �º��壬��������
		paint.setFakeBoldText(true);
		for (int i = 0; i < weekNames.length; i++)
		{
			if(i == 0 || i == weekNames.length - 1)
				paint.setColor(sundaySaturdayColor);
			else
				paint.setColor(weekNameColor);

			left = borderMargin + everyWeekWidth * i
					+ (everyWeekWidth - paint.measureText(weekNames[i])) / 2;
			
			// ����������origin������
			//paint.setTextAlign(Paint.Align.RIGHT); 
			
			// ���ֿ�ʼ���� x y ����ԭ�㣬paint���Ǵ����½ǿ�ʼ����
			canvas.drawText(weekNames[i], left, top + paint.getTextSize()+weekNameMargin, paint);
			
			// yΪtop���������ں���֮��
			//canvas.drawText(weekNames[i], left, top, paint);
		}

	}

}
