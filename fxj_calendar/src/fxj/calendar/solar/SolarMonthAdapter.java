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
	
	/** ֪ͨ������ڱ仯�ӿ� */
	private OnClickedDayChangedListener onClickedDayChange ;
	
	public OnClickedDayChangedListener getOnClickedDayChange() {
		return onClickedDayChange;
	}
	public void setOnClickedDayChange(OnClickedDayChangedListener onClickedDayChange) {
		this.onClickedDayChange = onClickedDayChange;
	}
	
	
	protected GestureDetector mGestureDetector;
	
	protected Time mSelectedMonth;		
	// ���������
	private Time mRealClickedDay = new Time(MyFixed.TIMEZONE);
	
	public Time getmRealClickedDay() {
		return mRealClickedDay;
	}

	public void setmRealClickedDay(Time mRealClickedDay) {
		this.mRealClickedDay = mRealClickedDay;
	}

	// ��һ�ε��������,��ʼֵ����������ͬ
	private Time lastClickedDay = new Time(MyFixed.TIMEZONE);
	
	/**
	 * ���õ�����ڣ���֪ͨlistener
	 * @param time ���������
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
	
	
	// TextView showClickedDate; // ��ʾ��������ڣ�ע�͵� 2015 9 30
	// ������ʾ   2015 8 30 ע��
	//TextView floatView;
	
	float mClickedxLocation;
	float mClickedyLocation;
	long mClickTime;
	
	/**
	 * touch listener�Ķ���ListView
	 * ��fragment���ݽ���,��Ϊfragment�е�ListView
	 * 
	 * 2015 8 12-->
	 * itemsLayout or WheelMonth.class?
	 * SolarMonth dispatch event �ַ� touch
	 */
	
	LinearLayout mLinearLayout;
	public void setmLinearLayout(LinearLayout mLinearLayout) {
		this.mLinearLayout = mLinearLayout;
		Log.d(TAG, "setmLinearLayout"+ this.mLinearLayout.getChildCount());
	}

	private static int mOnDownDelay;// �����¼���ָ���־�ֹ�����ʱ��
	private static final int mOnTapDelay = 100; // ��ת����ͼ֮ǰ����ʾ������������ʱ��
	private static int mTotalClickDelay; 
	private float mMovedPixelToCancel; // �����¼�����С�ƶ����루���أ���Ҳ�ǵ����¼�������ƶ�����
	
	public SolarMonthAdapter (Context context) {
		super(context);
		this.mContext = context;
		// ��ü����¼���������Ϣ��ʱ�䣬����
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
		// ��ʼ�����Ƽ�����ֻʶ��ACTION_UP
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
		
		SolarView view; // ��Ҫ���ص�view
		HashMap<String, Integer> drawingParam = null; // draw view�Ĳ���
		
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
		 * ����view�Ĳ���,Ŀǰֻ��Ҫpostion�������� �� ��  2015 8 6  ���Ƶ��ʱ������Ҫ��Ĳ���
		 */
		view.setMonthParams(drawingParam);
		//view.setmClickedDayIndex(-1);
		// �ظ����õ�view��Ҫ���·���߶�
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
	 * view ȷ����event���ĸ�view�Ϸ�������adapter�̳�onTouchListener��ԭ��
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
		// 2015 8 30  ��仰���������������ã�����֤��
		mWheelMonth.invalidate();
		return true;
		
	}
	
	/**
	 * <p> �ɵ�������꣬�����������ڣ�����adapter��Ա���� mRealClickedDay��
	 * <p> ����ȫ�ֵ�����ڣ�application �У� 
	 * @param solarView ����¼����õĶ���
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
