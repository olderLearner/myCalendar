package fxj.calendar.year;



import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.R.layout;
import fxj.calendar.util.MyFixed;
import fxj.calendar.year.YearAdapter.onMonthTapUpListener;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

public class YearFragment extends ListFragment {

	private static final String TAG = "WholeYearFragment";
	
	private static final String KEY_CURRENT_TIME = "current_time";
	
	public static final int MIN_CALENDAR_YEAR = 1970;
	public static final int MAX_CALENDAR_YEAR = 2036;
	protected static final int GOTO_SCROLL_DURATION = 500;
	
	protected Context context;
	protected YearAdapter mAdapter;
	protected ListView mListView;
	protected Time mSelectedYear = null;
	public void setmSelectedYear(Time mSelectedYear) {
		this.mSelectedYear.set(mSelectedYear);
	}
	
	private MyApp myApp;
	
	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}

	// goto 方法中的临时变量
	protected Time mTempTime = new Time();
	
	protected float mFriction = 1.0f;// 摩擦系数
	protected float mMinimumFlingVelocity; // 最小滑动速度
	public static float mScale = 0;// dp pixel 比例系数
	protected int BOTTOM_BUFFER = 20;

	private onMonthTapUpListener mListener;
    public static int LIST_TOP_OFFSET = -1;  // so that the top line will be under the separator隔板,分离器
	
	@Override
	public void onAttach(Activity activity) {
		
		Log.d(TAG, "onAttach");
		try {
			mListener = (onMonthTapUpListener) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()+": must implement onScanBTAddressClickedListener");
		}	
		super.onAttach(activity);
		this.context = activity;
		
		myApp = (MyApp) activity.getApplicationContext();
		
		ViewConfiguration vc = ViewConfiguration.get(activity);
		mMinimumFlingVelocity = vc.getScaledMinimumFlingVelocity();
		
		mSelectedYear = new Time(MyFixed.TIMEZONE);
		mSelectedYear.set(myApp.getmCurrentClickedTime());
		mSelectedYear.normalize(true);
		
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = getResources().getDisplayMetrics();
		
		//Log.d(TAG, "heightPixels-->"+metrics.heightPixels+"\n"+"widthPixels-->"+metrics.widthPixels+ "\n" +
				//"dpi-->"+metrics.densityDpi);

		
		if (mScale == 0) {
			mScale = getResources().getDisplayMetrics().density;
			//Log.d(TAG, "mScale-->" + mScale);
			if (mScale != 1) {
				BOTTOM_BUFFER *= mScale;
				LIST_TOP_OFFSET *= mScale;
			}
		}
		setupAdapter();
		setListAdapter(mAdapter);
	}
	
	protected DataSetObserver mObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			Time month = mAdapter.getmRealSelectedYear();
			//Log.d(TAG, "DataSetObserver-->perform:year-->"+ month.year);
			goTo(month.toMillis(true), false, true, false);
			super.onChanged();
		}
		
	};
	
	protected void setupAdapter() {
		if (mAdapter == null) {
			mAdapter = new YearAdapter(context,mSelectedYear);
			mAdapter.registerDataSetObserver(mObserver);
		}
		//mAdapter.setmRealSelectedYear(mSelectedYear);
		mAdapter.notifyDataSetChanged();
		mAdapter.setmListener(mListener);
		//Log.d(TAG, "notifyDataSetChanged-->perform");
	}

	@Override
	/**
	 * 可能与activity的oncreate同时进行，所以这个方法中不能初始化一些依赖activity视图系统的事情。
	 * 因为activity可能还没有初始化完成
	 */
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		if(savedInstanceState != null && savedInstanceState.containsKey(KEY_CURRENT_TIME)) {
			goTo(savedInstanceState.getLong(KEY_CURRENT_TIME), false, true, true);
		}
		super.onCreate(savedInstanceState);
	}

	
	
	@Override
	/**
     * fragment 保存当前状态，
     * 方法会在任何时刻被调用，fragment失去焦点
     * Called to ask the fragment to save its current dynamic state, 
     * so it can later be reconstructed in a new instance of its process is restarted. 
     * If a new instance of the fragment later needs to be created, 
     * the data you place in the Bundle here will be available in the Bundle given to onCreate(Bundle), 
     * onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     * 
     */
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(KEY_CURRENT_TIME, mSelectedYear.toMillis(true));
		super.onSaveInstanceState(outState);
	}

	@Override
	/**
	 * 引入ListFragmetn的布局文件，布局中必要含有ListView且id=android.R.id.list
	 * 
	 * 
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.wholeyear_fragment, container, false);
		
		return v;
		//return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	/**
	 * fragment的activity已经创建完成，同时fragment中的view已经实例化，
	 * 可以提取view中的组件做最后的初始化或者保存状态，
	 * 此时fragment和activity已经完全绑定在一起
	 */
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setUpListView();
		
		// 空指针错误，是调用这个方法的对象为空，
		
		mAdapter.setmListView(mListView);//传递ListView实例到adapter用于处理触摸事件
	}

	/**
	 * 配置fragment中的listview
	 */
	protected void setUpListView() {
		
		mListView = getListView();
		//if(mListView !=null) Log.d(TAG, mListView + "is not null");
		
		/*
		 *  Transparent background on scroll
		 *  No dividers ListView 之间没有分割线
		 *  Items are clickable
		 *  The thumb gets in the way, so disable it
		 */
        mListView.setCacheColorHint(0);
        mListView.setDivider(null);
//        mListView.setDivider(new ColorDrawable(Color.RED));
//        mListView.setDividerHeight(1);// pixel
        
        
        mListView.setItemsCanFocus(true);
        mListView.setFastScrollEnabled(false);
        mListView.setVerticalScrollBarEnabled(false);
        
        mListView.setFadingEdgeLength(0);
        // Make the scrolling behavior nicer
        mListView.setFriction(ViewConfiguration.getScrollFriction() * mFriction);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	/**
	 * fragment going to be active
	 * 
	 */
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		setupAdapter(); // 调用了方法内部的 notify data set changed 
		doResumeUpdates();
	}
	
	/**
     * Updates the user preference fields. Override this to use a different
     * preference space.
     */
	protected void doResumeUpdates() {
		//Log.d(TAG, "doResumeUpdates-->perform");
		goTo(mSelectedYear.toMillis(true), false, false, false);
	}

	
	/**
     * This moves to the specified time in the view. If the time is not already
     * in range it will move the list so that the first of the month containing
     * the time is at the top of the view. If the new time is already in view
     * the list will not be scrolled unless forceScroll is true. This time may
     * optionally be highlighted as selected as well.
     *
     * @param time The time to move to
     * @param animate Whether to scroll to the given time or just redraw at the
     *            new location
     * @param setSelected Whether to set the given time as selected，
     * 将传递进来的time赋值给成员变量mSelectedYear,设为选择的年份
     * @param forceScroll Whether to recenter even if the time is already
     *            visible
     * @return Whether or not the view animated to the new location
     */
    public boolean goTo(long time, boolean animate, boolean setSelected, boolean forceScroll) {
        
    	Time tmp = new Time();;
    	tmp.set(time);
    	tmp.normalize(true);
    	
    	//Log.d(TAG, "FullYearFragment.goTo():year-->" + tmp.year);
    	
    	if (time == -1) {
            Log.e(TAG, "time is invalid");
            return false;
        }
        
        
        // Set the selected day
        if (setSelected) {
            mSelectedYear.set(time);
            mSelectedYear.normalize(true);
        }

        // If this view isn't returned yet we won't be able to load the lists
        // current position, so return after setting the selected day.
        // fragment 处于不可交互状态，则不刷新view
        if (!isResumed()) {
            Log.d(TAG, "We're not visible yet");    
            return false;
        }
        
        // 设置要前往的年份，目标年份在list中的位置
        mTempTime.set(time);
        // Get the year we're going to
        // push Util function into Calendar public api.
        int position = mTempTime.year - MIN_CALENDAR_YEAR;

        View child;
        int i = 0;
        int top = 0;
        // Find a child that's completely in the view
        do {
            child = mListView.getChildAt(i++);
            if (child == null) {
                break;
            }
            top = child.getTop();//The top of this view, in pixels.
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "child at " + (i-1) + " has top " + top);
            }
        } while (top < 0);

        if (setSelected) {
            mAdapter.setmSelectedMonth(mSelectedYear);
           // Log.d(TAG, "goTo:mSelectedYear-->" + mSelectedYear.year);
        }
        //Log.d(TAG, "GoTo position " + position);

		if (animate) {
			// ListView在指定的时间滚动到指定的位置，并且指定滚动结束后，单元的顶部与父类顶部的距离
			mListView.smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET,
					GOTO_SCROLL_DURATION);
			return true;
		} else {
			// Sets the selected item and positions the selection y pixels from the top edge of the ListView
			// y The distance from the top edge of the ListView (plus padding) that the item will be positioned.
			// 单元的上边缘与listview顶部的距离
			mListView.setSelectionFromTop(position, LIST_TOP_OFFSET);
		}
        
        return false;
    }
	
    public void goToThisYear() {
		// TODO Auto-generated method stub
    	Time time = new Time(MyFixed.TIMEZONE);
		time.setToNow();
		time.normalize(true);
		mSelectedYear.set(time);
		myApp.setmCurrentClickedTime(mSelectedYear);
		int position = mSelectedYear.year - MIN_CALENDAR_YEAR;
		mListView.setSelectionFromTop(position, LIST_TOP_OFFSET);
	}
    

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}
	
		
	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		Log.d(TAG, "onDetach");
		super.onDetach();
	}

	
	
	
	
	

}
