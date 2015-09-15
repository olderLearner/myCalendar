package fxj.calendar.year;


import fxj.calendar.AddEventActivity;
import fxj.calendar.MyApp;
import fxj.calendar.R;
import fxj.calendar.R.id;
import fxj.calendar.R.layout;
import fxj.calendar.util.MyFixed;
import fxj.calendar.year.YearAdapter.onMonthTapUpListener;
import android.R.color;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WholeYearActivity extends Activity implements OnClickListener ,onMonthTapUpListener{

	private static final String TAG = "WholeYearActivity"; 
	private ActionBar mActionBar;
	private Button today,add,update;
	private YearFragment wholeYearFragment;
	private MyApp myApp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wholeyear);
		myApp = (MyApp) getApplicationContext();
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(false);
		
		today = (Button) findViewById(R.id.whole_today);
		today.setOnClickListener(this);
		add = (Button) findViewById(R.id.whole_add);
		add.setOnClickListener(this);
		update = (Button) findViewById(R.id.whole_update);
		update.setOnClickListener(this);
		
		
		wholeYearFragment = new YearFragment();
		//wholeYearFragment.setMyApp(myApp);
		getFragmentManager().beginTransaction().add(R.id.wholeyear, wholeYearFragment).commit();
		//wholeYearFragment.goTo(myApp.getmCurrentClickedTime().toMillis(true), false, false, false);
		//wholeYearFragment.setmSelectedYear(myApp.getmCurrentClickedTime());
		
		//fullYearFragment.goTo(time, animate, setSelected, forceScroll); 
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.whole_today:
			wholeYearFragment.goToThisYear();// 2015 9 13 直接跳过去以后在优化，点击回到solar界面需要重新考虑高度问题会出现小bug
			break;
		case R.id.whole_add:
			Intent intent_solaradd = new Intent(this, AddEventActivity.class);
			startActivity(intent_solaradd);
			break;

		case R.id.whole_update:

			break;

		}
	}
	
	@Override
	public void onMonthTapUp(Time time) {
		
		//Toast.makeText(this, "主activity ： 点击日期:"+time.year+"-"+(time.month+1), Toast.LENGTH_SHORT).show();
		myApp.setmCurrentClickedTime(time);
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN) {
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		
		
		return super.onKeyDown(keyCode, event);
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
