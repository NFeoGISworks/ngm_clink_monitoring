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
import android.widget.Spinner;
import android.widget.TextView;


public class LineWorkFragment
        extends Fragment
{
    protected TextView mWorkTypeName;
    protected Spinner  mLineName;
    protected TextView mObjectCaption;
    protected Spinner  mObjectName;
    protected TextView mPhotoHintText;

    protected int mWorkType = MainActivity.UNKNOWN_WORK;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.ine_work_fragment, null);

        mWorkTypeName = (TextView) view.findViewById(R.id.work_type_name);
        mLineName = (Spinner) view.findViewById(R.id.line_name);
        mObjectCaption = (TextView) view.findViewById(R.id.object_caption);
        mObjectName = (Spinner) view.findViewById(R.id.object_name);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text);

        switch (mWorkType) {
            case MainActivity.LAYING_WORK:
                mWorkTypeName.setText(R.string.construction_length_laying);
                mObjectCaption.setText(R.string.construction_length);
                mPhotoHintText.setText(R.string.make_photos_to_confirm);
                break;

            case MainActivity.MOUNTING_WORK:
                mWorkTypeName.setText(R.string.clutch_or_cross_mounting);
                mObjectCaption.setText(R.string.clutch_or_cross);
                mPhotoHintText.setText(R.string.make_photos_to_confirm_clutch);
                break;

            case MainActivity.MEASURING_WORK:
                mWorkTypeName.setText(R.string.construction_length_laying);
                mObjectCaption.setVisibility(View.INVISIBLE);
                mObjectName.setVisibility(View.INVISIBLE);
                mPhotoHintText.setText(R.string.make_photos_to_confirm);
                break;
        }

        return view;
    }


    public void setParams(int workType)
    {
        mWorkType = workType;
    }
}