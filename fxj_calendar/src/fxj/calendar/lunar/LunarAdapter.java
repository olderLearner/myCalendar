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
	
	protected String mHomeTimeZone = Time.getCurrentTimezone(); // ʱ����String��ʾ
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
	 * touch listener�Ķ���ListView
	 * ��fragment���ݽ���,��Ϊfragment�е�ListView
	 */
	ListView mListView;
	public void setmListView(ListView v) {
		if(v==null) Log.d(TAG, "v is null");
		this.mListView = v;
	}
	
	private static int mOnDownDelay;// �����¼���ָ���־�ֹ�����ʱ��
	private static final int mOnTapDelay = 100; // ��ת����ͼ֮ǰ����ʾ������������ʱ��
	private static int mTotalClickDelay; 
	private float mMovedPixelToCancel; // �����¼�����С�ƶ����루���أ���Ҳ�ǵ����¼�������ƶ�����
	
	public LunarAdapter (Context context) {
		this.mContext = context;
		// ��ü����¼���������Ϣ��ʱ�䣬����
		ViewConfiguration vc = ViewConfiguration.get(context);
		mMovedPixelToCancel = vc.getScaledTouchSlop();
		mOnDownDelay = ViewConfiguration.getTapTimeout();
		mTotalClickDelay = mOnDownDelay + mOnTapDelay;
		
		floatView = (TextView) ((Activity) context).findViewById(R.id.lunarmonth_float_textview);
		
		
		init();
	}
	
	protected void init () {
		// ��ʼ�����Ƽ�����ֻʶ��ACTION_UP
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
		
		LunarView view; // ��Ҫ���ص�view
		HashMap<String, Integer> drawingParam = null; // draw view�Ĳ���
		
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
		 * ����view�Ĳ���,Ŀǰֻ��Ҫpostion�������� �� ��  2015 8 6  ���Ƶ��ʱ������Ҫ��Ĳ���
		 * 
		 */
		view.setMonthParams(drawingParam);
		//view.setmClickedDayIndex(-1);
		// �ظ����õ�view��Ҫ���·���߶�
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
	 * view ȷ����event���ĸ�view�Ϸ�������adapter�̳�onTouchListener��ԭ��
	 * <P>MotiomEevent : ACTION_DOWN,ACTION_UP,ACTION_CANCEL,ACTION_MOVE,ACTION_SCROLL
	 */
	public boolean onTouch(View v, MotionEvent event) {
		
		int action = event.getAction();
		
		// mGestureDetector ֻ��д��onSingleTapUp,����mGestureDetector.onTouchEeventֻ�е�����upʱ�ŷ���true
		if (mGestureDetector.onTouchEvent(event)) { 
			mSingleTapUpView = (LunarView) v;
			
			// ���down��up��ʱ���
			long delay = System.currentTimeMillis() - mClickTime;
			/*
			 * Make sure the animation is visible for at least mOnTapDelay - mOnDownDelay (ms)
			 * ȷ�����������ʾ100ms��
			 * runnable ִ��clear clicked month �����ָ�����Ϊδ���״̬
			 * x = action_up - action_down down upʱ���
			 * y ���������ʾʱ��
			 * 1��x>y������ִ��runnable,
			 * 2��x<y, ��ʱ y-x ms ִ�� 
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
				//floatView.setVisibility(View.GONE); ����ִ��
				break;
			case MotionEvent.ACTION_UP:// On Up/scroll/move/cancel: hide the "clicked" color.
				floatView.setVisibility(View.GONE); // ��Ϊ���� ���շ���false������������view���ڷ��͸���view
				break;
			case MotionEvent.ACTION_SCROLL:
			case MotionEvent.ACTION_CANCEL:
				//floatView.setVisibility(View.GONE);
				clearClickedView((LunarView)v);
				break;
			case MotionEvent.ACTION_MOVE:
				
				//floatView.setVisibility(View.VISIBLE);
				//floatView.setText(myApp.getmCurrentClickedTime().format2445());
				// ���ʱ�ƶ��ľ��볬���˵��������������룬��������ʾ����
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
	 * ��������� �� ����Ϣ�� time����
	 * @param month tap up ���·�
	 */
	protected void onMonthTapped(Time month) {
		
	}

	private void clearClickedView(LunarView v) {
		//mListView.removeCallbacks(mDoClick);// �Ƴ�runnable
		synchronized (v) {
			//v.clearClickMonth();// ���������棬����FullYearView
		}
		mClickedView = null; // �ͷŵ����view����
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
				// ��֤ͬһʱ��ֻ��ִ��һ�������view����ʾ����
				// mClickedView��ΪmListView�б������view��Ԫ
				synchronized (mClickedView) {
					
					Time mTime = new Time(mHomeTimeZone);
					mTime = mClickedView.setClickedDay(mClickedxLocation,mClickedyLocation);
					if (mTime != null) {
						myApp.setmCurrentClickedTime(mTime);
						((Activity) mContext).setTitle(mTime.year+"��");
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
	 *  set()�����У����ж��Ƿ�Ϊ�գ�������Ҫ��newһ�����������set
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
