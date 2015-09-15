/*
 * ��ʾ�������ݵ�view������ͷxml�ļ���������������
 * 
 * 
 */
package fxj.calendar;


import android.app.Activity;
import android.graphics.Canvas;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class CalendarView extends View
{
	private static final String TAG = "Main.CalendarView";
	public Calendar ce;


	@Override
	protected void onDraw(Canvas canvas)
	{
		Log.d(TAG, "call onDraw of CalendarView");
		ce.draw(canvas);

	}

	/**
	 * CalendarView���췽����new��һ��Calendar(activity, this);
	 * 				new���ĸ�ֵ��CalendarView�еĳ�Ա���� Calendar ce��
	 * Calendar���췽�������ø���Ĺ��췽��super(activity, view);
	 *                  ͬʱnew ������3��Ԫ�أ�border week grid����ӵ�Calendar��ArrayList��
	 * 
	 * @param activity
	 */
	public CalendarView(Activity activity)
	{
		super(activity);
		this.setBackgroundColor(getResources().getColor(R.color.CalendarView_bg));
		// ����calendar����
		ce = new Calendar(activity, this);
	}


	/**
	 * �����¼�
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motion)
	{
		Log.d(TAG, "onTouchEvent-->call");
		
		ce.grid.setCellX(motion.getX());
		Log.d(TAG, motion.getX() + "");
		ce.grid.setCellY(motion.getY());
		Log.d(TAG, motion.getY() + "");
		// ���������������������
		if (ce.grid.inBoundary())
		{
			// ����ʱ�䷢�������»�������������view�� onDraw������ʵ���ǵ���Calendar���draw������
			// ������ǰ���������ڻ��Ʊ���
			// ���������£���ת�����������������ڴ��������ϻ��Ʊ���ͼ
			this.invalidate();
		}
		return super.onTouchEvent(motion);
	}

	@Override
	/**
	 * �����¼���
	 * �ƶ�����view�е�С���ӣ����ƶ�ѡ�е����ڣ�
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		switch (keyCode)
		{

			case KeyEvent.KEYCODE_DPAD_UP:
			{
				/*
				 *  �����ϼ������������ƶ�һ�� -1 -- 4
				 *  ���ݵ�grid.class�еĲ�������ǰ����������-1
				 */
				ce.grid.setCurrentRow(ce.grid.getCurrentRow() - 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_DOWN:
			{
				/*
				 *  �����¼������������ƶ�һ�� 1 -- 6
				 *  ���ݵ�grid.class�еĲ�������ǰ����������+1
				 */
				ce.grid.setCurrentRow(ce.grid.getCurrentRow() + 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_LEFT:
			{
				/*
				 *  ������������������ƶ�һ��
				 *  ���ݵ�grid.class�еĲ�������ǰ����������-1
				 */
				ce.grid.setCurrentCol(ce.grid.getCurrentCol() - 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			{
				/*
				 *  �����Ҽ������������ƶ�һ��
				 *  ���ݵ�grid.class�еĲ�������ǰ����������+1
				 */
				ce.grid.setCurrentCol(ce.grid.getCurrentCol() + 1);
				break;
			}
		
		}
		
		return true;
	}
}
