/*
 * http://blog.const.net.cn/a/17206.htm sqlite index 博客
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
	// true表示有记录，false表示没有记录
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

	// 从0开始
	private int currentRow, currentCol;
	private boolean redrawForKeyDown = false;

	// 选择的年  月
	public int currentYear, currentMonth;
	// 选择的天 天在days[i]中目录
	// currentDay1 保存实际点击的日期，主界面记录菜单，传递currentDay1到AllRecord界面
	public int currentDay = -1, currentDay1 = -1, currentDayIndex = -1;
	private java.util.Calendar calendar = java.util.Calendar.getInstance();

	public void setCurrentRow(int currentRow)
	{
		/*
		 *  row 0-5 
		 *  上月或者上一年的12月
		 *  光标原本在第一行，
		 *  直接将grid.currentYear,currentMonth,currentDay更新为：移动之后的日期
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
			 *  =右边为当前的currentDay，=左面：跳至上月的currentDay
			 *  上月天数+当前日期-7，即为跳转到上月的日期！
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
		 *  跳到下一月或者下一年的1月 ，
		 *  光标已经在第六行（currentRow=5），
		 *  2种情况 30 31 1 2 3 4 5 ； 31 1 2 3 4 5 6
		 *  再向下移动跳到下一个月份currentRow=6
		 */
		else if (currentRow > 5)
		{
			int n = 0;
			// 第六行，第一列i=35；
			for (int i = 35; i < days.length; i++)
			{
				if (!days[i].startsWith("*"))//不以*开头为当前月
					n++; // 当前月在这一行有多少天
				else
					break;
			}
			/*
			 *  移动之后的currentDay = 7 - n + currentCol + 1
			 *  7-n:下一月在这行有多少天
			 *  currentCol(0-6) + 1: 当前列在这一行的第几天
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
		 * 移动没有超出当前的范围:2-->1 (代码1-0) 5-->6(代码4-->5)
		 * redrawForKeyDown置位
		 * 3种情况：1) 移动之后还在当前月
		 * 			2) 移动到上一月
		 * 			3) 移动到下一月
		 */
		this.currentRow = currentRow;// 
		redrawForKeyDown = true;
		view.invalidate();
	}

	/**
	 *  获得当前月中(包含显示的上月和下月的日期)所有包含记录信息的日期，需要访问数据库
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
		// 清空recordDays[42]数组，全部置为false
		for (int i = 0; i < recordDays.length; i++)
			recordDays[i] = false;
		
		// 找到days中第一个不带*号的，表示当前月第一天，返回这一天的位置
		for (int i = 0; i < days.length; i++)
		{
			// 找到day中第一个不带*号的，表示当前月第一天
			if (!days[i].startsWith("*"))
			{
				beginDayIndex = i;
				break;
			}
		}
		// 执行sql语句
		Cursor cursor = dbService.execSQL(sql);
		// 遍历所有记录，将与包含记录的日期对应的recordDays数组相应位置设成true
		while (cursor.moveToNext())
		{

			int day = cursor.getInt(0) - 1;

			recordDays[beginDayIndex + day] = true;
		}
		
		
		
		
		// days数组中包含前月的日期，则需要查询上月的记录信息，方法与当月类似
		/*
		 * 10月 11月 在程序中为 9 10，截取数据位置应该做改变，否则会出现bug
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
			
			// 开始查询上月记录的日期 ，minday上月在这月里面的最小的那一天
			int minDay = Integer.parseInt(days[0].substring(1));// 去掉*号
			/**
			 * sql 语句：
			 * cast(expr AS target_type) 将expr转化target_type类型
			 * and 与
			 * where xxx and yyy ：条件满足 xxx 和 yyy 
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
		// 如果最后一个元素带*号，则查询下月的记录信息，方法与当月类似
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
		// 当前所在列0列（0-6）
		if (currentCol < 0)
		{
			// 第一行，日历跳至上一月
			if (currentRow == 0)
			{

				currentMonth--;

				if (currentMonth == -1)
				{
					currentMonth = 11;
					currentYear--;
				}
				//上月最后一天
				currentDay = getMonthDays(currentYear, currentMonth);
				currentDay1 = currentDay;
				cellX = -1;
				cellX = -1;
				view.invalidate();
				return;
			}

			else
			{
				// 上一行最后一列，本月
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
	 * 获得选定日期所在行：0-5
	 * @return row 0-5
	 */
	public int getCurrentRow()
	{
		return currentRow;
	}

	/**
	 * 获得选定日期所在列：0-6
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
	 * @return int 当前年月有多少天
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
	 * 生成当前月需要显示的日期文本(包含前月 当前月 下月，*开头)，并将结果保存到days变量中；
	 * <p>currentDayIndex：点击的天在days[]中目录；
	 * <p>currentYear,currentMonth,currentDay为Grid成员变量；
	 * <p>两种情况：
	 * <p>一：CalendarView点击or按键移动；
	 * <p>    调用draw方法，currentYear,currentMonth,currentDay还是点击或移动之前日期,
	 *     所以currentDayIndex还是之前日期的值（cuurentDay值没有改变），真实的currentDayIndex滞后,
	 *     isCurrentDay方法中，根据col row 或者cellX cellY 确定点击的currentDay值,
	 *     此时currentDay为当前点击的日期，此时在调用此方法currentDayIndex为真实值。
	 *     
	 * <p>二：主界面的菜单按钮（今天 ，指定日期），
	 * <p>		传递选择的currentYear,currentMonth,currentDay到Grid.draw()中；
	 * 		currentDayIndex即为选择的日期的目录；
	 * 
	 */
	private void calculateDays()
	{
		// 当前年 当前月 1号 对象
		// January 0 December 11
		calendar.set(currentYear, currentMonth, 1);

		// 获得当前年月的 第一天是所在周的 第几天
		// Sunday 1 Monday 2 .... Saturday 7
		int week = calendar.get(java.util.Calendar.DAY_OF_WEEK);
		int monthDays = 0; // 当前月有多少天
		int prevMonthDays = 0; // 前一个月有多少天
		
		// 当前月份有多少天 1 -- 31
		monthDays = getMonthDays(currentYear, currentMonth);
		
		if (currentMonth == 0)
			// 上月是上一年的12月 currentYear-1 12月：11
			// 返回上一个月有多少天，从1开始
			prevMonthDays = getMonthDays(currentYear - 1, 11);
		else
			// 上月是今年
			prevMonthDays = getMonthDays(currentYear, currentMonth - 1);
		
		// 上月分配到当前月的日期文字 前面加*号标记
		for (int i = week, day = prevMonthDays; i > 1; i--, day--)
		{
			// 当前月第一天在days目录：days[week-1],所以上月挨着这月1号目录：days[week-2]
			// 共有week-1天显示
			days[i - 2] = "*" + String.valueOf(day);
		}
		// 当前月的日期文字 days[42] 0-41,所以当前月开始下标是day[week - 1]
		for (int day = 1, i = week - 1; day <= monthDays; day++, i++)
		{
			days[i] = String.valueOf(day);
			// Grid成员变量currentDay，表示点击的那一天
			// 程序第一次运行currentDay = -1
			if (day == currentDay)
			{
				currentDayIndex = i;
Log.d(TAG, "Grid.calculateDays():currentDayIndex-->" + currentDayIndex);
			}
		}
		// 生成下月分配到当前月的日期文字，前面加*号标记
		for (int i = week + monthDays - 1, day = 1; i < days.length; i++, day++)
		{
			days[i] = "*" + String.valueOf(day);
		}

	}

	/**
	 * Grid 构造方法
	 * @param activity
	 * @param view
	 */
	public Grid(Activity activity, View view)
	{
		super(activity, view);
		// 如果数据库为null，则创建一个新的数据库
		if (dbService == null)
		{
			// 在grid类中要通过DBservice获得当前月日是否包含记录，若包含记录则在日期文字前显示星号
			dbService = new DBService(activity);
		}
		
		// 自定义的view中获得 主activity 中的控件
		tvMsg1 = (TextView) activity.findViewById(R.id.tvMsg1);
		//tvMsg2 = (TextView) activity.findViewById(R.id.tvMsg2);
		// 日期文本的颜色：白色
		dayColor = activity.getResources().getColor(R.color.day_color);
		// 今天日期文本的颜色：红色
		todayColor = activity.getResources().getColor(R.color.today_color);
		// 今天日期文本边框颜色：红色
		todayBackgroundColor = activity.getResources().getColor(
				R.color.today_background_color);
		// 日历网格线颜色：白色
		innerGridColor = activity.getResources().getColor(
				R.color.inner_grid_color);
		// 上月或下月日期文字颜色：灰色。单击这些日期会跳转到上月或者下月
		prevNextMonthDayColor = activity.getResources().getColor(
				R.color.prev_next_month_day_color);
		// 当前日期文字的颜色：白色
		currentDayColor = activity.getResources().getColor(
				R.color.current_day_color);
		// 星期六 星期日文字颜色：暗红色
		sundaySaturdayPrevNextMonthDayColor = activity.getResources().getColor(
				R.color.sunday_saturday_prev_next_month_day_color);
		// 日期字体尺寸 15dp
		daySize = activity.getResources().getDimension(R.dimen.day_size);
		// 日期文字距当前网格顶端的偏移量，用于微调日期文字的位置 11dp
		dayTopOffset = activity.getResources().getDimension(
				R.dimen.day_top_offset);
		// 当前文字的尺寸 15sp
		currentDaySize = activity.getResources().getDimension(
				R.dimen.current_day_size);
		// 月份名称：以数组形式返回
		monthNames = activity.getResources().getStringArray(R.array.month_name);
		// 白色
		paint.setColor(activity.getResources().getColor(R.color.border_color));
		
		// 当前的年月
		currentYear = calendar.get(java.util.Calendar.YEAR);
		currentMonth = calendar.get(java.util.Calendar.MONTH);
		// month 值是 0-11 7月会打印出来6
		// 第一次运行程序 new Grid 会输出一次
		Log.d(TAG, "Grid()-->" + currentYear + "-" + currentMonth);
		Log.d(TAG, "Grid()-->" + calendar);
		//Log.d(TAG, Integer.valueOf(currentMonth).toString());
	}

	/**
	 * 是否为当前点击的日期，是：返回true，不是：返回false
	 * <p>如果是当前点击日期，currentDayIndex cellX cellY 重置为初值-1；
	 * 		currentDayIndex是形参，怎么重置！！
	 * <p>错误：这个日期之后的所有未画的日期，再次进入此方法，都返回false；
	 * <p>纠正：找到日期之后flag=true，isCurrentDay()&& flag == false，不需要进入方法，结果为false 
	 * @param dayIndex 画日期文字days[i]的i
	 * @param currentDayIndex 今天日期在days[]中的位置
	 * @param cellRect 包围这个日期的的长方形
	 * @return boolean result
	 */
	private boolean isCurrentDay(int dayIndex, int currentDayIndex,
			Rect cellRect)
	{
		boolean result = false;
		if (redrawForKeyDown == true)// 若移动了手机的上下左右键
		{
			// dayIndex 是否等于currentRow 和 currentCol 所确定的日期
			result = dayIndex == (7 * ((currentRow > 0) ? currentRow : 0) + currentCol);
			if (result) //是移动到的日期，
				redrawForKeyDown = false;

		}
		else if (cellX != -1 && cellY != -1) // 手指点击了相应的日期
		{
			// 点击的坐标是否在days[i]的格子内
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
		 * 程序第一次启动，主界面菜单选择指定日期,进入此else
		 */
		else
		{
			result = (dayIndex == currentDayIndex);

		}
		
		/*
		 *  找到了选定的日期，result设为true
		 *  移动或者点击，找到点击日期之后，currentDayIndex=-1，cellX cellY = -1
		 */
		if (result)
		{
			if (currentRow > 0 && currentRow < 6)
			{
				currentDay1 = currentDay;

			}
			
			currentDayIndex = -1;// 这尼玛是形参，怎么重置！！！
			cellX = -1;
			cellY = -1;

		}
		return result;
	}

	/**
	 *  更新日历头的信息
	 * @param boolean today(点击的日期是否为今天)
	 */
	private void updateMsg(boolean today)
	{
		String monthName = monthNames[currentMonth];// 一月 ... 十二月
		String dateString = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
		
		// 获得calendar实例，把点击的年月日传递到calendar中
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(currentYear, currentMonth, currentDay);

		dateString = sdf.format(calendar.getTime());// 有warning
		if (today) {
			tvMsg1.setTextColor(todayColor);
		} else {
			tvMsg1.setTextColor(activity.getResources().getColor(R.color.tvMsg1_color));
		}
		dateString += "  " + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
				+ "周";
		tvMsg1.setText(dateString);
				
		/*
		String lunarStr = "";// 阴历

		monthName += " 第" + calendar.get(java.util.Calendar.WEEK_OF_MONTH)
				+ "周";
		tvMsg1.setText(monthName);
		
		if (today)dateString += "(今天)";
		dateString += "   本年第" + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
				+ "周";
		tvMsg2.setText(dateString);
*/
	}

	/**
	 * 坐标点是否在border界内
	 * @return true 在界内
	 * false 在界外
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
		// 应用第一次启动draw()第一次调用 currentDay初值是 -1；
		// CalendarView中点击，传递的currentDay是点击值
Log.d(TAG, "Grid.draw()_first-->" + currentYear + "-" + currentMonth + "-" + currentDay);
		
		left = borderMargin;
		// 边框的顶端
		top = borderMargin + weekNameSize + weekNameMargin * 2 + 4;
		
		float calendarWidth = view.getMeasuredWidth() - left * 2;
		float calendarHeight = view.getMeasuredHeight() - top - borderMargin;
		// 每个日期格子的宽度和高度
		float cellWidth = calendarWidth / 7;
		float cellHeight = cellWidth;
		//float cellHeight = calendarHeight / 6;
		
		int c = paint.getColor();
		paint.setColor(activity.getResources().getColor(R.color.inner_grid_color));
		
		// 绘制顶端的直线 
		canvas.drawLine(left, top, left + view.getMeasuredWidth()
				- borderMargin * 2, top, paint);
		// 画横线
		for (int i = 1; i < 6; i++)
		{
			 canvas.drawLine(left, top + (cellHeight) * i, left +
			 calendarWidth,
			 top + (cellHeight) * i, paint);
		}
		// 画竖线
		/*for (int i = 1; i < 7; i++)
		{
			 canvas.drawLine(left + cellWidth * i, top, left + cellWidth * i,
			 view.getMeasuredHeight() - borderMargin, paint);
		}*/
		paint.setColor(c);

		// 画日期

		// 生成当前月所要显示的日期文本String days[]数组
		calculateDays();

		java.util.Calendar calendar = java.util.Calendar.getInstance();
		// 获得当前日期的天 1 - 28、29、30、31
		int day = calendar.get(java.util.Calendar.DATE);
		// 获得当前日期的年，月
		int myYear = calendar.get(java.util.Calendar.YEAR), myMonth = calendar
				.get(java.util.Calendar.MONTH);
//Log.d(TAG, "Grid.draw()-->" + myYear + "-" + myMonth + "-" + day);
		
		// 将calendar对象设置为：当前年 当前月  第一天
		calendar.set(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
				1);
		// 这个月的第一天是周几，1 2 3 4 5 6 7 
		int week = calendar.get(java.util.Calendar.DAY_OF_WEEK);
		// todayIndex:今天在所在月days数组的目录,(week和day都是从1开始，days从0开始计数，所以week + day - 2)
		// for循环中draw days[i]会查看days[i]是的在边框画红色，表示当前天
		int todayIndex = week + day - 2;
		
		boolean today = false; // 更行日历头，today=true，日期后面+(今天)
		boolean sunsatIsCurrent = false;
		
		// 今天在days中的目录
		if (currentDayIndex == -1) // 程序第一次运行currentDayIndex = -1,程序运行后值不会为-1
		{
			currentDayIndex = todayIndex; // 将当前日期在days中的位置赋值给 currentDayIndex
Log.d(TAG, "Grid.draw():currentDayIndex-->" + currentDayIndex);
		}
		boolean flag = false; // for 循环中的一个标志位
		
		// 查询数据库中的记录信息
		getRecordDays();
		
/**
 *  绘制日期文本,一共42个日期
 */
		//paint.setAntiAlias(true);
		//paint.setTextAlign(Paint.Align.CENTER);
		for (int i = 0; i < days.length; i++)
		{

//Log.d(TAG, "Grid.draw().for循环-->" + i);
			
			today = false;
			sunsatIsCurrent = false;
			
			int row = i / 7; // 除法：确定所在行
			int col = i % 7; // 求余：确定所在列
			String text = String.valueOf(days[i]);
			
			// 上月和下月的周末， days中*开头的
			// 根据不同日期，设置不同的画笔颜色
			if ((i % 7 == 0 || (i - 6) % 7 == 0) && text.startsWith("*"))
			{
				paint.setColor(sundaySaturdayPrevNextMonthDayColor);// 天蓝
			}
			else if (i % 7 == 0 || (i - 6) % 7 == 0)
			{
				paint.setColor(sundaySaturdayColor); // 深天蓝
				sunsatIsCurrent = true;
			}
			else if (text.startsWith("*"))
			{
				paint.setColor(prevNextMonthDayColor); // 前月后月 普通日期：灰色
			}
			else
			{
				paint.setColor(dayColor); // 当前月普通日期：黑色
			}
			
			// 去掉days数组中的*号
			text = text.startsWith("*") ? text.substring(1) : text;
			
			// 包围日期的长方形
			Rect dst = new Rect();
			dst.left = (int) (left + cellWidth * col);
			dst.top = (int) (top + cellHeight * row);
			dst.bottom = (int) (dst.top + cellHeight + 1);
			dst.right = (int) (dst.left + cellWidth + 1);
			
			String myText = text;
			
			/*
			 * 以方框的中心点画日期
			 */
			float central_x = left + cellWidth*col + cellWidth/2;
			float central_y = top + cellHeight*row + cellHeight/2 + paint.getTextSize()/2;
			
			// 如果当前日期包含信息，在日期文字前加*
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
			
			// draw 日期文字的坐标；坐标起始左下角
			float textLeft = left + cellWidth * col
					+ (cellWidth - paint.measureText(myText)) / 2;
			float textTop = top + cellHeight * (row + 1)
					- (cellHeight - paint.getTextSize() + paint.descent()) / 2;			
			
			/**
			 * paint.FontMetrics 字型尺寸
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
			 * 以方框的中心点画日期
			 
			float central_x = left + cellWidth*col + cellWidth/2;
			float central_y = top + cellHeight*row + cellHeight/2 + paint.getTextSize()/2;*/
			
			
			/*
			 * 如果选定或者点击的日期是当前的年月日，today标志置位，更新日历头+(今天)
			 *  如果所画的年or月不是myYear myMonth，if不执行，
			 *  todayIndex是真实的今天，而不是点击的currentDay
			 */
			if (myYear == currentYear && myMonth == currentMonth
					&& i == todayIndex)
			{
				// 如果days[i] 是今天，绘制红色内衬 文字边框颜色			
				/*paint.setTextSize(currentDaySize);
				paint.setColor(todayBackgroundColor);// 画笔：red
				dst.left += 1;
				dst.top += 1;
				canvas.drawLine(dst.left, dst.top, dst.right, dst.top, paint);
				canvas.drawLine(dst.right, dst.top, dst.right, dst.bottom,
						paint);
				canvas.drawLine(dst.right, dst.bottom, dst.left, dst.bottom,
						paint);
				canvas.drawLine(dst.left, dst.bottom, dst.left, dst.top, paint);*/

				
				paint.setColor(todayColor); // 红色，
				
				today = true; // 点击或者选定的日期是今天，today设为true
			}

			/*
			 *  当单击当前月中显示的上月或者下月日期时，自动显示上月或下月的日历 
			 *  点击后不是立刻画点击的上月或者下月，而且先画当月
			 *  2015 7 8:基础知识不牢固！== 优先级高于 &&，
			 *  找到点击日期后，flag=true，不需调用isCurrentDay方法，
			 */
			/**
			 * (一)点击上月的日期
			 * 
			 */
			if (isCurrentDay(i, currentDayIndex, dst) && flag == false)
			{
				
				/*
				 *  如果点击的是上月或者下月，if中会将currentYear,currentMont,currentDay,
				 *  设置为点击的年月日，currentDay1=currentDay，break跳出for循环，draw结束，
				 *  执行CalendarView onDraw()方法
				 */
				if (days[i].startsWith("*"))
				{
					// 下月
					if (i > 20)
					{
						currentMonth++;
						if (currentMonth == 12)
						{
							currentMonth = 0;
							currentYear++;
						}
//Log.d(TAG, "Grid.draw().下月-->" + i);						
						// 刷新当前日历，重新显示下月日历
						view.invalidate(); // CalendarParent(父类)中的view

					}
					// 上月
					else
					{
						currentMonth--;
						if (currentMonth == -1)
						{
							currentMonth = 11;
							currentYear--;
						}
Log.d(TAG, "Grid.draw().上月-->" + i + "-" + currentMonth);						
//Log.d(TAG, "Grid.draw().上月-->" + i);
						view.invalidate();// 刷新当前日历，重新显示上月日历

					}
					// Parses the specified string as a signed decimal integer value. 
					// The ASCII character - ('-') is recognized as the minus sign
					currentDay = Integer.parseInt(text);
					currentDay1 = currentDay;
					cellX = -1;
					cellY = -1;
					break;// 跳出for循环，draw()执行完毕，接着执行view.invalidate();

				}
				/*
				 * 先在当前日期上显示一个背景图，
				 * 画笔：红色
				 * 画日期 ，日期会在背景图上
				 * col row currentDay变量设置为点击日期
				 * currentday1 赋值为 currentDay
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
					// 在dst上绘制bitmap
					canvas.drawBitmap(bitmap, src, dst, paint);*/
					
					/*if(today) {
						paint.setColor(todayColor); // 红底	
					} else {
						paint.setColor(dayColor); // 黑底
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
					
					paint.setColor(currentDayColor);// 画笔设为白色，选中的日期白字黑底
					
					
					// CalendarView中onKeyDown调用get方法
					currentCol = col;// currentCol设置为当前点击列
					currentRow = row;// currentRow设置为当前点击行
					
					currentDay = Integer.parseInt(text);
					currentDay1 = currentDay;
					// 更新日历头，所以年月日必须为点击的日期
					updateMsg(today);
Log.d(TAG, "Grid.draw()_isCurrentDay:currentDayIndex-->" + currentDayIndex);
				}
			}
			
			// 在网格中画出文字
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
