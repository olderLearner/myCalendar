package fxj.calendar.lunar;


import fxj.calendar.R;
import android.R.color;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LunarMonthActivity extends Activity {

	private static final String TAG = "LunarMonthActivity"; 
	
	private ActionBar mActionBar;
	//private MyApp myApp;
	private TextView tv;

	private LayoutInflater inflater;

	private View mActionBarView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_lunarmonth);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		
		mActionBarView = inflater.inflate(R.layout.weekname_layout, null);		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT, 30,Gravity.BOTTOM|Gravity.FILL_HORIZONTAL);
		mActionBar.setCustomView(mActionBarView, mLayoutParams);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.show();
		
		//myApp = (MyApp) getApplicationContext();		
		//tv.setText(myApp.getmCurrentClickedTime().format2445());
		
		LunarFragment MonthFragment = new LunarFragment();		
		getFragmentManager().beginTransaction()
				.add(R.id.lunarmonth_container, MonthFragment).commit();
		//fullYearFragment.goTo(time, animate, setSelected, forceScroll); 		
		//MonthFragment.setMyApp(myApp);
	}

	
	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	

}
