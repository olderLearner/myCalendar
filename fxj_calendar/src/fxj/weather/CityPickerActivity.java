package fxj.weather;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import fxj.calendar.MonthActivity;
import fxj.calendar.R;
import fxj.calendar.R.id;
import fxj.calendar.R.layout;
import fxj.calendar.datepicker.OnWheelScrollListener;
import fxj.calendar.datepicker.WheelView;
import fxj.calendar.datepicker.adapter.NumericWheelAdapter;
import fxj.weather.city.CityAdapter;
import fxj.weather.city.DistrictAdapter;
import fxj.weather.city.Province;
import fxj.weather.city.ProvinceAdapter;
import fxj.weather.util.HttpUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CityPickerActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "CityPickerActivity";
	
	private LayoutInflater inflater = null;
	private WheelView mProvince;
	private WheelView mCity;
	private WheelView mDistrict;
	private int curYear;
	private int curMonth;
	private int curDay;
	private int dis_year;
	private int dis_month;
	private int dis_day;
	
	LinearLayout mLinearLayout;
	View mView = null;
	Button btn_ok, btn_cancel;
	
	/** weather 变量 */
	private List<Province> provinces;
	private int pIndex = 0;
	private String districtNum;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_citypicker);
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		//this.provinces = MonthActivity.provinces;
		mLinearLayout = (LinearLayout) findViewById(R.id.city_picker_container);
		//mLinearLayout.addView(getDatepicker());
		
		
		
		init();// 加载内容确实是耗很多时间
		
		//mLinearLayout.addView(getDatepicker());
		
		
		/** weather 代码 */

		/** 解析城市 */
		
		

		
		
		
		
		//display_Date = (TextView) findViewById(R.id.showdate_datepicker);
		btn_ok = (Button) findViewById(R.id.city_picker_btn_ok);
		btn_ok.setOnClickListener(this);
		btn_cancel = (Button) findViewById(R.id.city_picker_btn_cancel);
		btn_cancel.setOnClickListener(this);
		
		
	}
	
	private void init(){

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				mLinearLayout.addView(getDatepicker());
			}
		};
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					provinces = HttpUtils.getProvinces(CityPickerActivity.this);
					handler.sendEmptyMessage(9);
				} catch (XmlPullParserException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
	}
	
	
	private View getDatepicker() {
		
		mView = inflater.inflate(R.layout.city_picker, null);
		
		mProvince = (WheelView) mView.findViewById(R.id.province_picker);
		ProvinceAdapter mProvinceAdapter = new ProvinceAdapter(this, provinces);
		mProvince.setViewAdapter(mProvinceAdapter);
		mProvince.setCyclic(true);
		mProvince.addScrollingListener(provinceScrollListener);
		mProvince.setCurrentItem(0);
		mProvince.setVisibleItems(5);
		
		
		mCity = (WheelView) mView.findViewById(R.id.city_picker);
		CityAdapter mCityAdapter = new CityAdapter(this, provinces.get(0).getCitys());
		mCity.setViewAdapter(mCityAdapter);
		mCity.setCyclic(true);
		mCity.addScrollingListener(cityScrollListener);
		mCity.setCurrentItem(0);
		mCity.setVisibleItems(5);
		
		
		mDistrict = (WheelView) mView.findViewById(R.id.district_picker);
		DistrictAdapter mDistrictAdapter = new DistrictAdapter(this, provinces.get(0).getCitys().get(0).getDisList());
		mDistrict.setViewAdapter(mDistrictAdapter);
		//initDistrict(curYear, curMonth);
		mDistrict.setCyclic(true);
		mDistrict.addScrollingListener(districtScrollListener);
		mDistrict.setCurrentItem(0);
		mDistrict.setVisibleItems(5);
				
				
		return mView;
	}

	/**
	 * update district wheel adapter
	 * @param year
	 * @param month
	 */
	private void initDistrict(int pIndex, int cIndex) {
		DistrictAdapter mDistrictAdapter = new DistrictAdapter(this, provinces.get(pIndex).getCitys()
				.get(cIndex).getDisList());
		mDistrict.setViewAdapter(mDistrictAdapter);
		mDistrict.setCurrentItem(0);
	}

	/**
	 * 
	 * @param pIndex
	 * @author fengxj fengxj1985@126.com
	 */
	private void initCity(int pIndex) {
		
		CityAdapter mCityAdapter = new CityAdapter(this, provinces.get(pIndex).getCitys());
		mCity.setViewAdapter(mCityAdapter);
		mCity.setCurrentItem(0);
		initDistrict(pIndex, 0);
		
	}
	
	/**
	 * update district items
	 */
	OnWheelScrollListener cityScrollListener = new OnWheelScrollListener() {
		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			
		}
		
		@Override
		public void onScrollingFinished(WheelView wheel) {
			int cIndex = wheel.getCurrentItem();
			int pIndex = mProvince.getCurrentItem();
			initDistrict(pIndex, cIndex);
		}
	};
	
	/**
	 * 
	 */
	OnWheelScrollListener districtScrollListener = new OnWheelScrollListener() {
		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			
		}
		
		@Override
		public void onScrollingFinished(WheelView wheel) {
			
		}
	};
	
	/**
	 * update city and district items
	 */
	OnWheelScrollListener provinceScrollListener = new OnWheelScrollListener() {
		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			
		}
		
		@Override
		public void onScrollingFinished(WheelView wheel) {

			int pIndex = wheel.getCurrentItem();
			initCity(pIndex);
		}
	};
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.city_picker_btn_ok:
			/* datePickerActivity 无法获得 CalendarView 对象，暂时不管
			calendarView.ce.grid.currentYear=dis_year;
			calendarView.ce.grid.currentMonth=dis_month;
			calendarView.ce.grid.currentDay=dis_day;
			*/
//			int[] selectedDate = new int[]{dis_year, dis_month, dis_day};
//			Intent selectedDateIntent = new Intent();
//			selectedDateIntent.putExtra("selectedDate", selectedDate);
//			
//			setResult(Activity.RESULT_OK, selectedDateIntent);
			finish();
			break;
		case R.id.city_picker_btn_cancel:
//			setResult(Activity.RESULT_CANCELED);
			this.finish();
			break;
		}
	}
	
	
	
}
