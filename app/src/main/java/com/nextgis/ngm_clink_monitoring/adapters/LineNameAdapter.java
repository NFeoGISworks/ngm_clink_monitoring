/*
 * Project:  NextGIS mobile apps for Compulink
 * Purpose:  Mobile GIS for Android
 * Authors:  Dmitry Baryshnikov (aka Bishop), polimax@mail.ru
 *           NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (C) 2014-2015 NextGIS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.ngm_clink_monitoring.adapters;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LineNameAdapter
        extends BaseAdapter
{
    protected Context     mContext;
    protected FoclProject mFoclProject;

    protected List<FoclStructItem> mFoclStructList;


    public LineNameAdapter(
            Context context,
            FoclProject foclProject)
    {
        mContext = context;
        mFoclProject = foclProject;

        int layerCount = mFoclProject.getLayerCount();
        mFoclStructList = new ArrayList<>(layerCount);

        for (int i = 0; i < layerCount; ++i) {
            FoclStruct foclStruct = (FoclStruct) mFoclProject.getLayer(i);

            boolean isChanges = foclStruct.isChanges();
            mFoclStructList.add(new FoclStructItem(foclStruct, isChanges));

            // for debug
            if (isChanges) {
                Log.d(Constants.TAG, "is changes, line: " + foclStruct.getNameOneString());
            }
            boolean isBuilt = foclStruct.getStatus().equals(FoclConstants.FIELD_VALUE_STATUS_BUILT);
            if (isBuilt) {
                Log.d(Constants.TAG, "built line: " + foclStruct.getNameOneString());
            }
        }

        Collections.sort(
                mFoclStructList, new Comparator<FoclStructItem>()
                {
                    @Override
                    public int compare(
                            FoclStructItem lhs,
                            FoclStructItem rhs)
                    {
                        String lhsName = lhs.getFoclStruct().getName();
                        String rhsName = rhs.getFoclStruct().getName();

                        if (TextUtils.isEmpty(lhsName) && TextUtils.isEmpty(rhsName)) {
                            return 0;
                        }

                        if (TextUtils.isEmpty(lhsName)) {
                            return -1;
                        }

                        if (TextUtils.isEmpty(rhsName)) {
                            return 1;
                        }

                        return lhsName.compareTo(rhsName);
                    }
                });
    }


    @Override
    public int getCount()
    {
        return mFoclStructList.size();
    }


    @Override
    public Object getItem(int position)
    {
        return mFoclStructList.get(position);
    }


    @Override
    public long getItemId(int position)
    {
        return mFoclStructList.get(position).getFoclStruct().getRemoteId();
    }


    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_line_name, null);
        }

        FoclStructItem item = (FoclStructItem) getItem(position);
        FoclStruct foclStruct = item.getFoclStruct();

        CheckedTextView tvFoclStructName =
                (CheckedTextView) convertView.findViewById(R.id.focl_struct_name);

        tvFoclStructName.setText(Html.fromHtml(foclStruct.getHtmlFormattedNameThreeStringsSmall()));
        tvFoclStructName.setChecked(
                foclStruct.getStatus().equals(FoclConstants.FIELD_VALUE_STATUS_BUILT));

        // for debug
//        if (item.isChanges()) {
//            tvFoclStructName.setTextColor(Color.RED);
//        } else {
//            tvFoclStructName.setTextColor(
//                    mContext.getResources().getColor(R.color.button_text_color));
//        }

        return convertView;
    }
}
