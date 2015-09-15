package fxj.calendar.lunar;

import java.util.HashMap;

import fxj.calendar.MonthActivity;
import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.db.CalendarDBService;
import fxj.calendar.util.LunarCalendar;
import fxj.calendar.util.MethodUtil;
import fxj.calendar.util.MyFixed;
import android.R.color;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.AbsListView.LayoutParams;
import android.widget.LinearLayout;

public class LunarView extends View {

	private static final String TAG = "LunarView";

	public static final String VIEW_PARAMS_YEAR = "year";
	public static final String VIEW_PARAMS_MONTH = "month";
	public static final String VIEW_PARAMS_DAY = "day";
	public static final String VIEW_PARAMS_CLICKED_MONTH = "clicked_month";
	public static final String VIEW_PARAMS_CLICKED_YEAR = "clicked_year";
	public static final String VIEW_PARAMS_CLICKED_DAY = "clicked_day";
	
	
	private MyApp myApp;
	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}
		
	// offset between day and list view up
	public static final int monthTextHeight = 60;
	private static final int monthTextPadding = 15;
	private static final int xOrigin = 0;
	private static final int yOrigin = monthTextHeight;
	// ���ֵĴ�С����ɫ
	private static final int monthTextSize = 28;
	private int nowMonthTextColor;
	private int monthTextColor;
	private static final int dayTextSize = 28;
	private static final int lunarTextSize = 15;
	private static final int dayTextColor = Color.BLACK;
	private int weekendColor;
	private int todayColor;
	
	private static int viewWidth;// ��ͼ��� �߶�	
	public static int getViewWidth() {
		return viewWidth;
	}
	private int viewHeight; // ������ȷ��ÿ�µĸ߶�
	private static int monthWidth;// �¿��	
	private int dayWidth ;// ���ڿ��
	public static final int dayHeight = 95;// ���� �߶�
	public static final int dayTextPadding = 35;
	public static final int lunarDayTextPadding = 10;
	// �����������ֵı����뾶
	private static final int currentDayCircleRadius = 19;
	// �ָ�����ɫ
	private int dividerLineColor;
		
	/*
	 * 
	 */
	private int days; // ������
	private int weeks;// ������
	public int getWeeks() {
		return weeks;
	}
	private int firstDayOfWeek;// ��һ���ܼ�
	private int lastDayOfWeek;// ���һ���ܼ�
	
	public int currentYear,currentMonth;
	public int currentDay=-1,currentDay1=-1,currentDayIndex=-1;
		

	Paint mPaint; // ��������
	Paint mLunarPaint;// lunar ����
	Time mToday; // ���������ݵ�Time����
	java.util.Calendar calendar;
	private LunarCalendar lunarCalendar;
	private String mTimeZone = Time.getCurrentTimezone();
	
	LinearLayout mLinearLayout;
	
	private Context mContext;

	
	private int mClickedYearIndex = -1;
	private int mClickedMonthIndex = -1; 
	private int mClickedDayIndex = -1;
	private boolean hasClickedDay = false;
	private boolean hasClickedMonth = false;
	private boolean hasClickedYear = false;
	private int mClickedIndex;

	private boolean[] eventDays = new boolean[31];
	private static final int eventDotPadding = 75;
	
	
	
	public int getmClickedIndex() {
		return mClickedIndex;
	}

	public void setmClickedIndex(int Index) {
		this.mClickedIndex = mClickedDayIndex;
	}

	
	
	public LunarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
		mToday = new Time(mTimeZone);
		mToday.setToNow();
		initColor();
	}

	public LunarView(Context context, Time now) {
		super(context);
		this.mToday = now;
		initPaint();		
		//myApp = (MyApp) context.getApplicationContext();		
		initParam(now);		
		initColor();
	}

	public LunarView(Context context, java.util.Calendar calendar) {
		super(context);
		this.calendar = calendar;
		initPaint();
		
		initParam(calendar);		
		initColor();
	}
		
	public LunarView(Context context) {
		super(context);
		this.mContext = context;
		mToday = new Time();
		mToday.setToNow();
		
		initPaint();
		initColor();
	}
	
	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mLunarPaint = new Paint();
		mLunarPaint.setAntiAlias(true);
		lunarCalendar = new LunarCalendar();
	}
	
	private void initColor() {
		Resources res = mContext.getResources();		
		dividerLineColor = res.getColor(R.color.divider_line_color); // ��ɫ
		weekendColor = res.getColor(R.color.weekendtext_color);
		todayColor = res.getColor(R.color.today_color);
		dividerLineColor = res.getColor(R.color.divider_line_color); // ��ɫ
		nowMonthTextColor = res.getColor(R.color.nowMonthTextColor);
		monthTextColor = res.getColor(R.color.monthTextColor);
	}
	
	/**
	 * adapter ���ô˷�����mToday ��ʼ��Ϊ����������Ļ�����
	 * 
	 * @param params
	 */
	public void setMonthParams(HashMap<String, Integer> params) {

		Log.d(TAG, "MonthView.setMonthParams-->" + mToday.year + "month");
		
		mToday = new Time(mTimeZone);
		mToday.set(1, params.get(VIEW_PARAMS_MONTH), params.get(VIEW_PARAMS_YEAR));
		setTag(params);
		initParam(mToday);
		
		if (params.containsKey(VIEW_PARAMS_CLICKED_DAY)) {
			mClickedDayIndex = params.get(VIEW_PARAMS_CLICKED_DAY);
		}
		hasClickedDay = mClickedDayIndex != -1;
		if (params.containsKey(VIEW_PARAMS_CLICKED_MONTH)) {
			mClickedMonthIndex = params.get(VIEW_PARAMS_CLICKED_MONTH);
		}
		hasClickedMonth = mClickedMonthIndex != -1;
		if (params.containsKey(VIEW_PARAMS_CLICKED_YEAR)) {
			mClickedYearIndex = params.get(VIEW_PARAMS_CLICKED_YEAR);
		}
		hasClickedYear = mClickedYearIndex != -1;
		

	}
	
	public java.util.Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(java.util.Calendar calendar) {
		this.calendar = calendar;
		initParam(this.calendar);
	}
			
	public Time getmToday() {
		return mToday;
	}

	public void setmToday(Time mToday) {
		this.mToday = mToday;
		initParam(this.mToday);
	}

	/**
	 * ��ȡcalendar����Я���Ĳ��������� onDraw
	 * @param calendar
	 * @return 
	 */
	private int initParam(java.util.Calendar calendar) {
	
		currentYear = calendar.get(java.util.Calendar.YEAR);
		currentMonth = calendar.get(java.util.Calendar.MONTH);
		currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH);
		days = MethodUtil.getDay(currentYear, currentMonth);
		calendar.set(currentYear, currentMonth, days);
		// ��Ҫ�Ĳ������ڼ���view�ĸ߶�
		weeks = calendar.get(java.util.Calendar.WEEK_OF_MONTH);	
		lastDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);// 1~7
		calendar.set(currentYear, currentMonth, 1);
		firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);// 1~7
		
		
		return weeks;
	}

	private int initParam(Time time) {
		
		currentYear = time.year;// 
		currentMonth = time.month;//0~11
		currentDay = time.monthDay;// 1~31			
		days = time.getActualMaximum(Time.MONTH_DAY);
		
		time.set(1, currentMonth, currentYear);
		time.normalize(true);// ������ã����� ���ò��ܳɹ�
		//String str = time.toString(); // ����
		firstDayOfWeek = time.weekDay;// 0~6
		
		int tmp = firstDayOfWeek;
		int tmp_week=1;
		for (int i=2;i<=days;i++) {
			tmp = (tmp+1)%7;
			if(tmp==0) tmp_week++;
		}
		weeks =tmp_week;// 1~
		firstDayOfWeek++;
		lastDayOfWeek = tmp + 1; // 0~6
			
		return weeks;
	}


	@Override
	public void draw(Canvas canvas) {

		//Log.d(TAG, "MonthView.draw()");
		
		drawDivider(canvas, mPaint, weeks, xOrigin, yOrigin);
		drawDay(canvas, mPaint, mLunarPaint, xOrigin, yOrigin);
		
	}

	
	private void drawDivider(Canvas canvas, Paint paint, int weekNum, float startX, float startY) {
		
		paint.setColor(dividerLineColor);
		for (int i = 0; i <= weekNum-1; i++) {
			if (i == 0) {
				canvas.drawRect((firstDayOfWeek - 1) * dayWidth, i * dayHeight
						+ startY, viewWidth, i * dayHeight + startY+0.75f, paint);
			} else if (i == weekNum-1) {
				canvas.drawRect(startX, i * dayHeight + startY, lastDayOfWeek* dayWidth, i * dayHeight + startY+0.75f, paint);
			} else {
				canvas.drawRect(startX, i * dayHeight + startY, viewWidth, i
						* dayHeight + startY+0.75f, paint);
			}

		}

	}


	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param startX
	 * @param startY
	 */
	private void drawDay(Canvas canvas, Paint paint, Paint lunar, float startX, float startY) {

		
		int drawDayRow = 1;
		int day;
		float drawX, drawY;
		int weekIndex = -1;
			
		/*
		 * �ж�����
		 */
		int mClickedNowDay=-1;
		boolean isToday = false;
		Time tmp = new Time(mTimeZone);
		tmp.setToNow();
		tmp.normalize(true);
		isToday = tmp.year == currentYear && tmp.month == currentMonth;
		if (isToday && mClickedNowDay == -1) {
			mClickedNowDay = tmp.monthDay;
		}
		
		/*
		 * draw month text
		 */
		String text = String.format(
				getResources().getString(R.string.month_format), currentMonth+1);
		paint.setColor(isToday?nowMonthTextColor:monthTextColor);
		paint.setTextSize(monthTextSize);
		canvas.drawText(text, (firstDayOfWeek - 1) * dayWidth
				+ (dayWidth - paint.measureText(text)) / 2, monthTextHeight
				- monthTextPadding, paint);
		
		paint.setTextSize(dayTextSize);
		lunar.setTextSize(lunarTextSize);
		
		
		/*
		 * �ж��������
		 */
		boolean isClickedDay = false;
		/*
		if (currentYear == mClickedYearIndex && currentMonth == mClickedMonthIndex) {
			isClickedDay = true;
		}
		*/
		
		/** 
		 * application ����
		 */		
		Time tmp1 = new Time(mTimeZone);
		tmp1.set(myApp.getmCurrentClickedTime());
		tmp1.normalize(true);
		isClickedDay = tmp1.year == currentYear && tmp1.month == currentMonth;
		if (isClickedDay ) {
			mClickedDayIndex = tmp1.monthDay;
		}
		
		getEventDays(currentYear, currentMonth);
		
		boolean mFlag = false;
				
		for (day = 1; day <= days; day++) {
			
			boolean sunsat = false;// flag ��������������Ƿ�����ĩ
			if (day == 1) {
				weekIndex = firstDayOfWeek - 1;
			} else {
				weekIndex = (weekIndex + 1) % 7;
			}

			if (weekIndex == 0 && day != 1) {
				drawDayRow++;
			}

			// text
			String dayText = String.valueOf(day);
			String lunarDay = lunarCalendar.getLunarDate(currentYear,
					currentMonth+1, day, false);
			// location
			drawX = startX + weekIndex * dayWidth
					+ (dayWidth - paint.measureText(dayText)) / 2;
			drawY = startY + (drawDayRow - 1) * dayHeight + dayTextPadding;
			
			if (eventDays[day-1] == true) {
				int color = paint.getColor();
				paint.setColor(dividerLineColor);
				float c_x = startX + weekIndex * dayWidth+ dayWidth / 2;
				float c_y = startY + (drawDayRow - 1) * dayHeight + eventDotPadding;

				canvas.drawCircle(c_x, c_y, paint.getTextSize() / 6, paint);
						
				paint.setColor(color);
			}
			
			
			
			// paint color
			if (weekIndex == 0 || weekIndex == 6) {
				paint.setColor(weekendColor);// ��ĩ
				lunar.setColor(weekendColor);
				sunsat = true;
			} else {
				paint.setColor(dayTextColor);
				lunar.setColor(weekendColor);
			}
			if (isToday && mClickedNowDay == day) {
				paint.setColor(todayColor);// ���������ǽ���
				lunar.setColor(todayColor);
			}
			
			if (isClickedDay) {
			// ȷ����������ڣ���������ɫ
			if (mFlag == false && mClickedDayIndex == day) {
				if (isToday && mClickedNowDay == day) {
					paint.setColor(todayColor);
				} else {
					paint.setColor(sunsat ? dividerLineColor : dayTextColor);
				}
				
				canvas.drawCircle(drawX + paint.measureText(dayText) / 2, drawY
						- (paint.getTextSize()-paint.descent()) / 2, currentDayCircleRadius, paint);
				paint.setColor(Color.WHITE);
				mFlag = true; 
				
				// ��������ͷ��ò��Ӧ����adapter����
			}
			}
			
			canvas.drawText(dayText, drawX, drawY, paint);
			canvas.drawText(lunarDay, startX + weekIndex * dayWidth
					+ (dayWidth - lunar.measureText(lunarDay)) / 2, drawY
					+ lunar.getTextSize() + lunarDayTextPadding, lunar);
		}

	}
	
	private void getEventDays(int year, int month) {

		for (int i= 0;i<eventDays.length;i++) {
			eventDays[i]  = false;
		}
		
		String sql = CalendarDBService.FIND_MONTH_EVENT_PART1 + year + "-" +String.format("%02d",(month+1))+ CalendarDBService.FIND_MONTH_EVENT_PART2;
		Cursor cursor = MonthActivity.calendarDBService.execSQL(sql);
		
		while(cursor.moveToNext()) {
			int day = Integer.valueOf(cursor.getString(0)) - 1;
			eventDays [day] = true;
		}
		
		
	}
	
	
	

	/**
	 * �ɵ���������������������
	 * @param x �������x����
	 * @param y �������y����
	 * @return 
	 */
	public Time setClickedDay(float x, float y) {
		Time month = getClickDayFromLocation(x, y);
		if (month != null) {
			mClickedMonthIndex = month.month;
			mClickedDayIndex = month.monthDay;
			mClickedYearIndex= month.year;
		}
		this.invalidate();
		return month;
	}

	/**
	 * �����������ݷ�Χ�ڷ���Time���񷵻�null FullYearAdapter will invoke this method
	 * 
	 * @param x
	 * @param y
	 * @return Time ������ǰ������µ�Time����
	 */
	public Time getClickDayFromLocation(float x, float y) {
		if (y < yOrigin) {
			return null;
		}
		int row = (int) (y - yOrigin) / dayHeight;
		int col = (int) (x - xOrigin) / dayWidth;

		if (row == 0 && col < (firstDayOfWeek - 1) || row == (weeks - 1)
				&& col > (lastDayOfWeek - 1)) {
			return null;
		} else {
			Time time = new Time(mTimeZone);
			int day = row * 7 + col + 2 - firstDayOfWeek;
			time.set(day, mToday.month, mToday.year);
			//myApp.setmCurrentClickedTime(time);
//Log.d(TAG, "���µĵ������Ϊ��"+myApp.getmCurrentClickedTime().toString());
			return time;
		}
	}

	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) {
	 * 
	 * float x = event.getX(); float y = event.getY(); setClickedMonth(x, y);
	 * 
	 * 
	 * performClick(); return super.onTouchEvent(event); }
	 * 
	 * @Override public boolean performClick() { Toast.makeText(context,
	 * "�����"+mClickedMonthIndex+"��", Toast.LENGTH_SHORT).show();
	 * 
	 * clearClickMonth(); return super.performClick(); }
	 */

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
/*
		Log.d(TAG,
				"MonthView.onMeasure:fatherViewwidth-->"
						+ MeasureSpec.getSize(widthMeasureSpec)
						+ "--fatherViewHeight--"
						+ MeasureSpec.getSize(heightMeasureSpec));
*/
		
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		monthWidth = viewWidth;
		dayWidth = monthWidth /7;
		viewHeight = monthTextHeight + weeks * dayHeight;
		setMeasuredDimension(viewWidth, viewHeight);

		
		/*
		Log.d(TAG, "MonthView.onMeasure()-->width" + getMeasuredWidthAndState()
				+ "--height--" + getMeasuredHeightAndState());*/
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		viewWidth = w;
		monthWidth = w;
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
