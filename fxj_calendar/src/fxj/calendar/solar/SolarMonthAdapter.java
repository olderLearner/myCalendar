package fxj.calendar.solar;

import java.util.Calendar;
import java.util.HashMap;

import fxj.calendar.MyApp;
import fxj.calendar.datepicker.adapter.NumericWheelAdapter;
import fxj.calendar.util.MyFixed;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SolarMonthAdapter extends NumericWheelAdapter implements OnTouchListener{

	private static final String TAG = "SolarMonthAdapter";
	
	protected static final int MONTH_COUNT = 12*(MyFixed.MAX_YEAR - MyFixed.MIN_YEAR);
												
	
	protected Context mContext;
	
	private MyApp myApp;
	
	public MyApp getMyApp() {
		return myApp;
	} 
	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}
	
	/** 通知点击日期变化接口 */
	private OnClickedDayChangedListener onClickedDayChange ;
	
	public OnClickedDayChangedListener getOnClickedDayChange() {
		return onClickedDayChange;
	}
	public void setOnClickedDayChange(OnClickedDayChangedListener onClickedDayChange) {
		this.onClickedDayChange = onClickedDayChange;
	}
	
	
	protected GestureDetector mGestureDetector;
	
	protected Time mSelectedMonth;		
	// 点击的日期
	private Time mRealClickedDay = new Time(MyFixed.TIMEZONE);
	
	public Time getmRealClickedDay() {
		return mRealClickedDay;
	}

	public void setmRealClickedDay(Time mRealClickedDay) {
		this.mRealClickedDay = mRealClickedDay;
	}

	// 上一次点击的日期,初始值入点击日期相同
	private Time lastClickedDay = new Time(MyFixed.TIMEZONE);
	
	/**
	 * 重置点击日期，并通知listener
	 * @param time 点击的日期
	 * @author Fengxj
	 */
	public void refreshClick(Time time) {
		this.mRealClickedDay = time;
		if (checkDayChange()) {
			this.notifyClickedDayChanged();
			lastClickedDay.set(mRealClickedDay);
		}
	}
	
	
	protected Calendar mCalendar;
	
	SolarView mClickedView;
	SolarMonth mWheelMonth;
	
	
	// TextView showClickedDate; // 显示点击的日期，注释掉 2015 9 30
	// 浮动显示   2015 8 30 注释
	//TextView floatView;
	
	float mClickedxLocation;
	float mClickedyLocation;
	long mClickTime;
	
	/**
	 * touch listener的对象ListView
	 * 由fragment传递进来,即为fragment中的ListView
	 * 
	 * 2015 8 12-->
	 * itemsLayout or WheelMonth.class?
	 * SolarMonth dispatch event 分发 touch
	 */
	
	LinearLayout mLinearLayout;
	public void setmLinearLayout(LinearLayout mLinearLayout) {
		this.mLinearLayout = mLinearLayout;
		Log.d(TAG, "setmLinearLayout"+ this.mLinearLayout.getChildCount());
	}

	private static int mOnDownDelay;// 单击事件手指保持静止的最短时间
	private static final int mOnTapDelay = 100; // 在转换视图之前，显示点击动画的最短时间
	private static int mTotalClickDelay; 
	private float mMovedPixelToCancel; // 滑动事件的最小移动距离（像素），也是单击事件的最大移动距离
	
	public SolarMonthAdapter (Context context) {
		super(context);
		this.mContext = context;
		// 获得监听事件的配置信息，时间，距离
		ViewConfiguration vc = ViewConfiguration.get(context);
		mMovedPixelToCancel = vc.getScaledTouchSlop();
		mOnDownDelay = ViewConfiguration.getTapTimeout();
		mTotalClickDelay = mOnDownDelay + mOnTapDelay;
		
		/*
		Activity act = (Activity) context;
		showClickedDate = (TextView) act.findViewById(R.id.listmonth_tv);
		floatView = (TextView) act.findViewById(R.id.listmonth_tv_float);
		*/
		
		init();
	}
	public SolarMonthAdapter (Context context , SolarMonth wheelMonth) {
		super(context);
		this.mWheelMonth = wheelMonth;
		this.mContext = context;
		//myApp = ((Activity) context).getApplicationContext();
		//myApp = (MyApp) context.getApplicationContext();
		ViewConfiguration vc = ViewConfiguration.get(context);
		mMovedPixelToCancel = vc.getScaledTouchSlop();
		mOnDownDelay = ViewConfiguration.getTapTimeout();
		mTotalClickDelay = mOnDownDelay + mOnTapDelay;
		init();
	}
	
	
	protected void init () {
		// 初始化手势监听，只识别ACTION_UP
		mGestureDetector = new GestureDetector(mContext, new CalendarGestureDetector());
		
		mSelectedMonth = new Time();
		mSelectedMonth.setToNow();
		//mRealClickedDay = new Time();
		mRealClickedDay.set(mSelectedMonth);
		lastClickedDay.set(mRealClickedDay);
		
		//myApp.setmCurrentClickedTime(mSelectedMonth);
	}
	
	protected class CalendarGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return true;
		}
		
	}
	
	protected void refresh() {
		this.notifyDataChangedEvent();
	}
	
	@Override
	protected void notifyDataChangedEvent() {
		super.notifyDataChangedEvent();
	}

	@Override
	public int getItemsCount() {
		return MONTH_COUNT;
	}

	@Override
	/**
	 * convertView :the old view which can reuse 
	 */
	public View getItem(int position, View convertView, ViewGroup parent) {
		
		SolarView view; // 需要返回的view
		HashMap<String, Integer> drawingParam = null; // draw view的参数
		
		if (convertView != null) {
			view = (SolarView) convertView;
			drawingParam = (HashMap<String, Integer>) view.getTag();
		}else {
			view = new SolarView(mContext);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
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
		
		
		/*
		int clickedDay = -1;
		int clickedMonth = -1;
		int clickedYear = -1;
		
		if (mRealClickedDay != null) {
			clickedDay = mRealClickedDay.monthDay;
			clickedMonth = mRealClickedDay.month;
			clickedYear = mRealClickedDay.year;
		}*/
		
		//Log.d(TAG, "getView-->selectedMonth" + selectedMonth);
		//drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_DAY, clickedDay);
		//drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_MONTH, clickedMonth);
		//drawingParam.put(SolarView.VIEW_PARAMS_CLICKED_YEAR, clickedYear);
		
		
		drawingParam.put(SolarView.VIEW_PARAMS_YEAR, MyFixed.MIN_YEAR + position/12);
		drawingParam.put(SolarView.VIEW_PARAMS_MONTH, position%12);
		//drawingParam.put(MonthView.VIEW_PARAMS_DAY, position%12);
		
		/*
		 * 设置view的参数,目前只需要postion即可推算 年 月  2015 8 6  绘制点击时可能需要别的参数
		 */
		view.setMonthParams(drawingParam);
		//view.setmClickedDayIndex(-1);
		// 重复利用的view需要重新分配高度
		if (convertView != null) {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					SolarView.getViewWidth(), SolarView.monthTextHeight
							+ view.getWeeks() * SolarView.dayHeight);
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
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mClickedView = (SolarView) v;
			mClickedxLocation = event.getX();
			mClickedyLocation = event.getY() + 36;
			
			handleClick(mClickedView);
			
			// Log.d(TAG, "adapter::onTouch:::  " +event.getY()+"   "+mRealClickedDay.monthDay);
			break;
		case MotionEvent.ACTION_UP:// On Up/scroll/move/cancel: hide the"clicked" color.
		case MotionEvent.ACTION_SCROLL:
		case MotionEvent.ACTION_CANCEL:

			break;
		case MotionEvent.ACTION_MOVE:
			mClickedxLocation = event.getX();
			mClickedyLocation = event.getY() + 36;
			handleClick(mClickedView);
			
			//Log.d(TAG, "adapter receive action move");
			break;
		default:
			break;
		}
		// 2015 8 30  这句话，到底有米有作用，待验证！
		mWheelMonth.invalidate();
		return true;
		
	}
	
	/**
	 * <p> 由点击的坐标，计算点击的日期，更新adapter成员变量 mRealClickedDay，
	 * <p> 更新全局点击日期（application 中） 
	 * @param solarView 点击事件作用的对象
	 */
	private void handleClick(SolarView solarView) {
		
		Time tmp = new Time(MyFixed.TIMEZONE);
		tmp.normalize(true);
		tmp = solarView.setClickedDay(mClickedxLocation,mClickedyLocation);
		if (tmp != null) {
			
			mRealClickedDay.set(tmp);
			myApp.setmCurrentClickedTime(mRealClickedDay);
			if (checkDayChange()) {
				
				notifyClickedDayChanged();
				//onClickedDayChange.onClickedDayChanged();
				lastClickedDay.set(mRealClickedDay);
			}
			
		}
	}

	private boolean checkDayChange() {
		boolean isChange = false;
		if (mRealClickedDay.year != lastClickedDay.year || mRealClickedDay.month != lastClickedDay.month || mRealClickedDay.monthDay != lastClickedDay.monthDay) {
			isChange =true;
		}
		
		return isChange;
	}
	
	public void notifyClickedDayChanged() {
		onClickedDayChange.onClickedDayChanged();
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
