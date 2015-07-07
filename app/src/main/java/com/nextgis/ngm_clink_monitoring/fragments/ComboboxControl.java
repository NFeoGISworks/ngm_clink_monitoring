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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


/**
 * A modified Spinner that doesn't automatically select the first entry in the list.
 * <p/>
 * Shows the prompt if nothing is selected.
 * <p/>
 * Limitations: does not display prompt if the entry list is empty.
 * <p/>
 * see http://stackoverflow.com/a/3427058/4727406
 * <p/>
 * Warning! This has been tested on Android 1.5 through 4.2, but buyer beware! Because this solution relies
 * on reflection to call the private AdapterView.setNextSelectedPositionInt() and
 * AdapterView.setSelectedPositionInt(), it's not guaranteed to work in future OS updates. It seems
 * likely that it will, but it is by no means guaranteed.
 */
public class ComboboxControl
        extends Spinner

{
    protected Context             mContext;
    protected Map<String, String> mAliasValueMap;


    public ComboboxControl(Context context)
    {
        super(context);
        mContext = context;
    }


    public ComboboxControl(
            Context context,
            AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }


    public ComboboxControl(
            Context context,
            AttributeSet attrs,
            int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    @Override
    public void setAdapter(SpinnerAdapter origSpinnerAdapter)
    {
        final SpinnerAdapter adapter = (SpinnerAdapter) java.lang.reflect.Proxy.newProxyInstance(
                origSpinnerAdapter.getClass().getClassLoader(), new Class[] {SpinnerAdapter.class},
                new SpinnerAdapterProxy(origSpinnerAdapter));

        super.setAdapter(adapter);

        try {
            final Method m =
                    AdapterView.class.getDeclaredMethod("setNextSelectedPositionInt", int.class);
            m.setAccessible(true);
            m.invoke(this, -1);

            final Method n =
                    AdapterView.class.getDeclaredMethod("setSelectedPositionInt", int.class);
            n.setAccessible(true);
            n.invoke(this, -1);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Intercepts getView() to display the prompt if position < 0
     */
    protected class SpinnerAdapterProxy
            implements InvocationHandler
    {
        protected SpinnerAdapter mSpinnerAdapter;
        protected Method         getView;


        protected SpinnerAdapterProxy(SpinnerAdapter spinnerAdapter)
        {
            mSpinnerAdapter = spinnerAdapter;
            try {
                this.getView = SpinnerAdapter.class.getMethod(
                        "getView", int.class, View.class, ViewGroup.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        public Object invoke(
                Object proxy,
                Method m,
                Object[] args)
                throws Throwable
        {
            try {
                return m.equals(getView) && (Integer) (args[0]) < 0
                       ? getView(
                        (Integer) args[0], (View) args[1], (ViewGroup) args[2])
                       : m.invoke(mSpinnerAdapter, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        protected View getView(
                int position,
                View convertView,
                ViewGroup parent)
                throws IllegalAccessException
        {
            if (position < 0) {
                final TextView v = (TextView) ((LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        android.R.layout.simple_spinner_item, parent, false);
                v.setText(getPrompt());
                v.setTextColor(getResources().getColor(R.color.color_600));
                return v;
            }
            return mSpinnerAdapter.getView(position, convertView, parent);
        }
    }


    public void setValues(FoclDictItem dictItem)
    {
        mAliasValueMap = new HashMap<>();

        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item);

        if (null != dictItem) {
            for (Map.Entry<String, String> entry : dictItem.entrySet()) {
                String value = entry.getKey();
                String value_alias = entry.getValue();

                mAliasValueMap.put(value_alias, value);
                spinnerArrayAdapter.add(value_alias);
            }

            spinnerArrayAdapter.sort(
                    new Comparator<String>()
                    {
                        @Override
                        public int compare(
                                String lhs,
                                String rhs)
                        {
                            if (null == lhs && null == rhs) {
                                return 0;
                            }

                            if (null == lhs) {
                                return -1;
                            }

                            if (null == rhs) {
                                return 1;
                            }

                            return lhs.compareToIgnoreCase(rhs);
                        }
                    });

        } else {
            setEnabled(false);
        }


        // The drop down view
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setAdapter(spinnerArrayAdapter);
    }


    public String getValue()
    {
        String valueAlias = (String) getSelectedItem();
        return mAliasValueMap.get(valueAlias);
    }
}
