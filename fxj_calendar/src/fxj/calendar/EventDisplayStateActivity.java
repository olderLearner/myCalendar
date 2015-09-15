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

public class EventDisplayStateActivity extends Activity implements OnClickListener {
	
	public static final String  DISPLAY_STATE = "displayStateSet";
	
	private ActionBar mActionBar;
	private LayoutInflater inflater;
	private View abTitle;
	private Button disBusy,disIdle;
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_display_state);
		this.setTitle("Ìí¼ÓÊÂ¼þ");
		inflater = getLayoutInflater();
		abTitle = inflater.inflate(R.layout.event_displaystate_title, null);
		
		Intent intent = new Intent();
		intent = getIntent();
		if (intent != null) {
			index  = intent.getIntExtra("displayIndex", 0);
		}
		
		initActionBar();
		initButton(index);
		
	}

	
	private void initButton(int index) {
		
		disBusy = (Button) findViewById(R.id.display_state_busy);
		disBusy.setOnClickListener(this);				
		
		disIdle = (Button) findViewById(R.id.display_state_idle);
		disIdle.setOnClickListener(this);
	
		
		switch (index) {
		case 0:
			disBusy.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
			break;
		case 1:
			disIdle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.event_right_red_check_small, 0);
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
		mActionBar.setCustomView(abTitle, mLayoutParams);
		mActionBar.setDisplayShowCustomEnabled(true);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.display_state_busy:
			Intent intentBusy = new Intent();
			intentBusy.putExtra(DISPLAY_STATE, 0);
			setResult(Activity.RESULT_OK, intentBusy);
			finish();
			break;
		case R.id.display_state_idle:
			Intent intentIdle = new Intent();
			intentIdle.putExtra(DISPLAY_STATE, 1);
			setResult(Activity.RESULT_OK, intentIdle);
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
