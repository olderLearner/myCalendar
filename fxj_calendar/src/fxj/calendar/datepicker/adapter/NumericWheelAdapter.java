/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fxj.calendar.datepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {
    
    /** The default min value */
    public static final int DEFAULT_MAX_VALUE = 9;

    /** The default max value */
    private static final int DEFAULT_MIN_VALUE = 0;
    
    // Values
    private int minValue;
    private int maxValue;
    
    // format
    private String format;
    
    private String label;
    
    /**
     * Constructor
     * <p> 数据值范围未指定，设为预定义值 0 ~ 9 ，format = null
     * @param context the current context
     */
    public NumericWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructor
     * <p> 指定绑定的数据范围 min~max，format = null
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }

    /**
     * Constructor
     * <p> 设定wheel范围 min~max，format设定为传入值
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     * @param format the format string
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context);
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    @Override
    /**
     * 先确定index是否在item范围内
     * 取得对应index应该显示的text值，
     */
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            return format != null ? String.format(format, value) : Integer.toString(value);
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }
    
    @Override
    /**
     * 这个方法有问题，textview没有使用，返回值是convertview
     * 是对的 2015-9-15，textview 装在 convertview中
     */
    public View getItem(int index, View convertView, ViewGroup parent) {
        if (index >= 0 && index < getItemsCount()) {
            if (convertView == null) {
                // 没有可以重复使用的view，根据资源id获取一个view的实例
            	// itemResourceId 父类中protected变量，子类可以使用，
            	// NumericWheelAdapter(Context context, int minValue, int maxValue, String format) 构造方法中
            	// 调用了父类的构造方法，itemResourceId 设为预定义值 -1：TEXT_VIEW_ITEM_RESOURCE
            	convertView = getView(itemResourceId, parent);
            }           
            /*
             * itemTextResourceId 为预定义值 0：NO_RESOURCE
             * 将converView 传递给textview（方法中做强制类型转换）
             */
            TextView textView = getTextView(convertView, itemTextResourceId);
            if (textView != null) {
                CharSequence text = getItemText(index);
                if (text == null) {
                    text = "";
                }
                textView.setText(text+label);
    
                if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
                    configureTextView(textView);
                }
            }
            return convertView;
        }
    	return null;
    }

	public void setLabel(String label) {
		this.label=label;
	}    
	
}
