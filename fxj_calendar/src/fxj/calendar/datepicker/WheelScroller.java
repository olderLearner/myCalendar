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
 */

package fxj.calendar.datepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates the 
 */
public class WheelScroller {
    
	
	
	private static final String TAG = "WheelScroller";
	
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
    private static final int SCROLLING_DURATION = 168;

    /** Minimum delta for scrolling */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    // Listener
    private ScrollingListener listener;
    
    // Context
    private Context context;
    
    // Scrolling
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;
    private float lastTouchedY;
    private boolean isScrollingPerformed;

    /**
     * Constructor
     * @param context the current context
     * @param listener the scrolling listener
     */
    public WheelScroller(Context context, ScrollingListener listener) {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        
        scroller = new Scroller(context);

        this.listener = listener;
        this.context = context;
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
     * Scroll the wheel, ����Ҫ��handler�д���
     * @param distance the scrolling distance����ֵ �����Ϲ�������ֵ�����¹���
     * @param time the scrolling duration
     */
    public void scroll(int distance, int time) {
        scroller.forceFinished(true);

        lastScrollY = 0;
        
        scroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);
        Log.d(TAG, "scroll()-->"+ "ִ���� MESSAGE_SCROLL");
        startScrolling();
    }
   
    /**
     * Stops scrolling,���� Scroller.forceFinished
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }
    
    /**
     * Handles Touch event ��wheelview ���ݵ�wheelscroller event
     * @param event the motion event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchedY = event.getY();
                scroller.forceFinished(true);
                clearMessages();
                break;
    
            case MotionEvent.ACTION_MOVE:
                // perform scrolling
                int distanceY = (int)(event.getY() - lastTouchedY);
                if (distanceY != 0) {
                    startScrolling();
                    listener.onScroll(distanceY);
                    lastTouchedY = event.getY();
                }
                break;
        }
Log.d(TAG, "WheelScroller-->onTouchEvent-->lastTouchedY-->"+ lastTouchedY);
Log.d(TAG, "WheelScroller-->onTouchEvent-->"+ event.getAction());
		
		// ����gestureDetector�󶨵�listener ��onScroll onFling ��������event����false
		// �������� scroll flingʱ��ִ��if����䣬�������������������event��action_up��ִ��justify();
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {

        	//Log.d(TAG, "WheelScroller-->onTouchEvent-->gestureDetector"+ event.getAction());
        	
        	justify(); // ִ��handler
        }

        return true;
    }
    
    // gesture listener
    /*
     * ���Ƽ�������action_move���ж�Ϊscroll���ƣ����Ƽ�������scroll������Ӧ�����ǻ�ִ��startScrolling ����������־��Ϊtrue��
     * ��scroll�ݻ�ΪFling��ʱ�����Ƽ�������������Ӧ�ķ�Ӧ����ʱ��ָ�Ѿ��뿪��Ļ��ͨ���������ָ��wheel��ô����
     */
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Do scrolling in onTouchEvent() since onScroll() are not call immediately
            //  when user touch and move the wheel
Log.d(TAG, "WheelScroller-->gestureListener-->onScroll");
        	return true;
        }

        // velocity ��ϵͳ��������λ: pixel/s����С����ָ�������ٶ�����
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            
Log.d(TAG, "WheelScroller-->gestureListener-->onFling");
        	lastScrollY = 0;
            final int maxY = 0x7FFFFFFF;
            final int minY = -maxY;
            // ������ -velocityY �ٶȹ�����minY maxY �ܴ�hander���ж���ʱ��������
            scroller.fling(0, lastScrollY, 0, (int) -velocityY, 0, 0, minY, maxY);
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
            
        	Log.d(TAG, "handler():scroller.computeScrollOffset()-->"+scroller.computeScrollOffset() +"--msg is--" + msg.what);
        	Log.d(TAG, "handler():scroller.isFinished()-->"+scroller.isFinished() +"--msg is--" + msg.what);
        	scroller.computeScrollOffset();// ������δֹͣʱ����õ�ǰ��λ��
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            Log.d(TAG, "handler():delta-->" + delta);
            lastScrollY = currY;
            if (delta != 0) {
            	Log.d(TAG, "handler():listener.onScroll(delta)" + delta);
            	listener.onScroll(delta);
                
            }
            
            // scrolling is not finished when it comes to final Y
            // so, finish it manually 
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            
            if (!scroller.isFinished()) {
                Log.d(TAG, msg.what+"");
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
        Log.d(TAG, "justify()-->"+ "ִ���˵�һ�����");
    	listener.onJustify();
    	Log.d(TAG, "justify()-->"+ "ִ���˵ڶ������");
    	setNextMessage(MESSAGE_JUSTIFY);
    	Log.d(TAG, "justify()-->"+ "ִ���� MESSAGE_JUSTIFY");
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
