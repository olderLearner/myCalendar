package fxj.calendar;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fxj.calendar.lunar.LunarMonthActivity;
import fxj.calendar.old.About;
import fxj.calendar.solar.OnMonthScrollListener;
import fxj.calendar.solar.SolarView;
import fxj.calendar.solar.SolarMonth;
import fxj.calendar.solar.SolarMonthAdapter;







import fxj.calendar.year.WholeYearActivity;
import android.R.color;
import android.R.integer;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Toast;

public class Main extends Activity implements android.view.View.OnClickListener
{
	private static final String TAG = "Main";
	
	private static final int REQUEST_SELECTED_DATE = 29;
	private static String[] date = new String[]{"����ɯɯ", "٤��ĩ����¯", "����", "ɳ����ӡ"
		, "���", "�����", "Ѫɫ����"};
	
	private ListView mListView;
	private SolarView mMonthViewS;
	private LinearLayout mWheelMonthContainer;
	private TextView tv;
	Calendar calendar;
	Time time;
	private LayoutInflater inflater;
	private View mView= null;
	private View mActionBarView = null;
	private SolarMonth mWheelMonth;
	private SolarMonthAdapter mWheelMonthAdatpter;
	private String mTimeZone= Time.getCurrentTimezone();
	
	private Button wheelMonth_btn_Today,wheelMonth_btn_Add,wheelMonth_btn_Lunar;
	
	
	
	
	
	
	private CalendarView calendarView;	
	public static Activity activity;
	
	private AlertDialog.Builder builder;
	private AlertDialog adMyDate;
	
	
	public static Remind remindQueue;
	public List<Remind> remindList = new ArrayList<Remind>();
	public AlarmManager am;
	public static MediaPlayer mediaPlayer;
	public static Vibrator vibrator;

	private ActionBar actionBar;
	private Menu menu;
	
	private DatePicker dpSelectDate;
	private TextView tvDate;
	private TextView tvLunarDate;
	private LinearLayout  myDateLayout;
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		
		super.onCreate(savedInstanceState);
		if (activity == null)
		{
			activity = this;
		}
		if (remindQueue == null)
		{
			remindQueue = new Remind();
		}
		if (am == null)
		{
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
		}
		
		Window window = getWindow();
		
		
		
		setContentView(R.layout.activity_solarmonth);
		mWheelMonthContainer = (LinearLayout) findViewById(R.id.solarmonth_container);
		
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		mView = inflater.inflate(R.layout.wheelmonth, null);
		mActionBarView = inflater.inflate(R.layout.weekname_layout, null);
		
		wheelMonth_btn_Today = (Button) findViewById(R.id.solarmonth_today);
		wheelMonth_btn_Today.setOnClickListener(this);
		wheelMonth_btn_Add = (Button) findViewById(R.id.solarmonth_add);
		wheelMonth_btn_Add.setOnClickListener(this);
		wheelMonth_btn_Lunar = (Button) findViewById(R.id.solarmonth_lunar);
		wheelMonth_btn_Lunar.setOnClickListener(this);
		
		
		
		mListView = (ListView) findViewById(R.id.record_listview);
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, date);
		mListView.setAdapter(mArrayAdapter);
		
		mWheelMonth = (SolarMonth) mView.findViewById(R.id.wheelmonthview);
		mWheelMonthAdatpter = new SolarMonthAdapter(this, mWheelMonth);
		mWheelMonth.setViewAdapter(mWheelMonthAdatpter);
		mWheelMonth.addScrollingListener(scroll_Listener);
		mWheelMonth.setCyclic(false);
		mWheelMonth.setVisibleItems(1);
		mWheelMonth.setCurrentItem((2015-1949)*12 + 7);
		mWheelMonth.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator));
		
		mWheelMonthContainer.addView(mView);
		
		
		calendarView = new CalendarView(this);
		
		
		
		
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		customActionbarHome(actionBar);
		
		/*
		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT, 30,Gravity.BOTTOM|Gravity.FILL_HORIZONTAL);
		actionBar.setCustomView(mActionBarView, mLayoutParams);
		actionBar.setDisplayShowCustomEnabled(true);*/
		//actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
		
		actionBar.show();
		
		
		
		
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
		}
	}
	
	OnMonthScrollListener scroll_Listener = new OnMonthScrollListener() {
		
		@Override
		public void onScrollingStarted(SolarMonth wheel) {
			
		}
		
		@Override
		public void onScrollingFinished(SolarMonth wheel) {
			
			int index = mWheelMonth.getCurrentItem();
			Main.this.setTitle((1949+index/12)+"��"+(index%12+1)+"��");
			
		}
	};
	
	
	public static void customActionbarHome(ActionBar bar) {
		try {
			
			
			/*
			 * ���������
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
			
			/*
			 * ���Ҳ������
			 */
			((FrameLayout) objHomeView).setBackgroundColor(Color.BLUE);
			((LinearLayout) objTitleLayout).setBackgroundColor(Color.TRANSPARENT);
			
			/* ûɶ����
			((LinearLayout) objTitleLayout).setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(200, 10);
			((LinearLayout) objTitleLayout).setLayoutParams(ll);
			*/
			
			Class<?> clazz = Class.forName("com.android.internal.widget.ActionBarView.HomeView");  
            Method m = clazz.getDeclaredMethod("setUpIndicator", Drawable.class);  
            m.setAccessible(true);  
              
            //MenuBuilderʵ��Menu�ӿڣ������˵�ʱ����������menu��ʵ����MenuBuilder����(java�Ķ�̬����)  
            m.invoke(bar, R.drawable.a03);
			
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
		Log.d(TAG, "onRestart()-->");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()-->");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()-->");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()-->");
	}



	class MenuItemClickParent
	{
		protected Activity activity;

		public MenuItemClickParent(Activity activity)
		{
			this.activity = activity;
		}
	}

	/**
	 * ��¼���ѣ��˵���ť
	 * @author Administrator
	 *
	 */
	class OnRecordRemindMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		public OnRecordRemindMenuItemClick(Activity activity)
		{
			super(activity);

		}

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			Intent intent = new Intent(activity, AllRecord.class);
			intent.putExtra("year", calendarView.ce.grid.currentYear);
			intent.putExtra("month", calendarView.ce.grid.currentMonth);
			intent.putExtra("day", calendarView.ce.grid.currentDay1);
			activity.startActivity(intent);
			return true;
		}

	}

	/**
	 * ���죬�˵���ť
	 * @author Administrator
	 *
	 */
	class OnTodayMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		public OnTodayMenuItemClick(Activity activity)
		{
			super(activity);

		}

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			Calendar calendar = Calendar.getInstance();
			
			// ��õ�ǰ�� �� �� �� ��������
			calendarView.ce.grid.currentYear = calendar.get(Calendar.YEAR);
			calendarView.ce.grid.currentMonth = calendar.get(Calendar.MONTH);
			calendarView.ce.grid.currentDay = calendar.get(Calendar.DATE);
			calendarView.invalidate();

			return true;
		}

	}

	/**
	 * ѡ�����ڣ��˵���ť
	 * @author Administrator
	 *
	 */
	class OnMyDateMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener, OnClickListener, OnDateChangedListener
	{
		private DatePicker dpSelectDate;
		private LinearLayout myDateLayout;
		private TextView tvDate;
		private TextView tvLunarDate;

		public OnMyDateMenuItemClick(Activity activity)
		{
			super(activity);
			myDateLayout = (LinearLayout) getLayoutInflater().inflate(
					R.layout.mydate, null);
			dpSelectDate = (DatePicker) myDateLayout
					.findViewById(R.id.dpSelectDate);

		}

		@Override
		/**
		 * dataPicker ÿһ�θı䶼���ô˷��������µڶ���textview
		 * 
		 */
		public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{

			// ��ʾ��ʽ
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��");
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			calendar.set(year, monthOfYear, dayOfMonth);
			if (tvDate != null)
				tvDate.setText(sdf.format(calendar.getTime()));
			else
				adMyDate.setTitle(sdf.format(calendar.getTime()));

			Calendar calendar1 = Calendar.getInstance();// ���ر���ʵ���ڵ�calendarʵ��
			// ���datepickerѡ������� ����ʵ������ͬ����tvdate������� �����족
			if (calendar1.get(Calendar.YEAR) == year
					&& calendar1.get(Calendar.MONTH) == monthOfYear
					&& calendar1.get(Calendar.DATE) == dayOfMonth)
			{
				if (tvDate != null)
					tvDate.setText(tvDate.getText() + "(����)");
				else
					adMyDate.setTitle(sdf.format(calendar.getTime()) + "(����)");
			}

			if (tvLunarDate == null)
				return;
			
		}

		@Override
		/**
		 * ȷ����ť�ļ�����,����Ϊdatepicker�������ֵ��
		 *  currentYear currentMonth currentDay����Ϊdate picker �������ֵ����ֵ���ݵ�grid���У�
		 *  ����view���·���
		 */
		public void onClick(DialogInterface dialog, int which)
		{
			calendarView.ce.grid.currentYear = dpSelectDate.getYear();
			calendarView.ce.grid.currentMonth = dpSelectDate.getMonth();
			calendarView.ce.grid.currentDay = dpSelectDate.getDayOfMonth();
			calendarView.invalidate();

		}

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			// Create a builder
			builder = new AlertDialog.Builder(activity);
			builder.setTitle("ָ������");
			
			// alertdialog �����ļ�
			myDateLayout = (LinearLayout) getLayoutInflater().inflate(
					R.layout.mydate, null);
			// datepicker textview textview ���
			dpSelectDate = (DatePicker) myDateLayout
					.findViewById(R.id.dpSelectDate);
			// ���� 
			tvDate = (TextView) myDateLayout.findViewById(R.id.tvDate);
			// ���� û����
			tvLunarDate = (TextView) myDateLayout
					.findViewById(R.id.tvLunarDate);

			// ��ʼ��datapicker int year int month int day ondatechangedlistener
			dpSelectDate.init(calendarView.ce.grid.currentYear,
					calendarView.ce.grid.currentMonth,
					calendarView.ce.grid.currentDay, this);

			builder.setView(myDateLayout);

			builder.setPositiveButton("ȷ��", this);
			builder.setNegativeButton("ȡ��", null);
			builder.setIcon(R.drawable.calendar_small);
			// AlertDialog: adMyDate
			adMyDate = builder.create();
			// ����onDateChanged ��ʼ��ʾtext view
			onDateChanged(dpSelectDate, dpSelectDate.getYear(), dpSelectDate
					.getMonth(), dpSelectDate.getDayOfMonth());
			adMyDate.show();

			return true;
		}
	}

	/**
	 * ���ڣ��˵���ť��
	 * ����Ҫ�������ݵ�About activity��Ҳ����ش�����
	 * @author Administrator
	 *
	 */
	class OnAboutMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		public OnAboutMenuItemClick(Activity activity)
		{
			super(activity);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			Intent intent = new Intent(activity, About.class);
			activity.startActivity(intent);
			return true;
		}

	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		setIconEnable(menu, true);
		MenuItem mSearch = (MenuItem) menu.findItem(R.id.action_search);
		mSearch.setIcon(R.drawable.red_search);
		
		
		
		return super.onCreateOptionsMenu(menu);
		
		/*MenuItem miToday = menu.add(0, 1, 1, "����");
		MenuItem miMyDate = menu.add(0, 2, 2, "ָ������");
		MenuItem miRecordRemind = menu.add(0, 3, 3, "��¼/����");				
		MenuItem miAbout = menu.add(0, 18, 18, "����");
		
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
              
            //MenuBuilderʵ��Menu�ӿڣ������˵�ʱ����������menu��ʵ����MenuBuilder����(java�Ķ�̬����)  
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
		}
		super.onActivityResult(requestCode, resultCode, data);// �޺�����
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		case android.R.id.home:
			Intent intent_wholeyear = new Intent(this, WholeYearActivity.class);
			startActivity(intent_wholeyear);
			break;
		
		case R.id.select_day:
			//Intent wheelView = new Intent(this,MainActivity.class);
			Intent wheelView = new Intent(this,DatePickerActivity.class);
			int[] curDate = new int[]{calendarView.ce.grid.currentYear,
					calendarView.ce.grid.currentMonth, calendarView.ce.grid.currentDay};
			wheelView.putExtra("currentDay", curDate);
			startActivityForResult(wheelView, REQUEST_SELECTED_DATE);
			break;
		case R.id.change_view:
			//Intent wheelView = new Intent(this,MainActivity.class);
			Intent changIntent = new Intent(this,MonthActivity.class);
			startActivity(changIntent);
			break;
		
		/* 2015 8 3 ע��
		case R.id.select_day:			
			
			 * ������
			private DatePicker dpSelectDate;
			private LinearLayout myDateLayout;
			private TextView tvDate;
			private TextView tvLunarDate;
			
			 
			 * final�ı������ܹ��ı�
			final DatePicker dpSelectDate;
			final LinearLayout  myDateLayout;
			final TextView tvDate;
			final TextView tvLunarDate;
			

			myDateLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.mydate, null);
			dpSelectDate = (DatePicker) myDateLayout.findViewById(R.id.dpSelectDate);
			tvDate = (TextView) myDateLayout.findViewById(R.id.tvDate);
			tvLunarDate = (TextView) myDateLayout.findViewById(R.id.tvLunarDate);
			
			// ��ʼ��DatePicker
			dpSelectDate.init(calendarView.ce.grid.currentYear, 
					calendarView.ce.grid.currentMonth, 
					calendarView.ce.grid.currentDay, new OnDateChangedListener() {
						
						@Override
						public void onDateChanged(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {

							// ��ʾ��ʽ
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��");
							java.util.Calendar calendar = java.util.Calendar.getInstance();
							calendar.set(year, monthOfYear, dayOfMonth);
							if (tvDate != null)
								tvDate.setText(sdf.format(calendar.getTime()));
							else
								adMyDate.setTitle(sdf.format(calendar.getTime()));

							Calendar calendar1 = Calendar.getInstance();// ���ر���ʵ���ڵ�calendarʵ��
							// ���datepickerѡ������� ����ʵ������ͬ����tvdate������� �����족
							if (calendar1.get(Calendar.YEAR) == year
									&& calendar1.get(Calendar.MONTH) == monthOfYear
									&& calendar1.get(Calendar.DATE) == dayOfMonth)
							{
								if (tvDate != null)
									tvDate.setText(tvDate.getText() + "(����)");
								else
									adMyDate.setTitle(sdf.format(calendar.getTime()) + "(����)");
							}

							if (tvLunarDate == null)
								return;
						}
					});
			// ��ʼ����ʾ tvDate
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��");
			java.util.Calendar calendar1 = java.util.Calendar.getInstance();
			calendar1.set(dpSelectDate.getYear(), dpSelectDate.getMonth(), dpSelectDate.getDayOfMonth());
			if (tvDate != null) tvDate.setText(sdf.format(calendar1.getTime()));
			
			new AlertDialog.Builder(this)
					.setTitle("ѡ������")
					.setView(myDateLayout)
					.setIcon(R.drawable.calendar_small)
					.setNegativeButton("ȡ��", null)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

							calendarView.ce.grid.currentYear = dpSelectDate.getYear();
							calendarView.ce.grid.currentMonth = dpSelectDate.getMonth();
							calendarView.ce.grid.currentDay = dpSelectDate.getDayOfMonth();
							calendarView.invalidate();
							if (myDateLayout != null) myDateLayout = null;
							if (dpSelectDate != null) dpSelectDate = null;
							if (tvDate != null) tvDate = null;
							if (tvLunarDate != null) tvLunarDate = null;
						}
					})
					.show();
			break;
			*/
			
		case R.id.record_remind:
			Intent intent = new Intent(activity, AllRecord.class);
			intent.putExtra("year", calendarView.ce.grid.currentYear);
			intent.putExtra("month", calendarView.ce.grid.currentMonth);
			intent.putExtra("day", calendarView.ce.grid.currentDay1);
			startActivity(intent);
			
			break;
		case R.id.about:
			Intent intent_about = new Intent(activity, About.class);
			startActivity(intent_about);
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
		
		// �����ƶ�����
		calendarView.onKeyDown(keyCode, event);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.solarmonth_today:

			
			Time mTime = new Time(mTimeZone);
			mTime.setToNow();
			mTime.normalize(true);
			int locationIndex = (mTime.year -1949)*12 + mTime.month;
			int currentIndex = mWheelMonth.getCurrentItem();
			int dir = locationIndex - currentIndex;
			if (dir == 0) {
				/*
				 * ���������Ϊ����
				 */
			} else if (dir>0) {// ��ȥ���·� �ص� ����  
				if (dir>5) {
					mWheelMonth.setCurrentItem(locationIndex,currentIndex,false);
				} else {
					int dis = 0;
					for(int i=0;i<dir;i++) {
						dis += mWheelMonth.getItemHeight(currentIndex+i);
					}										
					//mWheelMonth.setCurrentItem(dis,dir*500);
					mWheelMonthAdatpter.setmRealClickedDay(mTime);
				}
			}
			
			
			
			
			mWheelMonthAdatpter.setmRealClickedDay(mTime);
			Toast.makeText(Main.this, "click today", 0).show();
			
			/*
			// ��õ�ǰ�� �� �� �� ��������
			calendarView.ce.grid.currentYear = calendar.get(Calendar.YEAR);
			calendarView.ce.grid.currentMonth = calendar.get(Calendar.MONTH);
			calendarView.ce.grid.currentDay = calendar.get(Calendar.DATE);
			calendarView.invalidate();
			*/
			break;
		case R.id.solarmonth_add:

			break;
		case R.id.solarmonth_lunar:
			Intent intent_lunarmonth = new Intent(this, LunarMonthActivity.class);
			startActivity(intent_lunarmonth);
			break;
			
		}
	}

	

}