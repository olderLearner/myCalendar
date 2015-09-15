/*
 * http://blog.const.net.cn/a/17206.htm sqlite index ����
 */
package fxj.calendar;



import fxj.calendar.db.DBService;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Grid extends CalendarParent implements Serializable
{
	private static final String TAG = "Main.Grid";
	public static DBService dbService;


	private String[] days = new String[42];
	// true��ʾ�м�¼��false��ʾû�м�¼
	private boolean[] recordDays = new boolean[42];
	private String[] monthNames = new String[12];// 0 - 11
	private TextView tvMsg1;
	private TextView tvMsg2;
	private TextView tvMsg3;
	private int dayColor;
	private int innerGridColor;
	private int prevNextMonthDayColor;
	private int currentDayColor;
	private int todayColor;
	private int todayBackgroundColor;
	private int sundaySaturdayPrevNextMonthDayColor;
	private float daySize;
	private float dayTopOffset;
	private float currentDaySize;
	private float cellX = -1, cellY = -1;

	// ��0��ʼ
	private int currentRow, currentCol;
	private boolean redrawForKeyDown = false;

	// ѡ�����  ��
	public int currentYear, currentMonth;
	// ѡ����� ����days[i]��Ŀ¼
	// currentDay1 ����ʵ�ʵ�������ڣ��������¼�˵�������currentDay1��AllRecord����
	public int currentDay = -1, currentDay1 = -1, currentDayIndex = -1;
	private java.util.Calendar calendar = java.util.Calendar.getInstance();

	public void setCurrentRow(int currentRow)
	{
		/*
		 *  row 0-5 
		 *  ���»�����һ���12��
		 *  ���ԭ���ڵ�һ�У�
		 *  ֱ�ӽ�grid.currentYear,currentMonth,currentDay����Ϊ���ƶ�֮�������
		 */
		if (currentRow < 0)
		{
			currentMonth--;
			if (currentMonth == -1)
			{
				currentMonth = 11;
				currentYear--;
			}
			/*
			 *  =�ұ�Ϊ��ǰ��currentDay��=���棺�������µ�currentDay
			 *  ��������+��ǰ����-7����Ϊ��ת�����µ����ڣ�
			 */
			currentDay = getMonthDays(currentYear, currentMonth) + currentDay
					- 7;
			currentDay1 = currentDay;
			cellX = -1;
			cellX = -1;
			view.invalidate();
			return;

		}
		/*
		 *  ������һ�»�����һ���1�� ��
		 *  ����Ѿ��ڵ����У�currentRow=5����
		 *  2����� 30 31 1 2 3 4 5 �� 31 1 2 3 4 5 6
		 *  �������ƶ�������һ���·�currentRow=6
		 */
		else if (currentRow > 5)
		{
			int n = 0;
			// �����У���һ��i=35��
			for (int i = 35; i < days.length; i++)
			{
				if (!days[i].startsWith("*"))//����*��ͷΪ��ǰ��
					n++; // ��ǰ������һ���ж�����
				else
					break;
			}
			/*
			 *  �ƶ�֮���currentDay = 7 - n + currentCol + 1
			 *  7-n:��һ���������ж�����
			 *  currentCol(0-6) + 1: ��ǰ������һ�еĵڼ���
			 */		
			currentDay = 7 - n + currentCol + 1;
			currentDay1 = currentDay;
			
			currentMonth++;
			if (currentMonth == 12)
			{
				currentMonth = 0;
				currentYear++;
			}
			cellX = -1;
			cellX = -1;
			view.invalidate();
			return;
		}
		/*
		 * �ƶ�û�г�����ǰ�ķ�Χ:2-->1 (����1-0) 5-->6(����4-->5)
		 * redrawForKeyDown��λ
		 * 3�������1) �ƶ�֮���ڵ�ǰ��
		 * 			2) �ƶ�����һ��
		 * 			3) �ƶ�����һ��
		 */
		this.currentRow = currentRow;// 
		redrawForKeyDown = true;
		view.invalidate();
	}

	/**
	 *  ��õ�ǰ����(������ʾ�����º����µ�����)���а�����¼��Ϣ�����ڣ���Ҫ�������ݿ�
	 */
	private void getRecordDays()
	{
		int beginIndex = 8;
		int endIndex = 7;
		int beginDayIndex = 0;
		
		if (currentMonth > 9)
		{
			// 2015-10-7 
			beginIndex = 9;
			endIndex = 8;
		}
		// SELECT SUBSTR(record_date,8) from t_records where substr(record_date, 1, 7)='2015-6-' group by substr(record_date, 1)
		String sql = "select substr(record_date," + beginIndex
				+ ") from t_records where substr(record_date, 1, " + endIndex
				+ ")='" + currentYear + "-" + currentMonth
				+ "-' group by substr(record_date, 1)";
		// ���recordDays[42]���飬ȫ����Ϊfalse
		for (int i = 0; i < recordDays.length; i++)
			recordDays[i] = false;
		
		// �ҵ�days�е�һ������*�ŵģ���ʾ��ǰ�µ�һ�죬������һ���λ��
		for (int i = 0; i < days.length; i++)
		{
			// �ҵ�day�е�һ������*�ŵģ���ʾ��ǰ�µ�һ��
			if (!days[i].startsWith("*"))
			{
				beginDayIndex = i;
				break;
			}
		}
		// ִ��sql���
		Cursor cursor = dbService.execSQL(sql);
		// �������м�¼�����������¼�����ڶ�Ӧ��recordDays������Ӧλ�����true
		while (cursor.moveToNext())
		{

			int day = cursor.getInt(0) - 1;

			recordDays[beginDayIndex + day] = true;
		}
		
		
		
		
		// days�����а���ǰ�µ����ڣ�����Ҫ��ѯ���µļ�¼��Ϣ�������뵱������
		/*
		 * 10�� 11�� �ڳ�����Ϊ 9 10����ȡ����λ��Ӧ�����ı䣬��������bug
		 */
		if (days[0].startsWith("*"))
		{
			int prevYear = currentYear, prevMonth = currentMonth - 1;
			if (prevMonth == -1)
			{
				prevMonth = 11;
				prevYear--;
			}
			
			beginIndex = 8;
			endIndex = 7;			
			if (prevMonth > 9)
			{
				beginIndex = 9;
				endIndex = 8;
			}
			
			// ��ʼ��ѯ���¼�¼������ ��minday�����������������С����һ��
			int minDay = Integer.parseInt(days[0].substring(1));// ȥ��*��
			/**
			 * sql ��䣺
			 * cast(expr AS target_type) ��exprת��target_type����
			 * and ��
			 * where xxx and yyy ���������� xxx �� yyy 
			 * 
			 */
			sql = "select substr(record_date," + beginIndex+ ") "
					+ "from t_records "
					+ "where substr(record_date, 1, " + endIndex + ")='" + prevYear + "-" + prevMonth + "-' "
					+ "and cast(substr(record_date, " + beginIndex+ ") as int) >= " + minDay 
					+ " group by substr(record_date, 1)";
			cursor = dbService.execSQL(sql);
			while (cursor.moveToNext())
			{

				int day = cursor.getInt(0);
				recordDays[day - minDay] = true;
			}
		}
		// ������һ��Ԫ�ش�*�ţ����ѯ���µļ�¼��Ϣ�������뵱������
		if (days[days.length - 1].startsWith("*"))
		{
			int nextYear = currentYear, nextMonth = currentMonth + 1;
			if (nextMonth == 12)
			{
				nextMonth = 0;
				nextYear++;
			}
			
			beginIndex = 8;
			endIndex = 7;			
			if (nextMonth > 9)
			{
				beginIndex = 9;
				endIndex = 8;
			}
			
			int maxDay = Integer.parseInt(days[days.length - 1].substring(1));
			sql = "select substr(record_date," + beginIndex
			+ ") from t_records where substr(record_date, 1, " + endIndex
			+ ")='" + nextYear + "-" + nextMonth 
			+ "-' and cast(substr(record_date," + beginIndex
			+ ") as int) <= " + maxDay + " group by substr(record_date, 1)";
			cursor = dbService.execSQL(sql);
			while (cursor.moveToNext())
			{

				int day = cursor.getInt(0);
				recordDays[days.length - (maxDay - day) - 1] = true;
			}
		}

	}

	public void setCurrentCol(int currentCol)
	{
		// ��ǰ������0�У�0-6��
		if (currentCol < 0)
		{
			// ��һ�У�����������һ��
			if (currentRow == 0)
			{

				currentMonth--;

				if (currentMonth == -1)
				{
					currentMonth = 11;
					currentYear--;
				}
				//�������һ��
				currentDay = getMonthDays(currentYear, currentMonth);
				currentDay1 = currentDay;
				cellX = -1;
				cellX = -1;
				view.invalidate();
				return;
			}

			else
			{
				// ��һ�����һ�У�����
				currentCol = 6;
				setCurrentRow(--currentRow);

			}
		}
		else if (currentCol > 6)
		{
			currentCol = 0;
			setCurrentRow(++currentRow);

		}
		this.currentCol = currentCol;
		redrawForKeyDown = true;
		view.invalidate();
	}

	/**
	 * ���ѡ�����������У�0-5
	 * @return row 0-5
	 */
	public int getCurrentRow()
	{
		return currentRow;
	}

	/**
	 * ���ѡ�����������У�0-6
	 * @return col 0-6
	 */
	public int getCurrentCol()
	{
		return currentCol;
	}

	public void setCellX(float cellX)
	{

		this.cellX = cellX;
	}

	public void setCellY(float cellY)
	{

		this.cellY = cellY;
	}
	
	/***
	 * 
	 * @param year
	 * @param month
	 * @return int ��ǰ�����ж�����
	 */
	private int getMonthDays(int year, int month)
	{
		month++;
		switch (month)
		{
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
			{
				return 31;
			}
			case 4:
			case 6:
			case 9:
			case 11:
			{
				return 30;
			}
			case 2:
			{
				if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0))
					return 29;
				else
					return 28;
			}
		}
		return 0;
	}
	
	/**
	 * ���ɵ�ǰ����Ҫ��ʾ�������ı�(����ǰ�� ��ǰ�� ���£�*��ͷ)������������浽days�����У�
	 * <p>currentDayIndex�����������days[]��Ŀ¼��
	 * <p>currentYear,currentMonth,currentDayΪGrid��Ա������
	 * <p>���������
	 * <p>һ��CalendarView���or�����ƶ���
	 * <p>    ����draw������currentYear,currentMonth,currentDay���ǵ�����ƶ�֮ǰ����,
	 *     ����currentDayIndex����֮ǰ���ڵ�ֵ��cuurentDayֵû�иı䣩����ʵ��currentDayIndex�ͺ�,
	 *     isCurrentDay�����У�����col row ����cellX cellY ȷ�������currentDayֵ,
	 *     ��ʱcurrentDayΪ��ǰ��������ڣ���ʱ�ڵ��ô˷���currentDayIndexΪ��ʵֵ��
	 *     
	 * <p>����������Ĳ˵���ť������ ��ָ�����ڣ���
	 * <p>		����ѡ���currentYear,currentMonth,currentDay��Grid.draw()�У�
	 * 		currentDayIndex��Ϊѡ������ڵ�Ŀ¼��
	 * 
	 */
	private void calculateDays()
	{
		// ��ǰ�� ��ǰ�� 1�� ����
		// January 0 December 11
		calendar.set(currentYear, currentMonth, 1);

		// ��õ�ǰ���µ� ��һ���������ܵ� �ڼ���
		// Sunday 1 Monday 2 .... Saturday 7
		int week = calendar.get(java.util.Calendar.DAY_OF_WEEK);
		int monthDays = 0; // ��ǰ���ж�����
		int prevMonthDays = 0; // ǰһ�����ж�����
		
		// ��ǰ�·��ж����� 1 -- 31
		monthDays = getMonthDays(currentYear, currentMonth);
		
		if (currentMonth == 0)
			// ��������һ���12�� currentYear-1 12�£�11
			// ������һ�����ж����죬��1��ʼ
			prevMonthDays = getMonthDays(currentYear - 1, 11);
		else
			// �����ǽ���
			prevMonthDays = getMonthDays(currentYear, currentMonth - 1);
		
		// ���·��䵽��ǰ�µ��������� ǰ���*�ű��
		for (int i = week, day = prevMonthDays; i > 1; i--, day--)
		{
			// ��ǰ�µ�һ����daysĿ¼��days[week-1],�������°�������1��Ŀ¼��days[week-2]
			// ����week-1����ʾ
			days[i - 2] = "*" + String.valueOf(day);
		}
		// ��ǰ�µ��������� days[42] 0-41,���Ե�ǰ�¿�ʼ�±���day[week - 1]
		for (int day = 1, i = week - 1; day <= monthDays; day++, i++)
		{
			days[i] = String.valueOf(day);
			// Grid��Ա����currentDay����ʾ�������һ��
			// �����һ������currentDay = -1
			if (day == currentDay)
			{
				currentDayIndex = i;
Log.d(TAG, "Grid.calculateDays():currentDayIndex-->" + currentDayIndex);
			}
		}
		// �������·��䵽��ǰ�µ��������֣�ǰ���*�ű��
		for (int i = week + monthDays - 1, day = 1; i < days.length; i++, day++)
		{
			days[i] = "*" + String.valueOf(day);
		}

	}

	/**
	 * Grid ���췽��
	 * @param activity
	 * @param view
	 */
	public Grid(Activity activity, View view)
	{
		super(activity, view);
		// ������ݿ�Ϊnull���򴴽�һ���µ����ݿ�
		if (dbService == null)
		{
			// ��grid����Ҫͨ��DBservice��õ�ǰ�����Ƿ������¼����������¼������������ǰ��ʾ�Ǻ�
			dbService = new DBService(activity);
		}
		
		// �Զ����view�л�� ��activity �еĿؼ�
		tvMsg1 = (TextView) activity.findViewById(R.id.tvMsg1);
		//tvMsg2 = (TextView) activity.findViewById(R.id.tvMsg2);
		// �����ı�����ɫ����ɫ
		dayColor = activity.getResources().getColor(R.color.day_color);
		// ���������ı�����ɫ����ɫ
		todayColor = activity.getResources().getColor(R.color.today_color);
		// ���������ı��߿���ɫ����ɫ
		todayBackgroundColor = activity.getResources().getColor(
				R.color.today_background_color);
		// ������������ɫ����ɫ
		innerGridColor = activity.getResources().getColor(
				R.color.inner_grid_color);
		// ���»���������������ɫ����ɫ��������Щ���ڻ���ת�����»�������
		prevNextMonthDayColor = activity.getResources().getColor(
				R.color.prev_next_month_day_color);
		// ��ǰ�������ֵ���ɫ����ɫ
		currentDayColor = activity.getResources().getColor(
				R.color.current_day_color);
		// ������ ������������ɫ������ɫ
		sundaySaturdayPrevNextMonthDayColor = activity.getResources().getColor(
				R.color.sunday_saturday_prev_next_month_day_color);
		// ��������ߴ� 15dp
		daySize = activity.getResources().getDimension(R.dimen.day_size);
		// �������־൱ǰ���񶥶˵�ƫ����������΢���������ֵ�λ�� 11dp
		dayTopOffset = activity.getResources().getDimension(
				R.dimen.day_top_offset);
		// ��ǰ���ֵĳߴ� 15sp
		currentDaySize = activity.getResources().getDimension(
				R.dimen.current_day_size);
		// �·����ƣ���������ʽ����
		monthNames = activity.getResources().getStringArray(R.array.month_name);
		// ��ɫ
		paint.setColor(activity.getResources().getColor(R.color.border_color));
		
		// ��ǰ������
		currentYear = calendar.get(java.util.Calendar.YEAR);
		currentMonth = calendar.get(java.util.Calendar.MONTH);
		// month ֵ�� 0-11 7�»��ӡ����6
		// ��һ�����г��� new Grid �����һ��
		Log.d(TAG, "Grid()-->" + currentYear + "-" + currentMonth);
		Log.d(TAG, "Grid()-->" + calendar);
		//Log.d(TAG, Integer.valueOf(currentMonth).toString());
	}

	/**
	 * �Ƿ�Ϊ��ǰ��������ڣ��ǣ�����true�����ǣ�����false
	 * <p>����ǵ�ǰ������ڣ�currentDayIndex cellX cellY ����Ϊ��ֵ-1��
	 * 		currentDayIndex���βΣ���ô���ã���
	 * <p>�����������֮�������δ�������ڣ��ٴν���˷�����������false��
	 * <p>�������ҵ�����֮��flag=true��isCurrentDay()&& flag == false������Ҫ���뷽�������Ϊfalse 
	 * @param dayIndex ����������days[i]��i
	 * @param currentDayIndex ����������days[]�е�λ��
	 * @param cellRect ��Χ������ڵĵĳ�����
	 * @return boolean result
	 */
	private boolean isCurrentDay(int dayIndex, int currentDayIndex,
			Rect cellRect)
	{
		boolean result = false;
		if (redrawForKeyDown == true)// ���ƶ����ֻ����������Ҽ�
		{
			// dayIndex �Ƿ����currentRow �� currentCol ��ȷ��������
			result = dayIndex == (7 * ((currentRow > 0) ? currentRow : 0) + currentCol);
			if (result) //���ƶ��������ڣ�
				redrawForKeyDown = false;

		}
		else if (cellX != -1 && cellY != -1) // ��ָ�������Ӧ������
		{
			// ����������Ƿ���days[i]�ĸ�����
			if (cellX >= cellRect.left && cellX <= cellRect.right
					&& cellY >= cellRect.top && cellY <= cellRect.bottom)
			{
				result = true;
			}
			else
			{
				result = false;
			} 
		}
		/*
		 * �����һ��������������˵�ѡ��ָ������,�����else
		 */
		else
		{
			result = (dayIndex == currentDayIndex);

		}
		
		/*
		 *  �ҵ���ѡ�������ڣ�result��Ϊtrue
		 *  �ƶ����ߵ�����ҵ��������֮��currentDayIndex=-1��cellX cellY = -1
		 */
		if (result)
		{
			if (currentRow > 0 && currentRow < 6)
			{
				currentDay1 = currentDay;

			}
			
			currentDayIndex = -1;// ���������βΣ���ô���ã�����
			cellX = -1;
			cellY = -1;

		}
		return result;
	}

	/**
	 *  ��������ͷ����Ϣ
	 * @param boolean today(����������Ƿ�Ϊ����)
	 */
	private void updateMsg(boolean today)
	{
		String monthName = monthNames[currentMonth];// һ�� ... ʮ����
		String dateString = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��");
		
		// ���calendarʵ�����ѵ���������մ��ݵ�calendar��
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(currentYear, currentMonth, currentDay);

		dateString = sdf.format(calendar.getTime());// ��warning
		if (today) {
			tvMsg1.setTextColor(todayColor);
		} else {
			tvMsg1.setTextColor(activity.getResources().getColor(R.color.tvMsg1_color));
		}
		dateString += "  " + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
				+ "��";
		tvMsg1.setText(dateString);
				
		/*
		String lunarStr = "";// ����

		monthName += " ��" + calendar.get(java.util.Calendar.WEEK_OF_MONTH)
				+ "��";
		tvMsg1.setText(monthName);
		
		if (today)dateString += "(����)";
		dateString += "   �����" + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
				+ "��";
		tvMsg2.setText(dateString);
*/
	}

	/**
	 * ������Ƿ���border����
	 * @return true �ڽ���
	 * false �ڽ���
	 */
	public boolean inBoundary()
	{
		if (cellX < borderMargin
				|| cellX > (view.getMeasuredWidth() - borderMargin)
				|| cellY < top
				|| cellY > (view.getMeasuredHeight() - borderMargin))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	float top, left;

	
	@Override
	public void draw(Canvas canvas)
	 {
		// Ӧ�õ�һ������draw()��һ�ε��� currentDay��ֵ�� -1��
		// CalendarView�е�������ݵ�currentDay�ǵ��ֵ
Log.d(TAG, "Grid.draw()_first-->" + currentYear + "-" + currentMonth + "-" + currentDay);
		
		left = borderMargin;
		// �߿�Ķ���
		top = borderMargin + weekNameSize + weekNameMargin * 2 + 4;
		
		float calendarWidth = view.getMeasuredWidth() - left * 2;
		float calendarHeight = view.getMeasuredHeight() - top - borderMargin;
		// ÿ�����ڸ��ӵĿ�Ⱥ͸߶�
		float cellWidth = calendarWidth / 7;
		float cellHeight = cellWidth;
		//float cellHeight = calendarHeight / 6;
		
		int c = paint.getColor();
		paint.setColor(activity.getResources().getColor(R.color.inner_grid_color));
		
		// ���ƶ��˵�ֱ�� 
		canvas.drawLine(left, top, left + view.getMeasuredWidth()
				- borderMargin * 2, top, paint);
		// ������
		for (int i = 1; i < 6; i++)
		{
			 canvas.drawLine(left, top + (cellHeight) * i, left +
			 calendarWidth,
			 top + (cellHeight) * i, paint);
		}
		// ������
		/*for (int i = 1; i < 7; i++)
		{
			 canvas.drawLine(left + cellWidth * i, top, left + cellWidth * i,
			 view.getMeasuredHeight() - borderMargin, paint);
		}*/
		paint.setColor(c);

		// ������

		// ���ɵ�ǰ����Ҫ��ʾ�������ı�String days[]����
		calculateDays();

		java.util.Calendar calendar = java.util.Calendar.getInstance();
		// ��õ�ǰ���ڵ��� 1 - 28��29��30��31
		int day = calendar.get(java.util.Calendar.DATE);
		// ��õ�ǰ���ڵ��꣬��
		int myYear = calendar.get(java.util.Calendar.YEAR), myMonth = calendar
				.get(java.util.Calendar.MONTH);
//Log.d(TAG, "Grid.draw()-->" + myYear + "-" + myMonth + "-" + day);
		
		// ��calendar��������Ϊ����ǰ�� ��ǰ��  ��һ��
		calendar.set(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
				1);
		// ����µĵ�һ�����ܼ���1 2 3 4 5 6 7 
		int week = calendar.get(java.util.Calendar.DAY_OF_WEEK);
		// todayIndex:������������days�����Ŀ¼,(week��day���Ǵ�1��ʼ��days��0��ʼ����������week + day - 2)
		// forѭ����draw days[i]��鿴days[i]�ǵ��ڱ߿򻭺�ɫ����ʾ��ǰ��
		int todayIndex = week + day - 2;
		
		boolean today = false; // ��������ͷ��today=true�����ں���+(����)
		boolean sunsatIsCurrent = false;
		
		// ������days�е�Ŀ¼
		if (currentDayIndex == -1) // �����һ������currentDayIndex = -1,�������к�ֵ����Ϊ-1
		{
			currentDayIndex = todayIndex; // ����ǰ������days�е�λ�ø�ֵ�� currentDayIndex
Log.d(TAG, "Grid.draw():currentDayIndex-->" + currentDayIndex);
		}
		boolean flag = false; // for ѭ���е�һ����־λ
		
		// ��ѯ���ݿ��еļ�¼��Ϣ
		getRecordDays();
		
/**
 *  ���������ı�,һ��42������
 */
		//paint.setAntiAlias(true);
		//paint.setTextAlign(Paint.Align.CENTER);
		for (int i = 0; i < days.length; i++)
		{

//Log.d(TAG, "Grid.draw().forѭ��-->" + i);
			
			today = false;
			sunsatIsCurrent = false;
			
			int row = i / 7; // ������ȷ��������
			int col = i % 7; // ���ࣺȷ��������
			String text = String.valueOf(days[i]);
			
			// ���º����µ���ĩ�� days��*��ͷ��
			// ���ݲ�ͬ���ڣ����ò�ͬ�Ļ�����ɫ
			if ((i % 7 == 0 || (i - 6) % 7 == 0) && text.startsWith("*"))
			{
				paint.setColor(sundaySaturdayPrevNextMonthDayColor);// ����
			}
			else if (i % 7 == 0 || (i - 6) % 7 == 0)
			{
				paint.setColor(sundaySaturdayColor); // ������
				sunsatIsCurrent = true;
			}
			else if (text.startsWith("*"))
			{
				paint.setColor(prevNextMonthDayColor); // ǰ�º��� ��ͨ���ڣ���ɫ
			}
			else
			{
				paint.setColor(dayColor); // ��ǰ����ͨ���ڣ���ɫ
			}
			
			// ȥ��days�����е�*��
			text = text.startsWith("*") ? text.substring(1) : text;
			
			// ��Χ���ڵĳ�����
			Rect dst = new Rect();
			dst.left = (int) (left + cellWidth * col);
			dst.top = (int) (top + cellHeight * row);
			dst.bottom = (int) (dst.top + cellHeight + 1);
			dst.right = (int) (dst.left + cellWidth + 1);
			
			String myText = text;
			
			/*
			 * �Է�������ĵ㻭����
			 */
			float central_x = left + cellWidth*col + cellWidth/2;
			float central_y = top + cellHeight*row + cellHeight/2 + paint.getTextSize()/2;
			
			// �����ǰ���ڰ�����Ϣ������������ǰ��*
			/*
		    if (recordDays[i]) 
		    	myText = "*" + myText;
		    */
			if (recordDays[i]) {
				int color = paint.getColor();
				paint.setColor(prevNextMonthDayColor);
				canvas.drawCircle(central_x, central_y + paint.getTextSize() / 2,
						paint.getTextSize() / 6, paint);
				paint.setColor(color);
			}
			
			paint.setTextSize(daySize);
			
			// draw �������ֵ����ꣻ������ʼ���½�
			float textLeft = left + cellWidth * col
					+ (cellWidth - paint.measureText(myText)) / 2;
			float textTop = top + cellHeight * (row + 1)
					- (cellHeight - paint.getTextSize() + paint.descent()) / 2;			
			
			/**
			 * paint.FontMetrics ���ͳߴ�
			 */
			/*
			// FontMetrics.ascent
			canvas.drawLine(textLeft, textTop + paint.ascent(),
					textLeft + paint.measureText(myText),
					textTop + paint.ascent(), paint);		
			// FontMetrics.descent
			canvas.drawLine(textLeft, textTop + paint.descent(),
					textLeft + paint.measureText(myText),
					textTop + paint.descent(), paint);
			// FontMetrics.bottom
			int tr = paint.getColor();
			paint.setColor(todayColor);
			canvas.drawLine(textLeft + 3*paint.measureText(myText)/2, textTop + paint.getFontMetrics().bottom,
					textLeft + 2*paint.measureText(myText),
					textTop + paint.getFontMetrics().bottom, paint);
			
			canvas.drawLine(textLeft, textTop + paint.ascent()+ paint.descent(),
					textLeft + paint.measureText(myText),
					textTop + paint.ascent()+ paint.descent(), paint);
						
			paint.setColor(tr);
			
			canvas.drawLine(textLeft, textTop,
					textLeft + paint.measureText(myText),
					textTop, paint);
			canvas.drawLine(textLeft, textTop, textLeft,
					textTop - paint.getTextSize(), paint);
			*/
			
			
			
			//float textTop = top + cellHeight * row
			//		+ (cellHeight - paint.getTextSize()) / 2 + dayTopOffset;
			
			/*
			 * �Է�������ĵ㻭����
			 
			float central_x = left + cellWidth*col + cellWidth/2;
			float central_y = top + cellHeight*row + cellHeight/2 + paint.getTextSize()/2;*/
			
			
			/*
			 * ���ѡ�����ߵ���������ǵ�ǰ�������գ�today��־��λ����������ͷ+(����)
			 *  �����������or�²���myYear myMonth��if��ִ�У�
			 *  todayIndex����ʵ�Ľ��죬�����ǵ����currentDay
			 */
			if (myYear == currentYear && myMonth == currentMonth
					&& i == todayIndex)
			{
				// ���days[i] �ǽ��죬���ƺ�ɫ�ڳ� ���ֱ߿���ɫ			
				/*paint.setTextSize(currentDaySize);
				paint.setColor(todayBackgroundColor);// ���ʣ�red
				dst.left += 1;
				dst.top += 1;
				canvas.drawLine(dst.left, dst.top, dst.right, dst.top, paint);
				canvas.drawLine(dst.right, dst.top, dst.right, dst.bottom,
						paint);
				canvas.drawLine(dst.right, dst.bottom, dst.left, dst.bottom,
						paint);
				canvas.drawLine(dst.left, dst.bottom, dst.left, dst.top, paint);*/

				
				paint.setColor(todayColor); // ��ɫ��
				
				today = true; // �������ѡ���������ǽ��죬today��Ϊtrue
			}

			/*
			 *  ��������ǰ������ʾ�����»�����������ʱ���Զ���ʾ���»����µ����� 
			 *  ����������̻���������»������£������Ȼ�����
			 *  2015 7 8:����֪ʶ���ι̣�== ���ȼ����� &&��
			 *  �ҵ�������ں�flag=true���������isCurrentDay������
			 */
			/**
			 * (һ)������µ�����
			 * 
			 */
			if (isCurrentDay(i, currentDayIndex, dst) && flag == false)
			{
				
				/*
				 *  �������������»������£�if�лὫcurrentYear,currentMont,currentDay,
				 *  ����Ϊ����������գ�currentDay1=currentDay��break����forѭ����draw������
				 *  ִ��CalendarView onDraw()����
				 */
				if (days[i].startsWith("*"))
				{
					// ����
					if (i > 20)
					{
						currentMonth++;
						if (currentMonth == 12)
						{
							currentMonth = 0;
							currentYear++;
						}
//Log.d(TAG, "Grid.draw().����-->" + i);						
						// ˢ�µ�ǰ������������ʾ��������
						view.invalidate(); // CalendarParent(����)�е�view

					}
					// ����
					else
					{
						currentMonth--;
						if (currentMonth == -1)
						{
							currentMonth = 11;
							currentYear--;
						}
Log.d(TAG, "Grid.draw().����-->" + i + "-" + currentMonth);						
//Log.d(TAG, "Grid.draw().����-->" + i);
						view.invalidate();// ˢ�µ�ǰ������������ʾ��������

					}
					// Parses the specified string as a signed decimal integer value. 
					// The ASCII character - ('-') is recognized as the minus sign
					currentDay = Integer.parseInt(text);
					currentDay1 = currentDay;
					cellX = -1;
					cellY = -1;
					break;// ����forѭ����draw()ִ����ϣ�����ִ��view.invalidate();

				}
				/*
				 * ���ڵ�ǰ��������ʾһ������ͼ��
				 * ���ʣ���ɫ
				 * ������ �����ڻ��ڱ���ͼ��
				 * col row currentDay��������Ϊ�������
				 * currentday1 ��ֵΪ currentDay
				 */
				else
				{
					paint.setTextSize(currentDaySize); // 15sp
					flag = true;
					/*Bitmap bitmap = BitmapFactory.decodeResource(activity
							.getResources(), R.drawable.day);
					Rect src = new Rect();
					src.left = 0;
					src.top = 0;
					src.right = bitmap.getWidth();
					src.bottom = bitmap.getHeight();
					// ��dst�ϻ���bitmap
					canvas.drawBitmap(bitmap, src, dst, paint);*/
					
					/*if(today) {
						paint.setColor(todayColor); // ���	
					} else {
						paint.setColor(dayColor); // �ڵ�
					}*/
					
					//paint.setColor(today ? todayColor : dayColor);
					if(today) {
						paint.setColor(todayColor);
					} else {
						paint.setColor(sunsatIsCurrent ? sundaySaturdayColor : dayColor);
					}
					
					canvas.drawCircle(left + cellWidth * col + cellWidth / 2,
							top + cellHeight * row + cellHeight / 2, 
							3*paint.getTextSize()/4, paint);
					
					paint.setColor(currentDayColor);// ������Ϊ��ɫ��ѡ�е����ڰ��ֺڵ�
					
					
					// CalendarView��onKeyDown����get����
					currentCol = col;// currentCol����Ϊ��ǰ�����
					currentRow = row;// currentRow����Ϊ��ǰ�����
					
					currentDay = Integer.parseInt(text);
					currentDay1 = currentDay;
					// ��������ͷ�����������ձ���Ϊ���������
					updateMsg(today);
Log.d(TAG, "Grid.draw()_isCurrentDay:currentDayIndex-->" + currentDayIndex);
				}
			}
			
			// �������л�������
			paint.setFakeBoldText(true);
			//canvas.drawText(myText, central_x, central_y, paint);
			canvas.drawText(myText, textLeft, textTop, paint);
			paint.setFakeBoldText(false);

		}
Log.d(TAG, "Grid.draw()_end:currentDayIndex-->" + currentDayIndex);
Log.d(TAG, "Grid.draw()_end-->" + currentYear + "-" + currentMonth + "-" + currentDay);
Log.d(TAG, "cellX-->" + cellX);
Log.d(TAG, "cellY-->" + cellY);
	}

}
