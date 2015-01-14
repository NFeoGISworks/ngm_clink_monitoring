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
        View view = inflater.inflate(R.layout.fragment_type_works, null);

        Button btnLaying = (Button) view.findViewById(R.id.btn_laying);
        Button btnClutchMounting = (Button) view.findViewById(R.id.btn_clutch_mounting);
        Button btnCrossMounting = (Button) view.findViewById(R.id.btn_cross_mounting);
        Button btnClosetMounting = (Button) view.findViewById(R.id.btn_closet_mounting);
        Button btnPoleMounting = (Button) view.findViewById(R.id.btn_pole_mounting);
        Button btnLineMeasuring = (Button) view.findViewById(R.id.btn_line_measuring);

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

        btnClutchMounting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.CLUTCH_MOUNTING_WORK);
                }
            }
        });

        btnCrossMounting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.CROSS_MOUNTING_WORK);
                }
            }
        });

        btnClosetMounting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.CLOSET_MOUNTING_WORK);
                }
            }
        });

        btnPoleMounting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.POLE_MOUNTING_WORK);
                }
            }
        });

        btnLineMeasuring.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mOnButtonsClickListener != null) {
                    mOnButtonsClickListener.OnButtonsClick(MainActivity.LINE_MEASURING_WORK);
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
