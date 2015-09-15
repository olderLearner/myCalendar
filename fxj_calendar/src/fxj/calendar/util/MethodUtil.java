package fxj.calendar.util;

import android.text.format.Time;

public class MethodUtil {
	
	
	public static final int MH_ACCELERATE_DECELETATE = 0;
	
	// pixel 
	private int distance;
	// millisecond
	private int totalTime;
	//
	private int step;
	private static int intervalTime;
	private int model;
	
	public MethodUtil(int distance, int millis, int step) {
		this.distance = distance;
		
	}
	
	public static int[] smoothModifyHeight(int distance, int millis, int step) {
		int[] intResult = new int[step*2];
		float[] floatResult = new float[step*2];
		
		float fdis = distance;
		float fmillis = millis;
		float fstep = step;
		
		float mSecond = fmillis/1000;		
		float mIntervalTime = mSecond/(2*fstep);		
		float mAccelerator = (4*fdis) / (mSecond*mSecond);
		
		float distance0_1 = mAccelerator*mIntervalTime*mIntervalTime/2;

		
		for (int i = 0;i<step;i++) {
			floatResult[i] = (2*i +1)*distance0_1;
			floatResult[2*step -1 -i] =floatResult[i];
		}
		
		int tmp = 0;
		float ftmp = 0;
		
		for (int k = 0;k<2*step;k++) {
			floatResult[k] += ftmp;
			intResult[k] = Math.round(floatResult[k]);
			ftmp += floatResult[k] - intResult[k];
			
			tmp += intResult[k];
			//System.out.println("intResult["+ k+ "] "+intResult[k]+"  ftmp  "+ ftmp+"  tmp  "+ tmp);
			
		}	
		return intResult;
		
	}
	
	
	public static int[] smoothModifyHeight(int distance, int millis, int step, int model) {
		int[] intResult = new int[step*2];
		float[] floatResult = new float[step*2];
		
		float fdis = distance;
		float fmillis = millis;
		float fstep = step;
		
		float mSecond = fmillis/1000;		
		float mIntervalTime = mSecond/(2*fstep);		
		float mAccelerator = (4*fdis) / (mSecond*mSecond);
		
		float distance0_1 = mAccelerator*mIntervalTime*mIntervalTime/2;

		
		for (int i = 0;i<step;i++) {
			floatResult[i] = (2*i +1)*distance0_1;
			floatResult[2*step -1 -i] =floatResult[i];
		}
			
		return intResult;
		
	}
	
	
	/**
	 * 计算一个月的天数
	 * @param y 年
	 * @param m 月 0~11
	 * @return
	 */
	public static int getDay(int y, int m) {

		m++;
		switch (m) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 2: {
			if ((y % 4 == 0) && (y % 100 == 0) || (y % 400 == 0))
				return 29;
			else
				return 28;
		}
		case 4:
		case 6:
		case 9:
		case 11:
			return 30;
		}
		return 0; // error result
	}
	
	public static Time indexToTime(int index) {
		Time time = new Time(MyFixed.TIMEZONE);
		time.setToNow();
		time.normalize(true);
		int year = MyFixed.MIN_YEAR + index/12;
		int month = index%12;
		int day = 1;
		if (time.year != year || time.month != month) {
			time.set(day, month, year);
		}
		return time;
	}
	
	public static int timeToIndex(Time time) {
		int index = -1;
		int year = time.year;
		int month = time.month;
		index = 12*(year - MyFixed.MIN_YEAR) + month;
		
		return index;
	}
	
	
}
