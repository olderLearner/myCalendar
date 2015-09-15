package fxj.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import fxj.calendar.Record.MenuItemClickParent;
import fxj.calendar.db.DBService;
import android.R.integer;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AllRecord extends ListActivity
{
	public static int year, month, day;
	
	public static List<String> recordArray;
	public static ArrayAdapter<String> arrayAdapter;
	public static List<Integer> idList = new ArrayList<Integer>();
	public static ListActivity myListActivity;
	private MenuItem miNewRecord;
	private MenuItem miModifyRecord;
	private MenuItem miDeleteRecord;
	private OnEditRecordMenuItemClick editRecordMenuItemClick = new OnEditRecordMenuItemClick(
			this);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		// 取得传递进来的 年 月  日
		year = getIntent().getExtras().getInt("year");
		month = getIntent().getExtras().getInt("month");
		day = getIntent().getExtras().getInt("day");
		
		// 查询传递的日期在数据库中是否有记录
		Cursor cursor = Grid.dbService.query(year + "-" + month + "-" + day);
		// M ArrayList
		if (recordArray == null)
			recordArray = new ArrayList<String>();
		// C arrayadapter
		if (arrayAdapter == null)
			arrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, recordArray);
		else
			arrayAdapter.clear();

		idList.clear();
		// 查询数据库中的记录，添加到adapter和idList中
		while (cursor.moveToNext())
		{
			arrayAdapter.add(cursor.getString(1));
			idList.add(cursor.getInt(0));

		}

		// 设置日期显示格式，获得calendar对象，将点击的年月日赋值给calendar，ListActivity的标题：相应格式的日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(year, month, day);
		setTitle(sdf.format(calendar.getTime()));
		setListAdapter(arrayAdapter);
		myListActivity = null;
		myListActivity = this;

	}

	
	
	
	
	
	@Override
	/**
	 * 点击列举的list
	 */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		startEditRecordActivity(position);
	}

	/**
	 * 进入Record activity，不需要返回数据，若处理会直接存入数据库
	 * @param index ：点击的view的 position
	 */
	public void startEditRecordActivity(int index)
	{
		Intent intent = new Intent(this, Record.class);
		intent.putExtra("edit", true);
		intent.putExtra("id", idList.get(index));
		intent.putExtra("index", index);
		startActivity(intent);
	}

	// menu 主类
	class MenuItemClickParent
	{
		protected Activity activity;

		public MenuItemClickParent(Activity activity)
		{
			this.activity = activity;
		}
	}

	/**
	 * 添加记录按钮
	 * @author fengxiaojun
	 *
	 */
	class OnAddRecordMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			// 开启record acvtivity
			Intent intent = new Intent(activity, Record.class);
			activity.startActivity(intent);
			return true;
		}

		public OnAddRecordMenuItemClick(Activity activity)
		{
			super(activity);
		}

	}

	/**
	 * 编辑
	 * @author fengxiaojun
	 *
	 */
	class OnEditRecordMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			AllRecord allRecord = (AllRecord) activity;

			int index = allRecord.getSelectedItemPosition();

			if (index < 0)
				return false;
			allRecord.startEditRecordActivity(index);
			return true;
		}

		public OnEditRecordMenuItemClick(Activity activity)
		{
			super(activity);
		}

	}

	/**
	 * 删除
	 * @author fengxiaojun
	 */
	class OnDeleteRecordMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		public OnDeleteRecordMenuItemClick(Activity activity)
		{
			super(activity);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			AllRecord allRecord = (AllRecord) activity;
			int index = allRecord.getSelectedItemPosition();

			if (index < 0)
				return false;
			// 移除M中的数据
			recordArray.remove(index);
			int id = idList.get(index);
			idList.remove(index);
			// 更新listview
			allRecord.setListAdapter(arrayAdapter);
			// 删除数据库中的信息
			Grid.dbService.deleteRecord(id);
			return true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.allrecord, menu);
		
		
		/*
		miNewRecord = menu.add(0, 1, 1, "添加记录");
		miModifyRecord = menu.add(0, 2, 2, "修改/查看");
		miDeleteRecord = menu.add(0, 4, 4, "删除记录");

		miNewRecord.setOnMenuItemClickListener(new OnAddRecordMenuItemClick(
				this));

		miModifyRecord.setOnMenuItemClickListener(editRecordMenuItemClick);
		miDeleteRecord.setOnMenuItemClickListener(new OnDeleteRecordMenuItemClick(this));
		*/
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.add_newrecord:
			
			break;
		case R.id.edit_record:
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}
