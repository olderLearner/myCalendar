package fxj.calendar.old;

import fxj.calendar.CalendarParent;
import fxj.calendar.R;
import fxj.calendar.R.color;
import android.app.Activity;
import android.graphics.Canvas;
import android.view.View;


public class Border extends CalendarParent
{

	public Border(Activity activity, View view)
	{
		super(activity, view);
		paint.setColor(activity.getResources().getColor(R.color.border_color));
	}

	@Override
	public void draw(Canvas canvas)
	{
		float left = borderMargin;
		float top = borderMargin;
		// view �� calenderView����
		float right = view.getMeasuredWidth() - left;
		float bottom = view.getMeasuredHeight() - top;
		canvas.drawLine(left, top, right, top, paint);// ���߿������ʣ�ʵ������textview������
		canvas.drawLine(right, top, right, bottom, paint);// �ұ߿�
		canvas.drawLine(right, bottom, left, bottom, paint);// �ױ߿�
		canvas.drawLine(left, bottom, left, top, paint);// ��߿�

	}

}
