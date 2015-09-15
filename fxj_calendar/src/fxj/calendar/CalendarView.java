/*
 * 显示日历内容的view和日历头xml文件构成日期主界面
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
	 * CalendarView构造方法中new了一个Calendar(activity, this);
	 * 				new出的赋值给CalendarView中的成员变量 Calendar ce；
	 * Calendar构造方法：调用父类的构造方法super(activity, view);
	 *                  同时new 日历的3个元素：border week grid，添加到Calendar的ArrayList中
	 * 
	 * @param activity
	 */
	public CalendarView(Activity activity)
	{
		super(activity);
		this.setBackgroundColor(getResources().getColor(R.color.CalendarView_bg));
		// 创建calendar对象
		ce = new Calendar(activity, this);
	}


	/**
	 * 触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motion)
	{
		Log.d(TAG, "onTouchEvent-->call");
		
		ce.grid.setCellX(motion.getX());
		Log.d(TAG, motion.getX() + "");
		ce.grid.setCellY(motion.getY());
		Log.d(TAG, motion.getY() + "");
		// 如果触摸在日历的内容中
		if (ce.grid.inBoundary())
		{
			// 触摸时间发生，重新绘制日历，调用view的 onDraw方法（实际是调用Calendar类的draw方法）
			// 触摸当前月则触摸日期绘制背景
			// 触摸上下月，跳转到上下月日历，并在触摸日期上绘制背景图
			this.invalidate();
		}
		return super.onTouchEvent(motion);
	}

	@Override
	/**
	 * 键盘事件：
	 * 移动日历view中的小格子，（移动选中的日期）
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		switch (keyCode)
		{

			case KeyEvent.KEYCODE_DPAD_UP:
			{
				/*
				 *  按向上键，日期向上移动一行 -1 -- 4
				 *  传递到grid.class中的参数：当前日期所在行-1
				 */
				ce.grid.setCurrentRow(ce.grid.getCurrentRow() - 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_DOWN:
			{
				/*
				 *  按向下键，日期向下移动一格 1 -- 6
				 *  传递到grid.class中的参数：当前日期所在行+1
				 */
				ce.grid.setCurrentRow(ce.grid.getCurrentRow() + 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_LEFT:
			{
				/*
				 *  按向左键，日期向左移动一格
				 *  传递到grid.class中的参数：当前日期所在列-1
				 */
				ce.grid.setCurrentCol(ce.grid.getCurrentCol() - 1);
				break;
			}
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			{
				/*
				 *  按向右键，日期向右移动一格
				 *  传递到grid.class中的参数：当前日期所在行+1
				 */
				ce.grid.setCurrentCol(ce.grid.getCurrentCol() + 1);
				break;
			}
		
		}
		
		return true;
	}
}
