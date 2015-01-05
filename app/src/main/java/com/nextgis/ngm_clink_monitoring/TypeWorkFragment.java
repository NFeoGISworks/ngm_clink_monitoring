/*******************************************************************************
 * Project:  NextGIS mobile apps for Compulink
 * Purpose:  Mobile GIS for Android
 * Authors:  Dmitry Baryshnikov (aka Bishop), polimax@mail.ru
 *           NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (C) 2014-2015 NextGIS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.nextgis.ngm_clink_monitoring;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class TypeWorkFragment
        extends Fragment
{
    protected OnButtonsClickListener mOnButtonsClickListener;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.type_works_fragment, null);

        Button btnLaying = (Button) view.findViewById(R.id.btn_laying);
        Button btnMounting = (Button) view.findViewById(R.id.btn_mounting);
        Button btnMeasuring = (Button) view.findViewById(R.id.btn_measuring);

        btnLaying.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.LAYING_WORK);
                }
            }
        });

        btnMounting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.MOUNTING_WORK);
                }
            }
        });

        btnMeasuring.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.MEASURING_WORK);
                }
            }
        });

        return view;
    }


    public void setOnButtonsClickListener(OnButtonsClickListener onButtonsClickListener)
    {
        mOnButtonsClickListener = onButtonsClickListener;
    }


    public interface OnButtonsClickListener
    {
        void OnButtonsClick(int workType);
    }
}
