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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;

import java.util.HashMap;
import java.util.Map;


public class StatusComboboxControl
        extends Spinner
{
    protected Context             mContext;
    protected Map<String, String> mAliasValueMap;


    public StatusComboboxControl(Context context)
    {
        super(context);
        mContext = context;
    }


    public StatusComboboxControl(
            Context context,
            AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }


    public StatusComboboxControl(
            Context context,
            AttributeSet attrs,
            int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    public void setValues(FoclDictItem dictItem)
    {
        String value;
        String value_alias;
        mAliasValueMap = new HashMap<>();
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item);


        value = FoclConstants.FIELD_VALUE_STATUS_PROJECT;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mAliasValueMap.put(value_alias, value);
        spinnerArrayAdapter.add(value_alias);


        value = FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mAliasValueMap.put(value_alias, value);
        spinnerArrayAdapter.add(value_alias);


        value = FoclConstants.FIELD_VALUE_STATUS_BUILT;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mAliasValueMap.put(value_alias, value);
        spinnerArrayAdapter.add(value_alias);


        // The drop down view
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setAdapter(spinnerArrayAdapter);
    }


    public String getValue()
    {
        String valueAlias = (String) getSelectedItem();
        return mAliasValueMap.get(valueAlias);
    }


    public static int getStausId(String status) {
        switch (status) {
            case FoclConstants.FIELD_VALUE_STATUS_PROJECT:
            default:
                return FoclConstants.STATUS_PROJECT_INDEX;
            case FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS:
                return FoclConstants.STATUS_IN_PROGRESS_INDEX;
            case FoclConstants.FIELD_VALUE_STATUS_BUILT:
                return FoclConstants.STATUS_BUILT_INDEX;
        }
    }
}
