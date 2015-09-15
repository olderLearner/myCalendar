package fxj.calendar;

import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import fxj.calendar.util.EventList;
import fxj.calendar.util.EventList;
import fxj.calendar.util.MyFixed;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EventAdapter extends BaseAdapter {

	private List<EventList> mList;
	private Context mContext;
	private final int MAX_TYPE= 3;
	private final int TYPE_1 = 0;
	private final int TYPE_2 = 2;
	LayoutInflater inflater;
	
	public EventAdapter(Context context, List<EventList> list) {
		this.mContext = context;
		this.mList = list;
		
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private class ViewHolder{
		int id;
		TextView  title;
		TextView  location;
		TextView  begin_date;
		TextView  stop_date;
	}
	private class EmptyViewHolder{
		int id;
		TextView title;
	}
	
	
	@Override
	public int getItemViewType(int position) {
		
//		if (mList.get(position).getId() ==-1) {
//			return TYPE_2;
//		} else {
//			return TYPE_1;
//		}
		
		switch(mList.get(position).getId()) {
		case -1:
			return TYPE_2;
		default:
			return TYPE_1;
		}
	}
	
	@Override
	public int getViewTypeCount() {

		return MAX_TYPE;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		EmptyViewHolder mEmptyViewHolder = null;
		ViewHolder mViewHolder =  null;
		
		int type = getItemViewType(position);
		Log.d("Month", "adatpter-->" + mList.size());
		if (convertView == null) {
			inflater = LayoutInflater.from(mContext);
			switch (type) {
			case TYPE_1:
				convertView = View.inflate(mContext, R.layout.event_list, null);
				//convertView = inflater.inflate(R.layout.event_list, parent, false);
				mViewHolder = new ViewHolder();
				mViewHolder.title = (TextView) convertView.findViewById(R.id.event_list_title);
				mViewHolder.location = (TextView) convertView.findViewById(R.id.event_list_location);
				mViewHolder.begin_date = (TextView) convertView.findViewById(R.id.event_list_begin_date);
				mViewHolder.stop_date = (TextView) convertView.findViewById(R.id.event_list_stop_date);
				convertView.setTag(mViewHolder);
				break;
			case TYPE_2:
				convertView = View.inflate(mContext, R.layout.event_list_empty, null);
				//convertView = inflater.inflate(R.layout.event_list_empty, parent, false);
				mEmptyViewHolder = new EmptyViewHolder();
				mEmptyViewHolder.title = (TextView) convertView.findViewById(R.id.event_list_empty_title);
				convertView.setTag(mEmptyViewHolder);
				break;
			}
		} else {
			switch(type) {
			case TYPE_1:
				mViewHolder = (ViewHolder) convertView.getTag();
				break;
			case TYPE_2:
				mEmptyViewHolder = (EmptyViewHolder) convertView.getTag();
				break;
			}
		}
		
		switch (type) {
		case TYPE_1:
			EventList tmp = new EventList();
			tmp = mList.get(position);
			mViewHolder.id = tmp.id;
			mViewHolder.title.setText(tmp.title);
			mViewHolder.location.setText(tmp.location);
			
			int b = Time.getJulianDay(tmp.begin_date, MyFixed.GMTOFF);
			int s = Time.getJulianDay(tmp.stop_date, MyFixed.GMTOFF);
			if ( b== s) {
				mViewHolder.begin_date.setText( MyFixed.mSDF_01.format(new Date(tmp.begin_date)).substring(11));
				mViewHolder.stop_date.setText(MyFixed.mSDF_01.format(new Date(tmp.stop_date)).substring(11));
			} else {
				mViewHolder.begin_date.setText( MyFixed.mSDF_01.format(new Date(tmp.begin_date)).substring(11));
				mViewHolder.stop_date.setText(" ");
			}
			
//			mViewHolder.begin_date.setText( MyFixed.mSDF_01.format(new Date(tmp.begin_date)).substring(11));
//			mViewHolder.stop_date.setText(MyFixed.mSDF_02.format(new Date(tmp.stop_date)).substring(5));
			
			break;
		case TYPE_2:
			EventList tmp1 = new EventList();
			tmp1 = mList.get(position);
			mEmptyViewHolder.id = tmp1.id;
			mEmptyViewHolder.title.setText(tmp1.title);
			break;
		}
		
		return convertView;
	}

}
