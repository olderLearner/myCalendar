/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
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

package fxj.calendar.datepicker;

import java.util.LinkedList;
import java.util.List;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Recycle stores wheel items to reuse. 
 */
public class WheelRecycle {
	// Cached items
	private List<View> items;
	
	// Cached empty items
	private List<View> emptyItems;
	
	// Wheel view
	private WheelView wheel;
	
	/**
	 * Constructor
	 * @param wheel the wheel view
	 */
	public WheelRecycle(WheelView wheel) {
		this.wheel = wheel;
	}

	/**
	 * Recycles items from specified layout.
	 * There are saved only items not included to specified range.
	 * All the cached items are removed from original layout.
	 * 
	 * <p> ���£����firstItem �Ծ��� range ��Χ�ڣ�firstItem���֣�ֻҪ���µ�view������ͼ�ھͲ����û���
	 * ���ϣ�firstItem ���µ�range.firstһ�£�ֻҪfirstItem���Ƴ���Ļ����ѭ��������ͼ����Ҫ����
	 * 
	 * <li> ��һ�ι������ºܴ���루������� ���� 8�� itemHeight������firstItem ���� range��Χ��-->�о���Щ����ȷ
	 * <li> 0 1 2 3 4 5 6 7 8 9 10  first:5--- ���ҹ���4��  5 ����range��Χ�ڣ���ʱ1������� firstItem,
	 * @param layout the layout containing items to be cached
	 * @param firstItem the number of first item in layout ����ƶ�֮ǰ��firstItem
	 * @param range the range of current wheel items 
	 * @return the new value of first item number
	 */
	public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range) {
		int index = firstItem;
		for (int i = 0; i < layout.getChildCount();) {
			if (!range.contains(index)) {
				recycleView(layout.getChildAt(i), index);// index ѭ�� ֱ��index��range��Χ��
				layout.removeViewAt(i);
				if (i == 0) { // first item
					firstItem++;
				}
			} else {
				i++; // go to next item
			}
			
			index++;
		}
		return firstItem;
	}
	
	/**
	 * Gets item view
	 * ȡ�������view������item��
	 * @return the cached view or null if items is null or contain nothing
	 */
	public View getItem() {
		return getCachedView(items);
	}

	/**
	 * Gets empty item view
	 * @return the cached empty view or null if items is null or contain nothing
	 */
	public View getEmptyItem() {
		return getCachedView(emptyItems);
	}
	
	/**
	 * Clears all views ,
	 * item  and emptyitem cache
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		if (emptyItems != null) {
			emptyItems.clear();
		}
	}

	/**
	 * Adds view to specified cache. Creates a cache list if it is null.
	 * ���� items emptyItems
	 * @param view the view to be cached
	 * @param cache the cache list
	 * @return the cache list
	 */
	private List<View> addView(View view, List<View> cache) {
		if (cache == null) {
			cache = new LinkedList<View>();
		}
		
		cache.add(view);
		return cache;
	}

	/**
	 * Adds view to cache. Determines view type (item view or empty one) by index.
	 * <p> 1) ����ѭ��ģʽͬʱindex���ڷ�Χ�ڣ���view��ӵ�emptyItem cache��
	 * 2) �����������indexת������Χ�ڣ���view��ӵ� items cache��
	 * @param view the view to be cached
	 * @param index the index of view
	 */
	private void recycleView(View view, int index) {
		int count = wheel.getViewAdapter().getItemsCount();

		if ((index < 0 || index >= count) && !wheel.isCyclic()) {
			// empty view
			emptyItems = addView(view, emptyItems);
		} else {
			while (index < 0) {
				index = count + index;
			}
			index %= count;
			items = addView(view, items);
		}
	}
	
	/**
	 * Gets view from specified cache.
	 * ȡ�������view��ȡ��һ������ɾ������
	 * @param cache the cache
	 * @return the first view from cache or null
	 */
	private View getCachedView(List<View> cache) {
		if (cache != null && cache.size() > 0) {
			View view = cache.get(0);
			cache.remove(0);
			return view;
		}
		return null;
	}

}
