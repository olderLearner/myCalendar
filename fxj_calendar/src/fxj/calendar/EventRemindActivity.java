package fxj.calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EventRemindActivity extends Activity implements OnClickListener {
	
	public static final String REMIND_SET = "remindSet";
	
	private ActionBar mActionBar;
	private LayoutInflater inflater;
	private View repeatTitle;
	private Button remNone,remAtBegin,rem5min,rem15min,rem30min,rem1hour,rem2hour,rem1day,rem2day,rem1week;
	//private int[] index = new int[2];
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_remind);
		this.setTitle("Ìí¼ÓÊÂ¼þ");
		inflater = getLayoutInflater();
		repeatTitle = inflater.inflate(R.layout.event_remind_title, null);
		
		Intent intent = new Intent();
		intent = getIntent();
		if (intent != null) {
			index  = intent.getIntExtra("remindIndex", 0);
		}
		
		initActionBar();
		initButton(index);
		
	}

	
	private void initButton(int index) {
		
		remNone = (Button) findViewById(R.id.remind_none);
		remNone.setOnClickListener(this);				
		
		remAtBegin = (Button) findViewById(R.id.remind_atbegin);
		remAtBegin.setOnClickListener(this);
		
		rem5min = (Button) findViewById(R.id.remind_5min);
		rem5min.setOnClickListener(this);
		
		rem15min = (Button) findViewById(R.id.remind_15min);
		rem15min.setOnClickListener(this);
		
		rem30min = (Button) findViewById(R.id.remind_30min);
		rem30min.setOnClickListener(this);
		
		rem1hour = (Button) findViewById(R.id.remind_1hour);
		rem1hour.setOnClickListener(this);
		
		rem2hour = (Button) findViewById(R.id.remind_2hour);
		rem2hour.setOnClickListener(this);
		
		rem1day = (Button) findViewById(R.id.remind_1day);
		rem1day.setOnClickListener(this);
		
		rem2day = (Button) findViewById(R.id.remind_2day);
		rem2day.setOnClickListener(this);
		
		rem1week = (Button) findViewById(R.id.remind_1week);
		rem1week.setOnClickListener(this);
		
		
		
		switch (index) {
		case 0:
			remNone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 1:
			remAtBegin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 2:
			rem5min.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 3:
			rem15min.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 4:
			//reMonth.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			rem30min.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			//reMonth.setCompoundDrawablePadding(100);
			break;
		case 5:
			rem1hour.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 6:
			rem2hour.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 7:
			rem1day.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 8:
			rem2day.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 9:
			rem1week.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;

		}
	}


	private void initActionBar() {
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
				Gravity.CENTER | Gravity.FILL_VERTICAL);
		mActionBar.setCustomView(repeatTitle, mLayoutParams);
		mActionBar.setDisplayShowCustomEnabled(true);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.remind_none:
			Intent intentNone = new Intent();
			intentNone.putExtra(REMIND_SET, 0);
			setResult(Activity.RESULT_OK, intentNone);
			finish();
			break;
		case R.id.remind_atbegin:
			Intent intentAtbegin = new Intent();
			intentAtbegin.putExtra(REMIND_SET, 1);
			setResult(Activity.RESULT_OK, intentAtbegin);
			finish();
			break;
		case R.id.remind_5min:
			Intent intent5min = new Intent();
			intent5min.putExtra(REMIND_SET, 2);
			setResult(Activity.RESULT_OK, intent5min);
			finish();
			break;
		case R.id.remind_15min:
			Intent intent15min = new Intent();
			intent15min.putExtra(REMIND_SET, 3);
			setResult(Activity.RESULT_OK, intent15min);
			finish();
			break;
		case R.id.remind_30min:
			Intent intent30min = new Intent();
			intent30min.putExtra(REMIND_SET, 4);
			setResult(Activity.RESULT_OK, intent30min);
			finish();
			break;
		case R.id.remind_1hour:
			Intent intent1hour = new Intent();
			intent1hour.putExtra(REMIND_SET, 5);
			setResult(Activity.RESULT_OK, intent1hour);
			finish();
			break;
		case R.id.remind_2hour:
			Intent intent2hour = new Intent();
			intent2hour.putExtra(REMIND_SET, 6);
			setResult(Activity.RESULT_OK, intent2hour);
			finish();
			break;
		case R.id.remind_1day:
			Intent intent1day = new Intent();
			intent1day.putExtra(REMIND_SET, 7);
			setResult(Activity.RESULT_OK, intent1day);
			finish();
			break;
		case R.id.remind_2day:
			Intent intent2day = new Intent();
			intent2day.putExtra(REMIND_SET, 8);
			setResult(Activity.RESULT_OK, intent2day);
			finish();
			break;
		case R.id.remind_1week:
			Intent intent1week = new Intent();
			intent1week.putExtra(REMIND_SET, 9);
			setResult(Activity.RESULT_OK, intent1week);
			finish();
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			
			/* api Ì«µÍ fuck
			Intent intentUp = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, intentUp)) {
				TaskStackBuilder.create(this)
								.addNextIntentWithParentStack(intentUp)
								.startActivities();
			} else {
				intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				NavUtils.navigateUpTo(this, intentUp);
			}*/
			
			setResult(Activity.RESULT_CANCELED);
			finish();
			//Toast.makeText(this, "do nothing return", Toast.LENGTH_SHORT).show();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

}
