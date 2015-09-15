package fxj.calendar.year;

import java.util.TimeZone;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.GetChars;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class YearListView extends ListView{

	

	private static final String TAG = "CustomListView";
	private static final boolean D = false;
	
	
    VelocityTracker mTracker;
    private static float mScale = 0;
    private float mMinFlingVelocity;
    private float mMaxFlingVelocity;
    private int topOffset = 3;
    

    // These define the behavior of the fling. Below MIN_VELOCITY_FOR_FLING, do the system fling
    // behavior. Between MIN_VELOCITY_FOR_FLING and MULTIPLE_MONTH_VELOCITY_THRESHOLD, do one month
    // fling. Above MULTIPLE_MONTH_VELOCITY_THRESHOLD, do multiple month flings according to the
    // fling strength. When doing multiple month fling, the velocity is reduced by this threshold
    // to prevent moving from one month fling to 4 months and above flings.
    
    private final static int FLING_LEVEL_1 = 1500;
    private final static int FLING_LEVEL_2 = 3000;
    private final static int FLING_LEVEL_3 = 4000;
    
    
    
    
    // disposable variable used for time calculations
    protected Time mTempTime;
    private long mDownActionTime;
    private final Rect mFirstViewRect = new Rect();

    Context mContext;
    
    TimeZone tz;

    // Updates the time zone when it changes
    private final Runnable mTimezoneUpdater = new Runnable() {
        @Override
        public void run() {
            if (mTempTime != null && mContext != null) {
                mTempTime.timezone =
                		tz.getDisplayName();
            }
        }
    };
    
    private int firstIndexHeight;
	private int firstIndex;
	

    public YearListView(Context context,AttributeSet attrs,int defStyle) {
    	super(context, attrs, defStyle);
    	init(context);
	}
    
    public YearListView(Context context) {
    	super(context);
    	init(context);
	}
    
    public YearListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
    
    private void init(Context c) {
    	tz = TimeZone.getDefault();
        mContext = c;
        //mTracker  = VelocityTracker.obtain();
        mTempTime = new Time(tz.getDisplayName());
        if (mScale == 0) {
            mScale = c.getResources().getDisplayMetrics().density;
            if (mScale != 1) {
                topOffset *= mScale;
            }
        }
        
        ViewConfiguration vc = ViewConfiguration.get(c);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity()+400;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        if(D) Log.d(TAG, "mMinFlingVelocity:  "+mMinFlingVelocity+"    : " + mMaxFlingVelocity);// 75pixel /s ~12000
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return processEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return processEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    private boolean processEvent (MotionEvent ev) {
    	
    	if (mTracker == null) {
    		mTracker = VelocityTracker.obtain();
    	}
    	mTracker.addMovement(ev);
    	
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            // Since doFling sends a cancel, make sure not to process it.
            case MotionEvent.ACTION_CANCEL:
                return false;
            // Start tracking movement velocity
            case MotionEvent.ACTION_DOWN:
                //mTracker.clear();
            	
            	View  first = getChildAt(0);
                int firstH = first.getHeight();
                first.getLocalVisibleRect(mFirstViewRect);
                int visi = mFirstViewRect.bottom - mFirstViewRect.top;
                if(D) Log.d(TAG, "down-->first view :  "+firstH + "  : "+ visi+ "+  个数   :" + getChildCount());
            	
                mDownActionTime = SystemClock.uptimeMillis();
                break;
            // Accumulate velocity and do a custom fling when above threshold
            case MotionEvent.ACTION_UP:
                //mTracker.addMovement(ev);
            	final VelocityTracker vt = mTracker;
            	vt.computeCurrentVelocity(1000, mMaxFlingVelocity);
            	final int pointerId = ev.getPointerId(0);
                //mTracker.computeCurrentVelocity(1000);    // in pixels per second
                
            	View  ff = getChildAt(0);
                int fs = ff.getHeight();
                ff.getLocalVisibleRect(mFirstViewRect);
                int vifsi = mFirstViewRect.bottom - mFirstViewRect.top;
                if(D) Log.d(TAG, "up-->first view :  "+fs + "  : "+ vifsi+ "+  个数   :" + getChildCount());
            	
            	
                final float vel =  mTracker.getYVelocity (pointerId);
                if(D) Log.d(TAG, "mTracker.getYVelocity ():  "+vel);
                
                if (Math.abs(vel) > mMinFlingVelocity) {
                    doFling(vel);
                    
                    if(mTracker != null) {
                    	mTracker.recycle();
                    	mTracker = null;
                    }
                    
                    return true;
                }
                if(mTracker != null) {
                	mTracker.recycle();
                	mTracker = null;
                }
                
                break;
            default:
                 //mTracker.addMovement(ev);
                 break;
        }
        return false;
    }

    // Do a "snap to start of month" fling
    private void doFling(float velocityY) {

        // Stop the list-view movement and take over
        MotionEvent cancelEvent = MotionEvent.obtain(mDownActionTime,  SystemClock.uptimeMillis(),
                MotionEvent.ACTION_CANCEL, 0, 0, 0);
        onTouchEvent(cancelEvent);
        
        int realIndex; // 确定起始 item
        boolean indexFlag = false;
        realIndex = getFirstIndex();
        if (realIndex != firstIndex){
        	indexFlag = true;
        }
        
        int dis= 0;
        int itemToJump = 0;
        float value = Math.abs(velocityY);
        
        if (value < FLING_LEVEL_1) {
            if (velocityY < 0) {
                /** 1 + offset ( */
            	itemToJump = 1;
//            	dis = calculateDistance(itemToJump, indexFlag);
//            	if (indexFlag == true) {
//            		dis = firstIndexHeight + getChildAt(0).getHeight() + topOffset;
//            	} else {
//            		dis = firstIndexHeight + topOffset;
//            	}
            } else {
                itemToJump = -1;
//                dis = calculateDistance(itemToJump, indexFlag);
//                if (indexFlag == true) {
//            		dis = -getChildAt(0).getHeight() + firstIndexHeight + topOffset;
//            	} else {
//            		dis = -2*getChildAt(0).getHeight() + firstIndexHeight + topOffset;
//            	}
            }
		} else if (value >= FLING_LEVEL_1 && value < FLING_LEVEL_2) {
			if (velocityY < 0) {
				itemToJump = 2;
			} else {
				itemToJump = -2;
			}
		} else if (value >= FLING_LEVEL_2 && value < FLING_LEVEL_3) {
			if (velocityY < 0) {
				itemToJump = 3;
			} else {
				itemToJump = -3;
			}
		} else {
			if (velocityY < 0) {
				itemToJump = 4;
			} else {
				itemToJump = -4;
			}
		}
		dis = calculateDistance(itemToJump, indexFlag);
		if (itemToJump == 0 || dis == 0)
			return;
		smoothScrollBy(dis, Math.abs(dis) * 3);
		firstIndexHeight = 0;
        
        

//        int index = getFirstIndex();
//        if(D) Log.d(TAG, "year:" + (index + 1970));
//        
//        
//        View  first = getChildAt(0);
//        int firstH = first.getHeight();
//        first.getLocalVisibleRect(mFirstViewRect);
//        int visi = mFirstViewRect.bottom - mFirstViewRect.top;
//        if(D) Log.d(TAG, "first view :  "+firstH + "  : "+ visi);
        
//        smoothScrollToPositionFromTop(index+itemToJump, 0, 500*(itemToJump>0?itemToJump:(-itemToJump)));
    }
    
    private int calculateDistance(int itemToJump, boolean flag) {
    	int dis=-1;
    	int offset = firstIndexHeight + topOffset;
    	int height = getChildAt(0).getHeight();
    	dis = (flag?itemToJump:itemToJump-1)*height + offset;
    	return dis;
    }
    
    
    private int getFirstIndex() {
        YearView child = (YearView) getChildAt(0);
        if (child == null) {
            return -1;
        }
        
        if(D) Log.d(TAG, "child.getYear():  "+child.getYear()+"geshu :"+ getChildCount());
        int height = child.getHeight();
        child.getLocalVisibleRect(mFirstViewRect);
        firstIndexHeight = mFirstViewRect.bottom - mFirstViewRect.top;
        firstIndex = child.getYear() - YearFragment.MIN_CALENDAR_YEAR;
        
        if (firstIndexHeight < height/2) {
        	
        	return child.getYear() - YearFragment.MIN_CALENDAR_YEAR+ 1;
        }
        return firstIndex;
    }
    
  
    
}
