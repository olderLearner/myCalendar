/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 * 
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *  
 *  http://www.it165.net/pro/html/201503/36722.html
 */

package fxj.calendar.datepicker;

import java.util.LinkedList;
import java.util.List;



import fxj.calendar.R;
import fxj.calendar.datepicker.adapter.WheelViewAdapter;


import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * Numeric wheel view.
 * 
 * @author Yuri Kanivets
 */
public class WheelView extends View {


	
	private static final String TAG = "WheelView";
	
	
	
	/** Top and bottom shadows colors */
	/*/ Modified by wulianghuan 2014-11-25
	private int[] SHADOWS_COLORS = new int[] { 0xFF111111,
			0x00AAAAAA, 0x00AAAAAA };
	//*/
	private int[] SHADOWS_COLORS = new int[] {0xefE9E9E9, 0x4fE9E9E9 };

	/** Top and bottom items offset (to hide that) */
	private static final int ITEM_OFFSET_PERCENT = 0;

	/** Left and right padding value */
	private static final int PADDING = 10;

	/** Default count of visible items */
	private static final int DEF_VISIBLE_ITEMS = 5;

	// Wheel Values
	private int currentItem = 0;

	// Count of visible items
	private int visibleItems = DEF_VISIBLE_ITEMS;

	// Item height
	private int itemHeight = 0;

	// Center Line
	private Drawable centerDrawable;

	// Wheel drawables
	private int wheelBackground = R.drawable.wheel_bg;
	private int wheelForeground = R.drawable.wheel_val;

	// Shadows drawables
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	// Draw Shadows
	private boolean drawShadows = true;

	// Scrolling
	private WheelScroller scroller;
	private boolean isScrollingPerformed;
	private int scrollingOffset;

	// Cyclic
	boolean isCyclic = false;

	// Items layout
	private LinearLayout itemsLayout;

	// The number of first item in layout
	private int firstItem;

	// View adapter
	private WheelViewAdapter viewAdapter;

	// Recycle
	private WheelRecycle recycle = new WheelRecycle(this);

	// Listeners
	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
	private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();
	
	String label="";

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	/**
	 * Initializes class data
	 * @param context the context
	 */
	private void initData(Context context) {
		scroller = new WheelScroller(getContext(), scrollingListener);
	}

	/*
	 *  Scrollinglistener WheelScroller 定义的内部接口 public ScrollingListener,
	 *  将WheelScroller.ScrollingListener实例通过WheelScroller的构造方法传到到WheelScroller中
	 *  这个实例中的监听方法，只在WheelScroller中使用
	 */
	WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
		@Override
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			// distance 需要滚动的距离，根据距离计算currentItem，并重绘
			doScroll(distance);

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

			scrollingOffset = 0;
			invalidate();
		}

		@Override
		public void onJustify() {
			if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
				Log.d(TAG, "修正偏移量：scrollingOffset-->"+ scrollingOffset);
				scroller.scroll(scrollingOffset, 0);
			}
		}
	};

	/**
	 * Set the the specified scrolling interpolator
	 * @param interpolator the interpolator
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
	 * Sets the desired count of visible items.
	 * Actual amount of visible items depends on wheel layout parameters.
	 * To apply changes and rebuild view call measure().
	 * 
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * Gets view adapter
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter() {
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
	 * Sets view adapter. Usually new adapters contain different views, so
	 * it needs to rebuild view by calling measure().
	 * 
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter) {
		if (this.viewAdapter != null) {
			this.viewAdapter.unregisterDataSetObserver(dataObserver);
		}
		this.viewAdapter = viewAdapter;
		if (this.viewAdapter != null) {
			this.viewAdapter.registerDataSetObserver(dataObserver);
		}

		invalidateWheel(true);
	}

	/**
	 * Adds wheel changing listener
	 * @param listener the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 * @param listener the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 * @param listener the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * @param listener the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Adds wheel clicking listener
	 * @param listener the listener
	 */
	public void addClickingListener(OnWheelClickedListener listener) {
		clickingListeners.add(listener);
	}

	/**
	 * Removes wheel clicking listener
	 * @param listener the listener
	 */
	public void removeClickingListener(OnWheelClickedListener listener) {
		clickingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about clicking
	 */
	protected void notifyClickListenersAboutClick(int item) {
		for (OnWheelClickedListener listener : clickingListeners) {
			listener.onItemClicked(this, item);
		}
	}

	/**
	 * Gets current value
	 * 
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 * 
	 * @param index the item index
	 * @param animated the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return; // throw?
		}

		int itemCount = viewAdapter.getItemsCount();

		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					index += itemCount; // 可循环状态下 -itemCount<index<0,选择的实际index=item - |index|
				}
				/*
				if (index<0) {
					index=itemCount - Math.abs(index)%itemCount;
				} else {
					index %= itemCount;
				}*/
				index %= itemCount; // index= index%itemCount
			} else{
				// 不循环的情况下 return
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {// 从当前位置滚动到目标位置
				
				int itemsToScroll = index - currentItem;// 当前item与目标item之差  
				
				/*
				 *  从哪个方向滚最快  向上or向下。
				 *  因为是循环滚动，当前位置到目标位置存在2个滚动方向，差值分别为：
				 *  a = abs(index - currentItem); b = itemCount - abs(index - currentItem)
				 *  a < b 时，按 a 的数目滚动，方向和大小 即为 index - currenItem，正向上 index>itemCount,
				 *  a > b 时，按 b 的数目滚动，方向与index - currentItem相反
				 */
				if (isCyclic) {
					int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
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

				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * 重载方法，滚动标志位：false
	 * @param index the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * @param isCyclic the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}

	/**
	 * Determine whether shadows are drawn
	 * ondraw()方法的drawShadow标志位
	 * @return true is shadows are drawn
	 */
	public boolean drawShadows() {
		return drawShadows;
	}

	/**
	 * Set whether shadows should be drawn
	 * ondraw()方法的drawShadow标志位
	 * @param drawShadows flag as true or false
	 */
	public void setDrawShadows(boolean drawShadows) {
		this.drawShadows = drawShadows;
	}

	/**
	 * Set the shadow gradient color
	 * 阴影的模糊梯度，自定义模式
	 * @param start
	 * @param middle
	 * @param end
	 */
	public void setShadowColor(int start, int middle, int end) {
		SHADOWS_COLORS = new int[] {start, middle, end};
	}

	/**
	 * Sets the drawable for the wheel background
	 * @param resource
	 */
	public void setWheelBackground(int resource) {
		wheelBackground = resource;
		setBackgroundResource(wheelBackground);
	}

	/**
	 * Sets the drawable for the wheel foreground
	 * item上下的两条线
	 * @param resource
	 */
	public void setWheelForeground(int resource) {
		wheelForeground = resource;
		centerDrawable = getContext().getResources().getDrawable(wheelForeground);
	}

	/**
	 * Invalidates wheel,清空caches and 清空itemsLayout 中所有小view
	 * @param clearCaches if true then cached views will be clear
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
	 * Initializes resources
	 * 前景色，上部渐变，下部渐变，设置背景色
	 * 
	 */
	private void initResourcesIfNecessary() {
		if (centerDrawable == null) {
			centerDrawable = getContext().getResources().getDrawable(wheelForeground);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
		}

		setBackgroundResource(wheelBackground);
	}

	/**
	 * Calculates desired height for layout
	 * 
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemHeight = layout.getChildAt(0).getMeasuredHeight();
		}

		int desired = itemHeight * visibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumHeight());
	}

	/**
	 * Returns height of wheel item,
	 * <p>item的高度，三种方法： 成员变量；从布局文件中得到item的高度；view的高度/显示的item数量
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
	 * Calculates control width and creates text layouts
	 * @param widthSize -->the input layout width
	 * @param mode -->the layout mode：MeasureSpec.EXACTLY；MeasureSpec.AT_MOST；MeasureSpec.UNSPECIFIED
	 * @return width -->the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		// MeasureSpec 以int形式存储 ，实际测量在onMeasure中
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
Log.d(TAG, "calculateLayoutWidth--measure--first");
		int width = itemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());
			
			// at_most mode 最大不能超过 widthSize
			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}
		
		// 宽度被限制为 exactly 
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
Log.d(TAG, "calculateLayoutWidth--measure--second");
		
		return width;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		Log.d(TAG, "onMeasure----");
		
		buildViewForMeasuring();

		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		setMeasuredDimension(width, height);
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
	 * @param width the layout width
	 * @param height the layout height
	 */
	private void layout(int width, int height) {
		int itemsWidth = width - 2 * PADDING;

		itemsLayout.layout(0, 0, itemsWidth, height); // viewgroup
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
			updateView();

			drawItems(canvas);
			drawCenterRect(canvas);
		}

		if (drawShadows) drawShadows(canvas);
	}

	/**
	 * Draws shadows on top and bottom of control
	 * @param canvas the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		/*/ Modified by wulianghuan 2014-11-25
		int height = (int)(1.5 * getItemHeight());
		//*/
		int height = (int)(2 * getItemHeight());
		//*/
		topShadow.setBounds(0, 0, getWidth(), height);
		topShadow.draw(canvas);

		bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * Draws items
	 * <p> draw 的方式是在 canvas上画layout，所以要将canvas的原点与itemLayout原点重合，默认情况下canvas与父视图重合
	 * 发生移动时，itemLayout上部超出了父视图
	 * <li> scroll 模式：向下 offset>0,top = 37 canvas 移位 -top + offset即为将进入视图的item的顶点 y坐标
	 * <li> 向上 offset <0 top = 0; canvas 移动 offset，即为当前firstItem的顶点y坐标（父视图只显示下面部分）
	 * @param canvas the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		
		canvas.save();

		int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
		Log.d(TAG, "drawItems:top-->"+ top+ "--scrollingOffset-->" + scrollingOffset);
		canvas.translate(PADDING, - top + scrollingOffset);
		
		// 在画布上绘制这个view
		itemsLayout.draw(canvas);

		canvas.restore();
	}

	/**
	 * Draws rect for current value
	 * @param canvas the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = (int) (getItemHeight() / 2 * 1.2);
		/*/ Remarked by wulianghuan 2014-11-27  使用自己的画线，而不是描边
		Rect rect = new Rect(left, top, right, bottom)
		centerDrawable.setBounds(bounds)
		centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
		centerDrawable.draw(canvas);
		//*/
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(R.color.province_line_borde));
		paint.setColor(Color.RED);
		// 设置线宽
		paint.setStrokeWidth((float) 2);
		// 绘制上边直线
		canvas.drawLine(0, center - offset, getWidth(), center - offset, paint);
		// 绘制下边直线
		canvas.drawLine(0, center + offset, getWidth(), center + offset, paint);
		//*/
	}

	@Override
	/**
	 * ACTION_DOWN ACTION_MOVE ACTION_UP
	 * 将Event传递到WheelScroller中处理，
	 * 
	 */
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (getParent() != null) {
					getParent().requestDisallowInterceptTouchEvent(true);// 停止监听touchevnt
				}
				break;

			case MotionEvent.ACTION_UP:
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

		return scroller.onTouchEvent(event);
	}

	/**
	 * Scrolls the wheel,根据滚动的距离和正负值 ：确定滚动后的 currentItem 和需要滚动的item 个数，调用setCurrentItem（）方法（内部调用 invalidate()）;
	 * <p> if delta < 0 item index up;  else item index down;
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta) {
		// delta 是每次move的距离，offset移动的增量在清零前，进入此方法offset要加上这次的移动增量
		
		Log.d(TAG, "doScroll():scrollingOffset-->"+ scrollingOffset + "--delta-->"+ delta);
		scrollingOffset += delta;

		Log.d(TAG, "doScroll():scrollingOffset-->"+ scrollingOffset);
		
		int itemHeight = getItemHeight();
		
		int count = scrollingOffset / itemHeight;// 需要移动的 item数量，负值：currentItem 增加；正直：currentItem 减小
		
		// count<0  滚轮上滑 item ++ ；count>0  滚轮下滑 item--
		int pos = currentItem - count;// 当前的位置- 移动的数量=目标位置 （index） pos 即为移动后的currentItem
		
		int itemCount = viewAdapter.getItemsCount();// data set 中的item 总数

		// 距离补偿，移动距离不能被itemHeigh整除时，余数小于itemHeigh一半时，fixPos=0，不需要修改pos位置
		int fixPos = scrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}
			
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) { // fixPos > itemHeight/2 滚轮向下，偏移量大于item高度的一半，则 currentItem -- count++
				pos--; // currentItem-- ，滑动数量++ count正，所以count++
				count++;
			} else if (fixPos < 0) {
				pos++;	// currentItem++
				count--; // offset是负值的前提下，count也是负值，滑动数量+1，则count--；
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount; // 循环模式下，index负值表示的数据是：index + itemCount ; 0-12 -1 表 12=13+ -1
				// 此while循环可改为 pos = itemCount - math.abd(pos % itemCount)
			}
			pos %= itemCount; // 确保 pos 在itemCount内(pos 是正数，且大于itemCount)
			
		} else { // 非循环模式下
			//
			if (pos < 0) {// index < 0 ，则将 index = 0（set 第一个元素移至中间，移动数量为currentItem个
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {// index超出 set范围，则将最后一个元素（itemCount - 1)放置在中间，移动数量为负值：-(itemCoun - currentItem) + 1;
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
			setCurrentItem(pos, false);// 此方法内部调用了invalidate();
		} else {
			invalidate(); // 当前位置 即为 pos，直接调用重绘
		}

		// update offset
		/*
		 * 翻转offset,一旦移动大于  itemHeight/2 count = 1 (down) -1(up) 
		 * offset 更新：offset - count * itemHeight ： 负值 (down) 正值(up)
		 */
		scrollingOffset = offset - count * itemHeight;
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	/**
	 * Scroll the wheel 滚到到目标位置,参数是item个数
	 * @param itemsToSkip items to scroll
	 * @param time scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items,
	 * <p>由currentItem offset 计算当前应该显示的第一个 item 的 index：first; 应该显示的 item数量：count，
	 * <Li> 构造ItemRange 参数为：first count
	 * @return the items range
	 */
	private ItemsRange getItemsRange() {
		if (getItemHeight() == 0) {
			return null;
		}

		int first = currentItem;
		int count = 1;

		while (count * getItemHeight() < getHeight()) {
			first--;
			count += 2; // top + bottom items
		}
		//Log.d(TAG, "getItemsRange:getHeight-->" + getHeight() + "--count-->" + count);
		
		
		if (scrollingOffset != 0) {			
			if (scrollingOffset > 0) {
				first--;// 向下移动，offset>0，first item ++；向上移动 first不变，
			}
			count++; // 只要offset!=0，视图内显示的view 就+1；

			// process empty items above the first or below the second
			// 如果 abs.offset 大于 itemHeight，firstItem - emptyItems，得range.first (fling 会出现这种情况)
			int emptyItems = scrollingOffset / getItemHeight();// int  。。。-2 -1 0 1 2 。。。 
			first -= emptyItems;
			count += Math.asin(emptyItems);
			Log.d(TAG, "getItemsRange:emptyItems-->" + emptyItems + "--asin(emptyItems)-->" + Math.asin(emptyItems));
			
		}
		Log.d(TAG, "getItemsRange:currentItem-->" + currentItem + "--count-->" + count+ "--scrollingOffset-->"+ scrollingOffset);		
		
		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 * <p> itemLayout == null 返回true
	 * <p> 每次调用用ivalidate（）都会调用rebuildItem，根据当前的currentItem值确定firstItem
	 * 将需要显示的view装入itemLayout
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		
		//Log.d(TAG, "rebuildItems");
		
		boolean updated = false;
		// 当前视图中应该显示的item范围，参数currentItem，并通过scrollingOffset微调
		ItemsRange range = getItemsRange();
		
		if (itemsLayout != null) {			
			/*
			 *  将itemLayout不在range范围内的item回收后，并从itemLayout中清除
			 *  firstItem是上一次的值，getItemsRange()方法中，由offset currentItem 重新计算了移动之后真实的 firstItem：range.first
			 *  recycleItem(itemsLayout, firstItem, range)，只要firstItem还在range内，返回值就是 firstItem
			 *  
			 *  
			 */			
			Log.d(TAG, "rebuildItems:firstItem 1 -->" + firstItem);
			int first = recycle.recycleItems(itemsLayout, firstItem, range);
			Log.d(TAG, "rebuildItems:first-->" + first);
			updated = firstItem != first;// 
			
			Log.d(TAG, "rebuildItems:updated-->" + updated);
			
			firstItem = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) { // 对于移动不超过itemHeight的移动，是否需要 rebuildItem 通过下面条件判定
			// 判断前一次firstItem 是否不等于 新的range.first(向下移 true ，向上 false）；布局的子视图个数 是否不等于 新range item 个数（只要发生移动 count = 8 返回 true） 
			Log.d(TAG, "rebuildItems:getChildCount-->" + itemsLayout.getChildCount()+"--range.getCount()-->" + range.getCount());
			
			updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
			
			Log.d(TAG, "rebuildItems:updated-->" + updated);
		}

		
		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
			
			// 向下移动：上一次的 firstItem 还在range范围内，真实的first已经变化，需要在顶部加入item view
			// 视图只显示了 firstItem - firstItem + visibleItems,所以要在firstItem之上添加即将进入的视图，数量：range.getFirst() -firstItem + 1;
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			// 上一次的 firstItem 已经不在range 范围内，更新为真实的 firstItem
			// 两种情形：一）向上滚动小于 item height 距离 firstItem 不变，itemLayout在最下 加入view: addViewItem(range.getlast(), false) 返回true firstItem不变
			// 二）向上滚动  大于itemHeight距离，由于移出视图的view被缓存并从布局中删除，布局中的view个数  < 7，视图内需要显示8个，新加入的view需要添加到布局的底部
			// 添加range.getCount()-itemsLayout.getChildCount()个
			// 三）向下滚动，firstItem 超出range，itemLayout中的原有视图都移除并caches，从firstItem开始添加 range.getCount()个视图
			firstItem = range.getFirst();
		}

		int first = firstItem;
Log.d(TAG, "rebuildItems:firstItem 2 -->" + firstItem);
		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
				first++;// adapter 为null 或者 adapter 绑定的data为null 且itemLayout没有子视图 才成立
			}
		}
		
		firstItem = first;
Log.d(TAG, "rebuildItems:firstItem -3->" + firstItem);		
//Log.d(TAG, "rebuildItems--updated-->" + updated+"--firstItem-->"+firstItem);
		return updated;
	}

	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
	 */
	private void updateView() {
		if (rebuildItems()) {
			Log.d(TAG, "updateView：getHeight()-->"+ getHeight()+"--getWidth()--"+getWidth());
			
			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);			
			layout(getWidth(), getHeight());
			
			Log.d(TAG, "updateView：getHeight()-->"+ getHeight()+ "itemLayout.height-->" +itemsLayout.getHeight());
		}
	}

	/**
	 * Creates item layouts if necessary, (current item layout is null)
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());
			itemsLayout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	/**
	 * Builds view for measuring；初始化父视图调用
	 * itemLayout是父视图：放置visibleItems个item子视图
	 * firstItem在最上，currentItem在中间，从最下的子视图开始放置
	 * <p>循环内调用 addView（view，0) 将view放置在第一个位置，循环结束: i 值 为 firstItem，最后一个放置在顶端的view 
	 * <p>firstItem ~ firstItem + visibleItems 即为目前视图内的全部视图,first即为日期在set中的index
	 * <p>程序完成：itemLayout 中放置了相应的视图，视图的内容由adapter所绑定的set and index 决定
	 * <p> onMeasure()调用此方法，由currentItem and visibleItems 计算父视图中子视图的个数，
	 * 并计算出firstItem的值
	 */
	private void buildViewForMeasuring() {
		
		Log.d(TAG, "buildViewForMeasuring--firstItem-->"+ firstItem);
		// clear all items
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		// add views
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
	}

	/**
	 * Adds item view  to items layout;adapter 为null 或者 adapter 绑定的data为null 才会返回false
	 * @param index the item index
	 * @param first true :the flag indicates if view should be first,false view add to the end of itemLayout
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		View view = getItemView(index);
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
	 * <p> true: viewadapter != null and viewAdapter.getItemCount >0 and (isCyclic or (index>=0 and index<viewadapter.getItemCoun)
	 * 
	 * <p> 适配器不为空且数据数目大于零的前提下，循环模式下返回true；非循环模式下index在0~ItemCount之间
	 * @param index the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
				(isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
	}

	/**
	 * Returns view for specified item from adapter!
	 * <p> index in the bound : adapter.getItem方法返回item view()
	 * <p> out of bounds : empty view from adapter.getEmptyItem()
	 * <p> getItem getEmptyItem 类似adapter的getview，如果convert view == null，则new一个textview or empty view
	 * @param index the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {
		
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}
		
		int count = viewAdapter.getItemsCount();
		// index 无效的情况下返回一个 emptyItem View;
		if (!isValidItemIndex(index)) {
			return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		// index 无效会返回null，上面代码已经将index转为有效代码，return不会返回null
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}
}
