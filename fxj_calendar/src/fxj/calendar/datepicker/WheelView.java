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
	 *  Scrollinglistener WheelScroller ������ڲ��ӿ� public ScrollingListener,
	 *  ��WheelScroller.ScrollingListenerʵ��ͨ��WheelScroller�Ĺ��췽��������WheelScroller��
	 *  ���ʵ���еļ���������ֻ��WheelScroller��ʹ��
	 */
	WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
		@Override
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			// distance ��Ҫ�����ľ��룬���ݾ������currentItem�����ػ�
			doScroll(distance);

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

			scrollingOffset = 0;
			invalidate();
		}

		@Override
		public void onJustify() {
			if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
				Log.d(TAG, "����ƫ������scrollingOffset-->"+ scrollingOffset);
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
					index += itemCount; // ��ѭ��״̬�� -itemCount<index<0,ѡ���ʵ��index=item - |index|
				}
				/*
				if (index<0) {
					index=itemCount - Math.abs(index)%itemCount;
				} else {
					index %= itemCount;
				}*/
				index %= itemCount; // index= index%itemCount
			} else{
				// ��ѭ��������� return
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {// �ӵ�ǰλ�ù�����Ŀ��λ��
				
				int itemsToScroll = index - currentItem;// ��ǰitem��Ŀ��item֮��  
				
				/*
				 *  ���ĸ���������  ����or���¡�
				 *  ��Ϊ��ѭ����������ǰλ�õ�Ŀ��λ�ô���2���������򣬲�ֵ�ֱ�Ϊ��
				 *  a = abs(index - currentItem); b = itemCount - abs(index - currentItem)
				 *  a < b ʱ���� a ����Ŀ����������ʹ�С ��Ϊ index - currenItem�������� index>itemCount,
				 *  a > b ʱ���� b ����Ŀ������������index - currentItem�෴
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
				scrollingOffset = 0; // ����

				int old = currentItem;
				currentItem = index; // ����currentItem ondraw��ʹ���µ�currentItem �ػ�

				notifyChangingListeners(old, currentItem);

				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * ���ط�����������־λ��false
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
	 * ondraw()������drawShadow��־λ
	 * @return true is shadows are drawn
	 */
	public boolean drawShadows() {
		return drawShadows;
	}

	/**
	 * Set whether shadows should be drawn
	 * ondraw()������drawShadow��־λ
	 * @param drawShadows flag as true or false
	 */
	public void setDrawShadows(boolean drawShadows) {
		this.drawShadows = drawShadows;
	}

	/**
	 * Set the shadow gradient color
	 * ��Ӱ��ģ���ݶȣ��Զ���ģʽ
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
	 * item���µ�������
	 * @param resource
	 */
	public void setWheelForeground(int resource) {
		wheelForeground = resource;
		centerDrawable = getContext().getResources().getDrawable(wheelForeground);
	}

	/**
	 * Invalidates wheel,���caches and ���itemsLayout ������Сview
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
	 * ǰ��ɫ���ϲ����䣬�²����䣬���ñ���ɫ
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
	 * <p>item�ĸ߶ȣ����ַ����� ��Ա�������Ӳ����ļ��еõ�item�ĸ߶ȣ�view�ĸ߶�/��ʾ��item����
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
	 * @param mode -->the layout mode��MeasureSpec.EXACTLY��MeasureSpec.AT_MOST��MeasureSpec.UNSPECIFIED
	 * @return width -->the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		// MeasureSpec ��int��ʽ�洢 ��ʵ�ʲ�����onMeasure��
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
			
			// at_most mode ����ܳ��� widthSize
			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}
		
		// ��ȱ�����Ϊ exactly 
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
	 * r-l ��
	 * b-t ��
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
	 * <p> draw �ķ�ʽ���� canvas�ϻ�layout������Ҫ��canvas��ԭ����itemLayoutԭ���غϣ�Ĭ�������canvas�븸��ͼ�غ�
	 * �����ƶ�ʱ��itemLayout�ϲ������˸���ͼ
	 * <li> scroll ģʽ������ offset>0,top = 37 canvas ��λ -top + offset��Ϊ��������ͼ��item�Ķ��� y����
	 * <li> ���� offset <0 top = 0; canvas �ƶ� offset����Ϊ��ǰfirstItem�Ķ���y���꣨����ͼֻ��ʾ���沿�֣�
	 * @param canvas the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		
		canvas.save();

		int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
		Log.d(TAG, "drawItems:top-->"+ top+ "--scrollingOffset-->" + scrollingOffset);
		canvas.translate(PADDING, - top + scrollingOffset);
		
		// �ڻ����ϻ������view
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
		/*/ Remarked by wulianghuan 2014-11-27  ʹ���Լ��Ļ��ߣ����������
		Rect rect = new Rect(left, top, right, bottom)
		centerDrawable.setBounds(bounds)
		centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
		centerDrawable.draw(canvas);
		//*/
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(R.color.province_line_borde));
		paint.setColor(Color.RED);
		// �����߿�
		paint.setStrokeWidth((float) 2);
		// �����ϱ�ֱ��
		canvas.drawLine(0, center - offset, getWidth(), center - offset, paint);
		// �����±�ֱ��
		canvas.drawLine(0, center + offset, getWidth(), center + offset, paint);
		//*/
	}

	@Override
	/**
	 * ACTION_DOWN ACTION_MOVE ACTION_UP
	 * ��Event���ݵ�WheelScroller�д���
	 * 
	 */
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (getParent() != null) {
					getParent().requestDisallowInterceptTouchEvent(true);// ֹͣ����touchevnt
				}
				break;

			case MotionEvent.ACTION_UP:
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

		return scroller.onTouchEvent(event);
	}

	/**
	 * Scrolls the wheel,���ݹ����ľ��������ֵ ��ȷ��������� currentItem ����Ҫ������item ����������setCurrentItem�����������ڲ����� invalidate()��;
	 * <p> if delta < 0 item index up;  else item index down;
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta) {
		// delta ��ÿ��move�ľ��룬offset�ƶ�������������ǰ������˷���offsetҪ������ε��ƶ�����
		
		Log.d(TAG, "doScroll():scrollingOffset-->"+ scrollingOffset + "--delta-->"+ delta);
		scrollingOffset += delta;

		Log.d(TAG, "doScroll():scrollingOffset-->"+ scrollingOffset);
		
		int itemHeight = getItemHeight();
		
		int count = scrollingOffset / itemHeight;// ��Ҫ�ƶ��� item��������ֵ��currentItem ���ӣ���ֱ��currentItem ��С
		
		// count<0  �����ϻ� item ++ ��count>0  �����»� item--
		int pos = currentItem - count;// ��ǰ��λ��- �ƶ�������=Ŀ��λ�� ��index�� pos ��Ϊ�ƶ����currentItem
		
		int itemCount = viewAdapter.getItemsCount();// data set �е�item ����

		// ���벹�����ƶ����벻�ܱ�itemHeigh����ʱ������С��itemHeighһ��ʱ��fixPos=0������Ҫ�޸�posλ��
		int fixPos = scrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}
			
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) { // fixPos > itemHeight/2 �������£�ƫ��������item�߶ȵ�һ�룬�� currentItem -- count++
				pos--; // currentItem-- ����������++ count��������count++
				count++;
			} else if (fixPos < 0) {
				pos++;	// currentItem++
				count--; // offset�Ǹ�ֵ��ǰ���£�countҲ�Ǹ�ֵ����������+1����count--��
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount; // ѭ��ģʽ�£�index��ֵ��ʾ�������ǣ�index + itemCount ; 0-12 -1 �� 12=13+ -1
				// ��whileѭ���ɸ�Ϊ pos = itemCount - math.abd(pos % itemCount)
			}
			pos %= itemCount; // ȷ�� pos ��itemCount��(pos ���������Ҵ���itemCount)
			
		} else { // ��ѭ��ģʽ��
			//
			if (pos < 0) {// index < 0 ���� index = 0��set ��һ��Ԫ�������м䣬�ƶ�����ΪcurrentItem��
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {// index���� set��Χ�������һ��Ԫ�أ�itemCount - 1)�������м䣬�ƶ�����Ϊ��ֵ��-(itemCoun - currentItem) + 1;
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
			setCurrentItem(pos, false);// �˷����ڲ�������invalidate();
		} else {
			invalidate(); // ��ǰλ�� ��Ϊ pos��ֱ�ӵ����ػ�
		}

		// update offset
		/*
		 * ��תoffset,һ���ƶ�����  itemHeight/2 count = 1 (down) -1(up) 
		 * offset ���£�offset - count * itemHeight �� ��ֵ (down) ��ֵ(up)
		 */
		scrollingOffset = offset - count * itemHeight;
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	/**
	 * Scroll the wheel ������Ŀ��λ��,������item����
	 * @param itemsToSkip items to scroll
	 * @param time scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items,
	 * <p>��currentItem offset ���㵱ǰӦ����ʾ�ĵ�һ�� item �� index��first; Ӧ����ʾ�� item������count��
	 * <Li> ����ItemRange ����Ϊ��first count
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
				first--;// �����ƶ���offset>0��first item ++�������ƶ� first���䣬
			}
			count++; // ֻҪoffset!=0����ͼ����ʾ��view ��+1��

			// process empty items above the first or below the second
			// ��� abs.offset ���� itemHeight��firstItem - emptyItems����range.first (fling ������������)
			int emptyItems = scrollingOffset / getItemHeight();// int  ������-2 -1 0 1 2 ������ 
			first -= emptyItems;
			count += Math.asin(emptyItems);
			Log.d(TAG, "getItemsRange:emptyItems-->" + emptyItems + "--asin(emptyItems)-->" + Math.asin(emptyItems));
			
		}
		Log.d(TAG, "getItemsRange:currentItem-->" + currentItem + "--count-->" + count+ "--scrollingOffset-->"+ scrollingOffset);		
		
		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 * <p> itemLayout == null ����true
	 * <p> ÿ�ε�����ivalidate�����������rebuildItem�����ݵ�ǰ��currentItemֵȷ��firstItem
	 * ����Ҫ��ʾ��viewװ��itemLayout
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		
		//Log.d(TAG, "rebuildItems");
		
		boolean updated = false;
		// ��ǰ��ͼ��Ӧ����ʾ��item��Χ������currentItem����ͨ��scrollingOffset΢��
		ItemsRange range = getItemsRange();
		
		if (itemsLayout != null) {			
			/*
			 *  ��itemLayout����range��Χ�ڵ�item���պ󣬲���itemLayout�����
			 *  firstItem����һ�ε�ֵ��getItemsRange()�����У���offset currentItem ���¼������ƶ�֮����ʵ�� firstItem��range.first
			 *  recycleItem(itemsLayout, firstItem, range)��ֻҪfirstItem����range�ڣ�����ֵ���� firstItem
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

		if (!updated) { // �����ƶ�������itemHeight���ƶ����Ƿ���Ҫ rebuildItem ͨ�����������ж�
			// �ж�ǰһ��firstItem �Ƿ񲻵��� �µ�range.first(������ true ������ false�������ֵ�����ͼ���� �Ƿ񲻵��� ��range item ������ֻҪ�����ƶ� count = 8 ���� true�� 
			Log.d(TAG, "rebuildItems:getChildCount-->" + itemsLayout.getChildCount()+"--range.getCount()-->" + range.getCount());
			
			updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
			
			Log.d(TAG, "rebuildItems:updated-->" + updated);
		}

		
		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
			
			// �����ƶ�����һ�ε� firstItem ����range��Χ�ڣ���ʵ��first�Ѿ��仯����Ҫ�ڶ�������item view
			// ��ͼֻ��ʾ�� firstItem - firstItem + visibleItems,����Ҫ��firstItem֮����Ӽ����������ͼ��������range.getFirst() -firstItem + 1;
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			// ��һ�ε� firstItem �Ѿ�����range ��Χ�ڣ�����Ϊ��ʵ�� firstItem
			// �������Σ�һ�����Ϲ���С�� item height ���� firstItem ���䣬itemLayout������ ����view: addViewItem(range.getlast(), false) ����true firstItem����
			// �������Ϲ���  ����itemHeight���룬�����Ƴ���ͼ��view�����沢�Ӳ�����ɾ���������е�view����  < 7����ͼ����Ҫ��ʾ8�����¼����view��Ҫ��ӵ����ֵĵײ�
			// ���range.getCount()-itemsLayout.getChildCount()��
			// �������¹�����firstItem ����range��itemLayout�е�ԭ����ͼ���Ƴ���caches����firstItem��ʼ��� range.getCount()����ͼ
			firstItem = range.getFirst();
		}

		int first = firstItem;
Log.d(TAG, "rebuildItems:firstItem 2 -->" + firstItem);
		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
				first++;// adapter Ϊnull ���� adapter �󶨵�dataΪnull ��itemLayoutû������ͼ �ų���
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
			Log.d(TAG, "updateView��getHeight()-->"+ getHeight()+"--getWidth()--"+getWidth());
			
			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);			
			layout(getWidth(), getHeight());
			
			Log.d(TAG, "updateView��getHeight()-->"+ getHeight()+ "itemLayout.height-->" +itemsLayout.getHeight());
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
	 * Builds view for measuring����ʼ������ͼ����
	 * itemLayout�Ǹ���ͼ������visibleItems��item����ͼ
	 * firstItem�����ϣ�currentItem���м䣬�����µ�����ͼ��ʼ����
	 * <p>ѭ���ڵ��� addView��view��0) ��view�����ڵ�һ��λ�ã�ѭ������: i ֵ Ϊ firstItem�����һ�������ڶ��˵�view 
	 * <p>firstItem ~ firstItem + visibleItems ��ΪĿǰ��ͼ�ڵ�ȫ����ͼ,first��Ϊ������set�е�index
	 * <p>������ɣ�itemLayout �з�������Ӧ����ͼ����ͼ��������adapter���󶨵�set and index ����
	 * <p> onMeasure()���ô˷�������currentItem and visibleItems ���㸸��ͼ������ͼ�ĸ�����
	 * �������firstItem��ֵ
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
	 * Adds item view  to items layout;adapter Ϊnull ���� adapter �󶨵�dataΪnull �Ż᷵��false
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
	 * <p> ��������Ϊ����������Ŀ�������ǰ���£�ѭ��ģʽ�·���true����ѭ��ģʽ��index��0~ItemCount֮��
	 * @param index the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
				(isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
	}

	/**
	 * Returns view for specified item from adapter!
	 * <p> index in the bound : adapter.getItem��������item view()
	 * <p> out of bounds : empty view from adapter.getEmptyItem()
	 * <p> getItem getEmptyItem ����adapter��getview�����convert view == null����newһ��textview or empty view
	 * @param index the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {
		
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}
		
		int count = viewAdapter.getItemsCount();
		// index ��Ч������·���һ�� emptyItem View;
		if (!isValidItemIndex(index)) {
			return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		// index ��Ч�᷵��null����������Ѿ���indexתΪ��Ч���룬return���᷵��null
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}
}
