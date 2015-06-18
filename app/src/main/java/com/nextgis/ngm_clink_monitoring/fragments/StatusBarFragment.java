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

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;

import java.text.DecimalFormat;


public class StatusBarFragment
        extends Fragment
        implements GpsEventListener
{
    protected TextView mLatView;
    protected TextView mLongView;
    //    protected TextView mAltView;
    protected TextView mAccView;
    protected TextView mDistView;

    protected String mLatText;
    protected String mLongText;
    protected String mAltText;
    protected String mAccText;
    protected String mDistText;

    protected long mLastLocationMillis;

    protected boolean mIsGPSFix = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setLocationDefaultText();
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_status_bar, null);

        mLatView = (TextView) view.findViewById(R.id.latitude_view);
        mLongView = (TextView) view.findViewById(R.id.longitude_view);
//        mAltView = (TextView) view.findViewById(R.id.altitude_view);
        mAccView = (TextView) view.findViewById(R.id.accuracy_view);
        mDistView = (TextView) view.findViewById(R.id.distance_view);

        setLocationViewsText();

        return view;
    }


    protected void setLocationDefaultText()
    {
        mLatText = "--";
        mLongText = "--";
        mAltText = "--";
        mAccText = "--";
        mDistText = "--";
    }


    protected void setLocationViewsText()
    {
        mLatView.setText(getString(R.string.latitude_caption) + " " + mLatText);
        mLongView.setText(getString(R.string.longitude_caption) + " " + mLongText);
//        mAltView.setText(mAltText);
        mAccView.setText(getString(R.string.accuracy_caption) + " " + mAccText);
        mDistView.setText(getString(R.string.distance_caption) + " " + mDistText);
    }


    @Override
    public void onLocationChanged(Location location)
    {
        if (location == null) {
            return;
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        app.setCurrentLocation(location);
        mLastLocationMillis = SystemClock.elapsedRealtime();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int nFormat = prefs.getInt(
                FoclSettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_DEGREES);

        mLatText = LocationUtil.formatLatitude(location.getLatitude(), nFormat, getResources()) +
                getString(R.string.coord_lat);

        mLongText = LocationUtil.formatLongitude(location.getLongitude(), nFormat, getResources()) +
                getString(R.string.coord_lon);

        DecimalFormat df = new DecimalFormat("0.0");

        double altitude = location.getAltitude();
        mAltText = df.format(altitude) + getString(R.string.altitude_unit);

        float accuracy = location.getAccuracy();
        mAccText = df.format(accuracy) + getString(R.string.accuracy_unit);

        // TODO: prevPointLocation
        Location prevPointLocation = new Location("");
        prevPointLocation.setLatitude(9);
        prevPointLocation.setLongitude(36);
        float distance = location.distanceTo(prevPointLocation);

        mDistText = df.format(distance) + getString(R.string.distance_unit);
        mDistView.setTextColor(
                distance > FoclConstants.MAX_DISTANCE_FROM_PREV_POINT ? 0xFF880000 : 0xFF008800);

        if (isVisible()) {
            setLocationViewsText();
        }
    }


    @Override
    public void onGpsStatusChanged(int event)
    {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                GISApplication app = (GISApplication) getActivity().getApplication();

                if (app.getCurrentLocation() != null) {
                    mIsGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 10000;
                }

                if (!mIsGPSFix) { // The fix has been lost.
                    setLocationDefaultText();
                    mDistView.setTextColor(Color.BLACK);

                    if (isVisible()) {
                        setLocationViewsText();
                    }
                }

                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                mIsGPSFix = true;
                break;
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();

        GISApplication app = (GISApplication) getActivity().getApplication();
        app.getGpsEventSource().addListener(this);
        setLocationViewsText();
    }


    @Override
    public void onStop()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();
        app.getGpsEventSource().removeListener(this);

        super.onStop();
    }
}
