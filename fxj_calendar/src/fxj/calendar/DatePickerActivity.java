package fxj.calendar;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import fxj.calendar.datepicker.OnWheelScrollListener;
import fxj.calendar.datepicker.WheelView;
import fxj.calendar.datepicker.adapter.NumericWheelAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DatePickerActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "DatePickerActivity";
	public static final int MAX_YEAR_DATEPICKER = 2049;
	public static final int MIN_YEAR_DATEPICKER = 1949;
	
	private LayoutInflater inflater = null;
	private WheelView mYear;
	private WheelView mMonth;
	private WheelView mDay;
	private int curYear;
	private int curMonth;
	private int curDay;
	private int dis_year;
	private int dis_month;
	private int dis_day;
	
	LinearLayout mLinearLayout;
	View mView = null;
	TextView display_Date;
	Button btn_ok, btn_cancel;
	java.util.Calendar calendar = java.util.Calendar.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datepicker);
		
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		if (this.getIntent() != null){
			int[] initDate = this.getIntent().getIntArrayExtra("currentDay");
			curYear = initDate[0];
			curMonth = initDate[1];
			curDay = initDate[2];
		} else {
			curYear = calendar.get(java.util.Calendar.YEAR);
			curMonth = calendar.get(java.util.Calendar.MONTH);
			curDay = calendar.get(java.util.Calendar.DATE);
		}
				
		mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_datepicker_wheelview);
		mLinearLayout.addView(getDatepicker());
		
		
		
		
		//display_Date = (TextView) findViewById(R.id.showdate_datepicker);
		btn_ok = (Button) findViewById(R.id.datepicker_btn_ok);
		btn_ok.setOnClickListener(this);
		btn_cancel = (Button) findViewById(R.id.datepicker_btn_cancel);
		btn_cancel.setOnClickListener(this);
		
		
		
		
	}

	private View getDatepicker() {
		
		/*
		curYear = calendar.get(java.util.Calendar.YEAR);
		curMonth = calendar.get(java.util.Calendar.MONTH);
		curDay = calendar.get(java.util.Calendar.DATE);
		*/
		
		mView = inflater.inflate(R.layout.wheel_datepicker, null);
		
		/*
		 * 2015 8 3 null pointer exception 
		 * mYear = (WheelView) findViewById(R.id.year_datepicker);
		 * 
		 * ������ mYear.setViewAdapter(mYearAdapter);��ΪmYear null������setViewAdapter�������� ��ָ���쳣
		 * 
		 * ԭ��findViewById(R.id.year_datepicker); ���ڸ���ͼ��Ѱ�� ����R.layout.activity_datepicker
		 * activity����ͼϵͳ�в�����WheelView��
		 * WheelView��R.layout.wheel_datepicker�����У�mView������������֣�����Ӧ����mView�л�ȡWheelView
		 */	
		//mYear = (WheelView) findViewById(R.id.year_datepicker); //error ���
		mYear = (WheelView) mView.findViewById(R.id.year_datepicker);
		NumericWheelAdapter mYearAdapter = new NumericWheelAdapter(this, 1949, 2049);
		mYearAdapter.setLabel("��");
		mYear.setViewAdapter(mYearAdapter);
		mYear.setCyclic(true);
		mYear.addScrollingListener(scrollListener);
		
		mMonth = (WheelView) mView.findViewById(R.id.month_datepicker);
		NumericWheelAdapter mMonthAdapter = new NumericWheelAdapter(this, 1, 12, "%02d");
		mMonthAdapter.setLabel("��");
		mMonth.setViewAdapter(mMonthAdapter);
		mMonth.setCyclic(true);
		mMonth.addScrollingListener(scrollListener);
		
		mDay = (WheelView) mView.findViewById(R.id.day_datepicker);
		initDay(curYear, curMonth);
		mDay.setCyclic(true);
		mDay.addScrollingListener(scrollListener);
				
		
		mYear.setCurrentItem(curYear - 1949);
		mYear.setVisibleItems(5);
		mMonth.setCurrentItem(curMonth);
		mMonth.setVisibleItems(5);
		mDay.setCurrentItem(curDay - 1);
		mDay.setVisibleItems(5);
				
		return mView;
	}

	/**
	 * ��ʼ�����ڹ��ֵ�������
	 * @param year
	 * @param month
	 */
	private void initDay(int year, int month) {
		
		NumericWheelAdapter mDayAdapter = new NumericWheelAdapter(this, 1, getDay(year, month), "%02d");
		mDayAdapter.setLabel("��");
		mDay.setViewAdapter(mDayAdapter);
	}

	/**
	 * ����ָ���������ж�����
	 * @param y year
	 * @param m month 
	 * @return int days
	 */
	private int getDay (int y, int m) {
		
		m++;
		switch (m) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return 31;
		case 2:
		{
			if ((y % 4 == 0) && (y % 100 == 0) || (y % 400 == 0))
				return 29;
			else
				return 28;
		}
		case 4:
		case 6:
		case 9:
		case 11:
			return 30;
		}
		return 0; //error result
	}
	
	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			
		}
		
		@Override
		/**
		 * ����ֹͣ����ȡ������ʾ�����֣�������ʾ�����������趨�յ�������
		 */
		public void onScrollingFinished(WheelView wheel) {

			dis_year = mYear.getCurrentItem() + 1949;
			dis_month = mMonth.getCurrentItem();
			Log.d(TAG, "dis_month-->"+ dis_month);
			initDay(dis_year, dis_month);// ��������������
			dis_day = mDay.getCurrentItem() + 1;
			
			// ����ǰ������ʾ��textView��
			
			/* ��ʽ��������  ԭ��֪�� 2015 8 3
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy��m��d��");
			calendar.set(dis_year, dis_month, dis_day);
			Log.d(TAG, "calendar-->"+ calendar.getTime());
			String date = sdf.format(calendar.getTime());
			Log.d(TAG, "msg-->"+ date);
			*/
			String date1 = new StringBuilder()
								.append(dis_year+"��")
								.append((dis_month+1)+"��")
								.append(dis_day+"��").toString();
			//Log.d(TAG, "msg-->"+ date1);
			//display_Date.setText(date1);
			setTitle(date1);
		}
	};
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.datepicker_btn_ok:
			/* datePickerActivity �޷���� CalendarView ������ʱ����
			calendarView.ce.grid.currentYear=dis_year;
			calendarView.ce.grid.currentMonth=dis_month;
			calendarView.ce.grid.currentDay=dis_day;
			*/
			int[] selectedDate = new int[]{dis_year, dis_month, dis_day};
			Intent selectedDateIntent = new Intent();
			selectedDateIntent.putExtra("selectedDate", selectedDate);
			
			setResult(Activity.RESULT_OK, selectedDateIntent);
			finish();
			break;
		case R.id.datepicker_btn_cancel:
			setResult(Activity.RESULT_CANCELED);
			this.finish();
			break;
		}
	}
	
	
	
}
