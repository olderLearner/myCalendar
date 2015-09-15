package fxj.calendar;

import java.text.Format;
import java.util.GregorianCalendar;

import fxj.calendar.datepicker.OnWheelScrollListener;
import fxj.calendar.datepicker.WheelView;
import fxj.calendar.datepicker.adapter.NumericWheelAdapter;
import fxj.calendar.util.EventList;
import fxj.calendar.util.MethodUtil;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AddEventActivity extends Activity implements OnClickListener{

	private static final String TAG = "AddEventActivity";
	
	private static final String emptyTitle = "新建事件";
	private static final int REQUEST_CODE_REPEAT = 99;
	private static final int REQUEST_CODE_REMIND_FIRST = 100;
	private static final int REQUEST_CODE_REMIND_SECOND = 101;
	private static final int REQUEST_CODE_DISPLAY_STATE = 102;
	
	
	private ActionBar mActionBar;
	private View mEventTitle;
	private LayoutInflater inflater;
	private LinearLayout mBegin_LinearLayout;
	private LinearLayout mStop_LinearLayout;
	private View mView;
	private WheelView mYear;
	private WheelView mMonth;
	private WheelView mDay;
	private WheelView mHour;
	private WheelView mMinute;
	/*
	 * 点击日期
	 */
	private int eventYear;
	private int eventMonth;
	private int eventDay;
	/*
	 * 当前 时间
	 */
	private int eventHour;
	private int eventMinute;
	
	// 事件开始时间
	private int event_begin_year;
	private int event_begin_month;
	private int event_begin_day;
	private int event_begin_hour;
	private int event_begin_minute;
	// 事件结束时间
	private int event_stop_year;
	private int event_stop_month;
	private int event_stop_day;
	private int event_stop_hour;
	private int event_stop_minute;
	// 事件持续时间
	private int event_last_year = 0;
	private int event_last_month = 0;
	private int event_last_day = 0;
	private int event_last_hour = 1;
	private int event_last_minute = 0;
	
	/*
	 * 重复
	 */
	public static final String[] repeatItem = new String[]{"永不","每天","每周","每两周","每月","每年"};
	// 数据库记录 0~5
	private int repeatItemIndex = 0;
	
	/*
	 * 提醒
	 */
	public static final long minuteToMillis = 60*1000;
	public static final String[] remindItem = new String[]{"无","事件发生时","5 分钟前","15 分钟前","30 分钟前","1 小时前","2 小时前","1 天前","2 天前","1 周前"};
	public static final long[] remindTime = new long[] { 0, 0,
			5 * minuteToMillis, 15 * minuteToMillis, 30 * minuteToMillis,
			60 * minuteToMillis, 120 * minuteToMillis,
			24 * 60 * minuteToMillis, 48 * 60 * minuteToMillis,
			7 * 24 * 60 * minuteToMillis };
	
	// 0~9
	private int remindItemIndex_first = 0;
	private int remindItemIndex_second = 0;
	private RelativeLayout mRelativeLayout_Second;
	
	/*
	 * 显示为
	 */
	public static final String[] displayStateItem = new String[]{"正忙","空闲"};
	private int displayStateItemIndex = 0;
	
	
	private EditText mTitle,mLocation,mURL,mTip;
	private Switch isTodaySwitch;
	private boolean isTodayEvent= false;
	
	private Button mBegin,mStop;
	private TextView mBegin_TextView,mStop_TextView;
	
	private Button mRepeat,mInviter,mRemind_first,mRemind_second,mCalendar,mDisplay;
	private TextView mRepeat_TextView,mInviter_TextView;
	private TextView mRemind_first_TextView,mRemind_second_TextView;
	private TextView mCalendar_TextView,mDisplay_TextView;
	
	private String mTimeZone = Time.getCurrentTimezone();
	private Time mTemp = new Time(mTimeZone);
	private MyApp myApp;
	
	
	private boolean beginFlag = false;
	private boolean stopFlag = false;

	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		myApp = (MyApp) getApplicationContext();
		inflater = this.getLayoutInflater();
		
		this.setTitle("取消");
		
		mTemp.set(myApp.getmCurrentClickedTime());
		event_begin_year = eventYear = mTemp.year;
		event_begin_month = eventMonth = mTemp.month;
		event_begin_day = eventDay = mTemp.monthDay;
		
		mTemp.setToNow();
		mTemp.normalize(true);
		event_begin_hour = eventHour = mTemp.hour;
		event_begin_minute = eventMinute = mTemp.minute;
		
		event_stop_year = event_begin_year + event_last_year;
		event_stop_month = event_begin_month + event_last_month;
		event_stop_day = event_begin_day + event_last_day;
		event_stop_hour = event_begin_hour + event_last_hour;
		event_stop_minute = event_begin_minute + event_last_minute;
		
		
		
		/*
		mTextView = new TextView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);		
		mTextView.setLayoutParams(lp);
		mTextView.setText("添加事件");
		mTextView.setTextSize(16);
		mTextView.setTextColor(Color.RED);
		mTextView.setGravity(Gravity.CENTER_VERTICAL);
		//mTextView.setBackgroundColor(getResources().getColor(R.color.event_layout_bg));
*/		
		mEventTitle =  inflater.inflate(R.layout.event_title, null);
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
				Gravity.CENTER | Gravity.FILL_VERTICAL);
		mActionBar.setCustomView(mEventTitle, mLayoutParams);
		mActionBar.setDisplayShowCustomEnabled(true);
		// mActionBar.setDisplayHomeAsUpEnabled(true);
		
		getDatepicker();
		mBegin_LinearLayout = (LinearLayout) findViewById(R.id.add_event_wheel_begin);
		mStop_LinearLayout = (LinearLayout) findViewById(R.id.add_event_wheel_stop);
		mRelativeLayout_Second = (RelativeLayout) findViewById(R.id.add_event_second_remind_container);
	}

	@Override
	protected void onStart() {
		
		mTitle = (EditText) findViewById(R.id.add_event_Title);
		mLocation = (EditText) findViewById(R.id.add_event_location);
		mURL = (EditText) findViewById(R.id.add_event_URL);
		mTip = (EditText) findViewById(R.id.add_event_tip);
		
		
		mBegin = (Button) findViewById(R.id.add_event_begin);		
		mBegin.setOnClickListener(this);	
		mBegin_TextView = (TextView) findViewById(R.id.add_event_begin_text);
		mBegin_TextView.setText(getDisplayText(1));
		
		
		mStop = (Button) findViewById(R.id.add_event_stop);
		mStop.setOnClickListener(this);
		mStop_TextView = (TextView) findViewById(R.id.add_event_stop_text);
		mStop_TextView.setText(getDisplayText(2));
		
		
		mRepeat = (Button) findViewById(R.id.add_event_repeat);
		mRepeat.setOnClickListener(this);
		mRepeat_TextView = (TextView) findViewById(R.id.add_event_repeat_text);
		mRepeat_TextView.setText(repeatItem[repeatItemIndex]);
		
		
		// 等待完善
		mInviter = (Button) findViewById(R.id.add_event_invitedperson);
		mInviter.setOnClickListener(this);
		
		mRemind_first = (Button) findViewById(R.id.add_event_remind_first);
		mRemind_first.setOnClickListener(this);
		mRemind_first_TextView = (TextView) findViewById(R.id.add_event_remind_first_text);
		mRemind_first_TextView.setText(remindItem[remindItemIndex_first]);
		
		mRemind_second = (Button) findViewById(R.id.add_event_remind_second);
		mRemind_second.setOnClickListener(this);
		mRemind_second_TextView = (TextView) findViewById(R.id.add_event_remind_second_text);
		mRemind_second_TextView.setText(remindItem[remindItemIndex_second]);
		
		// 等待完善
		mCalendar = (Button) findViewById(R.id.add_event_calendar);
		mCalendar.setOnClickListener(this);
		
		
		mDisplay = (Button) findViewById(R.id.add_event_display_state);
		mDisplay.setOnClickListener(this);
		mDisplay_TextView = (TextView) findViewById(R.id.add_event_display_state_text);
		mDisplay_TextView.setText(displayStateItem[displayStateItemIndex]);
		
		super.onStart();
	}
	
	/**
	 * 1: mBegin_TextView title 
	 * 2: mStop_TextView title
	 * @param code
	 * @return
	 */
	private String getDisplayText(int code) {
		
		StringBuilder mStrBuilder = new StringBuilder();
		Time tmp = new Time(mTimeZone);
		tmp.setToNow();
		tmp.normalize(true);
		switch (code) {
		case 1:
			mStrBuilder.append(event_begin_year).append("年")
						.append(event_begin_month+1).append("月")
						.append(event_begin_day).append("日   ")
						.append(event_begin_hour).append(":")
						.append(String.format("%02d", event_begin_minute));
			// .append((mTemp.minute/15)*15);
			break;
		case 2:
			//mStrBuilder.append(tmp.hour+1).append(":").append(String.format("%02d", tmp.minute));;
			
			mStrBuilder.append(event_stop_year).append("年")
						.append(event_stop_month+1).append("月")
						.append(event_stop_day).append("日   ")
						.append(event_stop_hour).append(":")
						.append(String.format("%02d", event_stop_minute));
			
			break;
		}
		return mStrBuilder.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_add_event, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case  R.id.add_event_edit_complete:
			/*	
			 * 	12 个
			 *  String title 			可以为null 默认值："新建事件"
			 *  String location 		可以为null 默认  " "
			 *  boolean isallday 		默认 false
			 *  long begin_date 		不为null
			 *  long stop_date 			不为null
			 *  boolean isremind		是否提醒 默认false 
			 *  long remind_first_date
			 *  long remind_second_date
			 *  int repeat 
			 *  int display_state
			 *  String url 			可以为null " "
			 *  String tip 			可以为null " "
			 */
			
			//SQLiteDatabase xx = MonthActivity.calendarDBService.getReadableDatabase();
			
			String event_title = mTitle.getText().toString().trim();
			String event_location = mLocation.getText().toString().trim();
			String event_url = mURL.getText().toString().trim();
			String event_tip = mTip.getText().toString().trim();
			boolean event_isallday = false;
			
			GregorianCalendar gc = new GregorianCalendar(event_begin_year, event_begin_month, event_begin_day, event_begin_hour, event_begin_minute);			
			long event_begin_date = gc.getTimeInMillis();
			gc.set(event_stop_year, event_stop_month, event_stop_day, event_stop_hour, event_stop_minute);
			long event_stop_date = gc.getTimeInMillis();
			
			boolean isremind = false;
			long event_remind_first_date = -1;
			long event_remind_second_date = -1;
			if (remindItemIndex_first != 0) {
				isremind = true;
				event_remind_first_date = event_begin_date + remindTime[remindItemIndex_first];
				if (remindItemIndex_second != 0) {
					event_remind_second_date = event_begin_date + remindTime[remindItemIndex_second];
				}
			} 
			
			int event_repeat = repeatItemIndex;
			int event_displaystate = displayStateItemIndex;
			
			MonthActivity.calendarDBService.insertRecord(event_title,
					event_location, false, event_begin_date, event_stop_date, isremind,
					event_remind_first_date, event_remind_second_date, event_repeat,
					event_displaystate, event_url, event_tip);
			
			/*
			Log.d(TAG, "保存的数据为：" + event_title+ "\n"+ event_location+"\n"+event_url + "\n" + event_tip + "\n" + "开始时间： " + event_begin_date +"\n"
					+ "结束时间:  " +event_stop_date+ "\n" + "是否提醒 :  " + isremind + "\n" + "第一次提醒时间：  " +event_remind_first_date+ "\n"
					+"第二次提醒：  "+event_remind_second_date);
			*/
			
			mTemp.set(event_begin_day, event_begin_month, event_begin_year);
			mTemp.normalize(true);
			EventList tmp = new EventList();
			tmp.id = MonthActivity.calendarDBService.getMaxId(mTemp.toMillis(true));
			tmp.title = event_title;
			tmp.location = event_location;
			tmp.begin_date = event_begin_date;
			tmp.stop_date = event_stop_date;
			//Log.d(TAG, cursor.getString(1)+ "----"+ cursor.getString(2));
			//Log.d(TAG, "mEventDisplay.title"+ mEventDisplay.title);
			MonthActivity.eventList.add(tmp);
			
			finish();
			
			break;
		case android.R.id.home:
			//Toast.makeText(this, "dian ji qu xiao", Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
		
		
		return super.onOptionsItemSelected(item);
		
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_event_begin:			
			if (stopFlag ==true) {
				mStop_LinearLayout.removeView(mView);	
				mStop_LinearLayout.setVisibility(View.GONE);
				mStop_TextView.setTextColor(Color.BLACK);
				stopFlag = false;
			}
			
			if (beginFlag == false) {
				updateWheelParams(event_begin_year, event_begin_month, event_begin_day, event_begin_hour, event_begin_minute);
				mBegin_LinearLayout.addView(mView);
				mBegin_LinearLayout.setVisibility(View.VISIBLE);
				mBegin_TextView.setTextColor(Color.RED);
				beginFlag = true;
			} else {
				mBegin_LinearLayout.removeView(mView);
				mBegin_LinearLayout.setVisibility(View.GONE);
				mBegin_TextView.setTextColor(Color.BLACK);
				beginFlag = false;
			}
			
			break;
		case R.id.add_event_stop:
			if (beginFlag == true) {
				mBegin_LinearLayout.removeView(mView);
				mBegin_LinearLayout.setVisibility(View.GONE);
				mBegin_TextView.setTextColor(Color.BLACK);
				beginFlag = false;
			}
			if (stopFlag ==false) {
				updateWheelParams(event_stop_year, event_stop_month, event_stop_day, event_stop_hour, event_stop_minute);
				mStop_LinearLayout.addView(mView);
				mStop_LinearLayout.setVisibility(View.VISIBLE);
				mStop_TextView.setTextColor(Color.RED);
				stopFlag = true;
			} else {
				mStop_LinearLayout.removeView(mView);
				mStop_LinearLayout.setVisibility(View.GONE);
				mStop_TextView.setTextColor(Color.BLACK);
				stopFlag = false;
			}
			
			break;
		case R.id.add_event_repeat:
			Intent intentRepeat = new Intent(this, EventRepeatActivity.class);
			intentRepeat.putExtra("repeatIndex", repeatItemIndex);
			startActivityForResult(intentRepeat, REQUEST_CODE_REPEAT);
			break;
		case R.id.add_event_invitedperson:
			
			Toast.makeText(this, "正在施工，稍后体验......", Toast.LENGTH_SHORT).show();
			break;
		case R.id.add_event_remind_first:

			Intent intentRemindFirst = new Intent(this, EventRemindActivity.class);
			/*
			int[] info = new int[2];
			info[0] = 0;
			info[1] = remindItemIndex_first;
			intentRemindFirst.putExtra("remindIndex", info);*/
			
			intentRemindFirst.putExtra("remindIndex", remindItemIndex_first);
			startActivityForResult(intentRemindFirst, REQUEST_CODE_REMIND_FIRST);
			break;
		case R.id.add_event_remind_second:
			
			Intent intentRemindSecond = new Intent(this, EventRemindActivity.class);
			intentRemindSecond.putExtra("remindIndex", remindItemIndex_second);
			startActivityForResult(intentRemindSecond, REQUEST_CODE_REMIND_SECOND);

			//Toast.makeText(this, "正在施工，稍后体验......", Toast.LENGTH_SHORT).show();
			break;
		case R.id.add_event_calendar:

			Toast.makeText(this, "正在施工，稍后体验......", Toast.LENGTH_SHORT).show();
			break;
		case R.id.add_event_display_state:
			
			Intent intentDisState = new Intent(this, EventDisplayStateActivity.class);
			intentDisState.putExtra("displayIndex", displayStateItemIndex);
			startActivityForResult(intentDisState, REQUEST_CODE_DISPLAY_STATE);
			//Toast.makeText(this, "正在施工，稍后体验......", Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CODE_REPEAT:
			if (resultCode == Activity.RESULT_OK) {
				repeatItemIndex = data.getIntExtra("repeatSet", 0);
				mRepeat_TextView.setText(repeatItem[repeatItemIndex]);
			}

			break;
		case REQUEST_CODE_REMIND_FIRST:
			if (resultCode == Activity.RESULT_OK) {
				remindItemIndex_first = data.getIntExtra(
						EventRemindActivity.REMIND_SET, 0);
				mRemind_first_TextView
						.setText(remindItem[remindItemIndex_first]);
			}
			if (remindItemIndex_first != 0) {
				mRelativeLayout_Second.setVisibility(View.VISIBLE);
			} else {
				mRelativeLayout_Second.setVisibility(View.GONE);
			}
			break;
		case REQUEST_CODE_REMIND_SECOND:
			if (resultCode == Activity.RESULT_OK) {
				remindItemIndex_second = data.getIntExtra(
						EventRemindActivity.REMIND_SET, 0);
				mRemind_second_TextView
						.setText(remindItem[remindItemIndex_second]);
			}
			break;
		case REQUEST_CODE_DISPLAY_STATE:
			if (resultCode == Activity.RESULT_OK) {
				displayStateItemIndex = data.getIntExtra(EventDisplayStateActivity.DISPLAY_STATE, 0);
				mDisplay_TextView.setText(displayStateItem[displayStateItemIndex]);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private View getDatepicker() {

		mView = inflater.inflate(R.layout.event_datepicker, null);

		mYear = (WheelView) mView.findViewById(R.id.year_datepicker);
		NumericWheelAdapter mYearAdapter = new NumericWheelAdapter(this, 1949,
				2049);
		mYearAdapter.setLabel("年");
		mYear.setViewAdapter(mYearAdapter);
		mYear.setCyclic(true);
		mYear.addScrollingListener(scrollListener);

		mMonth = (WheelView) mView.findViewById(R.id.month_datepicker);
		NumericWheelAdapter mMonthAdapter = new NumericWheelAdapter(this, 1,
				12, "%02d");
		mMonthAdapter.setLabel("月");
		mMonth.setViewAdapter(mMonthAdapter);
		mMonth.setCyclic(true);
		mMonth.addScrollingListener(scrollListener);

		mDay = (WheelView) mView.findViewById(R.id.day_datepicker);
		initDay(eventYear, eventMonth);
		mDay.setCyclic(true);
		mDay.addScrollingListener(scrollListener);
		
		mHour = (WheelView) mView.findViewById(R.id.hour_datepicker);
		NumericWheelAdapter mHourAdapter = new NumericWheelAdapter(this, 0,
				23, "%02d");
		mHourAdapter.setLabel("时");
		mHour.setViewAdapter(mHourAdapter);
		mHour.setCyclic(true);
		mHour.addScrollingListener(scrollListener);
		
		mMinute = (WheelView) mView.findViewById(R.id.minute_datepicker);
		NumericWheelAdapter mMinuteAdapter = new NumericWheelAdapter(this, 0,
				59, "%02d");
		mMinuteAdapter.setLabel("分");
		mMinute.setViewAdapter(mMinuteAdapter);
		mMinute.setCyclic(true);
		mMinute.addScrollingListener(scrollListener);
		

		mYear.setCurrentItem(event_begin_year - 1949);
		mYear.setVisibleItems(5);
		mMonth.setCurrentItem(event_begin_month);
		mMonth.setVisibleItems(5);
		mDay.setCurrentItem(event_begin_day - 1);
		mDay.setVisibleItems(5);
		mHour.setCurrentItem(event_begin_hour);
		mHour.setVisibleItems(5);
		mMinute.setCurrentItem(event_begin_minute);
		//mMinute.setVisibility(5);gone 8 invisible 4 visible 0
		mMinute.setVisibleItems(5);
		
		return mView;
	}
	
	private void updateWheelParams(int year,int month,int day,int hour,int minute) {
		
		mYear.setCurrentItem(year - 1949);
		mMonth.setCurrentItem(month);
		initDay(year, month);
		mDay.setCurrentItem(day - 1);
		mHour.setCurrentItem(hour);
		mMinute.setCurrentItem(minute);
	}
	
	private void initDay(int year, int month) {

		NumericWheelAdapter mDayAdapter = new NumericWheelAdapter(this, 1,
				MethodUtil.getDay(year, month), "%02d");
		mDayAdapter.setLabel("日");
		mDay.setViewAdapter(mDayAdapter);
	}

	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {

		@Override
		public void onScrollingStarted(WheelView wheel) {

		}

		@Override
		/**
		 * 滚轮停止，获取滚轮显示的数字，根据显示的年月重新设定日的适配器
		 */
		public void onScrollingFinished(WheelView wheel) {

			int dis_year = mYear.getCurrentItem() + 1949;
			int dis_month = mMonth.getCurrentItem();
			initDay(dis_year, dis_month);// 更新日期适配器
			int dis_day = mDay.getCurrentItem() + 1;
			int dis_hour = mHour.getCurrentItem();
			int dis_minute = mMinute.getCurrentItem();
			// 将当前日期显示在textView上

			/*
			 * 格式化有问题 原理不知道 2015 8 3 SimpleDateFormat sdf = new
			 * SimpleDateFormat("yyyy年m月d日"); calendar.set(dis_year, dis_month,
			 * dis_day); Log.d(TAG, "calendar-->"+ calendar.getTime()); String
			 * date = sdf.format(calendar.getTime()); Log.d(TAG, "msg-->"+
			 * date);
			 * 
			 * String date1 = new StringBuilder().append(dis_year + "年")
					.append((dis_month + 1) + "月").append(dis_day + "日   ")
					.append(dis_hour + ":").append(String.format("%02d", dis_minute))
					.toString();
			 * 
			 * 
			 */
			// 开始 滚轮显示
			if (beginFlag == true) {
				updateEventBeginParams(dis_year, dis_month, dis_day, dis_hour, dis_minute);								
				mBegin_TextView.setText(getDisplayText(1));
				mStop_TextView.setText(getDisplayText(2));
			}
			if (stopFlag == true) {
				updateEventStopParams(dis_year, dis_month, dis_day, dis_hour, dis_minute);
				updateEventLastParams();
				mStop_TextView.setText(getDisplayText(2));
			}
			
		}
	};
	
	private void updateEventBeginParams(int year, int month, int day, int hour, int minute) {
		if (beginFlag == true) {
			event_begin_year = year;
			event_begin_month = month;
			event_begin_day = day;
			event_begin_hour = hour;
			event_begin_minute = minute;
			
			updateEventStopParams();
			
		}
		
	}
	
	
	private void updateEventStopParams() {
		event_stop_year = event_begin_year + event_last_year;
		event_stop_month = event_begin_month + event_last_month;
		event_stop_day = event_begin_day + event_last_day;
		event_stop_hour = event_begin_hour + event_last_hour;
		event_stop_minute = event_begin_minute + event_last_minute;
	}
	
	private void updateEventStopParams(int year, int month, int day, int hour,
			int minute) {
		event_stop_year = year;
		event_stop_month = month;
		event_stop_day = day;
		event_stop_hour = hour;
		event_stop_minute = minute;
	}
	
	private void updateEventLastParams() {
		
		event_last_year = event_stop_year - event_begin_year;
		event_last_month = event_stop_month - event_begin_month;
		event_last_day = event_stop_day - event_begin_day;
		event_last_hour = event_stop_hour - event_begin_hour;
		event_last_minute = event_stop_minute - event_begin_minute;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
