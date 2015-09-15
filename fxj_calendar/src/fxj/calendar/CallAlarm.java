package fxj.calendar;

import fxj.calendar.db.DBService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CallAlarm extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		DBService dbService = new DBService(context);
		// 在数据库中查询当前时间是否有需要提醒的记录
		Remind remind = dbService.getRemindMsg();
		if (remind != null)
		{
			Intent myIntent = new Intent(context, AlarmAlert.class);
			Bundle bundleRet = new Bundle();
			bundleRet.putString("remindMsg", remind.msg);
			bundleRet.putBoolean("shake", remind.shake);
			bundleRet.putBoolean("ring", remind.ring);
			myIntent.putExtras(bundleRet);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//startActivty(myIntent); error
			context.startActivity(myIntent);
		}

	}

}
