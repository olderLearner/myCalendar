package fxj.calendar.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.text.format.Time;

public class MyFixed {

	public MyFixed() {
	}

	public static final String TIMEZONE = Time.getCurrentTimezone();
	public static final int MIN_YEAR = 1949;
	public static final int MAX_YEAR = 2049;
	
	public static final long GMTOFF = 86400;
	public static final SimpleDateFormat mSDF_01 = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
	public static final SimpleDateFormat mSDF_02 = new SimpleDateFormat("yyyy年M月dd日 HH:mm",Locale.CHINA);
	public static final SimpleDateFormat mSDF_03 = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
	public static final SimpleDateFormat mSDF_04 = new SimpleDateFormat("yyyy年M月d日 HH:mm",Locale.CHINA);
}
