package fxj.calendar.solar;

import java.util.LinkedList;
import java.util.List;

import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.datepicker.ItemsRange;
import fxj.calendar.util.MethodUtil;
import fxj.calendar.util.MyFixed;



import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

public class SolarMonth extends ViewGroup {

	private static final String TAG = "SolarMonth";
	
	/*
	 * 2015 8 14
	 * 2���·ݸ߶Ȳ�һ������£�ƽ�����ɣ���Ҫ�����Ż�
	 */
	private static final int MODIFY_HEIGHT_TIME = 280;
	private static final int MODIFY_HEIGHT_DELAY = 10;
	private static final int MODIFY_HEIGHT_STEP = 5;
	private static final int MODIFY_HEIGHT_FREQ = 20;
	private static final float SPEED = 0.25f;
	private int[] stepDistance = new int[10];
	private int mMatchCount = 0;
	private static final int MODIFY_HEIGHT = 0;
	
	/** �������¼���  parameter	 */
	private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();// 180ms
	private static final int SHOW_PRESS = 1;
	private static final int RUSH_CLICK = 2;
	private boolean mClickedFlag = false;
	private MotionEvent mCurrentDownEvent;
	private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;
    private boolean mAlwaysInTapRegion = false;
    private boolean mStillDown = false;
    private int mTouchSlopSquare;// 12pixel X 12 pixel
	
    
    private boolean flingFlag = false;
	
	public void setFlingFlag(boolean flingFlag) {
		this.flingFlag = flingFlag;
	}

	private float lastTouchedY;

	private float lastTouchedX;

	private long mClickedTime;
	
	/** Top and bottom items offset (to hide that) */
	private static final int ITEM_OFFSET_PERCENT = 0;

	/** Left and right padding value */
	private static final int PADDING = 0;

	/** Default count of visible items */
	private static final int DEF_VISIBLE_ITEMS = 3;

	// wheel current item value
	private int currentItem = 0;
	private int lastItem = 0;
	// the number of first item in itemsLayout
	private int firstItem;
	// Count of visible items
	private int visibleItems = DEF_VISIBLE_ITEMS;

	// Item height decide by item index
	private int itemHeight = 0;
	// view �ĸ߶�
	private int solarMonthHeight;

	public int getSolarMonthHeight() {
		return solarMonthHeight;
	}

	// Center Line
	private Drawable centerDrawable;

	// Wheel drawables
	private int wheelBackground = R.drawable.wheel_bg;
	private int wheelForeground = R.drawable.wheel_val;

	// Scrolling
	private SolarMonthScroller scroller;
	private boolean isScrollingPerformed;
	private int scrollingOffset;
	public int getScrollingOffset() {
		return scrollingOffset;
	}

	
	boolean isCyclic = false;

	// items layout container of month view
	private LinearLayout itemsLayout;
	
	// view adapter
	private SolarMonthAdapter viewAdapter;

	// recycle
	private SolarMonthRecycle recycle = new SolarMonthRecycle(this);

	// Listeners
	private List<OnMonthChangedListener> changingListeners = new LinkedList<OnMonthChangedListener>();
	private List<OnMonthScrollListener> scrollingListeners = new LinkedList<OnMonthScrollListener>();
	private List<OnMonthClickedListener> clickingListeners = new LinkedList<OnMonthClickedListener>();

	// String label="";
	
	private MyApp myApp;
	
	public MyApp getMyApp() {
		return myApp;
	}

	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}
	
	/**
	 * constructor
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SolarMonth(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	public SolarMonth(Context context) {
		super(context);
		initData(context);
	}

	public SolarMonth(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	private void initData(Context context) {
		scroller = new SolarMonthScroller(getContext(), scrollingListener, this);
		scroller.setmHandler(mHeightHandler);
		
		int touchSlop;
		final ViewConfiguration mVC = ViewConfiguration.get(context);
		touchSlop =mVC.getScaledTouchSlop();
		mTouchSlopSquare = touchSlop*touchSlop;
		
		
		Log.d(TAG, "getScaledTouchSlop()-->" + touchSlop + "---getTapTimeout()-->"+ ViewConfiguration.getTapTimeout());
		
	}

	/*
	 * Scrollinglistener WheelScroller ������ڲ��ӿ� public ScrollingListener,
	 * ��WheelScroller.ScrollingListenerʵ��ͨ��WheelScroller�Ĺ��췽��������WheelScroller��
	 * ���ʵ���еļ���������ֻ��WheelScroller��ʹ��
	 */
	SolarMonthScroller.ScrollingListener scrollingListener = new SolarMonthScroller.ScrollingListener() {
		@Override
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			
			doScroll(distance);// distance ��Ҫ�����ľ��룬���ݾ������currentItem�����ػ�

			// �������û��ִ�й� 2015 8 2
			int height = getHeight();
			if (scrollingOffset > height) {
				scrollingOffset = height;
				scroller.stopScrolling();
			} else if (scrollingOffset < -height) {
				scrollingOffset = -height;
				scroller.stopScrolling();
			}
		}

		@Override
		/**
		 *  WheelView ������־��Ϊ false��֪ͨ����������ֹͣ��������ȡ���ݣ����� offset �ػ�view.
		 *  
		 */
		public void onFinished() {
			if (isScrollingPerformed) {
				notifyScrollingListenersAboutEnd();
				isScrollingPerformed = false;
			}
			if (flingFlag) flingFlag = false;
			scrollingOffset = 0;

			/*
			 *  ʹWheelMonth�߶���currentItem�߶ȱ���һ�� �� ������Ӳ����Ҫ�Ż�
			 *  2015 8 13 ����ƽ����һЩ�����ǲ�����,�� onJustify()�� ����
			 */
			invalidate();
		}

		@Override
		public void onJustify() {
			
			/*
			 * 2015 8 30 
			 * ��ʼ����currentItem = lastItem,
			 * �������У�ÿ�� currentItem ��������ص��˷���ʱ����2��ֵ���ȣ����ʾ�·ݸı䣬�趨Ŀ���·ݵ�1��ΪĬ�ϵ������;
			 * ��Ŀ���·��ǵ��£����������趨Ϊ���죬
			 * ���currentItem ͬ���� lastItem;
			 */
			if (lastItem != currentItem) {
				SolarMonth.this.refreshClickedDayForMoving(currentItem);
				lastItem = currentItem;
			}
			
			/**
			 * 2015 8 31
			 * ������Ӧ�� move action ��ָ�ͷ�ʱ��Math.abs(offset)����ָ��ֵ�������� currentItem �ϱ�Ե λ�ã�
			 * justifyScroll���������У���SolarMonth�߶���currentItem�߶Ȳ�һ�£������ runnable �ڹ�������ǰ�����߶�
			 * 
			 * ����ָ��ȷ�еĹ������룬�ҹ���ֹͣʱ currentItem ���ϱ�Ե��view���룬�������������
			 * fling �������Ϊscroller.fling()�����Ǿ�ȷ���ƶ����������ñ�־λ����Ϊflingģʽ�µ� justify�������ô˷���
			 * �еĸ߶�������fling ģʽ��ר�е�����������
			 */
			if (Math.abs(scrollingOffset) > SolarMonthScroller.MIN_DELTA_FOR_SCROLLING) {
				
				//Log.d(TAG, "onJustify-->"+ scrollingOffset);
				if (Math.abs(scrollingOffset) <= 70) {
					SolarMonth.this.justifyScroll(scrollingOffset, 8*Math.abs(scrollingOffset));
				} else {
					SolarMonth.this.justifyScroll(scrollingOffset, 4*Math.abs(scrollingOffset));
				}
				
				/**
				 * ���������޸ĸ߶ȣ��ǳ���� 2015 8 30
				 * 2015 8 31 �޸ģ���ӷ��� justifyScroll() �������������߶�����һ��������
				 */
				/*if (itemsLayout.getChildCount() != 0) {
					int heightOffset = SolarMonth.this.getHeight()
							- (getItemHeight(currentItem) - 36);
					if (Math.abs(heightOffset) > 5) {

						ViewHeightOffset = MethodUtil.smoothModifyHeight(
								-heightOffset, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
						SolarMonth.this.postDelayed(mModifyHeight, MODIFY_HEIGHT_DELAY);
					}
				}*/
			}
		}
	};

	/**
	 * 9*30 = 270
	 * 
	 */
	private final Runnable mModifyHeight = new Runnable() {

		@Override
		public void run() {

			if (mMatchCount < 10) {
				LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(
						SolarMonth.this.getWidth(), 36
								+ stepDistance[mMatchCount]
								+ SolarMonth.this.getHeight());
				SolarMonth.this.setLayoutParams(lP);
				mMatchCount++;

				SolarMonth.this.postDelayed(mModifyHeight, MODIFY_HEIGHT_FREQ);
			} else {
				mMatchCount = 0;
				SolarMonth.this.removeCallbacks(mModifyHeight);
			}

		}

	};
	

	/**
	 *  handler �汾
			if (itemsLayout.getChildCount() != 0) {
				
				int heightOffset = SolarMonth.this.getHeight() - (getItemHeight(currentItem)-36);
				if (Math.abs(heightOffset)> 5) {
					ViewHeightOffset = MethodUtil.smoothModifyHeight(-heightOffset, 280, 5);
					}
					//SolarMonth.this.ccc=0;
					//SolarMonth.this.mMatch = true;
					mHeightHandler.sendEmptyMessageDelayed(0, 50);
					
				}
			
	 */
	private  Handler mHeightHandler = new Handler() {
		
		private int mMatchCount;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (mMatchCount < 10) {
					LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(
							SolarMonth.this.getWidth(), 36
									+ stepDistance[mMatchCount]
									+ SolarMonth.this.getHeight());
					SolarMonth.this.setLayoutParams(lP);
					mMatchCount++;
					mHeightHandler.sendEmptyMessageDelayed(0, 30);

				} else {
					mMatchCount = 0;
					removeMessages(0);
				}
				break;
			case 1:
				if (msg.arg1 != -1) {
					if (stepDistance.length != 0) {
						for(int i =0;i<stepDistance.length;i++) {
							stepDistance[i] = 0;
						}
					}
					stepDistance = MethodUtil.smoothModifyHeight(-msg.arg1, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
					Log.d("Month", "ִ���� msg 1");
					this.sendEmptyMessage(0);	
				}
 				
				break;
			}
			
		};
		
		
	};
	
	/**
	 * <li> ���ݹ�����Ŀ��index�����Ŀ���·ݵ� 1��time���󣬸�ֵ�� application�У�����Ŀ���·��ǵ�ǰ�£�������ѡ��Ϊ���죬
	 * MethodUtil.indexToTime(index) �����߼�
	 * <li> ����adapter�ĸ���click���������� mRealClickedDay lastClickedDay����֪ͨ�����������ڸı�
	 * @param index the index which fling to
	 */
	public void refreshClickedDayForMoving (int index) {
		myApp.setmCurrentClickedTime(MethodUtil.indexToTime(index));
		viewAdapter.refreshClick(myApp.getmCurrentClickedTime());
	}
	

	/**
	 * Set the the specified scrolling interpolator
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.setInterpolator(interpolator);
	}

	/**
	 * Gets count of visible items
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets the desired count of visible items. Actual amount of visible items
	 * depends on wheel layout parameters. To apply changes and rebuild view
	 * call measure().
	 * 
	 * @param count
	 *            the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * Gets view adapter
	 * 
	 * @return the view adapter
	 */
	public SolarMonthAdapter getViewAdapter() {
		return viewAdapter;
	}

	// Adapter listener
	private DataSetObserver dataObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			invalidateWheel(false);
		}

		@Override
		public void onInvalidated() {
			invalidateWheel(true);
		}
	};
	

	/**
	 * Sets view adapter. Usually new adapters contain different views, so it
	 * needs to rebuild view by calling measure().
	 * 
	 * @param viewAdapter
	 *            the view adapter
	 */
	public void setViewAdapter(SolarMonthAdapter viewAdapter) {
		if (this.viewAdapter != null) {
			this.viewAdapter.unregisterDataSetObserver(dataObserver);
		}
		this.viewAdapter = viewAdapter;
		if (this.viewAdapter != null) {
			this.viewAdapter.registerDataSetObserver(dataObserver);
		}
//Log.d(TAG, "setViewAdapter::itemsLayout::"+itemsLayout.getChildCount());// ���ʱ���ǿ�ָ�����
		if (itemsLayout != null) {// ���ʱ��itemsLayout �� null
			
			Log.d(TAG, "setViewAdapter::itemsLayout::"+itemsLayout.getChildCount());
			viewAdapter.setmLinearLayout(itemsLayout);
		}

		invalidateWheel(true);
	}

	/**
	 * Adds Month changing listener
	 * @param listener  the listener
	 */
	public void addChangingListener(OnMonthChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes Month changing listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeChangingListener(OnMonthChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnMonthChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addScrollingListener(OnMonthScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeScrollingListener(OnMonthScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnMonthScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnMonthScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Adds wheel clicking listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addClickingListener(OnMonthClickedListener listener) {
		clickingListeners.add(listener);
	}

	/**
	 * Removes wheel clicking listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeClickingListener(OnMonthClickedListener listener) {
		clickingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about clicking
	 */
	protected void notifyClickListenersAboutClick(int item) {
		for (OnMonthClickedListener listener : clickingListeners) {
			listener.onItemClicked(this, item);
		}
	}

	/**
	 * Gets current item value
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	public void setLastItem(int lastItem) {
		this.lastItem = lastItem;
	}
	
	public int getLastItem() {
		return lastItem;
	}
	
	
	/**
	 * ����ÿ���ƶ��ľ��룬���� runnable ��������߶�
	 * @param diff ת���·ݵĸ߶Ȳ�
	 * @param delayMillis ��ʱʱ��
	 */
	private void doModifyHeight(int diff, int delayMillis) {
		
		stepDistance = MethodUtil.smoothModifyHeight(diff, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
		SolarMonth.this.postDelayed(mModifyHeight, delayMillis);
	}
	
	
	/**
	 * justify �������ã�
	 * <li> 
	 * @param distance 
	 * @param millis 
	 */
	public void justifyScroll(int distance, int millis) {
		scroller.scroll(distance, millis);
		if (!flingFlag && itemsLayout.getChildCount() != 0) {

			int heightOffset = SolarMonth.this.getHeight()
					- (getItemHeight(currentItem) - 36);
			if (Math.abs(heightOffset) > 5) {
				doModifyHeight(-heightOffset, MODIFY_HEIGHT_DELAY);
			}
		}
	}
	
	/**
	 *  fling ���Ƹ߶�������SolarMonthScroller ���ж�����Ϊflingʱ�����ô˷��������ݵ�ǰitem��Ŀ��item��
	 *  ���ҽ� fling_Flag ��Ϊ true��onFinish() �и�λ������������scroller.fling();���������µ�offset΢Сƫ�ƣ�
	 *  ����ִ�� justifyScroll�����е������߶ȷ����飻
	 * @param mNew
	 * @param mOld
	 * @param flingFlag
	 */
	public void modifyHeightCausedByFling (int mNew,int mOld, boolean flingFlag) {
		
		this.flingFlag = flingFlag;
		int oldHeight = getItemHeight(mOld);
		int mNewHeight = getItemHeight(mNew);
		if(oldHeight != mNewHeight) {
			doModifyHeight(mNewHeight - oldHeight, MODIFY_HEIGHT_DELAY);
		}
		
	}
	
	/**
	 * setCurrentItem ��4�����صķ���
	 */
	
	/**
	 * ����currentItem,���ù�������������-�����ƶ���Ŀ��λ��
	 * <li> ������������������� today ��ť������ǰ������ʾ�·��� today�����·ݲ� 5���£���������������ڵ��·�
	 * <li> ����ָ��item �Ǵ����
	 * @param item 
	 * 				����֮���·ݵ� index ֵ
	 * @param distance
	 * 				�����ľ���
	 * @param millis
	 * 				������ʱ
	 */
	/*public void setCurrentItem(int item, int distance, int millis) {
		
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			throw new NullPointerException("adapter is null or have none item");
			//return; // throw?
		}
		int itemCount = viewAdapter.getItemsCount();

		if (item < 0 || item >= itemCount) {
			this.currentItem = item;
			scroller.scroll(distance, millis);
		}
		
	}*/
	
	/**
	 * ����currentItem,���ù�������������-�����ƶ���Ŀ��λ�ã����趨currentItem��SolarMonth.doScroll(),
	 * �ڹ����и���currentItem��
	 * <li> ������������������� today ��ť������ǰ������ʾ�·��� today�����·ݲ� 4 ���£���������������ڵ��·�
	 * @param mNew
	 * 				����֮���·ݵ� index ֵ
	 * @param mOld
	 * 				����ʱ���·� index ֵ
	 * @param distance		�����ľ���
	 * @param millis		������ʱ
	 */
	public void setCurrentItem(int mNew,int mOld, int distance, int millis) {
		
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			throw new NullPointerException("adapter is null or have none item");
			//return; // throw?
		}
		int itemCount = viewAdapter.getItemsCount();
		
		if (mNew >= 0 || mNew <= itemCount) {
			int oldHeight = getItemHeight(mOld);
			int mNewHeight = getItemHeight(mNew);
			scroller.scroll(distance, millis);
			if(oldHeight != mNewHeight) {
				doModifyHeight(mNewHeight - oldHeight, millis-200);
			}
			
		}
		
	}
	
	
	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * ���ط������ǹ���ģʽ
	 * @param index 
	 * 				the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}
	
	/**
	 * Sets the current item. Does nothing when index is wrong.
	 * @param index
	 *            the item index
	 * @param animated
	 *            the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			throw new NullPointerException("adapter is null or have none item");
			//return; // throw?
		}

		int itemCount = viewAdapter.getItemsCount();

		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					/*
					 * ��ѭ��״̬��-itemCount<index<0,
					 * ѡ���ʵ��index=item - |index|
					 */
					index += itemCount;  
				}
				/*
				 * if (index<0) { index=itemCount - Math.abs(index)%itemCount; }
				 * else { index %= itemCount; }
				 */
				index %= itemCount; // index= index%itemCount
			} else {
				// ��ѭ��������� return
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {// ����ģʽ

				int itemsToScroll = index - currentItem;// ��ǰitem��Ŀ��item֮��

				/*
				 * ���ĸ��������� ����or���¡� ��Ϊ��ѭ����������ǰλ�õ�Ŀ��λ�ô���2���������򣬲�ֵ�ֱ�Ϊ�� a =
				 * abs(index - currentItem); b = itemCount - abs(index -
				 * currentItem) a < b ʱ���� a ����Ŀ����������ʹ�С ��Ϊ index -
				 * currenItem�������� index>itemCount, a > b ʱ���� b ����Ŀ������������index -
				 * currentItem�෴
				 */
				if (isCyclic) {
					int scroll = itemCount + Math.min(index, currentItem)
							- Math.max(index, currentItem);
					// scroll > 0
					if (scroll < Math.abs(itemsToScroll)) {
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}

				scroll(itemsToScroll, 0);
			} else {
				scrollingOffset = 0; // ����
				int old = currentItem;
				currentItem = index; // ����currentItem ondraw��ʹ���µ�currentItem �ػ�
				notifyChangingListeners(old, currentItem);
				invalidate();// ֱ�ӵ����ػ�
			}
		}
	}
	
	
	/**
	 * �ɱ任��index ���� SolarMonth �ĸ߶Ȳ�ֵ����ǰ��height һ�£������µ� currentItem,�����ػ棬
	 * ���������µ� currentItem,�����ػ棬����ƽ���޸� SolarMonth�ĸ߶ȡ�
	 * <li> ������������������ today����������ʾ�·��� ���������·������� 4������ô˷���
	 * @param mNew 
	 * @param old
	 * @param animated default false
	 */
	public void setCurrentItem(int mNew, int old, boolean animated) {
		int oldHeight = getItemHeight(old);
		int mNewHeight = getItemHeight(mNew);
		if(oldHeight == mNewHeight) {
			setCurrentItem(mNew, animated);
		} else {
			setCurrentItem(mNew, animated);
			doModifyHeight(mNewHeight - oldHeight, 0);		
		}
		
	}
	
	/**
	 * ��lunar ����ת��Ϊ solar ����
	 * <li> ò����ֱ���ػ棬�������ò��ûʹ�ã�MonthActivity �� ֱ������ currentItem �� view�� LayoutParams
	 * @param mNew
	 * @param old
	 */
	public void lunarToSolar(int mNew, int old) {
		int oldHeight = getItemHeight(old);
		int mNewHeight = getItemHeight(mNew)-36;
		if(oldHeight == mNewHeight) {
			scrollingOffset = 0; 			
			currentItem = mNew; 
			invalidate();
		} else {			
			/*
			int offsetHeight = mNewHeight - oldHeight;
			ViewHeightOffset = ModifyHeight.smoothModifyHeight(
					offsetHeight, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
			SolarMonth.this.postDelayed(mModifyHeight,0);*/
			
			scrollingOffset = 0; 			
			currentItem = mNew; 
			invalidate();
			
			/*if (this.getHeight() != mNewHeight ) {
				int offsetHeight = mNewHeight - this.getHeight();
				ViewHeightOffset = ModifyHeight.smoothModifyHeight(
						offsetHeight, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
				SolarMonth.this.postDelayed(mModifyHeight,0);
			}*/
					
		}
	}
	
	

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown
	 * the last one
	 * 
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * 
	 * @param isCyclic
	 *            the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}

	/**
	 * Sets the drawable for the wheel background
	 * 
	 * @param resource
	 */
	public void setWheelBackground(int resource) {
		wheelBackground = resource;
		setBackgroundResource(wheelBackground);
	}

	/**
	 * Sets the drawable for the wheel foreground item���µ�������
	 * 
	 * @param resource
	 */
	public void setWheelForeground(int resource) {
		wheelForeground = resource;
		centerDrawable = getContext().getResources().getDrawable(
				wheelForeground);
	}

	/**
	 * Invalidates wheel,
	 * <p>
	 * clearCaches true ���recycle itemsLayout ������view
	 * 
	 * @param clearCaches
	 *            if true then cached views will be clear
	 */
	public void invalidateWheel(boolean clearCaches) {
		if (clearCaches) {
			recycle.clearAll();
			if (itemsLayout != null) {
				itemsLayout.removeAllViews();
			}
			scrollingOffset = 0;

		} else if (itemsLayout != null) {
			// cache all items
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		}

		invalidate();
	}

	/**
	 * Initializes resources ���ñ���ɫ
	 * 
	 */
	private void initResourcesIfNecessary() {
		setBackgroundResource(wheelBackground);
	}

	/**
	 * Calculates desired height for layout ��Ϊwheel month
	 * ��ֹ״ֻ̬��һ��view������itemLayout��ֻ��һ����view�� ��ʱ���µ�itemHeight�ǵ�ǰ��ʾ����view�ĸ߶�:
	 * currentItemHeight;
	 * 
	 * @param layout
	 *            the source layout
	 * 
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemHeight = layout.getChildAt(0).getMeasuredHeight();
		}

		int desired = itemHeight * visibleItems - itemHeight
				* ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumHeight());
	}

	/**
	 * Returns height of wheel item,
	 * <p>
	 * item�ĸ߶ȣ����ַ����� ��Ա�������Ӳ����ļ��еõ�item�ĸ߶ȣ�view�ĸ߶�/��ʾ��item����
	 * 
	 * 2015 ��Ҫ������ƣ�heightֵ ��Ϊ3����� 1��upItemHeight 2) currentItemHeight 3)
	 * downItemHeight
	 * 
	 * @return the item height
	 */
	private int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		}

		if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
			itemHeight = itemsLayout.getChildAt(0).getHeight();
			return itemHeight;
		}

		return getHeight() / visibleItems;
	}

	/**
	 * ��indexֵ���㣬monthView�ĸ߶�
	 * 
	 * @param index
	 * @return
	 */
	public int getItemHeight(int index) {

		int year = 1949 + index / 12;
		int month = index % 12;
		Time time = new Time(MyFixed.TIMEZONE);
		time.set(1, month, year);
		time.normalize(true);

		int days = time.getActualMaximum(Time.MONTH_DAY);
		int tmp = time.weekDay;
		int tmp_weeks = 1;
		for (int i = 2; i <= days; i++) {
			tmp = (tmp + 1) % 7;
			if (tmp == 0) {
				tmp_weeks++;
			}
		}

		itemHeight = tmp_weeks * SolarView.dayHeight
				+ SolarView.monthTextHeight;
		return itemHeight;
	}

	/**
	 * Ӧ�ø�Ϊ����߶ȣ���� match parent
	 * 
	 * 
	 * Calculates control width and creates text layouts
	 * 
	 * @param widthSize
	 *            -->the input layout width
	 * @param mode
	 *            -->the layout
	 *            mode��MeasureSpec.EXACTLY��MeasureSpec.AT_MOST��MeasureSpec
	 *            .UNSPECIFIED
	 * @return width -->the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {

		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		// MeasureSpec ��int��ʽ�洢 ��ʵ�ʲ�����onMeasure��
		// ʹ��ʵ�Σ����� widthMearsureSpec
		itemsLayout
				.measure(MeasureSpec.makeMeasureSpec(widthSize,
						MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(
						0, MeasureSpec.UNSPECIFIED));
//Log.d(TAG, "calculateLayoutWidth--measure--first");
		int width = itemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			// at_most mode ����ܳ���
			// widthSize�����width����ָ����widthSize����widthΪwidthSize
			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}

		// ��ȱ�����Ϊ exactly
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING,
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED));
//Log.d(TAG, "calculateLayoutWidth--measure--second");

		return width;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//Log.d(TAG, "onMeasure----" + heightSize);

		// ��itemLayout��װ��view
		buildViewForMeasuring();
		// �����Ϊexactly
		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);// ���㲼��������view�߶�

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		setMeasuredDimension(width, height - 36);
//Log.d(TAG, "onMeasure----" + getMeasuredHeightAndState());
		solarMonthHeight = getMeasuredHeightAndState();
//Log.d("MonthActivity", "onMeasure----" + getMeasuredHeightAndState());
	}

	@Override
	/**
	 * r-l ��
	 * b-t ��
	 */
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layout(r - l, b - t);
	}

	/**
	 * Sets layouts width and height
	 * @param width  the layout width
	 * @param height the layout height
	 */
	private void layout(int width, int height) {
		int itemsWidth = width - 2 * PADDING;

		// ��itemLayout��items����Ŀռ�
		itemsLayout.layout(0, 0, itemsWidth, height); // viewgroup
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
			updateView();

			drawItems(canvas);
			solarMonthHeight= this.getHeight();
		}
	}

	/**
	 * Draws items
	 * <p>
	 * draw �ķ�ʽ���� canvas�ϻ�layout������Ҫ��canvas��ԭ����itemLayoutԭ���غϣ�
	 * Ĭ�������canvas�븸��ͼ��WheelView���غϣ� �����ƶ�ʱ��itemLayout�ϲ������˸���ͼ
	 * <li>scroll ģʽ������ offset>0,top = 37 canvas ��λ -top + offset��Ϊ��������ͼ��item�Ķ���
	 * y����
	 * <li>���� offset <0 top = 0; canvas �ƶ�
	 * offset����Ϊ��ǰfirstItem�Ķ���y���꣨����ͼֻ��ʾ���沿�֣�
	 * 
	 * 2015 8 11 ����λ���ƶ���Ҫ���¼����߼�����Ϊÿ��itemHeight��һ����
	 * 
	 * 
	 * 
	 * 
	 * @param canvas the canvas for drawing
	 *            
	 */
	private void drawItems(Canvas canvas) {

		canvas.save();
		int top = (currentItem - firstItem) * getItemHeight(firstItem);

		canvas.translate(PADDING, -top + scrollingOffset - 36);

		// �ڻ����ϻ������view��linearLayout �Ͳ����е���view
		itemsLayout.draw(canvas);

		canvas.restore();
	}

	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//Log.d(TAG, "-----onInterceptTouchEvent====");
		return true;
		// super.onInterceptTouchEvent(ev); return false		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//this.invalidate();
		return super.dispatchTouchEvent(ev);
		
	}
	
	@Override
	/**
	 * �Ȳ����ô���
	 */
	public boolean performClick() {
		return super.performClick();
	}
	
	
	@Override
	/**
	 * ACTION_DOWN ACTION_MOVE ACTION_UP
	 * ��Event���ݵ�WheelScroller�д���
	 */
	public boolean onTouchEvent(MotionEvent event) {
		
		handleGesture (event);
		
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastTouchedY = event.getY();
            lastTouchedX = event.getX();
            mClickedTime = System.currentTimeMillis();
            
            scroller.onTouchEvent(event);
			break;
		case MotionEvent.ACTION_MOVE:
			int distanceY = (int) Math.abs(event.getY() - lastTouchedY);
        	int distanceX = (int) Math.abs(event.getX() - lastTouchedX);
        	long delay = System.currentTimeMillis() - mClickedTime;
//Log.d(TAG, "----interval time between down and move ===="+delay);
        	/*
        	if (delay > 200 ) {
        		mDispatchToMonthViewS = true;
        	} else {
        		mDispathcToScroller = true;
        	}
        	*/
        	if (mClickedFlag == true) {
        		itemsLayout.dispatchTouchEvent(event);
        	} else {
        		scroller.onTouchEvent(event);
        	}
        	
			//itemsLayout.dispatchTouchEvent(event); 
        	//getParent().requestDisallowInterceptTouchEvent(true);// ��ֹ����ͼ�����¼�
			break;
		case MotionEvent.ACTION_UP:
			float delayup = System.currentTimeMillis() - mClickedTime;
//Log.d(TAG, "----interval time between down and up ===="+delayup);
        	scroller.onTouchEvent(event);
        	
			if (!isScrollingPerformed) {
				// ��������û�б�ִ�й��� ����Ϊ 2015 8 1
				int distance = (int) event.getY() - getHeight() / 2;
				if (distance > 0) {
					distance += getItemHeight() / 2;
				} else {
					distance -= getItemHeight() / 2;
				}
				int items = distance / getItemHeight();
				if (items != 0 && isValidItemIndex(currentItem + items)) {
					notifyClickListenersAboutClick(currentItem + items);
				}
			}
			break;
		}

		return true;
	}
	
	private void handleGesture (MotionEvent ev) {
		
		final int action = ev.getAction();
				
		final boolean pointerUp =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;
        
        
        switch (action & MotionEvent.ACTION_MASK) {
        
        case MotionEvent.ACTION_DOWN:
        	mDownFocusX = mLastFocusX = focusX;
        	mDownFocusY = mLastFocusY = focusY;
        	if (mCurrentDownEvent != null ) {
        		mCurrentDownEvent.recycle();
        	}
        	mCurrentDownEvent = MotionEvent.obtain(ev);
        	
        	mAlwaysInTapRegion = true;
        	mStillDown = true;
        	//mHandler.sendEmptyMessageAtTime(RUSH_CLICK, mCurrentDownEvent.getDownTime()+ 100);
        	mHandler.sendEmptyMessageAtTime(SHOW_PRESS, mCurrentDownEvent.getDownTime()+ TAP_TIMEOUT);
        	
        	break;
        case MotionEvent.ACTION_MOVE:
        	//final float scrollX = mLastFocusX - focusX;
        	//final float scrollY = mLastFocusY - focusY;
        	if (mAlwaysInTapRegion) {
        		final int deltaX = (int) (focusX - mDownFocusX);
                final int deltaY = (int) (focusY - mDownFocusY);
                int distance = (deltaX * deltaX) + (deltaY * deltaY);
                
                if (distance > mTouchSlopSquare) {
                	//mHandler.removeMessages(2);
                	mHandler.removeMessages(SHOW_PRESS);               	
                	mLastFocusX = focusX;
                	mLastFocusY = focusY;
                	mAlwaysInTapRegion = false;
                	
                }
                
        	}
        	break;
        case MotionEvent.ACTION_UP:
        	if (mClickedFlag == true) {
        		mClickedFlag = false;
        	}
        	if (mAlwaysInTapRegion == true) {
        		mHandler.sendEmptyMessage(RUSH_CLICK);
        		mHandler.removeMessages(SHOW_PRESS);
        	}
        	
        	break;
        case MotionEvent.ACTION_CANCEL:
        	if (mClickedFlag == true) {
        		mClickedFlag = false;
        	}
        	if (mAlwaysInTapRegion == true) {
        		
        		mHandler.removeMessages(SHOW_PRESS);
        	}       	
        	break;
        
        }
				
	}
	
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case SHOW_PRESS:
				/*
				 * ��־λ��λʱ��180s��
				 * action_up ȡ����Ϣ
				 */
				mClickedFlag = true;
				itemsLayout.dispatchTouchEvent(mCurrentDownEvent);
				break;
			case RUSH_CLICK:
				/*
				 * 100ms û��move event ��ִ�У�
				 * action_up,��ȡ��
				 */
				itemsLayout.dispatchTouchEvent(mCurrentDownEvent);
				break;
			default:
				throw new RuntimeException("Unknown  message " + msg);
			}
			
		};
		
	};
	
	
	/**
	 * Scrolls the wheel,���ݹ����ľ��������ֵ ��ȷ��������� currentItem ����Ҫ������item
	 * ����������setCurrentItem�����������ڲ����� invalidate()��;
	 * <p>
	 * if delta < 0 item index up; else item index down;
	 * 
	 * @param delta
	 *            the scrolling value
	 */
	private void doScroll(int delta) {

		// delta��ÿ��move�ļ�����룬offset�����move������down��ʼ���������ԭ�����ƫ������ÿ���ƶ�delta��offset +=delta
		scrollingOffset += delta;

		// int itemHeight = getItemHeight();
		// 2015 8 12 �޸Ĳο��߶�
		int itemHeight = getItemHeight(firstItem);

		// ��Ҫ�ƶ��� item��������ֵ��currentItem ���ӣ���ֵ��currentItem ��С
		int count = scrollingOffset / itemHeight;

		/*
		 * count<0 �����ϻ� current item ++ ��count>0 �����»� current item-- ��ǰ��λ��-
		 * �ƶ�������=Ŀ��λ�� ��index�� pos ��Ϊ�ƶ����currentItem �ϻ���count��ֵ�����item index ���ӣ�
		 * �»� ���෴
		 */
		int pos = currentItem - count;

		int itemCount = viewAdapter.getItemsCount();// data set �е�item ����

		// ���벹�����ƶ����벻�ܱ�itemHeigh����ʱ������С��itemHeighһ��ʱ��fixPos=0������Ҫ�޸�posλ��
		int fixPos = scrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}

		/**
		 * ��������firstItemHeightһ�룬currentItem�ı䣬 ��ֵ��offset����ȷ�����ƶ���count����+1��count
		 * �� count++ �� count--��
		 */
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) { // fixPos > itemHeight/2 �������£�ƫ��������item�߶ȵ�һ�룬��
								// currentItem -- count++
				pos--; // currentItem-- ����������++ count��������count++
				count++;
			} else if (fixPos < 0) {
				pos++; // currentItem++
				count--; // offset�Ǹ�ֵ��ǰ���£�countҲ�Ǹ�ֵ����������+1����count--��
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount; // ѭ��ģʽ�£�index��ֵ��ʾ�������ǣ�index + itemCount ;
									// 0-12 -1 �� 12=13+ -1
				// ��whileѭ���ɸ�Ϊ pos = itemCount - math.abd(pos % itemCount)
			}
			pos %= itemCount; // ȷ�� pos ��itemCount��(pos ���������Ҵ���itemCount)

		} else { // ��ѭ��ģʽ��
			//
			if (pos < 0) {// index < 0 ���� index = 0��set
							// ��һ��Ԫ�������м䣬�ƶ�����ΪcurrentItem��
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {// index���� set��Χ�������һ��Ԫ�أ�itemCount -
											// 1)�������м䣬�ƶ�����Ϊ��ֵ��-(itemCoun -
											// currentItem) + 1;
				count = currentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {// ͬѭ��ģʽ
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) { // ͬѭ��ģʽ
				pos++;
				count--;
			}
		}

		int offset = scrollingOffset;

		if (pos != currentItem) {
			// set new currentItem ,�˷����ڲ�������invalidate();
			setCurrentItem(pos, false);
		} else {
			invalidate(); // currentItemδ�ı䣬ֱ�ӵ����ػ�
		}

		// update offset
		/*
		 * ��תoffset,һ���ƶ����� itemHeight/2 offset >0 count = 1 (down����ָ����) offset <
		 * 0 count = -1(up,��ָ����)
		 * 
		 * offset - count * itemHeight �� ��ֵ (down) ��ʱ������ͬ�����ƶ���ÿ�μ����delta
		 * ����ֵ��offset+ delta �����ӽ�0 currentItem������view�����غ�
		 * ��ֵ(up)��offset��firstItemӦ��view�еĸ߶ȣ�ÿ���ƶ���deltaֵΪ����offset��+�� + delta(-)
		 * = firstItem��view����ʾ�ĸ߶� -firstItemHeight + offset ���ǻ��������λ��
		 */
		scrollingOffset = offset - count * itemHeight;

		// fling ���
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	/**
	 * Scroll the wheel ������Ŀ��λ��,������item����
	 * 
	 * @param itemsToSkip
	 *            items to scroll
	 * @param time
	 *            scrolling duration
	 * 
	 *            2015 8 11 ��Ҫ���޸ģ�ItemHeight ����Indexֵ���� ��ñ�һ���ࣨ��ʵ����һ��������
	 *            itemToScroll ��Ҫ������item��Ŀ��move fling ��Ϊ1 ������죬��ǰλ�������λ��֮�� ������5
	 *            setcurentItem��С��5�������ʱ�䣩
	 * 
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items,
	 * <p>
	 * ��currentItem and offset ���㵱ǰӦ����ʾ�ĵ�һ�� item �� index��first; Ӧ����ʾ��
	 * item������count��
	 * <Li>����ItemRange ����Ϊ��first count
	 * 
	 * @return the items range
	 * 
	 *         2015 8 15 ���� monthWheel ��count��ֵֻ��Ϊ1����2�� ����߼���ô�ģ� ����1��ֻҪoffset !=
	 *         0 �ж� y �����ƶ��� count++�� offset>0 firstItem--, �½������ͼ��curentItem���棬
	 *         firstItem = �½������ͼ�� index offset<0 firstItem = currentItem
	 *         ,�½�����ͼ��view�� currentItem ����
	 */
	private ItemsRange getItemsRange() {
		if (getItemHeight() == 0) {
			return null;
		}

		int first = currentItem;
		int count = 1;

		/*
		 * // 2015 8 12 ��ע�͵� while (count * getItemHeight() < getHeight()) {
		 * first--; count += 2; // top + bottom items } //Log.d(TAG,
		 * "getItemsRange:getHeight-->" + getHeight() + "--count-->" + count);
		 */

		if (scrollingOffset != 0) {
			if (scrollingOffset > 0) {
				first--;// �����ƶ���offset>0��first item ++�������ƶ� first���䣬
			}
			count++; // ֻҪoffset!=0����ͼ����ʾ��view ��+1��

			// process empty items above the first or below the second
			// ��� abs.offset ���� itemHeight��firstItem - emptyItems����range.first
			// (fling ������������)
			int emptyItems = scrollingOffset / getItemHeight(first);// int ������-2
																	// -1 0 1 2
																	// ������
			first -= emptyItems;
			count += Math.asin(emptyItems);
//Log.d(TAG, "getItemsRange:emptyItems-->" + emptyItems+ "--asin(emptyItems)-->" + Math.asin(emptyItems));
					

		}
//Log.d(TAG, "getItemsRange:currentItem-->" + currentItem + "--count-->"+ count + "--scrollingOffset-->" + scrollingOffset);
				

		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 * <p>
	 * itemLayout == null ����true
	 * <p>
	 * ÿ�ε�����ivalidate�����������rebuildItem�����ݵ�ǰ��currentItemֵȷ��firstItem
	 * ����Ҫ��ʾ��viewװ��itemLayout
	 * 
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {

		boolean updated = false;

		// ��ǰ��ͼ��Ӧ����ʾ��item��Χ������currentItem����ͨ��scrollingOffset΢��
		ItemsRange range = getItemsRange();

		if (itemsLayout != null) {
			/*
			 * ��itemLayout����range��Χ�ڵ�item���պ󣬲���itemLayout�����
			 * firstItem����һ�ε�ֵ��getItemsRange()�����У���offset currentItem
			 * ���¼������ƶ�֮����ʵ�� firstItem��range.first recycleItem(itemsLayout,
			 * firstItem, range)��ֻҪfirstItem����range�ڣ�����ֵ���� firstItem
			 */
//			Log.d(TAG, "rebuildItems:firstItem 1 -->" + firstItem);
			int first = recycle.recycleItems(itemsLayout, firstItem, range);
//			Log.d(TAG, "rebuildItems:first-->" + first);

			updated = firstItem != first;//

//			Log.d(TAG, "rebuildItems:updated-->" + updated);

			firstItem = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			/*
			 * �ƶ����벻����itemHeight���Ƿ���Ҫ rebuildItem ͨ�����������ж�
			 * 
			 * ֻҪ��һ�η����ƶ� updated ���᷵��true�� �ƶ������� ����false
			 * 
			 * �ж�ǰһ��firstItem �Ƿ񲻵��� �µ�range.first(������ true ������ false���� ���ֵ�����ͼ����
			 * �Ƿ񲻵��� ��range item ������ֻҪ�����ƶ� count = 2 ���� true��rebuild֮��
			 * itemsLayout.getChildCount()==2
			 */
//	Log.d(TAG,"rebuildItems:getChildCount-->"+ itemsLayout.getChildCount()+ "--range.getCount()-->" + range.getCount());
					
							
							

			updated = firstItem != range.getFirst()
					|| itemsLayout.getChildCount() != range.getCount();

//			Log.d(TAG, "rebuildItems:updated-->" + updated);
		}
		// �����ж��Ƿ���Ҫ������ͼ

		/*
		 * ȷ���µ�firstItem ͬʱ��itemsLayout�����view
		 */
		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {

			/*
			 * �����ƶ�firstItem ���ֲ��� �����ƶ�����һ�ε� firstItem
			 * ����range��Χ�ڣ���ʵ��first�Ѿ��仯����Ҫ�ڶ���ѹ���µ�item view��������firstItemֵ
			 * 
			 * ��ͼֻ��ʾ�� firstItem ~ firstItem + visibleItems,
			 * Ҫ��firstItem֮����Ӽ����������ͼ��������range.getFirst() -firstItem + 1;
			 */
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			// ��һ�ε� firstItem �Ѿ�����range ��Χ�ڣ�����Ϊ��ʵ�� firstItem
			// �������Σ�һ�����Ϲ���С�� item height ���룬firstItem ���䣬itemLayout������ ����view:
			// addViewItem(range.getlast(), false) ����true firstItem����
			// �������Ϲ��� ����itemHeight���룬�����Ƴ���ͼ��view�����沢�Ӳ�����ɾ���������е�view���� <
			// 7����ͼ����Ҫ��ʾ8�����¼����view��Ҫ��ӵ����ֵĵײ�
			// ���range.getCount()-itemsLayout.getChildCount()��
			// �������¹�����firstItem
			// ����range��itemLayout�е�ԭ����ͼ���Ƴ���caches����firstItem��ʼ���
			// range.getCount()����ͼ
			firstItem = range.getFirst();
		}

		int first = firstItem;
//Log.d(TAG, "rebuildItems:firstItem 2 -->" + firstItem);

		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false)
					&& itemsLayout.getChildCount() == 0) {
				first++;// adapter Ϊnull ���� adapter �󶨵�dataΪnull
						// ��itemLayoutû������ͼ �ų���
			}
		}
		//viewAdapter.setmLinearLayout(itemsLayout);
		
		firstItem = first;
//Log.d(TAG, "rebuildItems:firstItem -3->" + firstItem);
		// Log.d(TAG, "rebuildItems--updated-->" +
		// updated+"--firstItem-->"+firstItem);
		return updated;
	}

	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items
	 * sizes. rebuildItems() itemLayout �������µ�view�����ı䣬��Ҫ���¼���λ�ô�С��Ϣ
	 */
	private void updateView() {
		if (rebuildItems()) {
//Log.d(TAG, "updateView��getHeight()-->" + getHeight()+ "--getWidth()--" + getWidth());
					

			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			layout(getWidth(), getHeight());

//Log.d(TAG, "updateView��getHeight()-->" + getHeight()+ "itemLayout.height-->" + itemsLayout.getHeight());
					
		}
	}

	/**
	 * Creates item layouts if necessary, (current item layout is null)
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());// view.getContext() return view's context
			
			itemsLayout.setOrientation(LinearLayout.VERTICAL);
			//viewAdapter.setmLinearLayout(itemsLayout);
		}
	}

	/**
	 * <li>onMeasure()���ô˷�������currentItem and visibleItems ���㸸��ͼ������ͼ�ĸ�����
	 * <p>
	 * Builds view for measuring����ʼ������ͼ����,
	 * itemLayout�Ǹ���ͼ������visibleItems��item����ͼ,
	 * firstItem�����ϣ�currentItem���м䣬�����µ�����ͼ��ʼ����,
	 * <p>ѭ���ڵ��� addView��view��0) ��view�����ڵ�һ��λ�ã�ѭ������: i ֵ Ϊ firstItem�����һ�������ڶ��˵�view
	 * 
	 * <p> firstItem ~ firstItem + visibleItems ��ΪĿǰ��ͼ�ڵ�ȫ����ͼ,first��Ϊ������set�е�index
	 *
	 * <p>������ɣ�itemLayout �з�������Ӧ����ͼ����ͼ��������adapter���󶨵�set and index ����
	 * 
	 * <p>onMeasure()���ô˷�������currentItem and visibleItems ���㸸��ͼ������ͼ�ĸ������������firstItem��ֵ
	 * 
	 * 
	 */
	private void buildViewForMeasuring() {

//Log.d(TAG, "buildViewForMeasuring--firstItem-->" + firstItem);
		// clear all items if itemsLayout is not null
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		// add views �����������view
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
		//viewAdapter.setmLinearLayout(itemsLayout);
		
	}

	/**
	 * Adds item view to items layout;adapter Ϊnull ���� adapter �󶨵�dataΪnull
	 * �Ż᷵��false
	 * 
	 * @param index
	 *            the item index
	 * @param first
	 *            true :the flag indicates if view should be first,false view
	 *            add to the end of itemLayout
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		SolarView view = (SolarView) getItemView(index);
		if (view != null) {
			if (first) {
				itemsLayout.addView(view, 0);
			} else {
				itemsLayout.addView(view);
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks whether item index is valid
	 * <p>
	 * true: viewadapter != null and viewAdapter.getItemCount >0 and (isCyclic
	 * or (index>=0 and index<viewadapter.getItemCoun)
	 * 
	 * <p>
	 * ��������Ϊ����������Ŀ�������ǰ���£�ѭ��ģʽ�·���true����ѭ��ģʽ��index��0~ItemCount֮��
	 * 
	 * @param index
	 *            the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return viewAdapter != null
				&& viewAdapter.getItemsCount() > 0
				&& (isCyclic || index >= 0
						&& index < viewAdapter.getItemsCount());
	}

	/**
	 * �Ӱ󶨵�adapter��ȡ�� index����Ӧ��view
	 * 
	 * Returns view for specified item from adapter!
	 * <p>
	 * index in the bound : adapter.getItem��������item view()
	 * <p>
	 * out of bounds : empty view from adapter.getEmptyItem()
	 * <p>
	 * getItem getEmptyItem ����adapter��getview�����convert view ==
	 * null����newһ��textview or empty view
	 * 
	 * @param index
	 *            the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {

		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}

		int count = viewAdapter.getItemsCount();
		// index ��Ч������·���һ�� emptyItem View;
		if (!isValidItemIndex(index)) {
			return viewAdapter
					.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		// index ��Ч�᷵��null����������Ѿ���indexתΪ��Ч���룬return���᷵��null
		// ���յ�view recycle.getItem()
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}

}
