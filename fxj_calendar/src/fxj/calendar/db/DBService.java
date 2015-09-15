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
		// 第一个分号之前是表名和里面的成员 create table table_name (column_1, column_2, ..., column_n);
		// table: [t_records]
		// item: id(integer)    title(varchar)  content(text)  record_date(date)  record_time(time)  
		// remind(boolean)  shake(boolean)  ring(boolean)
		// 1|fengxiaojun|you must study very hard!|2015-6-6|14:30:0|true|true|true
		// | 列的分隔符
		String sql = "CREATE TABLE [t_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "  [title] VARCHAR(30) NOT NULL,  [content] TEXT,  [record_date] DATE NOT NULL,[remind_time] TIME,"
				+ "[remind] BOOLEAN,[shake] BOOLEAN,[ring] BOOLEAN)"
				+ ";CREATE INDEX [unique_title] ON [t_records] ([title]);"
				+ "CREATE INDEX [remind_time_index] ON [t_records] ([remind_time]);"
				+ "CREATE INDEX [record_date_index] ON [t_records] ([record_date]);"
				+ "CREATE INDEX [remind_index] ON [t_records] ([remind])";
		// 上面的sql语句 只执行了第一个；之前的部分
		db.execSQL(sql);

	}

	public DBService(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 *  重载，执行指定的sql语句，并返回查询的结果
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
	 * 升级数据库
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// 如果存在 [t_records]则将其删除
		String sql = "drop table if exists [t_records]";
		// SQLiteDatabase.execSQL 执行不返回结果的sql语句，；隔开的多条sql语句不支持！ 
		db.execSQL(sql);
		// table: [t_records]
		// item: id(integer)    title(varchar)  content(text)  record_date(date)  record_time(time)  
		// remind(boolean)  shake(boolean)  ring(boolean)
		// 1|fengxiaojun|you must study very hard!|2015-6-6|14:30:0|true|true|true
		// | 列的分隔符
		sql = "CREATE TABLE [t_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "  [title] VARCHAR(30) NOT NULL,  [content] TEXT,  [record_date] DATE NOT NULL,[remind_time] TIME,"
				+ "[remind] BOOLEAN,[shake] BOOLEAN,[ring] BOOLEAN)"
				+ ";CREATE INDEX [unique_title] ON [t_records] ([title]);"
				+ "CREATE INDEX [remind_time_index] ON [t_records] ([remind_time]);"
				+ "CREATE INDEX [record_date_index] ON [t_records] ([record_date]);"
				+ "CREATE INDEX [remind_index] ON [t_records] ([remind])";
		db.execSQL(sql); // 创建一个新的 [t_records] 表

	}

	/**
	 * 向数据库中插入记录标题，记录内容，记录日期，（调用另一个方法，后3个参数设为：null false false）
	 * @param title
	 * @param content
	 * @param recordDate
	 */
	public void insertRecord(String title, String content, String recordDate)
	{
		insertRecord(title, content, recordDate, null, false, false);
	}

	/**
	 * 向数据库中插入记录标题，记录内容，记录日期，提醒时间，震动，铃声
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
	 * 根据记录的id，删除相应的记录
	 * @param id
	 */
	public void deleteRecord(int id)
	{
		String sql = "delete from t_records where id = " + id;
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(sql);
	}

	/**
	 * 根据记录的id，更新记录的信息
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
	 * 获得指定日期中所有提醒记录中的最大id值
	 * max(id) 指定日期的id最大的那个记录的id值
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
	 * 查询指定日期的所有提醒记录
	 * 返回的数据：id | title
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
	 * 根据id(唯一的id)获得提醒记录信息：title,content,shake,ring.
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
	 *  返回当前时间的提醒记录信息，返回Remind(class)or返回null
	 *  <p>CallAlarm(broadcast receiver)调用此方法，
	 *  每一次执行receiver都查询当前时间(year,month,date,hour,minute)是否有需要提醒的纪录，
	 *  有则调用AlarmAlert，并将携带提醒信息的Remind传递过去,显示提醒信息。
	 *  更改信息为提醒完毕(remind=false);
	 *  <p>
	 * @return Remind-->boolean shake；boolean ring；Date date；String msg；
	 */
	public Remind getRemindMsg()
	{
		try
		{
			// 获得此时此刻的：年 月 日 小时 分钟 (int)
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = 0;
			
			/*
			 *  查询3列信息
			 *  select title,shake,ring from t_records 
			 *  where record_date='201x-x-xx' and remind_time='12:24:30' and remind='true'
			 *  3个查询条件
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
				db.execSQL(sql);// 设置提醒标志位为false
				
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
