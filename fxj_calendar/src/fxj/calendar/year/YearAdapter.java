package fxj.calendar.year;

import java.util.HashMap;

import fxj.calendar.util.MyFixed;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class YearAdapter extends BaseAdapter implements OnTouchListener{

	private static final String TAG = "FullYearAdapter";
	protected static final int YEAR_COUNT = YearFragment.MAX_CALENDAR_YEAR - YearFragment.MIN_CALENDAR_YEAR;
	
	protected Context mContext;
	protected GestureDetector mGestureDetector;
	protected Time mSelectedMonth;	
	protected String mHomeTimeZone; // 时区，String表示
	protected Time mRealSelectedYear = null;
	
	YearView mSingleTapUpView;
	YearView mClickedView;
	
	
	float mClickedxLocation;
	float mClickedyLocation;
	long mClickTime;
	
	/**
	 * touch listener的对象ListView
	 * 由fragment传递进来,即为fragment中的ListView
	 */
	ListView mListView;
	public void setmListView(ListView v) {
		if(v==null) Log.d(TAG, "v is null");
		this.mListView = v;
	}
	
	private static int mOnDownDelay;// 单击事件手指保持静止的最短时间
	private static final int mOnTapDelay = 100; // 在转换视图之前，显示点击动画的最短时间
	private static int mTotalClickDelay; 
	private float mMovedPixelToCancel; // 滑动事件的最小移动距离（像素），也是单击事件的最大移动距离
	
	public YearAdapter (Context context,Time time) {
		this.mContext = context;
		// 获得监听事件的配置信息，时间，距离
		ViewConfiguration vc = ViewConfiguration.get(context);
		mMovedPixelToCancel = vc.getScaledTouchSlop();
		mOnDownDelay = ViewConfiguration.getTapTimeout();
		mTotalClickDelay = mOnDownDelay + mOnTapDelay;
		this.mRealSelectedYear = new Time(MyFixed.TIMEZONE);
		this.mRealSelectedYear.set(time);
		this.mRealSelectedYear.normalize(true);
		init();
	}
	
	protected void init () {
		// 初始化手势监听，只识别ACTION_UP
		mGestureDetector = new GestureDetector(mContext, new CalendarGestureDetector());
		
		mSelectedMonth = new Time();
		mSelectedMonth.setToNow();
		//mRealSelectedYear = new Time();
		//mRealSelectedYear.set(mSelectedMonth);
	}
	
	protected class CalendarGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return true;
		}	
	}
	
	protected void refresh() {
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return YEAR_COUNT;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	/**
	 * convertView :the old view which can reuse 
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		
		YearView view; // 需要返回的view
		HashMap<String, Integer> drawingParam = null; // draw view的参数
		
		if (convertView != null) {
			view = (YearView) convertView;
			drawingParam = (HashMap<String, Integer>) view.getTag();
		}else {
			view = new YearView(mContext);
			//Log.d(TAG, "getView-->new fullyearview" + view);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			view.setLayoutParams(params);
			view.setClickable(true);
			view.setOnTouchListener(this);
		}
		if (drawingParam == null) {
			drawingParam = new HashMap<String, Integer>();
		}
		drawingParam.clear();
		
		int selectedMonth = -1;
		selectedMonth = mSelectedMonth.month;
		
		//Log.d(TAG, "getView-->selectedMonth" + selectedMonth);
		
		drawingParam.put(YearView.VIEW_PARAMS_SELECTED_MONTH, selectedMonth);
		drawingParam.put(YearView.VIEW_PARAMS_YEAR, YearFragment.MIN_CALENDAR_YEAR + position);
		view.setYearParams(drawingParam);
		view.invalidate();
		return view;
	}

	@Override
	
	/**
	 * view 确定了event在哪个view上发生，由adapter继承onTouchListener的原因
	 * <P>MotiomEevent : ACTION_DOWN,ACTION_UP,ACTION_CANCEL,ACTION_MOVE,ACTION_SCROLL
	 */
	public boolean onTouch(View v, MotionEvent event) {
		
		int action = event.getAction();
		// mGestureDetector 只重写了onSingleTapUp,所以mGestureDetector.onTouchEevent只有当单击up时才返回true
		if (mGestureDetector.onTouchEvent(event)) { 
			mSingleTapUpView = (YearView) v;
			// 点击down和up的时间差
			long delay = System.currentTimeMillis() - mClickTime;
			/*
			 * Make sure the animation is visible for at least mOnTapDelay - mOnDownDelay (ms)
			 * 确保点击动作显示100ms；
			 * runnable 执行clear clicked month 动作恢复日历为未点击状态
			 * x = action_up - action_down down up时间差
			 * y 点击动作显示时间
			 * 1）x>y，立刻执行runnable,
			 * 2）x<y, 延时 y-x ms 执行 
			 */
			mListView.postDelayed(mDoSingleTapUp, delay>=mTotalClickDelay?0:mTotalClickDelay-delay);
			return true;
		} else {
			switch (action) {
			case MotionEvent.ACTION_DOWN:// Animate a click - on down: show the selected day in the "clicked" color.
				mClickedView = (YearView) v;
				mClickedxLocation = event.getX();
				mClickedyLocation = event.getY();
				mClickTime = System.currentTimeMillis();
				mListView.postDelayed(mDoClick, mOnDownDelay);
				break;
			case MotionEvent.ACTION_UP:// On Up/scroll/move/cancel: hide the "clicked" color.
			case MotionEvent.ACTION_SCROLL:
			case MotionEvent.ACTION_CANCEL:
				clearClickedView((YearView)v);
				break;
			case MotionEvent.ACTION_MOVE:
				// 点击时移动的距离超出了点击所允许的最大距离，清除点击显示画面
				if (Math.abs(event.getX() - mClickedxLocation) > mMovedPixelToCancel) {
                    clearClickedView((YearView)v);
                }
				break;
			default:
				break;
			}
		}
		
		return false;
	}

	/**
	 * 包含点击的 年 月信息的 time对象
	 * @param month tap up 的月份
	 */
	protected void onMonthTapped(Time month) {
		mListener.onMonthTapUp(month);
	}

	private void clearClickedView(YearView v) {
		mListView.removeCallbacks(mDoClick);// 移除runnable
		synchronized (v) {
			v.clearClickMonth();// 清除点击画面，调用FullYearView
		}
		mClickedView = null; // 释放点击的view对象
	}
	
	private final Runnable mDoSingleTapUp = new Runnable(){

		@Override
		public void run() {
			if (mSingleTapUpView != null) {
				Time month = mSingleTapUpView.getClickMonthFromLocation(mClickedxLocation, mClickedyLocation);
				if (month != null) {
					onMonthTapped(month);
				}
				clearClickedView(mSingleTapUpView);
				mSingleTapUpView = null;
			}
		}
		
	};
	
	 // Perform the tap animation in a runnable to allow a delay before showing the tap color.
    // This is done to prevent a click animation when a fling is done.
	private final Runnable mDoClick = new Runnable(){

		@Override
		public void run() {
			if (mClickedView != null) {
				// 保证同一时间只能执行一个点击的view的显示动作
				// mClickedView即为mListView中被点击的view单元
				synchronized (mClickedView) {
					mClickedView.setClickedMonth(mClickedxLocation, mClickedyLocation);
				}
				mClickedView = null;
				
				// This is a workaround , sometimes the top item on the listview doesn't refresh on
                // invalidate, so this forces a re-draw.
				mListView.invalidate();
			}
			
		}
		
	};
	/**
	 * mRealSelectedYear get() and set()
	 * set()方法中，先判断是否为空，空则需要先new一个对象
	 * @return
	 */
	public Time getmRealSelectedYear() {
		return mRealSelectedYear;
	}

	public void setmRealSelectedYear(Time mRealSelectedYear) {
		if(mRealSelectedYear == null) {
			mRealSelectedYear = new Time();
			
		}
		this.mRealSelectedYear.set(mRealSelectedYear);
	}

	/**
	 * mSelectedMonth get() and set()
	 * 
	 * @return
	 */
	public Time getmSelectedMonth() {
		return mSelectedMonth;
	}

	public void setmSelectedMonth(Time mSelectedMonth) {
		this.mSelectedMonth.set(mSelectedMonth);
	}
	
	
	public interface onMonthTapUpListener{
		public void onMonthTapUp(Time time);
	}
	private onMonthTapUpListener mListener;
	public void setmListener(onMonthTapUpListener mListener) {
		this.mListener = mListener;
	}
	
	
}
