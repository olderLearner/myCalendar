package fxj.calendar.solar;

import java.text.Format;
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

public class SolarView extends View {

	private static final String TAG = "SolarView";

	public static final String VIEW_PARAMS_YEAR = "year";
	public static final String VIEW_PARAMS_MONTH = "month";
	public static final String VIEW_PARAMS_DAY = "day";
	public static final String VIEW_PARAMS_CLICKED_MONTH = "clicked_month";
	public static final String VIEW_PARAMS_CLICKED_YEAR = "clicked_year";
	public static final String VIEW_PARAMS_CLICKED_DAY = "clicked_day";
	
	private boolean[] eventDays= new boolean[31];
	private static final int eventDotPadding = 55;
	
	// ԭ������
	public static final int monthTextHeight = 35;
	private static final int monthTextPadding = 2;
	private static final int xOrigin = 0;
	private static final int yOrigin = monthTextHeight;

	// ��ͼ��� �߶�
	private static int viewWidth;	
	public static int getViewWidth() {
		return viewWidth;
	}
	private int viewHeight; // ���·�����ȷ��

	// ����   ��� �߶�
	private int dayWidth ;
	public static final int dayHeight = 70;	
	public static final int dayTextPadding = 35;
	
	private int days; // ������
	private int weeks;// ������
	public int getWeeks() {
		return weeks;
	}

	private int firstDayOfWeek;// ��һ���ܼ�
	private int lastDayOfWeek;// ���һ���ܼ�
	
	public int currentYear,currentMonth;
	public int currentDay=-1,currentDay1=-1,currentDayIndex=-1;
	
	// ���ֵĴ�С����ɫ
	private static final int monthTextSize = 29;
	private int nowMonthTextColor;
	private int monthTextColor;
	private static final int dayTextSize = 29;
	private static final int lunarTextSize = 16;
	private static final int dayTextColor = Color.BLACK;
	private int weekendColor;
	private int todayColor;
	
	// ����������ֵı����뾶
	private static final int currentDayCircleRadius = 18;
	// �ָ��� ��ɫ
	private int dividerLineColor;

	private Paint mPaint; // ��������
	private Paint lunarPaint; // ũ������
	
	private Time mToday; // ���������ݵ�Time����
	public Time getmToday() {
		return mToday;
	}

	public void setmToday(Time mToday) {
		this.mToday = mToday;
	}
	
	private java.util.Calendar calendar;
	public java.util.Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(java.util.Calendar calendar) {
		this.calendar = calendar;
	}
	
	private MyApp myApp;

	public MyApp getMyApp() {
		return myApp;
	} 

	public void setMyApp(MyApp myApp) {
		this.myApp = myApp;
	}
	
	LinearLayout mLinearLayout;
	private Context context;

	
	private int mClickedDayIndex = -1; // ���������
	private int mClickedMonthIndex = -1; // ������·�
	private int mClickedYearIndex = -1; // ��������	
	protected boolean hasClickedMonth = false;
	private boolean hasClickedDay = false;
	private boolean hasClickedYear = false;
	
	private LunarCalendar lunarCalendar;

	

	public SolarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initPaint();
		mToday = new Time(MyFixed.TIMEZONE);
		mToday.setToNow();
		initParam(mToday);
		initColor();
	}

	public SolarView(Context context, Time now) {
		super(context);
		this.mToday = now;
		this.context = context;
		initPaint();
		initParam(now);
		initColor();
		
	}

	public SolarView(Context context, java.util.Calendar calendar) {
		super(context);
		this.calendar = calendar;
		this.context = context;
		initPaint();
	
		initParam(calendar);
		initColor();
	}
	
	
	
	public SolarView(Context context) {
		super(context);
		this.context = context;
		initPaint();		
		//mToday = new Time();
		//mToday.setToNow();
		initColor();

	}
	
	
	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		lunarPaint = new Paint();
		lunarPaint.setAntiAlias(true);
		lunarCalendar = new LunarCalendar();
	}
	
	private void initColor() {
		Resources res = context.getResources();		
		dividerLineColor = res.getColor(R.color.divider_line_color); // ��ɫ
		weekendColor = res.getColor(R.color.weekendtext_color);
		todayColor = res.getColor(R.color.today_color);
		dividerLineColor = res.getColor(R.color.divider_line_color); // ��ɫ
		nowMonthTextColor = res.getColor(R.color.nowMonthTextColor);
		monthTextColor = res.getColor(R.color.monthTextColor);
	}
	

	/**
	 * adapter ���ô˷�����mToday��ʼ��Ϊ����������Ļ�����
	 * 
	 * @param params
	 */
	public void setMonthParams(HashMap<String, Integer> params) {

		//Log.d(TAG, "MonthView.setMonthParams-->" + mToday.year + "month");
		
		mToday = new Time(MyFixed.TIMEZONE);
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

	/**
	 * 
	 * @param time
	 * @return
	 */
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
		weeks =tmp_week;// 1~ 4 5 6 
		firstDayOfWeek++;// 1~7
		lastDayOfWeek = tmp + 1; // 1~7
			
		return weeks;
	}
	

	@Override
	public void draw(Canvas canvas) {

		//Log.d(TAG, "SolarView.draw()");
		
		drawDivider(canvas, mPaint, weeks, xOrigin, yOrigin);
		drawDay(canvas, mPaint, lunarPaint, xOrigin, yOrigin);
		
	}

	
	private void drawDivider(Canvas canvas, Paint paint, int weekNum, float startX, float startY) {
		String text = String.format(
				getResources().getString(R.string.month_format), currentMonth+1);
		paint.setColor(monthTextColor);
		paint.setTextSize(monthTextSize);
		canvas.drawText(text, startX + (firstDayOfWeek - 1) * dayWidth
				+ (dayWidth - paint.measureText(text)) / 2, startY
				- monthTextPadding, paint);
		
		/*
		 * ������һ��͸����ͼ���ǵ�������
		 * 2015 8 17
		 * ��drawRect ����drawLine ��ʱ���
		 */
		paint.setColor(dividerLineColor);
		
		//paint.setColor(Color.BLACK);
		//paint.setAlpha(255);
		//paint.setTextSize(0.5f); useless
		//float tmp = paint.getStrokeWidth();
		//paint.setStrokeWidth(0.5f);
		
		for (int i = 0; i <= weekNum-1; i++) {
			if (i == 0) {
				canvas.drawRect((firstDayOfWeek - 1) * dayWidth, i * dayHeight
						+ startY, viewWidth, i * dayHeight + startY+0.75f, paint);
			} else if (i == weekNum - 1) {
				canvas.drawRect(startX, i * dayHeight + startY, lastDayOfWeek* dayWidth, i * dayHeight + startY+0.75f, paint);
				
				//canvas.drawLine(startX, i * dayHeight + startY, lastDayOfWeek
						//* dayWidth, i * dayHeight + startY, paint);
			} else {
				canvas.drawRect(startX, i * dayHeight + startY, viewWidth, i
						* dayHeight + startY+0.75f, paint);
			}

		}
		//paint.setStrokeWidth(tmp);
	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param lunar
	 * @param startX
	 * @param startY
	 */
	private void drawDay(Canvas canvas, Paint paint, Paint lunar, float startX, float startY) {
		
		int drawDayRow = 1;
		int day;
		float drawX, drawY;
		int weekIndex = -1;
		
		/*
		 * whether today in this month
		 */
		int mNowDay = -1;
		boolean isToday= false;
		Time time = new Time(MyFixed.TIMEZONE);
		time.setToNow();
		time.normalize(true);
		isToday = time.year == currentYear && time.month == currentMonth;
		if (isToday && mNowDay == -1) {
			mNowDay = time.monthDay;
		}
		
		/*
		 * draw month text
		 */
		String text = String.format(
				getResources().getString(R.string.month_format), currentMonth+1);
		paint.setColor(isToday?nowMonthTextColor:monthTextColor);
		paint.setTextSize(monthTextSize);
		canvas.drawText(text, startX + (firstDayOfWeek - 1) * dayWidth
				+ (dayWidth - paint.measureText(text)) / 2, startY - monthTextPadding, paint);
		
		/*
		 * ȷ�����������
		 */
		boolean isClickedDay = false;
		/*
		if (currentYear == mClickedYearIndex && currentMonth == mClickedMonthIndex) {
			isClickedDay = true;
		}*/
		//Time tmp1 = new Time(mTimeZone);
		//tmp1.set(myApp.getmCurrentClickedTime());
		
		time.set(myApp.getmCurrentClickedTime());
		time.normalize(true);
		isClickedDay = time.year == currentYear && time.month == currentMonth;
		if (isClickedDay ) {
			mClickedDayIndex = time.monthDay;
		}
		
		getEventDays(currentYear, currentMonth);
		
		
		paint.setTextSize(dayTextSize);		
		lunar.setTextSize(lunarTextSize);
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

			
			
			String dayText = String.valueOf(day);
			String lunarDay = lunarCalendar.getLunarDate(currentYear,
					currentMonth, day, false);

			drawX = startX + weekIndex * dayWidth
					+ (dayWidth - paint.measureText(dayText)) / 2;
			drawY = startY + (drawDayRow - 1) * dayHeight + dayTextPadding;
			
			/*
			 * �м�¼�����ڣ��������滭Բ��
			 */
			if (eventDays[day-1] == true) {
				int color = paint.getColor();
				paint.setColor(dividerLineColor);
				float c_x = startX + weekIndex * dayWidth+ dayWidth / 2;
				float c_y = startY + (drawDayRow - 1) * dayHeight + eventDotPadding;

				canvas.drawCircle(c_x, c_y, paint.getTextSize() / 6, paint);
						
				paint.setColor(color);
			}
			
			
			if (weekIndex == 0 || weekIndex == 6) {
				paint.setColor(dividerLineColor);// ������������ĩ ��ɫ����
				lunar.setColor(dividerLineColor);
				sunsat = true;
			} else {
				paint.setColor(dayTextColor); // ��ɫ����
				lunar.setColor(dividerLineColor);
			}
			if (isToday && mNowDay == day) {
				paint.setColor(todayColor);// ���������ǽ��� ��ɫ����
				lunar.setColor(todayColor);
			}
			
			
			
			
			if (isClickedDay) {
				// ȷ����������ڣ���������ɫ
				if (mFlag == false && mClickedDayIndex == day) {
					if (isToday && mNowDay == day) {
						paint.setColor(todayColor);
					} else {
						paint.setColor(sunsat ? dividerLineColor : dayTextColor);
					}

					canvas.drawCircle(
							drawX + paint.measureText(dayText) / 2,
							drawY - (paint.getTextSize() - paint.descent()) / 2,
							currentDayCircleRadius, paint);
					paint.setColor(Color.WHITE);
					mFlag = true;

					// ��������ͷ��ò��Ӧ����adapter����
				}
			}


			canvas.drawText(dayText, drawX, drawY, paint);

			/*canvas.drawText(lunarDay, startX + weekIndex * dayWidth
					+ (dayWidth - lunar.measureText(lunarDay)) / 2, drawY
					+ lunar.getTextSize() + 10, lunar);*/
		}
		
	}

	/**
	 * ��ѯָ���·����м�¼������
	 * @param year
	 * @param month
	 */
	private void getEventDays(int year, int month) {

		for (int i= 0;i<eventDays.length;i++) {
			eventDays[i]  = false;
		}
		
		String sql = CalendarDBService.FIND_MONTH_EVENT_PART1 + year + "-" +String.format("%02d",(month+1))+ CalendarDBService.FIND_MONTH_EVENT_PART2;
		Cursor cursor = MonthActivity.calendarDBService.execSQL(sql);
		
		while(cursor.moveToNext()) {
			
			int day = Integer.valueOf(cursor.getString(0)) - 1;
			//int day = cursor.getInt(0)-1; // getInt() 8 9,����ת�������� ����ֵ�� 0
			//Log.d(TAG, "jieguo-->" + cursor.getInt(0)+"  convert to int " + Integer.valueOf(cursor.getString(0)));
			if (day == -1) day=1;
			eventDays[day] = true;
		}
		
	}

	/**
	 * �ɵ������������������·ݣ����ص�������ڶ��� time
	 * @param x  �������x����
	 * @param y  �������y����
	 * @return time ���������
	 */
	public Time setClickedDay(float x, float y) {
		Time month = getClickDayFromLocation(x, y);
		if (month != null) {
			mClickedMonthIndex = month.month;
			mClickedDayIndex = month.monthDay;
			mClickedYearIndex= month.year;
		}
		//this.invalidate();
		return month;
	}

	/**
	 * �����������ݷ�Χ�ڷ���Time���񷵻�null 
	 * 
	 * @param x
	 * @param y
	 * @return Time ������ǰ������µ�Time����
	 */
	public Time getClickDayFromLocation(float x, float y) {
		
		if (y <= yOrigin)
			return null;

		int row = (int) (y - yOrigin) / dayHeight;
		int col = (int) (x - xOrigin) / dayWidth;
		if (row == 0 && col < (firstDayOfWeek - 1) || row == (weeks - 1)
				&& col > (lastDayOfWeek - 1)) {
			return null;
		} else {
			Time time = new Time(MyFixed.TIMEZONE);
			int day = row * 7 + col + 2 - firstDayOfWeek;
			time.set(day, mToday.month, mToday.year);
			time.normalize(true);
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

		/*Log.d(TAG,"SolarView.onMeasure:fatherViewwidth-->"+ MeasureSpec.getSize(widthMeasureSpec)
				+ "--fatherViewHeight--"+ MeasureSpec.getSize(heightMeasureSpec)); */
		
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		dayWidth = viewWidth /7;
		viewHeight = monthTextHeight + weeks * dayHeight;
		setMeasuredDimension(viewWidth, viewHeight);
		
		/*Log.d(TAG, "SolarView.onMeasure()-->width" + getMeasuredWidthAndState()
				+ "--height--" + getMeasuredHeightAndState());*/
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		viewWidth = w;
		
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
