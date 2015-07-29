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

package com.nextgis.ngm_clink_monitoring.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.CreateObjectFragment;


public class DistanceExceededDialog
        extends YesNoDialog
        implements CreateObjectFragment.OnDistanceChangedListener
{
    protected CreateObjectFragment mParent;

    protected Float    mDistance;
    protected TextView mDistanceView;

    protected Button mBtnRepeat;
    protected Button mBtnNewStartPoint;

    protected OnRepeatClickedListener   mOnRepeatClickedListener;
    protected OnNewPointClickedListener mOnNewPointClickedListener;


    public void setParams(CreateObjectFragment parent, Float distance)
    {
        mParent = parent;
        mDistance = distance;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_distance_exceeded, null);

        mDistanceView = (TextView) view.findViewById(R.id.distance_from_prev_point_de);
        mBtnRepeat = (Button) view.findViewById(R.id.btn_repeat_de);
        mBtnNewStartPoint = (Button) view.findViewById(R.id.btn_new_start_point_de);

        mDistanceView.setText(mParent.getDistanceText(mDistance));
        mDistanceView.setTextColor(mParent.getDistanceTextColor(mDistance));

        mBtnRepeat.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (null != mOnRepeatClickedListener) {
                            mOnRepeatClickedListener.onRepeatClicked();
                        }

                        dismiss();
                    }
                });

        mBtnNewStartPoint.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (null != mOnNewPointClickedListener) {
                            mOnNewPointClickedListener.onNewPointClicked();
                        }

                        dismiss();
                    }
                });


        setIcon(R.drawable.ic_action_warning);
        setTitle(R.string.warning);
        setView(view);

        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onPause()
    {
        mParent.setOnOnDistanceChangedListener(null);
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mParent.setOnOnDistanceChangedListener(this);
    }


    @Override
    public void onDistanceChanged(float distance)
    {
        mDistanceView.setText(mParent.getDistanceText(distance));
        mDistanceView.setTextColor(mParent.getDistanceTextColor(distance));
    }


    public void setOnRepeatClickedListener(OnRepeatClickedListener onRepeatClickedListener)
    {
        mOnRepeatClickedListener = onRepeatClickedListener;
    }


    public interface OnRepeatClickedListener
    {
        void onRepeatClicked();
    }


    public void setOnNewPointClickedListener(OnNewPointClickedListener onNewPointClickedListener)
    {
        mOnNewPointClickedListener = onNewPointClickedListener;
    }


    public interface OnNewPointClickedListener
    {
        void onNewPointClicked();
    }
}
