/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *  custom by fxj 2015 8 31
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
 */

package fxj.calendar.solar;

import fxj.calendar.MyApp;
import fxj.calendar.util.MethodUtil;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates the 
 */
public class SolarMonthScroller {
    
	
	
	private static final String TAG = "SolarMonthScroller";
	
	/**
     * Scrolling listener interface
     */
    public interface ScrollingListener {
        /**
         * Scrolling callback called when scrolling is performed.
         * @param distance the distance to scroll
         */
        void onScroll(int distance);

        /**
         * Starting callback called when scrolling is started
         */
        void onStarted();
        
        /**
         * Finishing callback called after justifying
         */
        void onFinished();
        
        /**
         * Justifying callback called to justify a view when scrolling is ended
         */
        void onJustify();
    }
    
    /** Scrolling duration */
    private static final int SCROLLING_DURATION = 500;

    /** Minimum delta for scrolling */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    // Listener
    private ScrollingListener listener;
    private SolarMonth solarMonth;
    
    // Context
    private Context context;
    
    // Scrolling
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private Scroller scroller_fling;
    private int lastScrollY;
    private float lastTouchedY;
    private boolean isScrollingPerformed;

	private float lastTouchedX;
	
	// 手指点击时的 item index 目前只用在 fling手势中
	private int downIndex;
	
	
	//private final Handler mHandler;
	private Handler mHandler;
    public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	/**
     * Constructor
     * @param context the current context
     * @param listener the scrolling listener
     */
    public SolarMonthScroller(Context context, ScrollingListener listener, SolarMonth wheelMonth) {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        
        scroller = new Scroller(context);
        scroller_fling = new Scroller(context, null,true);

        this.listener = listener;
        this.context = context;
        this.solarMonth = wheelMonth;
    }
    
    /**
     * Set the the specified scrolling interpolator
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
        
    }
    
    
    /**
     * Scroll the wheel, 软件自动滚动，在handler中处理重绘,每移动一段距离，重绘一次
     * @param distance the scrolling distance，正值 ：向上滚动；负值：向下滚动
     * @param time the scrolling duration
     */
    public void scroll(int distance, int time) {
        
    	scroller.forceFinished(true);
        
        /**
         * 这样也不好，滚动到还差70pixel时，滚动停止，速度为0.然后调用justify，又来一个加速减速会很生硬！
         * 弃用！2015 8 30 
         */
        /*if(Math.abs(distance) > 420) {
        	distance = distance>0?distance - 70:distance +70;
        }*/
        
    	
        lastScrollY = 0;
        
        scroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);
        startScrolling();
        
        //int heightOffset = solarMonth.getHeight()- (solarMonth.getItemHeight(solarMonth.getCurrentItem()) - 36);
        /*int heightOffset = solarMonth.getItemHeight(solarMonth.getLastItem())- solarMonth.getItemHeight(solarMonth.getCurrentItem());
		if (Math.abs(heightOffset) > 5) {
			Message msg = mHandler.obtainMessage(1, heightOffset, -1);
			mHandler.sendMessageDelayed(msg, time-280);
			Log.d("Month", "发送了 msg 1");
			//viewHeightOffset = MethodUtil.smoothModifyHeight(-heightOffset, 280, 5);
			//solarMonth.postDelayed(mModifyHeight, time-280);
		}*/
        
    }
   
    /**
     * Stops scrolling,调用 Scroller.forceFinished
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }
    
    /**
     * Handles Touch event ，event 从 SolarMonth 传递到 SolarMonthScroller 
     * action_down 强制停止滚动，清空handle中消息队列，记录当前触摸位置
     * action_move 求出2次move之间的y方向移动距离，记录最新点的坐标
     * 		调用listener.onScroll(distanceY)，（WheelMonth根据每次移动的距离进行重绘）
     * 		视图随手指移动进行重绘
     * 
     * action_up offset不为零，调用scroller对象 的滚动方法，使offset置零（currentItem 上边缘与父view对齐）
     * 
     * @param event the motion event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        
    	boolean result = false;
    	switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchedY = event.getY();
                lastTouchedX = event.getX();
                
//Log.d(TAG, "onTouchEvent::ACTION_DOWN:"+event.getY());
                
                downIndex = solarMonth.getCurrentItem();
                scroller.forceFinished(true);
                clearMessages();
                
                break;
            case MotionEvent.ACTION_MOVE:
                // perform scrolling
                
            	int distanceY = (int)(event.getY() - lastTouchedY);
            	int distanceX = (int) (event.getX() - lastTouchedX);
// Log.d(TAG, "onTouchEvent::ACTION_move:"+event.getY());
            	
            	
                if (distanceY != 0) {
                    startScrolling();
                    listener.onScroll(distanceY);
                    lastTouchedY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
            	
            	break;
        }
		
		/*
		 *  gestureDetector 处理 onScroll onFling ，其他的event返回false
		 *  当手势是 scroll fling时候不执行if中语句，当手势是其他情况，且event是action_up，执行justify();
		 */
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {      	
        	justify(); // 执行handler
        }

        return true;
    }
    
    /*
     * gesture listener
     * 手势监听器，action_move可判定为scroll手势，手势监听器对scroll不做响应，返回true，
     * 当scroll演化为Fling的时候手势监听器会做出相应的反应，此时手指已经离开屏幕，onFling方法 指定 内容  滚动
     */
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Do scrolling in onTouchEvent() since onScroll() are not call immediately
            //  when user touch and move the wheel
        	
        	return true;
        }

        /*
         *  velocity 由系统采样，单位: pixel/s，大小和手指滑动的速度正比
         *  符号与手机坐标系一致，x轴 向左 + ，y 轴 向下 + 。
         * e1 : action_down 信息
         * e2 : action_up 信息
         *  
         * scroller 的滚动方向与坐标系相反，y轴速度 是 - velocity
         */
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	//Log.d(TAG, "velocityY  :"+ velocityY);
        	lastScrollY = 0;
        	int offset = (int)(e2.getY()-e1.getY());
            if(velocityY>100) {// 向下滚 item-- 
            	if (velocityY <1500) {
            		velocityY = 1500;
            	}
            	int i = solarMonth.getItemHeight(downIndex-1);
            	//scroller.startScroll(0, 0, 0, -i+offset, 2*(i-Math.abs(solarMonth.getScrollingOffset())));
            	scroller.fling(0, 0, 0, (int) -velocityY, 0, 0, -i+offset, 0);
            	solarMonth.modifyHeightCausedByFling(downIndex-1,downIndex,true);
            	
            } else if (velocityY < -100) {// 向上滚 item++ 
            	if (velocityY > -1500) {
            		velocityY = -1500;
            	}
            	int j = solarMonth.getItemHeight(downIndex);
            	//scroller.startScroll(0, 0, 0, j+offset, 2*(j-Math.abs(solarMonth.getScrollingOffset())));
            	scroller.fling(0, 0, 0, (int) -velocityY, 0, 0, 0, j+offset);
            	solarMonth.modifyHeightCausedByFling(downIndex+1,downIndex,true);
            
            } else {return false;}
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;
    
    /**
     * Set next message to queue. Clears queue before.
     * 
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }
    
    // animation handler
    /*
     * 在这个线程中，调用computeScrollOffset()截获当前滚动的location,调用getCurrY得到y方向的位置currY
     * lastScrollY - currY 得到滚动距离，并将 currY 赋给 lastScrollY 记录当前位置
     * 调用listener.onScroll(distance) 重绘view，方法中根据offset的值确定是否调用scroller.forceFinished
     * 判断1：比较currY 与 finalY 的差值，小于阈值则判定滚动结束，记录当前位置
     * 
     * 判断2：1）如果滚动没有结束，向message队列中继续发送滚动消息；
     * 		2）滚动结束，且当前消息是MESSAGE_SCROLL 调用 justify()方法
     * 		3）1）2）都不成立调用finishScrolling()。
     */
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            
        	//Log.d(TAG, "handler():scroller.computeScrollOffset()-->"+scroller.computeScrollOffset() +"--msg is--" + msg.what);
        	//Log.d(TAG, "handler():scroller.isFinished()-->"+scroller.isFinished() +"--msg is--" + msg.what);
        	
        	scroller.computeScrollOffset();// 滚动并未停止时，获得当前的位置
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
            	listener.onScroll(delta);
            }
            
            // scrolling is not finished when it comes to final Y
            // so, finish it manually 
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            
            if (!scroller.isFinished()) {
// Log.d(TAG, msg.what+"");
            	animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {// 滚动停止，将MESSAGE_SCROLL清除消息队列，调用justify(); 检查offset值
                justify();
            } else {
                finishScrolling();
            }
        }
    };
    
    /**
     * Justifies wheel 修正滚轮位置使视图内显示7个item，且currentItem在中间
     * <p> listener.onJustify();-->如果offset大于指定的阈值，则视图位置不正确，调用滚动方法，else 返回
     * <p> MESSAGE_JUSTIFY 压入消息队列,
     * <li>WheelScroller.isScrollingPerformed = false;
     * <li>WheelView.isScrollingPerformed = false;
     */
    private void justify() {
//        Log.d(TAG, "justify()-->"+ "执行了第一个输出");
    	listener.onJustify();
//    	Log.d(TAG, "justify()-->"+ "执行了第二个输出");
    	setNextMessage(MESSAGE_JUSTIFY);
//    	Log.d(TAG, "justify()-->"+ "执行了 MESSAGE_JUSTIFY");
    }

    /**
     * Starts scrolling 滚动标志位置 true
     * <li>WheelScroller.isScrollingPerformed = true;
     * <li>WheelView.isScrollingPerformed = true;通知绑定的监听器OnWheelScrollListener： WheelView正在滚动
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            // 置位 wheel view 滚动标志，通知绑定的监听器 wheel view 正在滚动
            listener.onStarted();
        }
    }

    /**
     * Finishes scrolling，滚动标志设为：false，滚轮停止滚动
     * <p> 此方法只通过handler调用，且在 justify()方法之后执行
     * <li>WheelScroller.isScrollingPerformed = false;
     * <li>WheelView.isScrollingPerformed = false;通知监听器滚轮停止，主activity可以提取滚轮当前数据，清零 offset 重绘view
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            
        	// wheel view 滚动标志设为 false，通知监听器滚轮停止，可以提取数据，
        	// 清零 offset 重绘view
        	listener.onFinished();
            isScrollingPerformed = false;
        }
    }
}
