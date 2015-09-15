package fxj.calendar.lunar;


import fxj.calendar.util.MethodUtil;
import fxj.calendar.util.MyFixed;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class LunarListView extends ListView{

	private static final String TAG = "LunarListView";
	private static final boolean D = true;
	
    VelocityTracker mTracker;
    private static float mScale = 0;
    private float mMinFlingVelocity;
    private float mMaxFlingVelocity;
    private int topOffset = 3;
    
    private final static int FLING_LEVEL_1 = 1500;
    private final static int FLING_LEVEL_2 = 3000;
    private final static int FLING_LEVEL_3 = 4000;
    
    // disposable variable used for time calculations
    protected Time mTempTime;
    private long mDownActionTime;
    private Rect mFirstViewRect = new Rect();

    Context mContext;
    
    private int firstIndexVisibleHeight;
	private int firstIndex;
	

    public LunarListView(Context context,AttributeSet attrs,int defStyle) {
    	super(context, attrs, defStyle);
    	init(context);
	}
    
    public LunarListView(Context context) {
    	super(context);
    	init(context);
	}
    
    public LunarListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
    
    private void init(Context c) {
        mContext = c;
        //mTracker  = VelocityTracker.obtain();
        mTempTime = new Time(MyFixed.TIMEZONE);
        if (mScale == 0) {
            mScale = c.getResources().getDisplayMetrics().density;
            if (mScale != 1) {
                topOffset *= mScale;
            }
        }
        
        ViewConfiguration vc = ViewConfiguration.get(c);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        //if(D) Log.d(TAG, "mMinFlingVelocity:  "+mMinFlingVelocity+"    : " + mMaxFlingVelocity);// 75pixel /s ~12000
        
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
        
        if (Math.abs(velocityY)<300) return;
        
        /**
         * realindex == firstIndex : 视图内只有2个view，且第一个占据空间大
         * realinde == firstIndex +1 ： 1）视图有2个view，第二个占据空间大
         * 								2）视图有3个view，中间的占据最大
         */
        int realIndex; // 
        boolean indexFlag = false;
        realIndex = getRealIndex(); // action up 时位置的计算
        
        if (realIndex != firstIndex){
        	indexFlag = true;
        }
        
        int dis= 0;
        int itemToJump = 0;
        float value = Math.abs(velocityY);
        int offset = firstIndexVisibleHeight + topOffset;
        if (value < FLING_LEVEL_1) {
            if (velocityY < 0) {// 上滑 ok
                /** 1 + offset ( */
            	itemToJump = 1;
//            	if (indexFlag == true) {
//					/**
//					 * target position realIndex+1 
//					 * dis = firstIndexVisibleHeight+ child1 height
//					 */
//            		for(int i= 0;i<itemToJump;i++) {
//            			dis += calculateHeightByIndex(realIndex+i);
//            		}
//            		dis += offset;
//            		//dis = calculateHeightByIndex(realIndex)+offset;
//            	}else {
//            		/**
//            		 * target position realIndex+1 
//            		 */
//            		
//            		for(int i= 0;i<itemToJump-1;i++) {
//            			dis += calculateHeightByIndex(realIndex+i);
//            		}
//            		dis += offset;
//            		//dis += offset;
//            	}
            } else {// 下滑
                itemToJump = -1;
//                if (indexFlag == true) {//ok
//                	/**
//                	 * goto reaIndex -1
//                	 * dis = - child0 height + firstIndexVisibleHeight
//                	 */
//                	for (int i = itemToJump;i<0;i++) {
//                		dis -= calculateHeightByIndex(realIndex+i);
//                	}
//                	dis += offset;
//                	
//                }else {
//                	for(int i = itemToJump;i<=0;i++) {
//                		dis -= calculateHeightByIndex(realIndex+i);
//                	}
//                	
//                	dis += offset;
//                }
            }
		} else if (value >= FLING_LEVEL_1 && value < FLING_LEVEL_2) {
			if (velocityY < 0) {
				itemToJump = 2;
				// if (indexFlag == true) {
				// for (int i=0;i<2;i++) {
				// dis += calculateHeightByIndex(realIndex+i);
				// }
				// dis += offset;
				// }else {
				// dis = calculateHeightByIndex(realIndex) + offset;
				// }
			} else {
				itemToJump = -2;
				// if(indexFlag == true) {
				// dis =
				// -calculateHeightByIndex(realIndex-1)-calculateHeightByIndex(realIndex-2)+
				// firstIndexVisibleHeight+topOffset;
				// }else {
				// dis = - calculateHeightByIndex(realIndex-1)-
				// calculateHeightByIndex(realIndex)-calculateHeightByIndex(realIndex-2)+firstIndexVisibleHeight+topOffset;
				// }
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

		//dis = calculateDistance(itemToJump, indexFlag);
        dis = calculateDistance(realIndex, itemToJump, indexFlag);
		if (itemToJump == 0 || dis == 0)
			return;
		smoothScrollBy(dis, Math.abs(dis) * 3);
		firstIndexVisibleHeight = 0;
        
        

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
    
    private int calculateHeightByIndex(int index) {
    	int h = -1;
    	int year = MyFixed.MIN_YEAR + index/12;
    	int month = index%12;
    	Time tmp = new Time(MyFixed.TIMEZONE);
        tmp.set(1, month, year);
        tmp.normalize(true);
    	
        int days = tmp.getActualMaximum(Time.MONTH_DAY);
		int firstDayOfWeek = tmp.weekDay;// 0~6
		
		int tmp1 = firstDayOfWeek;
		int tmp_week=1;
		for (int i=2;i<=days;i++) {
			tmp1 = (tmp1+1)%7;
			if(tmp1==0) tmp_week++;
		}
		h = tmp_week* LunarView.dayHeight + LunarView.monthTextHeight;
    	
    	return h;
    }
    
    private int calculateDistance(int index,int itemToJump, boolean flag) {
    	int dis=-1;
    	int offset = firstIndexVisibleHeight + topOffset;
    	if (itemToJump >0) {//上滑
    		itemToJump = flag?itemToJump:itemToJump-1;  		
    		for(int i = 0;i<itemToJump;i++) {
    			dis += calculateHeightByIndex(index+i);
    		}
    	}else {//下滑
    		if(flag ==true) {
    			for (int i = itemToJump;i<0;i++) {
            		dis -= calculateHeightByIndex(index+i);
            	}
    		}else {
    			for (int i = itemToJump;i<=0;i++) {
            		dis -= calculateHeightByIndex(index+i);
            	}
    		}
    	}
    	dis += offset;
    	return dis;
    }
    
    /**
     * 根据显示内容的多少确定index 位置
     * 页面同时显示1~ 2~3个月份
     * @return
     */
    private int getRealIndex() {
    	LunarView child = (LunarView) getChildAt(0);
        if (child == null) {
            return -1;
        }
        int count = getChildCount();// child 数量
        Time tmp = new Time(MyFixed.TIMEZONE);
        tmp.set(child.getmToday());
        tmp.normalize(true);
        int index0 = MethodUtil.timeToIndex(tmp);// child 0 index
        
        if(D) Log.d(TAG, "child 0-->"+tmp.year+"-"+(tmp.month+1)+"---children--->"+ count);
        
        if (count==3) return (index0+1);// 包含3个子view，中间的是主index
        
        if (count ==2) {
			int height0 = child.getHeight();
			child.getLocalVisibleRect(mFirstViewRect);
			firstIndexVisibleHeight = mFirstViewRect.bottom
					- mFirstViewRect.top;
			firstIndex = index0;

			LunarView child1 = (LunarView) getChildAt(1);
			int height1 = child1.getHeight();
			mFirstViewRect = null;
			mFirstViewRect = new Rect();
			child1.getLocalVisibleRect(mFirstViewRect);
			int secondIndexVisibleHeight = mFirstViewRect.bottom
					- mFirstViewRect.top;
			if (firstIndexVisibleHeight < secondIndexVisibleHeight) {
				return firstIndex + 1;
			}
			
        }
        return firstIndex;
    }
    
  
    
}
