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
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.CreateObjectFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class DistanceExceededDialog
        extends YesNoDialog
        implements GpsEventListener
{
    protected GpsEventSource mGpsEventSource;

    protected String   mObjectLayerName;
    protected Float    mDistance;
    protected TextView mDistanceView;

    protected Button mBtnRepeat;
    protected Button mBtnNewStartPoint;

    protected OnRepeatClickedListener   mOnRepeatClickedListener;
    protected OnNewPointClickedListener mOnNewPointClickedListener;


    public void setParams(
            String objectLayerName,
            Float distance)
    {
        mObjectLayerName = objectLayerName;
        mDistance = distance;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (null != mObjectLayerName) {
            outState.putString(FoclConstants.OBJECT_LAYER_NAME, mObjectLayerName);
        }

        if (null != mDistance) {
            outState.putFloat(FoclConstants.DISTANCE, mDistance);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (null != savedInstanceState) {
            mObjectLayerName = savedInstanceState.getString(FoclConstants.OBJECT_LAYER_NAME);
            mDistance = savedInstanceState.getFloat(FoclConstants.DISTANCE);
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        mGpsEventSource = app.getGpsEventSource();

        if (null != mGpsEventSource) {
            mGpsEventSource.addListener(this);
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        CreateObjectFragment createObjectFragment =
                (CreateObjectFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_CREATE_OBJECT);
        if (null != createObjectFragment) {
            mOnRepeatClickedListener = createObjectFragment;
            mOnNewPointClickedListener = createObjectFragment;
        }
    }


    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }


    @Override
    public void onDestroy()
    {
        if (null != mGpsEventSource) {
            mGpsEventSource.removeListener(this);
        }

        super.onDestroy();
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

        mDistanceView.setText(CreateObjectFragment.getDistanceText(getActivity(), mDistance));
        mDistanceView.setTextColor(CreateObjectFragment.getDistanceTextColor(mDistance));

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
        setView(view, true);

        return super.onCreateDialog(savedInstanceState);
    }


    public interface OnRepeatClickedListener
    {
        void onRepeatClicked();
    }


    public interface OnNewPointClickedListener
    {
        void onNewPointClicked();
    }


    @Override
    public void onLocationChanged(Location location)
    {
    }


    @Override
    public void onBestLocationChanged(Location location)
    {
        float distance = CreateObjectFragment.getMinDistanceFromPrevPoints(
                getActivity(), mObjectLayerName, location);

        mDistanceView.setText(CreateObjectFragment.getDistanceText(getActivity(), distance));
        mDistanceView.setTextColor(CreateObjectFragment.getDistanceTextColor(distance));
    }


    @Override
    public void onGpsStatusChanged(int event)
    {
    }
}
