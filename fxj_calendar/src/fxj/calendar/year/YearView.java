package fxj.calendar.year;

import java.util.HashMap;
import java.util.ResourceBundle;




import fxj.calendar.R;
import fxj.calendar.R.string;
import android.R.color;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Toast;

public class YearView extends View {

	private static final String TAG = "WholeYearView";
	private static final boolean D = false;
	
	public static final String VIEW_PARAMS_YEAR = "year";
	public static final String VIEW_PARAMS_SELECTED_MONTH = "selected_month";
	protected static final int DEFAULT_SELECTED_MONTH = -1;
	
	// ��һ�±�ѡ���� 0-11��or ���û���·ݱ�ѡ�� ��-1
	protected int mSelectedMonth = DEFAULT_SELECTED_MONTH;
	protected boolean hasSelectedMonth = false;
	
	
	// �����ֵ���ʼ����
	private static final int x_yearOrigin = 25;
	private static final int y_yearOrigin = 45;
	// һ��������ʼ����
	private int x_monthOrigin = x_yearOrigin;
	private int y_monthOrigin = y_yearOrigin + 40;
	private int monthsStart = y_monthOrigin - monthTextSize;

	// view ��� �߶�
	private static int viewWidth;
	private static final int viewHeight = 660;
	// �·�����Ŀ�ȣ��߶�
	private static int monthWidth;
	private static final int monthHeight = 146;
	// ��������Ŀ�ȣ��߶�
	private static final int dayWidth = 20;
	private static final int dayHeight = 20;

	// ���ֵĴ�С����ɫ
	private static final int yearTextSize = 38;
	private static final int yeatTextColor = Color.BLACK;
	private static final int monthTextSize = 22;
	private static int monthTextColor;
	private static final int dayTextSize = 14;
	private static final int dayTextColor = Color.BLACK;
	// �����������ֵı����뾶
	private static final int currentDayCircleRadius = 10;
	// �� �������ݷָ��� ��ɫ
	private int dividerLineColor;

	Paint mPaint; // ��������
	Time mToday; // ���������ݵ�Time����

	private Context context;
	
	//protected Paint paint_click; // ������򻭱� 2015 7 24 ��֮ǰҪ��ʼ��������
	protected Paint paint_click = new Paint();
	
	protected Rect rect_click; // �������Χ
	
	protected int mMonthOutlineColor;
	private int mClickedMonthColor;
	private int mClickedMonthIndex = -1; //0-11
	
	private static final int mClickedAlpha = 128;
	private String mTimeZone = Time.getCurrentTimezone();

	private int year;

	public int getYear() {
		return year;
	}

	
	
	public YearView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mToday = new Time(mTimeZone);
		mToday.setToNow();
	}

	public YearView(Context context, Time now) {
		super(context);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mToday = now;
	}
	
	
	public YearView(Context context) {
		super(context);
		this.context = context;
		
		
		//Log.d(TAG, "FullYearView constructor");
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		//mToday = new Time();
		//mToday.setToNow();

		Resources res = context.getResources();
		monthTextColor = res.getColor(R.color.month_text_color);// ��ɫ
		mClickedMonthColor = res.getColor(R.color.month_clicked_bg_color);// ��ɫ
		dividerLineColor = res.getColor(R.color.divider_line_color); // ��ɫ
		mMonthOutlineColor = res.getColor(color.black);
	}

	/**
	 * adapter ���ô˷�����mToday��ʼ��Ϊ����������Ļ�����
	 * @param params
	 */
	public void setYearParams(HashMap<String, Integer> params) {
		
		mToday = new Time(mTimeZone);
		year = params.get(VIEW_PARAMS_YEAR);
		mToday.set(1, 0, year);
		setTag(params);
		if (params.containsKey(VIEW_PARAMS_SELECTED_MONTH)) {
			mSelectedMonth = params.get(VIEW_PARAMS_SELECTED_MONTH);
		}
		hasSelectedMonth = mSelectedMonth != -1;
		
		if(D) Log.d(TAG, "FullYearView.setYearparams:Year-->" + mToday.year+ "--mSelectedMonth--" + mClickedMonthIndex);
		
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		if(D) Log.d(TAG, "draw");
		drawYear(canvas, mPaint, mToday);
		drawDivider(canvas, mPaint);
		drawMonths(canvas, mPaint, mToday);
		
		drawClick(canvas);
		//super.draw(canvas);
	}

	private void drawYear(Canvas canvas, Paint p, Time t) {
		p.setTextSize(yearTextSize);
		p.setColor(yeatTextColor);
		canvas.drawText(String.format(
				getResources().getString(R.string.year_format), t.year),
				x_yearOrigin, y_yearOrigin, p);

	}

	private void drawDivider(Canvas canvas, Paint p) {
		p.setColor(dividerLineColor);
		canvas.drawRect(x_yearOrigin, y_yearOrigin + 9, viewWidth, y_yearOrigin + 9+1f, p);
		//canvas.drawLine(x_yearOrigin, y_yearOrigin + 9, viewWidth,y_yearOrigin + 9, p);
				
	}

	private void drawMonths(Canvas canvas, Paint paint, Time time) {
		int drawX, drawY;
		Time nowTime = new Time();
		nowTime.setToNow();
		boolean drawToday = false;
		for (int row = 1; row <= 4; row++) {
			for (int col = 1; col <= 3; col++) {
				int month = (row - 1) * 3 + col;
				drawX = x_monthOrigin + (col - 1) * monthWidth;
				drawY = y_monthOrigin + (row - 1) * monthHeight;
				drawMonth(canvas, paint, month, drawX, drawY);

				time.set(1, month - 1, time.year); // ��time���õ���ǰ��Ҫ����������
				/*
				 * if (time.year == nowTime.year && time.month == nowTime.month)
				 * { drawToday = true; } else {drawToday = false;}
				 */
				drawToday = time.year == nowTime.year
						&& time.month == nowTime.month;
				drawDay(canvas, paint, drawToday, time, nowTime.monthDay,
						drawX, drawY + dayHeight);

			}
		}

	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param month
	 *            1-11
	 * @param startX
	 * @param startY
	 */
	private void drawMonth(Canvas canvas, Paint paint, int month, int startX,
			int startY) {

		paint.setTextSize(monthTextSize);
		paint.setColor(monthTextColor);
		canvas.drawText(String.format(
				getResources().getString(R.string.month_format), month),
				startX, startY, paint);

	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param isToday
	 * @param time
	 * @param today
	 *            current day number 1-31
	 * @param startX
	 * @param startY
	 */
	private void drawDay(Canvas canvas, Paint paint, boolean isToday,
			Time time, int today, int startX, int startY) {

		paint.setTextSize(dayTextSize);
		int drawDayRow = 1;
		int day;
		int drawX, drawY;
		int weekIndex = -1;

		for (day = 1; day <= 31; day++) {
			if (day <= time.getActualMaximum(Time.MONTH_DAY)) {

				if (day == 1) {
					time.set(day, time.month, time.year);
					time.normalize(true);
					weekIndex = time.weekDay;
				} else {
					weekIndex = (weekIndex + 1) % 7;
				}

				if (weekIndex == 0 && day != 1) {
					drawDayRow++;
				}

				drawX = startX + weekIndex * dayWidth;
				drawY = startY + (drawDayRow - 1) * dayHeight;

				if (isToday && today == day) {
					paint.setColor(monthTextColor);
					canvas.drawCircle(drawX + 8, drawY -5,
							currentDayCircleRadius, paint);
					paint.setColor(Color.WHITE);
				} else {
					paint.setColor(dayTextColor);
				}

				if (day < 10) {
					drawX += 4;
				}

				canvas.drawText(day + "", drawX, drawY, paint);

			} else {
				return;
			}

		}

	}

	private void drawClick(Canvas canvas) {
		if (mClickedMonthIndex != -1) {
			
			int alpha = paint_click.getAlpha();
			paint_click.setColor(mClickedMonthColor);
			paint_click.setAlpha(mClickedAlpha);
			rect_click = getClickPositionRect();
			canvas.drawRect(rect_click, paint_click);
			paint_click.setAlpha(alpha);
		}
	}

	/**
	 * ���ݵ�����·ݼ��㸲�Ƕ���������
	 * <p> ��������left = x_monthOrigin + monthWidth * mClickedMonthIndex % 3
	 * Ҫ�ȼ�����%3�����ڵ��У��ٳ�����ռ��ȣ�
	 * @return Rectangle represent the area just clicked
	 */
	protected Rect getClickPositionRect() {
		if (mClickedMonthIndex != -1) {
			int left = x_monthOrigin + monthWidth * (mClickedMonthIndex % 3);
			int right = left + monthWidth;
			int top = monthsStart + monthHeight * (mClickedMonthIndex
					/ 3);
			int bottom = top + monthHeight;
			return new Rect(left, top, right, bottom);
		} else {
			return new Rect(0, 0, 0, 0);
		}
	}

	/**
	 * �������·ݸ��ǵ�͸���㣬�����¼�������action up ʱ���ô˷���
	 * ��������Ҫ���ã�����Ҫ����Ϊpublic
	 */
	public void clearClickMonth() {
		if (mClickedMonthIndex != -1) {
			mClickedMonthIndex = -1;
		}
		this.invalidate();
	}

	/**
	 * �ɵ������������������·�
	 * @param x �������x����
	 * @param y �������y����
	 */
	public void setClickedMonth(float x, float y) {
		Time month = getClickMonthFromLocation(x, y);
		if (month != null) {
			mClickedMonthIndex = month.month;
//Toast.makeText(context, "�������:"+month.year+"-"+(mClickedMonthIndex+1), Toast.LENGTH_SHORT).show();
			this.invalidate();
		}
		
	}

	/**
	 * �����������ݷ�Χ�ڷ���Time���񷵻�null
	 * FullYearAdapter will invoke this method
	 * @param x
	 * @param y
	 * @return Time ������ǰ������µ�Time����
	 */
	public Time getClickMonthFromLocation(float x, float y) {
		if (x <= x_monthOrigin || x >= (x_monthOrigin + 3 * monthWidth)
				|| y <= y_monthOrigin || y >= (y_monthOrigin + 4 * monthHeight)) {
			return null;
		} else {
			Time time = new Time(mTimeZone);
			int row = (int) (y-y_monthOrigin) / monthHeight;
			int col = (int) (x-x_monthOrigin) / monthWidth;
			int month = row*3+ col;
			time.set(1, month, mToday.year);
			return time;
		}
	}

	
	/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		float x = event.getX();
		float y = event.getY();
		setClickedMonth(x, y);
		
		
		performClick();
		return super.onTouchEvent(event);
	}

	@Override
	public boolean performClick() {
		Toast.makeText(context, "�����"+mClickedMonthIndex+"��", Toast.LENGTH_SHORT).show();
		
		clearClickMonth();
		return super.performClick();
	}*/

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if(D) Log.d(TAG, "FullView.onMeasure--screen:width--"+MeasureSpec.getSize(widthMeasureSpec)+"--screenHeight--"+MeasureSpec.getSize(heightMeasureSpec));
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		monthWidth = (viewWidth - x_yearOrigin) / 3;
		setMeasuredDimension(viewWidth, viewHeight);
		if(D) Log.d(TAG, "FullView.onMeasure()-->width--"+ getMeasuredWidthAndState() +"--height--" + getMeasuredHeightAndState());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		viewWidth = w;
		monthWidth = (w - x_yearOrigin) / 3;
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
