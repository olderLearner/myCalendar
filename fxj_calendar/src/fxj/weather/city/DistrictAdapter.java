package fxj.weather.city;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fxj.calendar.datepicker.adapter.NumericWheelAdapter;

/** 
 * @author fengxj fengxj1985@126.com
 * @version ����ʱ�䣺2015��9��15�� ����10:55:31 
 * 
 */
public class DistrictAdapter extends NumericWheelAdapter {

	private List<District> mList;
	private Context mContext;
	
	
	public DistrictAdapter(Context context) {
		super(context);
		
	}

	public DistrictAdapter(Context context, List<District> list) {
		super(context);
		this.mContext = context;
		this.mList = list;
		
	}
	
	
	
//	public ProvinceAdapter(Context context,List<Province> provinces) {
//			
//		super(context);
//		this.mContext = context;
//		this.mList = provinces;
//		
//	}

	@Override
	protected void configureTextView(TextView view) {
		
		view.setTextSize(20);
		view.setTextColor(0xFF585858);
        view.setGravity(Gravity.CENTER);
        //view.setHeight(100); // û����
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setLines(1);
	}
	
	
	@Override
	public CharSequence getItemText(int index) {
		return mList.get(index).toString();
	}

	@Override
	public int getItemsCount() {
		return mList.size();
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		
		
		 if (index >= 0 && index < getItemsCount()) {
	            if (convertView == null) {
	            	/*
	            	 *  itemResourceId ������protected�������������ʹ�ã�
	            	 *  NumericWheelAdapter(Context context, int minValue, int maxValue, String format) ���췽����
	            	 *  �����˸���Ĺ��췽����itemResourceId ��ΪԤ����ֵ -1��TEXT_VIEW_ITEM_RESOURCE
	            	 */
	            	convertView = getView(itemResourceId, parent);
	            }           
	            /*
	             * convertView ��Ϊnull
	             * itemTextResourceId ΪԤ����ֵ 0��NO_RESOURCE
	             * ��converView ���ݸ�textview����������ǿ������ת����
	             */
	            TextView textView = getTextView(convertView, itemTextResourceId);
	            
	            if (textView != null) {
	                CharSequence text = getItemText(index);
	                if (text == null) {
	                    text = "";
	                }
	                textView.setText(text);
	    
	                if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
	                    configureTextView(textView);
	                }
	            }
	            
	            return convertView;
	        }
	    	return null;
	}

	@Override
	protected void notifyDataChangedEvent() {
		
		super.notifyDataChangedEvent();
		
		
	}
	
	
	
	
	
	
	
	
	
	
}
