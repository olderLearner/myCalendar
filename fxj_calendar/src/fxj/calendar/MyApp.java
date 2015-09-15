package fxj.calendar;

import fxj.calendar.util.MyFixed;
import android.app.Application;
import android.text.format.Time;

public class MyApp extends Application {

	private Time mCurrentClickedTime;
	/**
	 * the time that clicked recently
	 * @return time 
	 */
	public Time getmCurrentClickedTime() {
		return mCurrentClickedTime;
	}

	/**
	 * set the time that clicked now
	 * @param mCurrentClickedTime
	 */
	public void setmCurrentClickedTime(Time mCurrentClickedTime) {
		this.mCurrentClickedTime = mCurrentClickedTime;
	}

	@Override
	public void onCreate() {
		Time time = new Time(MyFixed.TIMEZONE);
		time.setToNow();
		time.normalize(true);
		this.setmCurrentClickedTime(time);
		super.onCreate();
	}

	
}
