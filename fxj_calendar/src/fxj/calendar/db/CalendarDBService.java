package fxj.calendar.db;

import java.util.Calendar;
import java.util.Date;

import fxj.calendar.EventRemind;
import fxj.calendar.Remind;
import fxj.calendar.util.MyFixed;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public class CalendarDBService extends SQLiteOpenHelper
{
	private final static int DATABASE_VERSION = 4;
	private final static String DATABASE_NAME = "calendar.database";
	public static final String COLUMN = "(title, location, isallday, begin_date, stop_date, "
			+ "isremind, remind_first_date, remind_second_date, "
			+ "repeat, display_state, url, tip)";
	public static final String FIND_MONTH_EVENT_PART1 = "select substr(date(begin_date/1000,'unixepoch','localtime'),9) from c_records"
			+ " where substr(date(begin_date/1000,'unixepoch','localtime'),1,7) = '";
	public static final String FIND_MONTH_EVENT_PART2 = "' group by substr(date(begin_date/1000,'unixepoch','localtime'),1)";
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// ��һ���ֺ�֮ǰ�Ǳ���������ĳ�Ա create table table_name (column_1, column_2, ..., column_n);
		// table: [t_records]
		// item: id(integer)    title(varchar)  content(text)  record_date(date)  record_time(time)  
		// remind(boolean)  shake(boolean)  ring(boolean)
		// 1|fengxiaojun|you must study very hard!|2015-6-6|14:30:0|true|true|true
		// | �еķָ���
		/*
		 * 13������
		 * begin_date ��¼������
		 */
		/*
		String sql = "CREATE TABLE [c_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[title] VARCHAR(30) NOT NULL,"
				+ "[location] VARCHAR(40),"
				+ "[isallday] BOOLEAN,"
				+ "[begin_date] INTEGER NOT NULL,"
				+ "[begin_time] TIME NOT NULL,"
				+ "[stop_date] DATE NOT NULL,"
				+ "[stop_time] TIME NOT NULL,"
				+ "[isremind] BOOLEAN,"
				+ "[remind_first_date] DATE,"
				+ "[remind_first_time] TIME,"
				+ "[remind_second_date] DATE,"
				+ "[remind_second_time] TIME,"
				+ "[repeat] INTEGER,"
				+ "[display_state] INTEGER,"
				+ "[url] TEXT,"
				+ "[tip] TEXT);";*/
		
		
		String sql = "CREATE TABLE [c_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[title] VARCHAR(30) NOT NULL,"
				+ "[location] VARCHAR(40),"
				+ "[isallday] BOOLEAN,"
				+ "[begin_date] INTEGER NOT NULL,"
				+ "[stop_date] INTEGER NOT NULL,"
				+ "[isremind] BOOLEAN,"
				+ "[remind_first_date] INTEGER,"
				+ "[remind_second_date] INTEGER,"
				+ "[repeat] INTEGER,"
				+ "[display_state] INTEGER,"
				+ "[url] TEXT,"
				+ "[tip] TEXT);";
		try{
			db.execSQL(sql);
			Log.d("MonthActivity", "zhi xing l chuangjian");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		/*
		String sql1 = "CREATE INDEX [unique_title] ON [c_records] ([title]);";
		db.execSQL(sql1);
		
		String sql2 = "CREATE INDEX [begin_date_index] ON [c_records] ([begin_time]);";
		db.execSQL(sql2);
		
		String sql3 = "CREATE INDEX [isremind_index] ON [c_records] ([isremind]);";
		db.execSQL(sql3);
		
		String sql4 = "CREATE INDEX [remind_first_date_index] ON [c_records] ([remind_first_date]);";
		db.execSQL(sql4);
		
		String sql5 = "CREATE INDEX [remind_first_time_index] ON [c_records] ([remind_first_time]);";
		db.execSQL(sql5);
		
		String sql6 = "CREATE INDEX [remind_second_date_index] ON [c_records] ([remind_second_date]);";
		db.execSQL(sql6);
		
		String sql7 = "CREATE INDEX [remind_second_time_index] ON [c_records] ([remind_second_time]);";
		db.execSQL(sql7);
		
		*/
	}

	public CalendarDBService(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * ɾ��ָ�����ֵ����ݿ�
	 * @param context
	 * @param name
	 * @return
	 */
	public boolean deleteDatebase(Context context, String name){
		return context.deleteDatabase(name);
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
		/*
		 *  ������� [c_records]����ɾ��
		 *  SQLiteDatabase.execSQL ִ�в����ؽ����sql��䣬�������Ķ���sql��䲻֧�֣�
		 *  
		 */
		String sql = "drop table if exists [c_records];";
		db.execSQL(sql);
		
		
		/*
		sql = "CREATE TABLE [c_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[title] VARCHAR(30) NOT NULL,"
				+ "[location] VARCHAR(40)"
				+ "[isallday] BOOLEAN,"
				+ "[begin_date] DATE NOT NULL,"
				+ "[begin_time] TIME NOT NULL,"
				+ "[stop_date] DATE NOT NULL,"
				+ "[stop_time] TIME NOT NULL,"
				+ "[isremind] BOOLEAN,"
				+ "[remind_first_date] DATE,"
				+ "[remind_first_time] TIME,"
				+ "[remind_second_date] DATE,"
				+ "[remind_second_time] TIME,"
				+ "[repeat] INTEGER,"
				+ "[display_state] INTEGER,"
				+ "[url] TEXT,"
				+ "[tip] TEXT);";
		*/
		String sql1 = "CREATE TABLE [c_records] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[title] VARCHAR(30) NOT NULL,"
				+ "[location] VARCHAR(40),"
				+ "[isallday] BOOLEAN,"
				+ "[begin_date] INTEGER NOT NULL,"
				+ "[stop_date] INTEGER NOT NULL,"
				+ "[isremind] BOOLEAN,"
				+ "[remind_first_date] INTEGER,"
				+ "[remind_second_date] INTEGER,"
				+ "[repeat] INTEGER,"
				+ "[display_state] INTEGER,"
				+ "[url] TEXT,"
				+ "[tip] TEXT);";
		
		db.execSQL(sql1);
		
		
		String sql2 = "CREATE INDEX [unique_title] ON [c_records] ([title]);";
		db.execSQL(sql2);
		
		String sql3 = "CREATE INDEX [begin_date_index] ON [c_records] ([begin_date]);";
		db.execSQL(sql3);
		
		String sql4 = "CREATE INDEX [remind_first_date_index] ON [c_records] ([remind_first_date]);";
		db.execSQL(sql4);
		
		String sql5 = "CREATE INDEX [remind_second_date_index] ON [c_records] ([remind_second_date]);";
		db.execSQL(sql5);
	}

	/**
	 * �����ݿ��в����¼���⣬��¼���ݣ���¼���ڣ���������һ����������3��������Ϊ��null false false��
	 * @param title
	 * @param content
	 * @param recordDate
	 */
	public void insertRecord(String title, String content, String recordDate)
	{
		//insertRecord(title, content, recordDate, null, false, false);
	}

	/**
	 * ��Ӽ�¼ id + 12
	 * @param title  				String
	 * @param location  			String
	 * @param isallday  			boolean
	 * @param begin_date 			long
	 * @param stop_date  			long
	 * @param isremind 		 		boolean 
	 * @param remind_first_date		long
	 * @param remind_second_date	long
	 * @param repeat 				int
	 * @param display_state 		int
	 * @param url 					String
	 * @param tip 					String
	 */
	
	public void insertRecord(String title, String location, boolean isallday,
			long begin_date, long stop_date, boolean isremind, long remind_first_date,long remind_second_date,
			 int repeat, int display_state, String url, String tip) {
		/*
		String isremind = "false";
		if (remind_first_date != 0) {
			isremind = "true";
		}*/
		
		if (title.length() ==0) {
			title = "�½��¼�";
		}
		
		String sql = new StringBuilder().append("insert into c_records"+COLUMN +"values")
							.append("('"+title)
							.append("','"+location)
							.append("','"+isallday)
							.append("','"+begin_date)
							.append("','"+stop_date)
							.append("','"+isremind)
							.append("','"+remind_first_date)
							.append("','"+remind_second_date)
							.append("','"+repeat)
							.append("','"+display_state)
							.append("','"+url)
							.append("','"+tip+"' );")
							.toString();
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sql);
		}catch (Exception e) {
			Log.d("error", e.getMessage());
		}
		
	}
	
	
	/**
	 * ���ݼ�¼��id��ɾ����Ӧ�ļ�¼
	 * 2015 8 28 ��βҪ��Ҫ+ ; ?
	 * @param id
	 */
	public void deleteRecord(int id)
	{
		String sql = "delete from c_records where id = " + id+";";
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(sql);
	}

	/**
	 * ����ָ��id�ļ�¼��Ϣ
	 * @param id
	 * @param title
	 * @param location
	 * @param isallday
	 * @param begin_date
	 * @param stop_date
	 * @param isremind
	 * @param remind_first_date
	 * @param remind_second_date
	 * @param repeat
	 * @param display_state
	 * @param url
	 * @param tip
	 */
	public void updateRecord(int id, String title, String location,
			boolean isallday, long begin_date, long stop_date, boolean isremind,
			long remind_first_date, long remind_second_date, int repeat,
			int display_state, String url, String tip) {
		
		if (title == null) {
			title = "�½��¼�";
		}
		/*
		if (location == null) location = "0";
		if (url == null) url = "0";
		if (tip == null) tip = "0";
		*/
		String sql = new StringBuilder().append("update c_records set title='").append(title+"', ")
							.append("location='").append(location+"', ")
							.append("isallday='").append(isallday+"', ")
							.append("begin_date='").append(begin_date+"', ")
							.append("stop_date='").append(stop_date+"', ")
							.append("isremind='").append(isremind+"', ")
							.append("remind_first_date='").append(remind_first_date+"', ")
							.append("remind_second_date='").append(remind_second_date+"', ")
							.append("repeat='").append(repeat+"', ")
							.append("display_state='").append(display_state+"', ")
							.append("url='").append(url+"', ")
							.append("tip='").append(tip+"' where id =")
							.append(id)
							.toString();
							
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sql);
		}catch (Exception e) {
			Log.d("error", e.getMessage());
		}
		
	}
	
	/**
	 * ���ָ���������������Ѽ�¼�е����idֵ
	 * max(id) ָ�����ڵ�id�����Ǹ���¼��idֵ
	 * @param date 2015-8-6
	 * @return integer 
	 */
	public int getMaxId(long date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select max(id) from c_records where begin_date > " + date +" and begin_date < ("+(date + 86400000)
						+ ")", null);
		cursor.moveToFirst();
		return cursor.getInt(0);

	}
	
	/**
	 * 
	 * �ܲ�����Ҫ�����Բ���  2015 8 28  ��ѯ����
	 * ��ѯָ�����ڵ��������Ѽ�¼
	 * ���ص����ݣ�id | title | location | begin_date | stop_date
	 * asc ����
	 * decs ����
	 * @param date  ��ʾ���ڵ� long
	 * @return cursor
	 */
	public Cursor query(long date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select id,title,location,begin_date,stop_date from c_records where begin_date >" + date +" and begin_date < ("+(date + 86400000)
						+ ") order by id asc",null);
		return cursor;
	}

	public Cursor queryByBeginDateAsc(long date)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select id,title,location,begin_date,stop_date from c_records where begin_date >" + date +" and begin_date < ("+(date + 86400000)
						+ ") order by begin_date asc",null);
		return cursor;
	}
	
	
	
	/**
	 * ����id��ü�¼��Ϣ��title,location,begin_date,stop_date,
	 * @param id
	 * @return cursor
	 */
	public Cursor query(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from c_records where id=" + id, null);
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
	public EventRemind getRemindMsg()
	{
		try
		{
			// ��ô�ʱ�˿̵ģ��� �� �� Сʱ ���� (int)
			/*
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = 0;
			*/
			
			Time time = new Time(MyFixed.TIMEZONE);
			time.setToNow();
			time.normalize(true);
			int year = time.year;
			int month = time.month;
			int day = time.monthDay;
			int hour = time.hour;
			int minute = time.minute;
			int second = 0;
			time.set(second, minute, hour, day, month, year);
			time.normalize(true);
			
			long nowTime = time.toMillis(true);
			
			/*
			 *  ��ѯ3����Ϣ
			 *  select title,shake,ring from t_records 
			 *  where record_date='201x-x-xx' and remind_time='12:24:30' and remind='true'
			 *  3����ѯ����
			 */
			/*String sql = "select title,location,begin_date,begin_time,stop_date,stop_time from c_records where ((remind_first_date='"
					+ year
					+ "-"
					+ month
					+ "-"
					+ day
					+ "' and remind_first_time='"
					+ hour
					+ ":"
					+ minute
					+ ":"
					+ second
					+ "') or(remind_second_date='"
					+ year
					+ "-"
					+ month
					+ "-"
					+ day
					+ "' and remind_second_time='"
					+ hour
					+ ":"
					+ minute + ":" + second + "')) and isremind='true'";*/
			
			// 2015 8 28  ������
			String sql = "select title,location,begin_date,stop_date from c_records where ((remind_first_date= "
					+ nowTime + ") or (remind_second_date=" + nowTime + ")) and isremind='true'";
			
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext())
			{
				EventRemind remind = new EventRemind();
				remind.title = cursor.getString(0);
				remind.location = cursor.getString(1);
				
				long begin_date = cursor.getLong(2);
				long stop_date = cursor.getLong(3);
				remind.begin_date = new Date(begin_date);
				remind.stop_date = new Date(stop_date);
				
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
