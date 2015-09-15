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
		// ȡ�ô��ݽ����� �� ��  ��
		year = getIntent().getExtras().getInt("year");
		month = getIntent().getExtras().getInt("month");
		day = getIntent().getExtras().getInt("day");
		
		// ��ѯ���ݵ����������ݿ����Ƿ��м�¼
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
		// ��ѯ���ݿ��еļ�¼����ӵ�adapter��idList��
		while (cursor.moveToNext())
		{
			arrayAdapter.add(cursor.getString(1));
			idList.add(cursor.getInt(0));

		}

		// ����������ʾ��ʽ�����calendar���󣬽�����������ո�ֵ��calendar��ListActivity�ı��⣺��Ӧ��ʽ������
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��");
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(year, month, day);
		setTitle(sdf.format(calendar.getTime()));
		setListAdapter(arrayAdapter);
		myListActivity = null;
		myListActivity = this;

	}

	
	
	
	
	
	@Override
	/**
	 * ����оٵ�list
	 */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		startEditRecordActivity(position);
	}

	/**
	 * ����Record activity������Ҫ�������ݣ��������ֱ�Ӵ������ݿ�
	 * @param index �������view�� position
	 */
	public void startEditRecordActivity(int index)
	{
		Intent intent = new Intent(this, Record.class);
		intent.putExtra("edit", true);
		intent.putExtra("id", idList.get(index));
		intent.putExtra("index", index);
		startActivity(intent);
	}

	// menu ����
	class MenuItemClickParent
	{
		protected Activity activity;

		public MenuItemClickParent(Activity activity)
		{
			this.activity = activity;
		}
	}

	/**
	 * ��Ӽ�¼��ť
	 * @author fengxiaojun
	 *
	 */
	class OnAddRecordMenuItemClick extends MenuItemClickParent implements
			OnMenuItemClickListener
	{

		@Override
		public boolean onMenuItemClick(MenuItem item)
		{
			// ����record acvtivity
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
	 * �༭
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
	 * ɾ��
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
			// �Ƴ�M�е�����
			recordArray.remove(index);
			int id = idList.get(index);
			idList.remove(index);
			// ����listview
			allRecord.setListAdapter(arrayAdapter);
			// ɾ�����ݿ��е���Ϣ
			Grid.dbService.deleteRecord(id);
			return true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.allrecord, menu);
		
		
		/*
		miNewRecord = menu.add(0, 1, 1, "��Ӽ�¼");
		miModifyRecord = menu.add(0, 2, 2, "�޸�/�鿴");
		miDeleteRecord = menu.add(0, 4, 4, "ɾ����¼");

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
