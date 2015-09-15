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
	protected String mHomeTimeZone; // ʱ����String��ʾ
	protected Time mRealSelectedYear = null;
	
	YearView mSingleTapUpView;
	YearView mClickedView;
	
	
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
	
	public YearAdapter (Context context,Time time) {
		this.mContext = context;
		// ��ü����¼���������Ϣ��ʱ�䣬����
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
		// ��ʼ�����Ƽ�����ֻʶ��ACTION_UP
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
		
		YearView view; // ��Ҫ���ص�view
		HashMap<String, Integer> drawingParam = null; // draw view�Ĳ���
		
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
	 * view ȷ����event���ĸ�view�Ϸ�������adapter�̳�onTouchListener��ԭ��
	 * <P>MotiomEevent : ACTION_DOWN,ACTION_UP,ACTION_CANCEL,ACTION_MOVE,ACTION_SCROLL
	 */
	public boolean onTouch(View v, MotionEvent event) {
		
		int action = event.getAction();
		// mGestureDetector ֻ��д��onSingleTapUp,����mGestureDetector.onTouchEeventֻ�е�����upʱ�ŷ���true
		if (mGestureDetector.onTouchEvent(event)) { 
			mSingleTapUpView = (YearView) v;
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
				// ���ʱ�ƶ��ľ��볬���˵��������������룬��������ʾ����
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
	 * ��������� �� ����Ϣ�� time����
	 * @param month tap up ���·�
	 */
	protected void onMonthTapped(Time month) {
		mListener.onMonthTapUp(month);
	}

	private void clearClickedView(YearView v) {
		mListView.removeCallbacks(mDoClick);// �Ƴ�runnable
		synchronized (v) {
			v.clearClickMonth();// ���������棬����FullYearView
		}
		mClickedView = null; // �ͷŵ����view����
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
				// ��֤ͬһʱ��ֻ��ִ��һ�������view����ʾ����
				// mClickedView��ΪmListView�б������view��Ԫ
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
	 * set()�����У����ж��Ƿ�Ϊ�գ�������Ҫ��newһ������
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
