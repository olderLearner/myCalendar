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
	
	// ��ָ���ʱ�� item index Ŀǰֻ���� fling������
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
     * Scroll the wheel, ����Զ���������handler�д����ػ�,ÿ�ƶ�һ�ξ��룬�ػ�һ��
     * @param distance the scrolling distance����ֵ �����Ϲ�������ֵ�����¹���
     * @param time the scrolling duration
     */
    public void scroll(int distance, int time) {
        
    	scroller.forceFinished(true);
        
        /**
         * ����Ҳ���ã�����������70pixelʱ������ֹͣ���ٶ�Ϊ0.Ȼ�����justify������һ�����ټ��ٻ����Ӳ��
         * ���ã�2015 8 30 
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
			Log.d("Month", "������ msg 1");
			//viewHeightOffset = MethodUtil.smoothModifyHeight(-heightOffset, 280, 5);
			//solarMonth.postDelayed(mModifyHeight, time-280);
		}*/
        
    }
   
    /**
     * Stops scrolling,���� Scroller.forceFinished
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }
    
    /**
     * Handles Touch event ��event �� SolarMonth ���ݵ� SolarMonthScroller 
     * action_down ǿ��ֹͣ���������handle����Ϣ���У���¼��ǰ����λ��
     * action_move ���2��move֮���y�����ƶ����룬��¼���µ������
     * 		����listener.onScroll(distanceY)����WheelMonth����ÿ���ƶ��ľ�������ػ棩
     * 		��ͼ����ָ�ƶ������ػ�
     * 
     * action_up offset��Ϊ�㣬����scroller���� �Ĺ���������ʹoffset���㣨currentItem �ϱ�Ե�븸view���룩
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
		 *  gestureDetector ���� onScroll onFling ��������event����false
		 *  �������� scroll flingʱ��ִ��if����䣬�������������������event��action_up��ִ��justify();
		 */
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {      	
        	justify(); // ִ��handler
        }

        return true;
    }
    
    /*
     * gesture listener
     * ���Ƽ�������action_move���ж�Ϊscroll���ƣ����Ƽ�������scroll������Ӧ������true��
     * ��scroll�ݻ�ΪFling��ʱ�����Ƽ�������������Ӧ�ķ�Ӧ����ʱ��ָ�Ѿ��뿪��Ļ��onFling���� ָ�� ����  ����
     */
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Do scrolling in onTouchEvent() since onScroll() are not call immediately
            //  when user touch and move the wheel
        	
        	return true;
        }

        /*
         *  velocity ��ϵͳ��������λ: pixel/s����С����ָ�������ٶ�����
         *  �������ֻ�����ϵһ�£�x�� ���� + ��y �� ���� + ��
         * e1 : action_down ��Ϣ
         * e2 : action_up ��Ϣ
         *  
         * scroller �Ĺ�������������ϵ�෴��y���ٶ� �� - velocity
         */
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	//Log.d(TAG, "velocityY  :"+ velocityY);
        	lastScrollY = 0;
        	int offset = (int)(e2.getY()-e1.getY());
            if(velocityY>100) {// ���¹� item-- 
            	if (velocityY <1500) {
            		velocityY = 1500;
            	}
            	int i = solarMonth.getItemHeight(downIndex-1);
            	//scroller.startScroll(0, 0, 0, -i+offset, 2*(i-Math.abs(solarMonth.getScrollingOffset())));
            	scroller.fling(0, 0, 0, (int) -velocityY, 0, 0, -i+offset, 0);
            	solarMonth.modifyHeightCausedByFling(downIndex-1,downIndex,true);
            	
            } else if (velocityY < -100) {// ���Ϲ� item++ 
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
     * ������߳��У�����computeScrollOffset()�ػ�ǰ������location,����getCurrY�õ�y�����λ��currY
     * lastScrollY - currY �õ��������룬���� currY ���� lastScrollY ��¼��ǰλ��
     * ����listener.onScroll(distance) �ػ�view�������и���offset��ֵȷ���Ƿ����scroller.forceFinished
     * �ж�1���Ƚ�currY �� finalY �Ĳ�ֵ��С����ֵ���ж�������������¼��ǰλ��
     * 
     * �ж�2��1���������û�н�������message�����м������͹�����Ϣ��
     * 		2�������������ҵ�ǰ��Ϣ��MESSAGE_SCROLL ���� justify()����
     * 		3��1��2��������������finishScrolling()��
     */
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            
        	//Log.d(TAG, "handler():scroller.computeScrollOffset()-->"+scroller.computeScrollOffset() +"--msg is--" + msg.what);
        	//Log.d(TAG, "handler():scroller.isFinished()-->"+scroller.isFinished() +"--msg is--" + msg.what);
        	
        	scroller.computeScrollOffset();// ������δֹͣʱ����õ�ǰ��λ��
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
            } else if (msg.what == MESSAGE_SCROLL) {// ����ֹͣ����MESSAGE_SCROLL�����Ϣ���У�����justify(); ���offsetֵ
                justify();
            } else {
                finishScrolling();
            }
        }
    };
    
    /**
     * Justifies wheel ��������λ��ʹ��ͼ����ʾ7��item����currentItem���м�
     * <p> listener.onJustify();-->���offset����ָ������ֵ������ͼλ�ò���ȷ�����ù���������else ����
     * <p> MESSAGE_JUSTIFY ѹ����Ϣ����,
     * <li>WheelScroller.isScrollingPerformed = false;
     * <li>WheelView.isScrollingPerformed = false;
     */
    private void justify() {
//        Log.d(TAG, "justify()-->"+ "ִ���˵�һ�����");
    	listener.onJustify();
//    	Log.d(TAG, "justify()-->"+ "ִ���˵ڶ������");
    	setNextMessage(MESSAGE_JUSTIFY);
//    	Log.d(TAG, "justify()-->"+ "ִ���� MESSAGE_JUSTIFY");
    }

    /**
     * Starts scrolling ������־λ�� true
     * <li>WheelScroller.isScrollingPerformed = true;
     * <li>WheelView.isScrollingPerformed = true;֪ͨ�󶨵ļ�����OnWheelScrollListener�� WheelView���ڹ���
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            // ��λ wheel view ������־��֪ͨ�󶨵ļ����� wheel view ���ڹ���
            listener.onStarted();
        }
    }

    /**
     * Finishes scrolling��������־��Ϊ��false������ֹͣ����
     * <p> �˷���ֻͨ��handler���ã����� justify()����֮��ִ��
     * <li>WheelScroller.isScrollingPerformed = false;
     * <li>WheelView.isScrollingPerformed = false;֪ͨ����������ֹͣ����activity������ȡ���ֵ�ǰ���ݣ����� offset �ػ�view
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            
        	// wheel view ������־��Ϊ false��֪ͨ����������ֹͣ��������ȡ���ݣ�
        	// ���� offset �ػ�view
        	listener.onFinished();
            isScrollingPerformed = false;
        }
    }
}
