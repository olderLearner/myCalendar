package fxj.calendar;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import fxj.calendar.db.CalendarDBService;
import fxj.calendar.lunar.LunarFragment;
import fxj.calendar.solar.OnClickedDayChangedListener;
import fxj.calendar.solar.OnMonthScrollListener;
import fxj.calendar.solar.SolarView;
import fxj.calendar.solar.SolarMonth;
import fxj.calendar.solar.SolarMonthAdapter;
import fxj.calendar.util.EventList;
import fxj.calendar.util.MyFixed;
import fxj.calendar.year.WholeYearActivity;
import fxj.weather.CityPickerActivity;
import fxj.weather.city.Province;
import fxj.weather.json.Result;
import fxj.weather.json.Weather;
import fxj.weather.json.Weather_data;
import fxj.weather.util.HttpUtils;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MonthActivity extends Activity implements android.view.View.OnClickListener,OnItemClickListener
{
	//private static final String TAG = MonthActivity.class.getCanonicalName();
	private static final String TAG = "MonthActivity";
	
	private static final int REQUEST_SELECTED_DATE = 29;

	private static final int REQUEST_SELECT_YEAR = 30;

	protected static final int DELAY_SHOW_EVENT = 1;

	protected static final int UPDATE_WEATHER = 99;
	
	public MyApp myApp;
	public static CalendarDBService calendarDBService;
	//public static DBService db;
	
	/*
	 * 显示事件记录，成员变量 
	 */
	private static String[] empty_date = new String[]{" ", "无事件", " ", " ", " "};
	private ListView mListView; // 显示 事件
	private ListView empty_ListView; // 显示：无事件
	public static List<EventList> eventList; // 若点击日期事件小于5条，补空事件，使listView 充满父控件
	private EventAdapter mEventAdapter;
	
	public static List<Integer> idList = new ArrayList<Integer>();
	
	
	/** weather 变量 */
	public static  List<Province> provinces;
	private String myHome = "101060403";
	private TextView home,homeWeather;
	
	
	
	
	
	
	private boolean switchFlag=true;
	
	/** solar 成员变量  */
	private SolarView mSloarView;
	private LinearLayout mSolarMonthContainer;
	private SolarMonth mSolarMonth;
	private SolarMonthAdapter mSolarMonthAdatpter;
	private Button solarMonth_btn_Today,solarMonth_btn_Add,solarMonth_btn_Lunar;
	
	/** lunar 成员变量  */
	/** 2015 8 20 test switch */
	View layout_Solar,layout_Lunar;
	
	/** first inflate lunar 需要初始化 lunar layout 中的控件*/
	private boolean lunarFlag= true;
	private Button lunarMonth_btn_Today,lunarMonth_btn_Add,lunarMonth_btn_Solar;
	
	/** 点击日期改变  */
	// 当前月份的 index ，初始化solar 赋初值
	private int currentIndex = -1;
	// 月份转换以前的 index 初始化solar 赋初值
	private int lastIndex = -1;
	
	
	private TextView tv;
	Calendar calendar;
	private Time mTempTime = new Time(MyFixed.TIMEZONE);
	private LayoutInflater inflater;
	private View mView= null;
	private View mActionBarView = null;
	
	/** ui  */
	public static Activity activity;
	private ActionBar actionBar;
	
	
	/** 以下为参考成员变量 */
	
	private AlertDialog.Builder builder;
	private AlertDialog adMyDate;
	
	private CalendarView calendarView;	
	public static Remind remindQueue;
	public List<Remind> remindList = new ArrayList<Remind>();
	public AlarmManager am;
	public static MediaPlayer mediaPlayer;
	public static Vibrator vibrator;

	
	private Menu menu;
	
	private DatePicker dpSelectDate;
	private TextView tvDate;
	private TextView tvLunarDate;
	private LinearLayout  myDateLayout;
	
	
	
	/*
	 *  solar lunar 转换菜单按钮的一些标志
	 * true solar false lunar
	 */
//	private MenuItem switchState;
//	private boolean firstLunarToSolar = true;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		myApp = (MyApp) getApplicationContext();
		updateSolarTitle();
		
		if (activity == null)
			activity = this;
		if (remindQueue == null)
			remindQueue = new Remind();
		if (am == null)
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
		if (calendarDBService == null) {
			calendarDBService = new CalendarDBService(this);
			SQLiteDatabase xx = calendarDBService.getReadableDatabase();
		}
		if (eventList == null) {
			eventList = new ArrayList<EventList>();
		}
		
		Window window = getWindow();
		
		/** 三种获得layout inflater 方法 */
		//inflater = window.getLayoutInflater();
		//inflater = this.getLayoutInflater();
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		layout_Solar = inflater.inflate(R.layout.activity_solarmonth, null);		
		layout_Lunar = inflater.inflate(R.layout.activity_lunarmonth, null);
		
		/** 三种装载view的方法 */
		//setContentView(R.layout.activity_main);
		//setContentView(view, params);		
		if (switchFlag = true) {
			setContentView(layout_Solar);//内部调用了 initActionBar（）；
			init_Solar();
			
		} else {
			setContentView(layout_Lunar);
			init_Lunar();
		}
		
		/** custom actionbar */
		initAcitonbar();
		
		/** 解析城市  */
		exactProvince();
		
		/** 事件提醒 */
		try
		{
			// Retrieve a PendingIntent that will perform a broadcast, like calling Context.sendBroadcast()
			Intent intent = new Intent(activity, CallAlarm.class);
			PendingIntent sender = PendingIntent.getBroadcast(activity, 0,
					intent, 0);
			am.setRepeating(AlarmManager.RTC, 0, 60 * 1000, sender);
		}
		catch (Exception e)
		{	
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//Log.d(TAG, "onStart()-->");
	}
	
	private void initAcitonbar() {
		
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.title_bar);
		mActionBarView = inflater.inflate(R.layout.monthactivity_title_view,
				null);// actionbar 加入自定义view
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		// actionBar.setDisplayShowTitleEnabled(false);
		customActionbarHome(actionBar);

		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		actionBar.setCustomView(mActionBarView, mLayoutParams);
		actionBar.setDisplayShowCustomEnabled(true);

		actionBar.show();
		
		//home = (TextView) mActionBarView.findViewById(R.id.main_city);
		homeWeather = (TextView) mActionBarView.findViewById(R.id.main_weather);
		
		/*
		 * ActionBar.LayoutParams mLayoutParams = new
		 * ActionBar.LayoutParams(LayoutParams.MATCH_PARENT,
		 * 30,Gravity.BOTTOM|Gravity.FILL_HORIZONTAL);
		 * actionBar.setCustomView(mActionBarView, mLayoutParams);
		 * actionBar.setDisplayShowCustomEnabled(true);
		 */
		// actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
		
	}
	
	
	private void exactProvince() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					provinces = HttpUtils.getProvinces(MonthActivity.this);
					
					handler.sendEmptyMessage(UPDATE_WEATHER);
					
				} catch (XmlPullParserException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}, "exact province").start();
		
//		new Thread(new Runnable() {
//		
//		@Override
//		public void run() {
//			try {
//				provinces = HttpUtils.getProvinces(MonthActivity.this);
//			} catch (XmlPullParserException e1) {
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//			
//			
//		}
//	}, "exact province").start();
		
	}
	
	/**
	 * time 设置为  00:00:00：000
	 * @param time
	 * @return
	 */
	private Time timeToQuery(Time time) {
		Time mTime = new Time(MyFixed.TIMEZONE);
		mTime.set(time.monthDay,time.month,time.year);
		mTime.normalize(true);
		return mTime;
	}
	
	private void initEmptyListView() {
		
		//empty_ListView = (ListView) findViewById(R.id.empty_listview);
		empty_ListView = new ListView(activity);
		empty_ListView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT));
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.empty_list_item, empty_date);
		empty_ListView.setAdapter(mArrayAdapter);
		empty_ListView.setClickable(false);
		empty_ListView.setVisibility(View.GONE);
		
	}
	
	
	/**
	 * t.set(31, 7, 2015),查询点击日期的事件的 time 对象必须采用此方式设置，
	 * 时间是 2015-8-31-00-00-000 ~ 2015-8-31-23-59-59-999
	 */
	private void init_EventAdapter() {
		
		Time t = new Time(MyFixed.TIMEZONE);
		t.set(timeToQuery(myApp.getmCurrentClickedTime()));
		t.normalize(true);
		
		//Cursor cursor = MonthActivity.calendarDBService.query(t.toMillis(true));
		Cursor cursor = MonthActivity.calendarDBService.queryByBeginDateAsc(t.toMillis(true));
		
		//Log.d(TAG, "cursor size --->"+ cursor.getCount());// 查询是对的
		/*
		 * while外定义的对象，while中赋值，添加到list中出问题，所有的值都是最后一个值
		 * 为什么以后在搞清楚吧 2015 8 29
		 */
		//EventDisplay mEventDisplay = new EventDisplay();
		if(eventList.size() != 0) eventList.clear();
		idList.clear();
		
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				EventList tmp = new EventList();
				tmp.id = cursor.getInt(0);
				tmp.title = cursor.getString(1);
				tmp.location = cursor.getString(2);
				tmp.begin_date = cursor.getLong(3);
				tmp.stop_date = cursor.getLong(4);
				eventList.add(tmp);
				idList.add(tmp.id);
			}
		}
		int l = eventList.size();
		if (l!=0 && l<5) {
			for (int j=l;j<5;j++) {
				EventList tmp = new EventList();
				tmp.id = -1;
				tmp.title = " ";
				tmp.location = " ";
				tmp.begin_date = 0;
				tmp.stop_date = 0;
				eventList.add(j, tmp);
				idList.add(tmp.id);
			}
		}
		
		mEventAdapter = new EventAdapter(this, eventList);
		
	}
	
	/**
	 * 初始化 lunarMonth 界面
	 */
	private void init_Lunar() {
		
		lunarFragment = new LunarFragment();		
		getFragmentManager().beginTransaction().add(R.id.lunarmonth_container, lunarFragment).commit();
		lunarFragment.setMyApp(myApp);
		
		lunarMonth_btn_Today = (Button) findViewById(R.id.lunarmonth_today);
		lunarMonth_btn_Today.setOnClickListener(this);
		
		lunarMonth_btn_Add = (Button) findViewById(R.id.lunarmonth_add);
		lunarMonth_btn_Add.setOnClickListener(this);
		
		lunarMonth_btn_Solar= (Button) findViewById(R.id.lunarmonth_solar);
		lunarMonth_btn_Solar.setOnClickListener(this);
	}
	
	/**
	 * 初始化 solarMonth 界面
	 */
	private void init_Solar() {
		
		mSolarMonthContainer = (LinearLayout) findViewById(R.id.solarmonth_container);
		mView = inflater.inflate(R.layout.wheelmonth, null);

		solarMonth_btn_Today = (Button) findViewById(R.id.solarmonth_today);
		solarMonth_btn_Today.setOnClickListener(this);
		
		solarMonth_btn_Add = (Button) findViewById(R.id.solarmonth_add);
		solarMonth_btn_Add.setOnClickListener(this);
		
		solarMonth_btn_Lunar = (Button) findViewById(R.id.solarmonth_lunar);
		solarMonth_btn_Lunar.setOnClickListener(this);


		initEmptyListView();
		mListView = (ListView) findViewById(R.id.record_listview);		
		init_EventAdapter();
		mListView.setAdapter(mEventAdapter);
		if (mEventAdapter.isEmpty()) {
			Log.d(TAG, "adapter is empty");
		}
		mListView.setOnItemClickListener(this);// listview 是 adapterview 子类
		((ViewGroup)mListView.getParent()).addView(empty_ListView);
		mListView.setEmptyView(empty_ListView);
		
		/*
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, date);
		mListView.setAdapter(mArrayAdapter);*/

		
		mSolarMonth = (SolarMonth) mView.findViewById(R.id.wheelmonthview);
		mSolarMonthAdatpter = new SolarMonthAdapter(this, mSolarMonth);
		mSolarMonthAdatpter.setMyApp(myApp);
		mSolarMonthAdatpter.setOnClickedDayChange(onClickDayChange);// 通知主activity点击日期更新接口
		
		mSolarMonth.setViewAdapter(mSolarMonthAdatpter);
		mSolarMonth.addScrollingListener(scroll_Listener);
		mSolarMonth.setCyclic(false);
		mSolarMonth.setVisibleItems(1);
		//mWheelMonth.setCurrentItem((2015 - 1949) * 12 + 7);
		int t = index(myApp.getmCurrentClickedTime());
		currentIndex = lastIndex = t; // 赋初值
		mSolarMonth.setCurrentItem(t);
		mSolarMonth.setLastItem(t);
		mSolarMonth.setMyApp(myApp);
		mSolarMonth.setInterpolator(AnimationUtils.loadInterpolator(this,
				android.R.anim.accelerate_decelerate_interpolator));
		//this.mSolarMonth = mSolarMonth;
		mSolarMonthContainer.addView(mView);
		
	}
	
	private void updateLunarTitle() {
		Time mTime = new Time(MyFixed.TIMEZONE);
		mTime.set(myApp.getmCurrentClickedTime());
		this.setTitle(mTime.year+"年");
	}
	
	private void updateSolarTitle() {
		Time mTime = new Time(MyFixed.TIMEZONE);
		mTime.set(myApp.getmCurrentClickedTime());
		this.setTitle(mTime.year+"年"+(mTime.month+1)+"月");
	}
	
	/**
	 * 计算点击日期在数组中的index
	 * @param time
	 * @return
	 */
	private int index(Time time) {
		
		Time mTime = new Time(MyFixed.TIMEZONE);
		mTime.set(time);
		mTime.normalize(true);
		int result = (mTime.year - MyFixed.MIN_YEAR)*12 + mTime.month;
		
		return result;
	}
	
	/**
	 * solar 月份变化监听器：滚动停止，更新title
	 */
	OnMonthScrollListener scroll_Listener = new OnMonthScrollListener() {
		
		@Override
		public void onScrollingStarted(SolarMonth wheel) {
			
		}
		
		@Override
		public void onScrollingFinished(SolarMonth wheel) {
			
			currentIndex = mSolarMonth.getCurrentItem();
			if (currentIndex != lastIndex) {
				// 月份改变更新标题
				MonthActivity.this.setTitle((MyFixed.MIN_YEAR+currentIndex/12)+"年"+(currentIndex%12+1)+"月");
				lastIndex = currentIndex;
			}
			
		}
	};
	
	/**
	 * solar 点击日期变化监听器
	 */
	OnClickedDayChangedListener onClickDayChange = new OnClickedDayChangedListener() {
		
		@Override
		public void onClickedDayChanged() {
			
			//displayClickDay();
			refreshEventList();
			
		}
	};

	private LunarFragment lunarFragment;
	
	/**
	 * 提示点击日期变化
	 */
	private void displayClickDay () {
		Time mTime = new Time(MyFixed.TIMEZONE);
		mTime.set(myApp.getmCurrentClickedTime());
		mTime.normalize(true);
		
		Date date = new Date(mTime.toMillis(true));
		Toast.makeText(MonthActivity.this, "点击:"+ MyFixed.mSDF_03.format(date), Toast.LENGTH_SHORT).show();
		
	}
	
	/**
	 * 点击日期改变，重新查找 event
	 */
	private void refreshEventList() {
		Time t = new Time(MyFixed.TIMEZONE);
		t.set(timeToQuery(myApp.getmCurrentClickedTime()));
		t.normalize(true);
		Cursor cursor = MonthActivity.calendarDBService.queryByBeginDateAsc(t.toMillis(true));
		
		if(eventList.size() !=0) eventList.clear();
		idList.clear();
		if (cursor.getCount() != 0) {
			while (cursor.moveToNext()) {
				EventList tmp = new EventList();
				tmp.id = cursor.getInt(0);
				tmp.title = cursor.getString(1);
				tmp.location = cursor.getString(2);
				tmp.begin_date = cursor.getLong(3);
				tmp.stop_date = cursor.getLong(4);
				eventList.add(tmp);
				idList.add(tmp.id);
			}
		}
		int l = eventList.size();
		if (l!=0 && l<5) {
			for (int j=l;j<5;j++) {
				EventList tmp = new EventList();
				tmp.id = -1;
				tmp.title = " ";
				tmp.location = " ";
				tmp.begin_date = 0;
				tmp.stop_date = 0;
				eventList.add(j, tmp);
				idList.add(tmp.id);
			}
		}
		
//		Log.d(TAG, "eventList size --->" + eventList.size());
//		for (int i = 0; i < eventList.size(); i++) {
//			Log.d(TAG, "each title -->" + eventList.get(i).getId());
//		}
		
		
		mEventAdapter.notifyDataSetChanged();
		
	}
	
	public void customActionbarHome(ActionBar bar) {
		try {
			
			/*
			 * 这段有作用
			 */
			/*
			ViewConfiguration config = ViewConfiguration.get(activity);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
			*/
			
			Class<?> cActionBarImpl = Class.forName("com.android.internal.app.ActionBarImpl");
			Class<?> cActionBarView = Class.forName("com.android.internal.widget.ActionBarView");
			
			Field fActionView = cActionBarImpl.getDeclaredField("mActionView");
			fActionView.setAccessible(true);
			Object objActionView = fActionView.get(bar);
			
			Field fHomeLayout = cActionBarView.getDeclaredField("mHomeLayout");
			fHomeLayout.setAccessible(true);
			Object objHomeView = fHomeLayout.get(objActionView);
			
			Field fTitleLayout = cActionBarView.getDeclaredField("mTitleLayout");
			fTitleLayout.setAccessible(true);
			Object objTitleLayout = fTitleLayout.get(objActionView);
			
			Field fTitleView = cActionBarView.getDeclaredField("mTitleView");
			fTitleView.setAccessible(true);
			Object objTitleView = fTitleView.get(objActionView);
			
//			Field fUpView = ((Class) objHomeView).getDeclaredField("mUpView");// 没用
//			fUpView.setAccessible(true);
//			Object objUpView = fUpView.get(objHomeView);
//			
//			((ImageView) objUpView).setBackgroundColor(Color.BLUE);
			
			
//			((TextView) objTitleView).setClickable(true);
//			((TextView) objTitleView).setTextColor(R.drawable.title_color); // 有作用
//			
//			((TextView) objTitleView).setOnClickListener(new android.view.View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Toast.makeText(MonthActivity.this, "点啥", Toast.LENGTH_SHORT).show();
//					
//				}
//			});
			
			/**这段有作用*/
			((FrameLayout) objHomeView).setBackgroundColor(Color.TRANSPARENT);
			((LinearLayout) objTitleLayout).setBackgroundColor(Color.TRANSPARENT);// 改了背景
			//((LinearLayout) objTitleLayout).setAlpha(0.2f);// 可以改透明度
			
			/* 没啥作用
			((LinearLayout) objTitleLayout).setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(200, 10);
			((LinearLayout) objTitleLayout).setLayoutParams(ll);
			*/
			
			Class<?> clazz = Class.forName("com.android.internal.widget.ActionBarView.HomeView");  
            Method m = clazz.getDeclaredMethod("setUpIndicator", Drawable.class);  
            m.setAccessible(true);             
            m.invoke(bar, getResources().getDrawable(R.drawable.home_up_indicator));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy-->");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()-->");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//refreshEventList();
		mEventAdapter.notifyDataSetChanged();
		
		
		Log.d(TAG, "onRestart()-->");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()-->");
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()-->");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.switchmenu, menu);
		this.menu = menu;
//		switchState = menu.findItem(R.id.switch_change_view);
//		if (switchFlag== true) {
//			switchState.setTitle("Lunar");
//		} else {
//			switchState.setTitle("Solar");
//		}
		
		return super.onCreateOptionsMenu(menu);
		
		/*MenuItem miToday = menu.add(0, 1, 1, "今天");
		MenuItem miMyDate = menu.add(0, 2, 2, "指定日期");
		MenuItem miRecordRemind = menu.add(0, 3, 3, "记录/提醒");				
		MenuItem miAbout = menu.add(0, 18, 18, "关于");
		
		miToday.setIcon(R.drawable.clock);
		miToday.setOnMenuItemClickListener(new OnTodayMenuItemClick(this));
		
		miMyDate.setIcon(R.drawable.calendar_small);
		miMyDate.setOnMenuItemClickListener(new OnMyDateMenuItemClick(this));
		
		miRecordRemind.setIcon(R.drawable.diary);
		miRecordRemind.setOnMenuItemClickListener(new OnRecordRemindMenuItemClick(
						this));
		
		miAbout.setIcon(R.drawable.about);
		miAbout.setOnMenuItemClickListener(new OnAboutMenuItemClick(this));*/
		
		//return true;
	}
	
	private void setIconEnable(Menu menu, boolean enable)  {  
        try   
        {  
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");  
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);  
            m.setAccessible(true);  
              
            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)  
            m.invoke(menu, enable);  
              
        } catch (Exception e)   
        {  
            e.printStackTrace();  
        }  
    }
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SELECTED_DATE:
			if (resultCode == Activity.RESULT_OK) {
				int[] newDate = data.getExtras().getIntArray("selectedDate");
				if (newDate.length == 3) {
					calendarView.ce.grid.currentYear = newDate[0];
					calendarView.ce.grid.currentMonth = newDate[1];
					calendarView.ce.grid.currentDay = newDate[2];
					calendarView.invalidate();
				}
			}
			break;
		case REQUEST_SELECT_YEAR:
			if (resultCode == Activity.RESULT_OK) {
				// 数据传递过来了  全局点击日期已经发生了变化，solar month这里老烦人了
				//Toast.makeText(this, "点击日期为："+myApp.getmCurrentClickedTime().year+"-"+myApp.getmCurrentClickedTime().month, Toast.LENGTH_SHORT).show();
				Time mTime = new Time(MyFixed.TIMEZONE);
				mTime.setToNow();
				mTime.normalize(true);
				mTempTime.set(myApp.getmCurrentClickedTime());
				if (mTime.year == mTempTime.year && mTime.month == mTempTime.month ) {// 今天所在的月份
					 // 从新更新点击日期为今天 2015 9 13
					myApp.setmCurrentClickedTime(mTime);
					mSolarMonthAdatpter.refreshClick(mTime);
					
					
					
					
					
//					int currentIndex = mSolarMonth.getCurrentItem();
//					int locationIndex = (mTime.year - MyFixed.MIN_YEAR) * 12 + mTime.month;
//					mSolarMonth.setCurrentItem(locationIndex, currentIndex,false);
					updateSolarTitle();
					
					
					//mSolarMonthAdatpter.notifyClickedDayChanged();
				} else { //不是当前月份
//					int currentIndex = mSolarMonth.getCurrentItem();
//					int locationIndex = (mTempTime.year - MyFixed.MIN_YEAR) * 12 + mTempTime.month;
//					mSolarMonth.setCurrentItem(locationIndex, currentIndex,false);
					
					updateSolarTitle();
					
					mSolarMonthAdatpter.refreshClick(mTempTime);
				}
				/** 暂时办法 */
				int curIndex = index(myApp.getmCurrentClickedTime());
				mSolarMonth.setCurrentItem(curIndex);
				
				LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(
						mSolarMonth.getWidth(), mSolarMonth.getItemHeight(curIndex));
				mSolarMonth.setLayoutParams(lP);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);// 无函数体
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		case android.R.id.home:
			Intent intent_wholeyear = new Intent(this, WholeYearActivity.class);
			startActivityForResult(intent_wholeyear, REQUEST_SELECT_YEAR);
			break;
		
		case R.id.switch_select_day:
			//Intent wheelView = new Intent(this,MainActivity.class);
			Intent wheelView = new Intent(this,DatePickerActivity.class);
			mTempTime = myApp.getmCurrentClickedTime();
			int[] curDate = new int[]{mTempTime.year, mTempTime.month, mTempTime.monthDay};
			wheelView.putExtra("currentDay", curDate);
			startActivityForResult(wheelView, REQUEST_SELECTED_DATE);
			break;
			
		case R.id.city_selector:
			Intent city_Intent = new Intent(this, CityPickerActivity.class);
			startActivity(city_Intent);
			
			
			
			break;
//		case R.id.switch_change_view:
//			if (switchFlag == true) {
//				MonthActivity.this.setContentView(layout_Lunar);
//				if (lunarFlag == true) {
//					init_Lunar();
//					lunarFlag = false;
//				}
//				switchFlag = false;
//				switchState.setTitle("Solar");
//			} else {
//				MonthActivity.this.setContentView(layout_Solar);
//				int curIndex = index(myApp.getmCurrentClickedTime());
//				mSolarMonth.setCurrentItem(curIndex);
//				
//				LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(
//						mSolarMonth.getWidth(), mSolarMonth.getItemHeight(curIndex));
//				mSolarMonth.setLayoutParams(lP);
//				
//				updateSolarTitle();
//				switchFlag = true;
//				switchState.setTitle("Lunar");
//				
//			}
//			break;
		case R.id.delete_database:
			// 确实能删掉
			//calendarDBService.deleteDatebase(activity, "calendar.database");
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_MENU) {
			//menu.performIdentifierAction(R.id.menu_more, 0);
		}
		return super.onKeyUp(keyCode, event);
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		
		// 键盘移动操作
		//calendarView.onKeyDown(keyCode, event);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.solarmonth_today:

			Time mTime = new Time(MyFixed.TIMEZONE);
			mTime.setToNow();
			mTime.normalize(true);
			mTempTime.set(myApp.getmCurrentClickedTime());
			if (mTime.year != mTempTime.year || mTime.month != mTempTime.month || mTime.monthDay != mTempTime.monthDay) {
				
				myApp.setmCurrentClickedTime(mTime);
				//mSolarMonthAdatpter.refreshClick(mTime); // 移动过后刷新，可以用handler or runnable 2015 9 2
				//mSolarMonthAdatpter.notifyClickedDayChanged();
			}
			
			int locationIndex = (mTime.year - MyFixed.MIN_YEAR) * 12 + mTime.month;
			int currentIndex = mSolarMonth.getCurrentItem();
			int dir = locationIndex - currentIndex;
			if (dir == 0) {
				/*
				 * 显示月份就是当前月份，如果点击的日期不是今天，把点击日期设为今天，调用重绘，
				 * 如果当前点击日期就是今天，准备做一个动画效果 2015 8 30 
				 */
				mSolarMonth.invalidate();
				mSolarMonthAdatpter.refreshClick(mTime);
			} else if (dir > 0) {// 过去的月份 回到 今天
				if (dir > 4) {
					mSolarMonth.setCurrentItem(locationIndex, currentIndex,false);
					mSolarMonthAdatpter.refreshClick(mTime);
					//refreshEventList();
					//updateSolarTitle();		
				} else {
					int dis = 0;
					for (int i = 0; i < dir; i++) {
						dis += mSolarMonth.getItemHeight(currentIndex + i);
					}
					//mSolarMonth.setCurrentItem(locationIndex, dis, dis * 4);
					//mSolarMonth.setCurrentItem(dis, dis * 2);// 没有即时更新 SolarMonth currentItem
					mSolarMonth.setCurrentItem(locationIndex, currentIndex,dis, dis * 2);
					//this.handler.sendEmptyMessageDelayed(DELAY_SHOW_EVENT, dis*2);
				}
			} else {
				if (dir > -5) {
					int dis = 0;
					for (int i = 0; i < -dir; i++) {
						dis -= mSolarMonth.getItemHeight(locationIndex + i);
					}
					//mSolarMonth.setCurrentItem(locationIndex, dis, -dis * 4);
					//mSolarMonth.setCurrentItem(dis, -dis * 2);
					mSolarMonth.setCurrentItem(locationIndex, currentIndex,dis, -dis * 2);
					
					//Message msg = handler.obtainMessage(DELAY_SHOW_EVENT,mTime);
					//this.handler.sendMessageDelayed(msg, -dis*2);
					//this.handler.sendEmptyMessageDelayed(DELAY_SHOW_EVENT, -dis*2);
				} else {
					mSolarMonth.setCurrentItem(locationIndex, currentIndex,false);
					mSolarMonthAdatpter.refreshClick(mTime);
					//refreshEventList();
					//updateSolarTitle();		
				}

			}
			updateSolarTitle();
			//refreshEventList();// 
			break;
		case R.id.solarmonth_add:
			Intent intent_solaradd = new Intent(this, AddEventActivity.class);
			startActivity(intent_solaradd);
			
			break;
		case R.id.solarmonth_lunar:
			MonthActivity.this.setContentView(layout_Lunar);
			if (lunarFlag == true) {
				init_Lunar();
				lunarFlag = false;
			}
			lunarFragment.goTo(myApp.getmCurrentClickedTime().toMillis(true), false, false, false);
			updateLunarTitle();
			break;
		case R.id.lunarmonth_today:
			lunarFragment.goToThisYear();
			break;
		case R.id.lunarmonth_add:
			Intent intent_lunaradd = new Intent(this, AddEventActivity.class);
			startActivity(intent_lunaradd);
			break;
		case R.id.lunarmonth_solar:
			MonthActivity.this.setContentView(layout_Solar);
			int curIndex = index(myApp.getmCurrentClickedTime());
			mSolarMonth.setCurrentItem(curIndex);
			
			LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(
					mSolarMonth.getWidth(), mSolarMonth.getItemHeight(curIndex));
			mSolarMonth.setLayoutParams(lP);
			
			updateSolarTitle();
			refreshEventList();
			break;
			
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		int data_id = eventList.get(position).id;
		if (data_id != -1) {
			Intent intent = new Intent(this, EventDetailActivity.class);
			intent.putExtra("id", data_id);
			startActivity(intent);
		} else {
			Toast.makeText(this, "click empty view", Toast.LENGTH_SHORT).show();
		}
		
		
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case DELAY_SHOW_EVENT:
				mSolarMonthAdatpter.refreshClick((Time)msg.obj);
				break;
			case UPDATE_WEATHER:
				/** myHome 101060403 */
				int p = Integer.valueOf(myHome.substring(3, 5));
				int c = Integer.valueOf(myHome.substring(5, 7));
				int d = Integer.valueOf(myHome.substring(7, 9));
				
				String home = provinces.get(p-1).getCitys().get(c-1).getDisList().get(d-1).getName();
				Log.d(TAG, "p-->" + p +"--c-->" + c + "--d-->"+ d+"--home-->" + home);
				new WeatherAsyncTask().execute(home);
				
				break;
			default:
				break;
			}
		};
	};

	// 异步类，获取天气数据
	class WeatherAsyncTask extends AsyncTask<String, Void, Weather> {

		@Override
		protected Weather doInBackground(String... params) {
			String url = HttpUtils.getURl(params[0]);

			String jsonStr = HttpUtils.getJsonStr(url);

			Weather weather = HttpUtils.fromJson(jsonStr);

			Result r = weather.getResults().get(0);
			/*
			 * List<Map<String,Object>> list = new
			 * ArrayList<Map<String,Object>>(); list = HttpUtils.toListMap(r);
			 */
			for (int i = 0; i < 3; i++) {
				Weather_data w = r.getWeather_data().get(i);
				// 下载图片资源
				w.setDayPicture(HttpUtils.getImage(w.getDayPictureUrl()));
				w.setNightPicture(HttpUtils.getImage(w.getNightPictureUrl()));
			}
			return weather;
		}

		@Override
		protected void onPostExecute(Weather result) {
			Result res = result.getResults().get(0);
			Weather_data wa = res.getWeather_data().get(0);
			String str = wa.getDate();
			
			//home.setText(res.getCurrentCity()+ ", ");
			homeWeather.setText(str.substring(0, 2)+","+wa.getWeather()+","+str.substring(14, str.length() - 1));
			
			// ivpic11.setImageResource(R.drawable.d00);
			// ivpic12.setImageResource(R.drawable.d01);
//			ivpic11.setImageBitmap(wa.getDayPicture());
//			ivpic12.setImageBitmap(wa.getNightPicture());

		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}