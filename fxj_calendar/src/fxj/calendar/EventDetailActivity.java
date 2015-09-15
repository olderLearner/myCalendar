package fxj.calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class EventDetailActivity extends Activity {
	
	private ActionBar mActionBar;
	private EventDetail eventDetail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_detail);
		this.setTitle("返回");
		initActionBar();
		Intent intent = this.getIntent();
		int id = intent.getIntExtra("id", -1);
		eventDetail = new EventDetail();
		
		Cursor cursor = MonthActivity.calendarDBService.query(id);
		while(cursor.moveToNext()) {
			
			eventDetail.title = cursor.getString(1);
			eventDetail.location = cursor.getString(2);
			
			
			
			
			
			
			
		}
		
		
	}
	
	
	private void initActionBar() {
		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		
		TextView midTitle = new TextView(this);
		midTitle.setText("事件详细资料");
		midTitle.setTextSize(17);
		
		midTitle.setGravity(Gravity.CENTER_VERTICAL);
		
		ActionBar.LayoutParams mLayoutParams = new ActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
				Gravity.CENTER | Gravity.FILL_VERTICAL);
		mActionBar.setCustomView(midTitle, mLayoutParams);
		mActionBar.setDisplayShowCustomEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_event_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.event_detail_menu_edit:
			Toast.makeText(this, "未完待续！", Toast.LENGTH_SHORT).show();
			break;
		case android.R.id.home:
			finish();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
}
