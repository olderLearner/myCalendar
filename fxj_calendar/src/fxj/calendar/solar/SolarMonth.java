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
	 * 2个月份高度不一致情况下，平滑过渡，需要继续优化
	 */
	private static final int MODIFY_HEIGHT_TIME = 280;
	private static final int MODIFY_HEIGHT_DELAY = 10;
	private static final int MODIFY_HEIGHT_STEP = 5;
	private static final int MODIFY_HEIGHT_FREQ = 20;
	private static final float SPEED = 0.25f;
	private int[] stepDistance = new int[10];
	private int mMatchCount = 0;
	private static final int MODIFY_HEIGHT = 0;
	
	/** 处理点击事件的  parameter	 */
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
	// view 的高度
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
	 * Scrollinglistener WheelScroller 定义的内部接口 public ScrollingListener,
	 * 将WheelScroller.ScrollingListener实例通过WheelScroller的构造方法传到到WheelScroller中
	 * 这个实例中的监听方法，只在WheelScroller中使用
	 */
	SolarMonthScroller.ScrollingListener scrollingListener = new SolarMonthScroller.ScrollingListener() {
		@Override
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			
			doScroll(distance);// distance 需要滚动的距离，根据距离计算currentItem，并重绘

			// 下面程序没有执行过 2015 8 2
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
		 *  WheelView 滚动标志设为 false，通知监听器滚轮停止，可以提取数据，清零 offset 重绘view.
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
			 *  使WheelMonth高度与currentItem高度保持一致 ， 动作生硬，需要优化
			 *  2015 8 13 动作平滑了一些，还是不理想,在 onJustify()中 调整
			 */
			invalidate();
		}

		@Override
		public void onJustify() {
			
			/*
			 * 2015 8 30 
			 * 初始化，currentItem = lastItem,
			 * 程序运行，每次 currentItem 因滚动而回调此方法时，若2个值不等，则表示月份改变，设定目标月份的1号为默认点击日期;
			 * 若目标月份是当月，则点击日期设定为今天，
			 * 最后将currentItem 同步给 lastItem;
			 */
			if (lastItem != currentItem) {
				SolarMonth.this.refreshClickedDayForMoving(currentItem);
				lastItem = currentItem;
			}
			
			/**
			 * 2015 8 31
			 * 此条件应对 move action 手指释放时，Math.abs(offset)大于指定值，则修正 currentItem 上边缘 位置，
			 * justifyScroll（）方法中，若SolarMonth高度与currentItem高度不一致，则调用 runnable 在滚动结束前修正高度
			 * 
			 * 对于指定确切的滚动距离，且滚动停止时 currentItem 的上边缘与view对齐，则此条件不成立
			 * fling 情况，因为scroller.fling()，不是精确的移动，所以设置标志位，若为fling模式下的 justify，不调用此方法
			 * 中的高度修正，fling 模式有专有的修正方法。
			 */
			if (Math.abs(scrollingOffset) > SolarMonthScroller.MIN_DELTA_FOR_SCROLLING) {
				
				//Log.d(TAG, "onJustify-->"+ scrollingOffset);
				if (Math.abs(scrollingOffset) <= 70) {
					SolarMonth.this.justifyScroll(scrollingOffset, 8*Math.abs(scrollingOffset));
				} else {
					SolarMonth.this.justifyScroll(scrollingOffset, 4*Math.abs(scrollingOffset));
				}
				
				/**
				 * 在这里面修改高度，非常柔和 2015 8 30
				 * 2015 8 31 修改；添加方法 justifyScroll() （滚动和修正高度做成一个方法）
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
	 *  handler 版本
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
					Log.d("Month", "执行了 msg 1");
					this.sendEmptyMessage(0);	
				}
 				
				break;
			}
			
		};
		
		
	};
	
	/**
	 * <li> 根据滚动的目标index，获得目标月份的 1号time对象，赋值到 application中，（若目标月份是当前月，则日期选定为今天，
	 * MethodUtil.indexToTime(index) 处理逻辑
	 * <li> 调用adapter的更新click方法，重置 mRealClickedDay lastClickedDay，并通知监听器，日期改变
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
//Log.d(TAG, "setViewAdapter::itemsLayout::"+itemsLayout.getChildCount());// 这个时候是空指针错误
		if (itemsLayout != null) {// 这个时候itemsLayout 是 null
			
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
	 * 计算每步移动的距离，调用 runnable 柔和修正高度
	 * @param diff 转换月份的高度差
	 * @param delayMillis 延时时间
	 */
	private void doModifyHeight(int diff, int delayMillis) {
		
		stepDistance = MethodUtil.smoothModifyHeight(diff, MODIFY_HEIGHT_TIME, MODIFY_HEIGHT_STEP);
		SolarMonth.this.postDelayed(mModifyHeight, delayMillis);
	}
	
	
	/**
	 * justify 方法调用，
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
	 *  fling 手势高度修正，SolarMonthScroller 中判定手势为fling时，调用此方法，传递当前item，目标item，
	 *  并且将 fling_Flag 置为 true（onFinish() 中复位），这样由于scroller.fling();方法所导致的offset微小偏移，
	 *  不会执行 justifyScroll（）中的修正高度方法块；
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
	 * setCurrentItem 有4个重载的方法
	 */
	
	/**
	 * 更新currentItem,调用滚动方法，加速-减速移动到目标位置
	 * <li> 调用条件：点击主界面 today 按钮，若当前界面显示月份与 today所在月份差 5个月，则滚动至今天所在的月份
	 * <li> 这样指定item 是错误的
	 * @param item 
	 * 				滚动之后，月份的 index 值
	 * @param distance
	 * 				滚动的距离
	 * @param millis
	 * 				滚动耗时
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
	 * 更新currentItem,调用滚动方法，加速-减速移动到目标位置，不设定currentItem，SolarMonth.doScroll(),
	 * 在滚动中更新currentItem，
	 * <li> 调用条件：点击主界面 today 按钮，若当前界面显示月份与 today所在月份差 4 个月，则滚动至今天所在的月份
	 * @param mNew
	 * 				滚动之后，月份的 index 值
	 * @param mOld
	 * 				滚动时，月份 index 值
	 * @param distance		滚动的距离
	 * @param millis		滚动耗时
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
	 * 重载方法，非滚动模式
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
					 * 可循环状态下-itemCount<index<0,
					 * 选择的实际index=item - |index|
					 */
					index += itemCount;  
				}
				/*
				 * if (index<0) { index=itemCount - Math.abs(index)%itemCount; }
				 * else { index %= itemCount; }
				 */
				index %= itemCount; // index= index%itemCount
			} else {
				// 不循环的情况下 return
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {// 滚动模式

				int itemsToScroll = index - currentItem;// 当前item与目标item之差

				/*
				 * 从哪个方向滚最快 向上or向下。 因为是循环滚动，当前位置到目标位置存在2个滚动方向，差值分别为： a =
				 * abs(index - currentItem); b = itemCount - abs(index -
				 * currentItem) a < b 时，按 a 的数目滚动，方向和大小 即为 index -
				 * currenItem，正向上 index>itemCount, a > b 时，按 b 的数目滚动，方向与index -
				 * currentItem相反
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
				scrollingOffset = 0; // 清零
				int old = currentItem;
				currentItem = index; // 更新currentItem ondraw中使用新的currentItem 重画
				notifyChangingListeners(old, currentItem);
				invalidate();// 直接调用重绘
			}
		}
	}
	
	
	/**
	 * 由变换的index 计算 SolarMonth 的高度差值，若前后height 一致，设置新的 currentItem,调用重绘，
	 * 否则，设置新的 currentItem,调用重绘，并且平滑修改 SolarMonth的高度。
	 * <li> 调用条件：主界面点击 today，若界面显示月份与 今天所在月份相距大于 4，则调用此方法
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
	 * 由lunar 界面转变为 solar 界面
	 * <li> 貌似是直接重绘，这个方法貌似没使用，MonthActivity 中 直接设置 currentItem 和 view的 LayoutParams
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
	 * Sets the drawable for the wheel foreground item上下的两条线
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
	 * clearCaches true 清空recycle itemsLayout 中所有view
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
	 * Initializes resources 设置背景色
	 * 
	 */
	private void initResourcesIfNecessary() {
		setBackgroundResource(wheelBackground);
	}

	/**
	 * Calculates desired height for layout 因为wheel month
	 * 静止状态只有一个view，所以itemLayout中只有一个子view。 此时更新的itemHeight是当前显示的子view的高度:
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
	 * item的高度，三种方法： 成员变量；从布局文件中得到item的高度；view的高度/显示的item数量
	 * 
	 * 2015 需要重新设计，height值 分为3种情况 1）upItemHeight 2) currentItemHeight 3)
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
	 * 由index值计算，monthView的高度
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
	 * 应该改为计算高度，宽度 match parent
	 * 
	 * 
	 * Calculates control width and creates text layouts
	 * 
	 * @param widthSize
	 *            -->the input layout width
	 * @param mode
	 *            -->the layout
	 *            mode：MeasureSpec.EXACTLY；MeasureSpec.AT_MOST；MeasureSpec
	 *            .UNSPECIFIED
	 * @return width -->the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {

		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		// MeasureSpec 以int形式存储 ，实际测量在onMeasure中
		// 使用实参，创建 widthMearsureSpec
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

			// at_most mode 最大不能超过
			// widthSize，如果width大于指定的widthSize，则width为widthSize
			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}

		// 宽度被限制为 exactly
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

		// 向itemLayout中装入view
		buildViewForMeasuring();
		// 宽度设为exactly
		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);// 计算布局中所有view高度

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
	 * r-l 宽
	 * b-t 高
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

		// 给itemLayout中items分配的空间
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
	 * draw 的方式是在 canvas上画layout，所以要将canvas的原点与itemLayout原点重合，
	 * 默认情况下canvas与父视图（WheelView）重合， 发生移动时，itemLayout上部超出了父视图
	 * <li>scroll 模式：向下 offset>0,top = 37 canvas 移位 -top + offset即为将进入视图的item的顶点
	 * y坐标
	 * <li>向上 offset <0 top = 0; canvas 移动
	 * offset，即为当前firstItem的顶点y坐标（父视图只显示下面部分）
	 * 
	 * 2015 8 11 画布位置移动需要重新计算逻辑，因为每个itemHeight不一样，
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

		// 在画布上绘制这个view，linearLayout 和布局中的子view
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
	 * 先不管用处了
	 */
	public boolean performClick() {
		return super.performClick();
	}
	
	
	@Override
	/**
	 * ACTION_DOWN ACTION_MOVE ACTION_UP
	 * 将Event传递到WheelScroller中处理，
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
        	//getParent().requestDisallowInterceptTouchEvent(true);// 禁止父视图拦截事件
			break;
		case MotionEvent.ACTION_UP:
			float delayup = System.currentTimeMillis() - mClickedTime;
//Log.d(TAG, "----interval time between down and up ===="+delayup);
        	scroller.onTouchEvent(event);
        	
			if (!isScrollingPerformed) {
				// 这个程序段没有被执行过！ 我认为 2015 8 1
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
				 * 标志位置位时间180s，
				 * action_up 取消消息
				 */
				mClickedFlag = true;
				itemsLayout.dispatchTouchEvent(mCurrentDownEvent);
				break;
			case RUSH_CLICK:
				/*
				 * 100ms 没有move event 就执行，
				 * action_up,不取消
				 */
				itemsLayout.dispatchTouchEvent(mCurrentDownEvent);
				break;
			default:
				throw new RuntimeException("Unknown  message " + msg);
			}
			
		};
		
	};
	
	
	/**
	 * Scrolls the wheel,根据滚动的距离和正负值 ：确定滚动后的 currentItem 和需要滚动的item
	 * 个数，调用setCurrentItem（）方法（内部调用 invalidate()）;
	 * <p>
	 * if delta < 0 item index up; else item index down;
	 * 
	 * @param delta
	 *            the scrolling value
	 */
	private void doScroll(int delta) {

		// delta是每次move的间隔距离，offset是这次move动作从down开始到现在相对原点的总偏移量，每次移动delta，offset +=delta
		scrollingOffset += delta;

		// int itemHeight = getItemHeight();
		// 2015 8 12 修改参考高度
		int itemHeight = getItemHeight(firstItem);

		// 需要移动的 item数量，负值：currentItem 增加；正值：currentItem 减小
		int count = scrollingOffset / itemHeight;

		/*
		 * count<0 滚轮上滑 current item ++ ；count>0 滚轮下滑 current item-- 当前的位置-
		 * 移动的数量=目标位置 （index） pos 即为移动后的currentItem 上滑，count负值，相减item index 增加，
		 * 下滑 ，相反
		 */
		int pos = currentItem - count;

		int itemCount = viewAdapter.getItemsCount();// data set 中的item 总数

		// 距离补偿，移动距离不能被itemHeigh整除时，余数小于itemHeigh一半时，fixPos=0，不需要修改pos位置
		int fixPos = scrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}

		/**
		 * 余数大于firstItemHeight一半，currentItem改变， 数值有offset符号确定，移动的count数量+1（count
		 * 正 count++ 负 count--）
		 */
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) { // fixPos > itemHeight/2 滚轮向下，偏移量大于item高度的一半，则
								// currentItem -- count++
				pos--; // currentItem-- ，滑动数量++ count正，所以count++
				count++;
			} else if (fixPos < 0) {
				pos++; // currentItem++
				count--; // offset是负值的前提下，count也是负值，滑动数量+1，则count--；
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount; // 循环模式下，index负值表示的数据是：index + itemCount ;
									// 0-12 -1 表 12=13+ -1
				// 此while循环可改为 pos = itemCount - math.abd(pos % itemCount)
			}
			pos %= itemCount; // 确保 pos 在itemCount内(pos 是正数，且大于itemCount)

		} else { // 非循环模式下
			//
			if (pos < 0) {// index < 0 ，则将 index = 0（set
							// 第一个元素移至中间，移动数量为currentItem个
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {// index超出 set范围，则将最后一个元素（itemCount -
											// 1)放置在中间，移动数量为负值：-(itemCoun -
											// currentItem) + 1;
				count = currentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {// 同循环模式
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) { // 同循环模式
				pos++;
				count--;
			}
		}

		int offset = scrollingOffset;

		if (pos != currentItem) {
			// set new currentItem ,此方法内部调用了invalidate();
			setCurrentItem(pos, false);
		} else {
			invalidate(); // currentItem未改变，直接调用重绘
		}

		// update offset
		/*
		 * 翻转offset,一旦移动大于 itemHeight/2 offset >0 count = 1 (down，手指向下) offset <
		 * 0 count = -1(up,手指向上)
		 * 
		 * offset - count * itemHeight ： 负值 (down) 此时继续向同方向移动，每次加入的delta
		 * 是正值，offset+ delta 慢慢接近0 currentItem顶部与view顶部重合
		 * 正值(up)，offset是firstItem应在view中的高度，每次移动的delta值为负，offset（+） + delta(-)
		 * = firstItem在view中显示的高度 -firstItemHeight + offset 就是画布顶点的位置
		 */
		scrollingOffset = offset - count * itemHeight;

		// fling 情况
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	/**
	 * Scroll the wheel 滚到到目标位置,参数是item个数
	 * 
	 * @param itemsToSkip
	 *            items to scroll
	 * @param time
	 *            scrolling duration
	 * 
	 *            2015 8 11 需要的修改，ItemHeight 根据Index值计算 最好编一个类（其实就是一个函数）
	 *            itemToScroll 需要滚动的item数目，move fling 都为1 点击今天，当前位置与今天位置之差 （大于5
	 *            setcurentItem，小于5加入滚动时间）
	 * 
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items,
	 * <p>
	 * 由currentItem and offset 计算当前应该显示的第一个 item 的 index：first; 应该显示的
	 * item数量：count，
	 * <Li>构造ItemRange 参数为：first count
	 * 
	 * @return the items range
	 * 
	 *         2015 8 15 对于 monthWheel ：count的值只能为1或者2， 这个逻辑怎么改？ 测试1：只要offset !=
	 *         0 判定 y 方向移动， count++， offset>0 firstItem--, 新进入的视图在curentItem上面，
	 *         firstItem = 新进入的视图的 index offset<0 firstItem = currentItem
	 *         ,新进入视图的view在 currentItem 下面
	 */
	private ItemsRange getItemsRange() {
		if (getItemHeight() == 0) {
			return null;
		}

		int first = currentItem;
		int count = 1;

		/*
		 * // 2015 8 12 先注释掉 while (count * getItemHeight() < getHeight()) {
		 * first--; count += 2; // top + bottom items } //Log.d(TAG,
		 * "getItemsRange:getHeight-->" + getHeight() + "--count-->" + count);
		 */

		if (scrollingOffset != 0) {
			if (scrollingOffset > 0) {
				first--;// 向下移动，offset>0，first item ++；向上移动 first不变，
			}
			count++; // 只要offset!=0，视图内显示的view 就+1；

			// process empty items above the first or below the second
			// 如果 abs.offset 大于 itemHeight，firstItem - emptyItems，得range.first
			// (fling 会出现这种情况)
			int emptyItems = scrollingOffset / getItemHeight(first);// int 。。。-2
																	// -1 0 1 2
																	// 。。。
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
	 * itemLayout == null 返回true
	 * <p>
	 * 每次调用用ivalidate（）都会调用rebuildItem，根据当前的currentItem值确定firstItem
	 * 将需要显示的view装入itemLayout
	 * 
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {

		boolean updated = false;

		// 当前视图中应该显示的item范围，参数currentItem，并通过scrollingOffset微调
		ItemsRange range = getItemsRange();

		if (itemsLayout != null) {
			/*
			 * 将itemLayout不在range范围内的item回收后，并从itemLayout中清除
			 * firstItem是上一次的值，getItemsRange()方法中，由offset currentItem
			 * 重新计算了移动之后真实的 firstItem：range.first recycleItem(itemsLayout,
			 * firstItem, range)，只要firstItem还在range内，返回值就是 firstItem
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
			 * 移动距离不超过itemHeight，是否需要 rebuildItem 通过下面条件判定
			 * 
			 * 只要第一次发生移动 updated 都会返回true， 移动过程中 返回false
			 * 
			 * 判断前一次firstItem 是否不等于 新的range.first(向下移 true ，向上 false）； 布局的子视图个数
			 * 是否不等于 新range item 个数（只要发生移动 count = 2 返回 true）rebuild之后
			 * itemsLayout.getChildCount()==2
			 */
//	Log.d(TAG,"rebuildItems:getChildCount-->"+ itemsLayout.getChildCount()+ "--range.getCount()-->" + range.getCount());
					
							
							

			updated = firstItem != range.getFirst()
					|| itemsLayout.getChildCount() != range.getCount();

//			Log.d(TAG, "rebuildItems:updated-->" + updated);
		}
		// 以上判断是否需要更新视图

		/*
		 * 确定新的firstItem 同时向itemsLayout中添加view
		 */
		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {

			/*
			 * 向上移动firstItem 保持不变 向下移动：上一次的 firstItem
			 * 还在range范围内，真实的first已经变化，需要在顶部压入新的item view，并更新firstItem值
			 * 
			 * 视图只显示了 firstItem ~ firstItem + visibleItems,
			 * 要在firstItem之上添加即将进入的视图，数量：range.getFirst() -firstItem + 1;
			 */
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			// 上一次的 firstItem 已经不在range 范围内，更新为真实的 firstItem
			// 两种情形：一）向上滚动小于 item height 距离，firstItem 不变，itemLayout在最下 加入view:
			// addViewItem(range.getlast(), false) 返回true firstItem不变
			// 二）向上滚动 大于itemHeight距离，由于移出视图的view被缓存并从布局中删除，布局中的view个数 <
			// 7，视图内需要显示8个，新加入的view需要添加到布局的底部
			// 添加range.getCount()-itemsLayout.getChildCount()个
			// 三）向下滚动，firstItem
			// 超出range，itemLayout中的原有视图都移除并caches，从firstItem开始添加
			// range.getCount()个视图
			firstItem = range.getFirst();
		}

		int first = firstItem;
//Log.d(TAG, "rebuildItems:firstItem 2 -->" + firstItem);

		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false)
					&& itemsLayout.getChildCount() == 0) {
				first++;// adapter 为null 或者 adapter 绑定的data为null
						// 且itemLayout没有子视图 才成立
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
	 * sizes. rebuildItems() itemLayout 加入了新的view发生改变，需要重新计算位置大小信息
	 */
	private void updateView() {
		if (rebuildItems()) {
//Log.d(TAG, "updateView：getHeight()-->" + getHeight()+ "--getWidth()--" + getWidth());
					

			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			layout(getWidth(), getHeight());

//Log.d(TAG, "updateView：getHeight()-->" + getHeight()+ "itemLayout.height-->" + itemsLayout.getHeight());
					
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
	 * <li>onMeasure()调用此方法，由currentItem and visibleItems 计算父视图中子视图的个数，
	 * <p>
	 * Builds view for measuring；初始化父视图调用,
	 * itemLayout是父视图：放置visibleItems个item子视图,
	 * firstItem在最上，currentItem在中间，从最下的子视图开始放置,
	 * <p>循环内调用 addView（view，0) 将view放置在第一个位置，循环结束: i 值 为 firstItem，最后一个放置在顶端的view
	 * 
	 * <p> firstItem ~ firstItem + visibleItems 即为目前视图内的全部视图,first即为日期在set中的index
	 *
	 * <p>程序完成：itemLayout 中放置了相应的视图，视图的内容由adapter所绑定的set and index 决定
	 * 
	 * <p>onMeasure()调用此方法，由currentItem and visibleItems 计算父视图中子视图的个数，并计算出firstItem的值
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

		// add views 从下向上添加view
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
		//viewAdapter.setmLinearLayout(itemsLayout);
		
	}

	/**
	 * Adds item view to items layout;adapter 为null 或者 adapter 绑定的data为null
	 * 才会返回false
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
	 * 适配器不为空且数据数目大于零的前提下，循环模式下返回true；非循环模式下index在0~ItemCount之间
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
	 * 从绑定的adapter中取出 index所对应的view
	 * 
	 * Returns view for specified item from adapter!
	 * <p>
	 * index in the bound : adapter.getItem方法返回item view()
	 * <p>
	 * out of bounds : empty view from adapter.getEmptyItem()
	 * <p>
	 * getItem getEmptyItem 类似adapter的getview，如果convert view ==
	 * null，则new一个textview or empty view
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
		// index 无效的情况下返回一个 emptyItem View;
		if (!isValidItemIndex(index)) {
			return viewAdapter
					.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		// index 无效会返回null，上面代码已经将index转为有效代码，return不会返回null
		// 回收的view recycle.getItem()
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}

}
