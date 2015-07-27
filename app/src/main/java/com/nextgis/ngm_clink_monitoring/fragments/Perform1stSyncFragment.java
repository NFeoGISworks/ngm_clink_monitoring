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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;


public class Perform1stSyncFragment
        extends Fragment
        implements GISApplication.OnSyncLayerCountListener, GISApplication.OnSyncCurrentLayerListener
{
    protected ProgressBar mProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        MainActivity activity = (MainActivity) getActivity();
        GISApplication app = (GISApplication) activity.getApplication();

        activity.setBarsView(null);

        View view = inflater.inflate(R.layout.fragment_perform_sync, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.sync_progress_ps);

        mProgressBar.setMax(app.getSyncLayerCount());
        mProgressBar.setProgress(app.getSyncCurrentLayer());

        return view;
    }


    @Override
    public void onPause()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();
        app.setOnSyncLayerCountListener(null);
        app.setOnSyncCurrentLayerListener(null);
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        GISApplication app = (GISApplication) getActivity().getApplication();
        app.setOnSyncLayerCountListener(this);
        app.setOnSyncCurrentLayerListener(this);
    }


    @Override
    public void onSyncLayerCount(int layerCount)
    {
        mProgressBar.setMax(layerCount);
    }


    @Override
    public void onSyncCurrentLayer(int currentLayer)
    {
        mProgressBar.setProgress(currentLayer);
    }
}
