package fxj.calendar.lunar;

import java.util.Calendar;
import java.util.HashMap;

import fxj.calendar.MonthActivity;
import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.solar.SolarView;
import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LunarAdapter extends BaseAdapter implements OnTouchListener{

	private static final String TAG = "LunarAdapter";
	protected static final int MONTH_COUNT = 12*(LunarFragment.MAX_CALENDAR_YEAR 
												- LunarFragment.MIN_CALENDAR_YEAR);
	
	private MyApp myApp;
	
	public MyApp getMyApp() {
		return myApp;
	}

	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}

	protected Context mContext;
	protected GestureDetector mGestureDetector;
	
	protected String mHomeTimeZone = Time.getCurrentTimezone(); // 时区，String表示
	protected Time mSelectedMonth;		
	protected Time mRealSelectedMonth = null;
	
	
	private Time mRealClickedDay = null;	
	public Time getmRealClickedDay() {
		return mRealClickedDay;
	}

	public void setmRealClickedDay(Time mRealClickedDay) {
		this.mRealClickedDay = mRealClickedDay;
	}

	
	
	protected Calendar mCalendar;
	
	LunarView mSingleTapUpView;
	LunarView mClickedView;
	TextView floatView;
	
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
	
	public LunarAdapter (Context context) {
		this.mContext = context;
		// 获得监听事件的配置信息，时间，距离
		ViewConfiguration vc = ViewConfiguration.get(context);
		mMovedPixelToCancel = vc.getScaledTouchSlop();
		mOnDownDelay = ViewConfiguration.getTapTimeout();
		mTotalClickDelay = mOnDownDelay + mOnTapDelay;
		
		floatView = (TextView) ((Activity) context).findViewById(R.id.lunarmonth_float_textview);
		
		
		init();
	}
	
	protected void init () {
		// 初始化手势监听，只识别ACTION_UP
		mGestureDetector = new GestureDetector(mContext, new CalendarGestureDetector());
		
		mSelectedMonth = new Time();
		mSelectedMonth.setToNow();
		
		//myApp.setmCurrentClickedTime(mSelectedMonth);
		
		mRealClickedDay = new Time();
		mRealClickedDay.set(mSelectedMonth);
	}
	
	protected class CalendarGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
	}
	
	protected void refresh() {
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return MONTH_COUNT;
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
		
		LunarView view; // 需要返回的view
		HashMap<String, Integer> drawingParam = null; // draw view的参数
		
		if (convertView != null) {
			view = (LunarView) convertView;
			drawingParam = (HashMap<String, Integer>) view.getTag();
		}else {
			view = new LunarView(mContext);
			LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(params);
			view.setClickable(true);
			view.setOnTouchListener(this);
			view.setMyApp(myApp);
		}
		if (drawingParam == null) {
			drawingParam = new HashMap<String, Integer>();
		}
		drawingParam.clear();
		
		int clickedDay = -1;
		int clickedMonth = -1;
		int clickedYear = -1;
		
		if (mRealClickedDay != null) {
			clickedDay = mRealClickedDay.monthDay;
			clickedMonth = mRealClickedDay.month;
			clickedYear = mRealClickedDay.year;
		}
		
		
		//Log.d(TAG, "getView-->selectedMonth" + selectedMonth);
		drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_DAY, clickedDay);
		drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_MONTH, clickedMonth);
		drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_YEAR, clickedYear);
		
		drawingParam.put(LunarView.VIEW_PARAMS_YEAR, LunarFragment.MIN_CALENDAR_YEAR + position/12);
		drawingParam.put(LunarView.VIEW_PARAMS_MONTH, position%12);
		//drawingParam.put(MonthView.VIEW_PARAMS_DAY, position%12);
		
		/*
		 * 设置view的参数,目前只需要postion即可推算 年 月  2015 8 6  绘制点击时可能需要别的参数
		 * 
		 */
		view.setMonthParams(drawingParam);
		//view.setmClickedDayIndex(-1);
		// 重复利用的view需要重新分配高度
		if (convertView != null) {
			LayoutParams layoutParams = new LayoutParams(
					LunarView.getViewWidth(), LunarView.monthTextHeight
							+ view.getWeeks() * LunarView.dayHeight);
			view.setLayoutParams(layoutParams);
		}		
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
			mSingleTapUpView = (LunarView) v;
			
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
			//mListView.postDelayed(mDoSingleTapUp, delay>=mTotalClickDelay?0:mTotalClickDelay-delay);
			return true;
			
		} else {
			switch (action) {
			case MotionEvent.ACTION_DOWN:// Animate a click - on down: show the selected day in the "clicked" color.
				mClickedView = (LunarView) v;
				
				mClickedxLocation = event.getX();
				mClickedyLocation = event.getY();
				mListView.postDelayed(mDoClick, mOnDownDelay);
				//floatView.setVisibility(View.GONE); 可以执行
				break;
			case MotionEvent.ACTION_UP:// On Up/scroll/move/cancel: hide the "clicked" color.
				floatView.setVisibility(View.GONE); // 因为方法 最终返回false，后续动作父view不在发送给子view
				break;
			case MotionEvent.ACTION_SCROLL:
			case MotionEvent.ACTION_CANCEL:
				//floatView.setVisibility(View.GONE);
				clearClickedView((LunarView)v);
				break;
			case MotionEvent.ACTION_MOVE:
				
				//floatView.setVisibility(View.VISIBLE);
				//floatView.setText(myApp.getmCurrentClickedTime().format2445());
				// 点击时移动的距离超出了点击所允许的最大距离，清除点击显示画面
				if (Math.abs(event.getX() - mClickedxLocation) > mMovedPixelToCancel) {
                    clearClickedView((LunarView)v);
                }
				break;
			default:
				break;
			}
		}
		
		return true;
		
	}
	
	
	/**
	 * 包含点击的 年 月信息的 time对象
	 * @param month tap up 的月份
	 */
	protected void onMonthTapped(Time month) {
		
	}

	private void clearClickedView(LunarView v) {
		//mListView.removeCallbacks(mDoClick);// 移除runnable
		synchronized (v) {
			//v.clearClickMonth();// 清除点击画面，调用FullYearView
		}
		mClickedView = null; // 释放点击的view对象
	}
/*	
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
	*/
	 // Perform the tap animation in a runnable to allow a delay before showing the tap color.
    // This is done to prevent a click animation when a fling is done.
	
	private final Runnable mDoClick = new Runnable(){

		@Override
		public void run() {
			if (mClickedView != null) {
				// 保证同一时间只能执行一个点击的view的显示动作
				// mClickedView即为mListView中被点击的view单元
				synchronized (mClickedView) {
					
					Time mTime = new Time(mHomeTimeZone);
					mTime = mClickedView.setClickedDay(mClickedxLocation,mClickedyLocation);
					if (mTime != null) {
						myApp.setmCurrentClickedTime(mTime);
						((Activity) mContext).setTitle(mTime.year+"年");
					}
					//showClickedDate.setText(myApp.getmCurrentClickedTime().format2445());
					
				}
				mClickedView = null;
				
				// This is a workaround , sometimes the top item on the listview doesn't refresh on
                // invalidate, so this forces a re-draw.
				
				int p = mListView.getChildCount();
				Log.d(TAG, "mListView child count-->"+p);
				for(int i =0;i<mListView.getChildCount();i++) {
					LunarView lv = (LunarView) mListView.getChildAt(i);
					//lv.setmClickedIndex((mRealClickedDay.year-1949)*12+mRealClickedDay.month);
					lv.invalidate();
				}
				
				mListView.invalidate();
				
			}
			
		}
		
	};
	
	
	/**
	 * mRealSelectedYear get()
	 *
	 * @return
	 */
	public Time getmRealSelectedYear() {
		return mRealSelectedMonth;
	}

	/**
	 *  set()方法中，先判断是否为空，空则需要先new一个对象否则不能set
	 * @param mRealSelectedYear
	 */
	public void setmRealSelectedYear(Time mRealSelectedYear) {
		if(mRealSelectedYear == null) {
			mRealSelectedYear = new Time();			
		}
		this.mRealSelectedMonth.set(mRealSelectedYear);
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
	
	
	
}
