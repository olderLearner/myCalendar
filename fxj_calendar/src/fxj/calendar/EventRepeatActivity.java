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

public class EventRepeatActivity extends Activity implements OnClickListener {
	
	private ActionBar mActionBar;
	private LayoutInflater inflater;
	private View repeatTitle;
	private Button reNever,reDay,reWeek,reTwoWeek,reMonth,reYear;
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_repeat);
		this.setTitle("Ìí¼ÓÊÂ¼þ");
		inflater = getLayoutInflater();
		repeatTitle = inflater.inflate(R.layout.event_repeat_title, null);
		
		Intent intent = new Intent();
		intent = getIntent();
		if (intent != null) {
			index  = intent.getIntExtra("repeatIndex", 0);
		}
		
		initActionBar();
		initButton(index);
		
	}

	
	private void initButton(int index) {
		
		reNever = (Button) findViewById(R.id.repeat_never);
		reNever.setOnClickListener(this);				
		//reNever.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
		
		reDay = (Button) findViewById(R.id.repeat_everyday);
		reDay.setOnClickListener(this);
		//reDay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
		
		reWeek = (Button) findViewById(R.id.repeat_everyweek);
		reWeek.setOnClickListener(this);
		
		reTwoWeek = (Button) findViewById(R.id.repeat_everytwoweek);
		reTwoWeek.setOnClickListener(this);
		
		reMonth = (Button) findViewById(R.id.repeat_everymonth);
		reMonth.setOnClickListener(this);
		
		reYear = (Button) findViewById(R.id.repeat_everyyear);
		reYear.setOnClickListener(this);
		
		switch (index) {
		case 0:
			reNever.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 1:
			reDay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 2:
			reWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 3:
			reTwoWeek.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 4:
			//reMonth.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			reMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			//reMonth.setCompoundDrawablePadding(100);
			break;
		case 5:
			reYear.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
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
		case R.id.repeat_never:
			Intent intentNever = new Intent();
			intentNever.putExtra("repeatSet", 0);
			setResult(Activity.RESULT_OK, intentNever);
			finish();
			break;
		case R.id.repeat_everyday:
			Intent intentEveryday = new Intent();
			intentEveryday.putExtra("repeatSet", 1);
			setResult(Activity.RESULT_OK, intentEveryday);
			finish();
			break;
		case R.id.repeat_everyweek:
			Intent intentEveryweek = new Intent();
			intentEveryweek.putExtra("repeatSet", 2);
			setResult(Activity.RESULT_OK, intentEveryweek);
			finish();
			break;
		case R.id.repeat_everytwoweek:
			Intent intentEverytwoweek = new Intent();
			intentEverytwoweek.putExtra("repeatSet", 3);
			setResult(Activity.RESULT_OK, intentEverytwoweek);
			finish();
			break;
		case R.id.repeat_everymonth:
			Intent intentEverymonth = new Intent();
			intentEverymonth.putExtra("repeatSet", 4);
			setResult(Activity.RESULT_OK, intentEverymonth);
			finish();
			break;
		case R.id.repeat_everyyear:
			Intent intentEveryyear = new Intent();
			intentEveryyear.putExtra("repeatSet", 5);
			setResult(Activity.RESULT_OK, intentEveryyear);
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
