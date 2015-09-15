package fxj.calendar.lunar;


import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.util.MyFixed;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

public class LunarFragment extends ListFragment implements OnScrollListener{

	private static final String TAG = "LunarFragment";
	private static final boolean D = true;
	
	private static final String KEY_CURRENT_TIME = "current_time";
	
	public static final int MIN_CALENDAR_YEAR = 1949;
	public static final int MAX_CALENDAR_YEAR = 2049;
	protected static final int GOTO_SCROLL_DURATION = 500;
	
	protected Context context;
	protected LunarAdapter mAdapter;
	protected ListView mListView;
	protected Time mSelectedMonth = null;
	
	// goto 方法中的临时变量
	protected Time mTempTime = new Time();
	
	protected float mFriction = 1.0f;// 摩擦系数
	protected float mMinimumFlingVelocity; // 最小滑动速度
	public static float mScale = 0;// dp pixel 比例系数 static 类型赋值之后不在变动？
	protected int BOTTOM_BUFFER = 20;
    public static int LIST_TOP_OFFSET = -1;  // so that the top line will be under the separator隔板,分离器
	
    
    private MyApp myApp;
    
    
	public MyApp getMyApp() {
		return myApp;
	}

	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}

	@Override
	public void onAttach(Activity activity) {
		
		Log.d(TAG, "onAttach");
		
		super.onAttach(activity);
		this.context = activity;
		
		ViewConfiguration vc = ViewConfiguration.get(activity);
		mMinimumFlingVelocity = vc.getScaledMinimumFlingVelocity();
		
		String tz = Time.getCurrentTimezone();
		mSelectedMonth = new Time(tz);// 初始化到 1 1 1970
		mSelectedMonth.normalize(true);
		
		if (mScale == 0) {
			mScale = getResources().getDisplayMetrics().density;
			if (D) Log.d(TAG, "mScale-->" + mScale);
			if (mScale != 1) {
				BOTTOM_BUFFER *= mScale;
				//LIST_TOP_OFFSET *= mScale;
			}
		}
		if (D) Log.d(TAG, "mScale-->" + mScale);
		setupAdapter();
		setListAdapter(mAdapter);
	}
	
	protected DataSetObserver mObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			
			Time month = new Time();
 			//month.setToNow();
 			//month.normalize(true);			
			
			if (myApp == null) {
				 month = mAdapter.getmRealSelectedYear();
			} else {
				 month = myApp.getmCurrentClickedTime();
			}
			
			if (D) Log.d(TAG, "DataSetObserver-->perform:year-->"+ month.year);
			goTo(month.toMillis(true), false, true, false);
			super.onChanged();
		}
		
	};

	/**
	 * 构造adapter实例对象，绑定监听器，
	 */
	protected void setupAdapter() {
		if (mAdapter == null) {
			mAdapter = new LunarAdapter(context);
			mAdapter.registerDataSetObserver(mObserver);
		}
		mAdapter.notifyDataSetChanged();
		if (D) Log.d(TAG, "notifyDataSetChanged-->perform");
		
		mAdapter.setMyApp(myApp);
	}

	@Override
	/**
	 * 可能与activity的oncreate同时进行，所以这个方法中不能初始化一些依赖activity视图系统的事情。
	 * 因为activity可能还没有初始化完成
	 */
	public void onCreate(Bundle savedInstanceState) {
		if (D) Log.d(TAG, "onCreate");
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
		outState.putLong(KEY_CURRENT_TIME, mSelectedMonth.toMillis(true));
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
		
		if (D) Log.d(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.lunarmonth_fragment, container, false);
		
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
		super.onActivityCreated(savedInstanceState);
		
		if (D) Log.d(TAG, "onActivityCreated");
		
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
        mListView.setOnScrollListener(this);
        
        mListView.setFadingEdgeLength(0);
        // Make the scrolling behavior nicer
        mListView.setFriction(ViewConfiguration.getScrollFriction() * mFriction);
	}

	@Override
	public void onStart() {
		if (D) Log.d(TAG, "onStart");
		//mAdapter.setMyApp(myApp);
		super.onStart();
	}

	@Override
	/**
	 * fragment going to be active
	 * 
	 */
	public void onResume() {
		super.onResume();
		if (D) Log.d(TAG, "onResume");
		
		setupAdapter(); // 调用了方法内部的 notify data set changed 
		doResumeUpdates();
	}
	
	/**
     * Updates the user preference fields. Override this to use a different
     * preference space.
     */
	protected void doResumeUpdates() {
		if (D) Log.d(TAG, "doResumeUpdates-->perform");
		goTo(mSelectedMonth.toMillis(true), false, false, false);
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
        
    	// 将参数 long time 转换为 Time 实例
    	// 无用的程序段，用于输出time所表示的时间
    	Time tmp = new Time();;
    	tmp.set(time);
    	tmp.normalize(true);    	
    	if (D) Log.d(TAG, "MonthFragment.goTo():year-->" + tmp.year);
    	
    	/*
    	// time 可以为负数，去掉这个判断。2015 8 6
    	if (time == -1) {
            Log.e(TAG, "time is invalid");
            return false;
        }*/
        
        
        // Set the selected day
        if (setSelected) {
        	mSelectedMonth.set(time);
        	mSelectedMonth.normalize(true);
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
        mTempTime.normalize(true);
        
        // Get the year we're going to
        // push Util function into Calendar public api.
        int position = 12*(mTempTime.year - MIN_CALENDAR_YEAR)+ mTempTime.month;

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
            mAdapter.setmSelectedMonth(mSelectedMonth);
            if (D) Log.d(TAG, "goTo:mSelectedYear-->" + mSelectedMonth.year);
        }
        if (D)  Log.d(TAG, "GoTo position " + position);

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
		myApp.setmCurrentClickedTime(time);
//		mSelectedYear.set(time);
//		myApp.setmCurrentClickedTime(mSelectedYear);
		int position = (time.year-MyFixed.MIN_YEAR)*12 + time.month;
		mListView.setSelectionFromTop(position, LIST_TOP_OFFSET);
	}
    
	@Override
	public void onPause() {
		if (D) Log.d(TAG, "onPause");
		super.onPause();
	}
	
		
	@Override
	public void onStop() {
		if (D) Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		if (D) Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		if (D) Log.d(TAG, "onDetach");
		super.onDetach();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:

			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

			break;

		case OnScrollListener.SCROLL_STATE_IDLE:

			break;

		}
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		int index = firstVisibleItem;
		
		if (visibleItemCount == 3) {
			index += 1;
		} else if (visibleItemCount == 2) {
			LunarView child0 = (LunarView) view.getChildAt(0);
			LunarView child1 = (LunarView) view.getChildAt(0);
			Rect mFirstRect = new Rect();
			Rect mSecondRect = new Rect();
			child0.getLocalVisibleRect(mFirstRect);
			int h0 = mFirstRect.bottom- mFirstRect.top;
			child1.getLocalVisibleRect(mSecondRect);
			int h1 = mSecondRect.bottom- mSecondRect.top;
			
			index = h0>=h1?index:index+1;		
			
		}
		
		int first = firstVisibleItem;
		int year = index/12+1949;
		int month = index%12+1;
		
		getActivity().setTitle(year+"年");
		
		
		
		if(D) Log.d(TAG, "first-->"+ year+"-"+month+ "--Count -->"+visibleItemCount);
		//Toast.makeText(getActivity(), "first-->"+ year+"-"+month+ "--Count -->"+visibleItemCount, Toast.LENGTH_SHORT).show();
		
		
		
	}
	
	
	
	

}
