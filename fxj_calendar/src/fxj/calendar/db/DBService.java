package fxj.calendar.db;

import java.util.Calendar;

import fxj.calendar.Remind;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBService extends SQLiteOpenHelper
{
	private final static int DATABASE_VERSION = 4;
	private final static String DATABASE_NAME = "calendar.db";

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// ��һ���ֺ�֮ǰ�Ǳ���������ĳ�Ա create table table_name (column_1, column_2, ..., column_n);
		// table: [t_records]
		// item: id(integer)    title(varchar)  content(text)  record_date(date)  record_time(time)  
		// remind(boolean)  shake(boolean)  ring(boolean)
		// 1|fengxiaojun|you must study very hard!|2015-6-6|14:30:0|true|true|true
		// | �еķָ���
		String sql = "CREATE TABLE [t_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "  [title] VARCHAR(30) NOT NULL,  [content] TEXT,  [record_date] DATE NOT NULL,[remind_time] TIME,"
				+ "[remind] BOOLEAN,[shake] BOOLEAN,[ring] BOOLEAN)"
				+ ";CREATE INDEX [unique_title] ON [t_records] ([title]);"
				+ "CREATE INDEX [remind_time_index] ON [t_records] ([remind_time]);"
				+ "CREATE INDEX [record_date_index] ON [t_records] ([record_date]);"
				+ "CREATE INDEX [remind_index] ON [t_records] ([remind])";
		// �����sql��� ִֻ���˵�һ����֮ǰ�Ĳ���
		db.execSQL(sql);

	}

	public DBService(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 *  ���أ�ִ��ָ����sql��䣬�����ز�ѯ�Ľ��
	 * @param sql
	 * @return cursor
	 */
	public Cursor execSQL(String sql)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	/**
	 * �������ݿ�
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// ������� [t_records]����ɾ��
		String sql = "drop table if exists [t_records]";
		// SQLiteDatabase.execSQL ִ�в����ؽ����sql��䣬�������Ķ���sql��䲻֧�֣� 
		db.execSQL(sql);
		// table: [t_records]
		// item: id(integer)    title(varchar)  content(text)  record_date(date)  record_time(time)  
		// remind(boolean)  shake(boolean)  ring(boolean)
		// 1|fengxiaojun|you must study very hard!|2015-6-6|14:30:0|true|true|true
		// | �еķָ���
		sql = "CREATE TABLE [t_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "  [title] VARCHAR(30) NOT NULL,  [content] TEXT,  [record_date] DATE NOT NULL,[remind_time] TIME,"
				+ "[remind] BOOLEAN,[shake] BOOLEAN,[ring] BOOLEAN)"
				+ ";CREATE INDEX [unique_title] ON [t_records] ([title]);"
				+ "CREATE INDEX [remind_time_index] ON [t_records] ([remind_time]);"
				+ "CREATE INDEX [record_date_index] ON [t_records] ([record_date]);"
				+ "CREATE INDEX [remind_index] ON [t_records] ([remind])";
		db.execSQL(sql); // ����һ���µ� [t_records] ��

	}

	/**
	 * �����ݿ��в����¼���⣬��¼���ݣ���¼���ڣ���������һ����������3��������Ϊ��null false false��
	 * @param title
	 * @param content
	 * @param recordDate
	 */
	public void insertRecord(String title, String content, String recordDate)
	{
		insertRecord(title, content, recordDate, null, false, false);
	}

	/**
	 * �����ݿ��в����¼���⣬��¼���ݣ���¼���ڣ�����ʱ�䣬�𶯣�����
	 * @param string title
	 * @param string content
	 * @param string recordDate
	 * @param string remindTime
	 * @param boolean shake
	 * @param boolean ring
	 */
	public void insertRecord(String title, String content, String recordDate,
			String remindTime, boolean shake, boolean ring)
	{
		try
		{
			String sql = "";
			String remind = "false";
			if (remindTime != null)
			{
				remind = "true";
			}
			else
			{
				remindTime = "0:0:0";
			}
			// insert into table_name(column_1,column_2,...,column_n) values ('value_1','value_2',...,'value_n');
			sql = "insert into t_records(title, content, record_date,remind_time, remind, shake, ring) values('"
					+ title
					+ "','"
					+ content
					+ "','"
					+ recordDate
					+ "','"
					+ remindTime
					+ "','"
					+ remind
					+ "','"
					+ shake
					+ "','"
					+ ring + "' );";
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sql);
		}
		catch (Exception e)
		{
			Log.d("error", e.getMessage());
		}

	}

	/**
	 * ���ݼ�¼��id��ɾ����Ӧ�ļ�¼
	 * @param id
	 */
	public void deleteRecord(int id)
	{
		String sql = "delete from t_records where id = " + id;
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(sql);
	}

	/**
	 * ���ݼ�¼��id�����¼�¼����Ϣ
	 * @param id
	 */
	public void updateRecord(int id, String title, String content,
			String remindTime, boolean shake, boolean ring)
	{
		try
		{
			String sql = "";
			String remind = "false";
			if (remindTime != null)
			{
				remind = "true";
			}
			else
			{
				remindTime = "0:0:0";
			}
			sql = "update t_records set title='" + title + "', content='"
					+ content + "' ,remind_time='" + remindTime + "', remind='"
					+ remind + "',shake='" + shake + "', ring='" + ring
					+ "' where id=" + id;
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sql);
		}
		catch (Exception e)
		{
			Log.d("updateRecord", e.getMessage());
		}
	}

	/**
	 * ���ָ���������������Ѽ�¼�е����idֵ
	 * max(id) ָ�����ڵ�id�����Ǹ���¼��idֵ
	 * @param date 2015-8-6
	 * @return integer 
	 */
	public int getMaxId(String date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select max(id) from t_records where record_date='" + date
						+ "'", null);
		cursor.moveToFirst();
		return cursor.getInt(0);

	}
	
	/**
	 * ��ѯָ�����ڵ��������Ѽ�¼
	 * ���ص����ݣ�id | title
	 * @param date 20xx-x-xx
	 * @return cursor
	 */
	public Cursor query(String date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select id,title from t_records where record_date='" + date
						+ "' order by id desc", null);
		return cursor;
	}

	/**
	 * ����id(Ψһ��id)������Ѽ�¼��Ϣ��title,content,shake,ring.
	 * @param id
	 * @return cursor
	 */
	public Cursor query(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select  title,content,shake,ring from t_records where id=" + id, null);
		return cursor;
	}

	/**
	 *  ���ص�ǰʱ������Ѽ�¼��Ϣ������Remind(class)or����null
	 *  <p>CallAlarm(broadcast receiver)���ô˷�����
	 *  ÿһ��ִ��receiver����ѯ��ǰʱ��(year,month,date,hour,minute)�Ƿ�����Ҫ���ѵļ�¼��
	 *  �������AlarmAlert������Я��������Ϣ��Remind���ݹ�ȥ,��ʾ������Ϣ��
	 *  ������ϢΪ�������(remind=false);
	 *  <p>
	 * @return Remind-->boolean shake��boolean ring��Date date��String msg��
	 */
	public Remind getRemindMsg()
	{
		try
		{
			// ��ô�ʱ�˿̵ģ��� �� �� Сʱ ���� (int)
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = 0;
			
			/*
			 *  ��ѯ3����Ϣ
			 *  select title,shake,ring from t_records 
			 *  where record_date='201x-x-xx' and remind_time='12:24:30' and remind='true'
			 *  3����ѯ����
			 */
			String sql = "select title,shake,ring from t_records where record_date='"
					+ year + "-" + month + "-" + day + "' and remind_time='"
					+ hour + ":" + minute + ":" + second
					+ "' and remind='true'";
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext())
			{
				String remindMsg = cursor.getString(0);
				// update table_name set xx='x',xx='x',xx='x' where xx='xxx' and xxx='xxx' and xxx='xx' 
				sql = "update t_records set remind='false', shake='false', ring='false' where record_date='"
						+ year + "-" + month + "-" + day
						+ "' and remind_time='" + hour + ":" + minute + ":"
						+ second + "' and remind='true'";
				db = this.getWritableDatabase();
				db.execSQL(sql);// �������ѱ�־λΪfalse
				
				Remind remind = new Remind();
				remind.msg = remindMsg;
				remind.date = calendar.getTime();
				
				remind.shake =Boolean.parseBoolean(cursor.getString(1));
				remind.ring =Boolean.parseBoolean(cursor.getString(2));
				return remind;
			}
		}
		catch (Exception e)
		{
			Log.d("getRemindMsg", e.getMessage());
		}
		return null;
	}
}
