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
 ******************************************************************************/

package com.nextgis.ngm_clink_monitoring.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.adapters.LineNameAdapter;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class LineListFragment
        extends Fragment
{
    protected ListView mLineNameList;

    protected Integer mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;

    protected Integer mLineId;
    protected String  mLineNameText;


    public void setParams(Integer foclStructLayerType)
    {
        mFoclStructLayerType = foclStructLayerType;
    }


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
        ActionBarActivity activity = (ActionBarActivity) getActivity();

        ViewGroup rootView =
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Toolbar typesToolbar = (Toolbar) rootView.findViewById(R.id.object_types_toolbar);
        typesToolbar.setVisibility(View.GONE);

        View view = inflater.inflate(R.layout.fragment_line_list, null);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.line_list_toolbar);
        toolbar.getBackground().setAlpha(255);
        toolbar.setTitle(""); // needed for screen rotation
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        activity.setSupportActionBar(toolbar);

        mLineNameList = (ListView) view.findViewById(R.id.line_list_ln);

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                toolbar.setTitle(activity.getString(R.string.cable_laying));
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                toolbar.setTitle(activity.getString(R.string.fosc_mounting));
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                toolbar.setTitle(activity.getString(R.string.cross_mounting));
                break;

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                toolbar.setTitle(activity.getString(R.string.access_point_mounting));
                break;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                toolbar.setTitle(activity.getString(R.string.line_measuring));
                break;
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            mLineNameList.setEnabled(false);
            mLineNameList.setAdapter(null);
            return view;
        }


        LineNameAdapter lineNameAdapter = new LineNameAdapter(getActivity(), foclProject);
        mLineNameList.setAdapter(lineNameAdapter);
        mLineNameList.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id)
                    {
                        mLineId = (int) id;
                        FoclStruct foclStruct = (FoclStruct) foclProject.getLayer(mLineId);
                        mLineNameText = foclStruct.getName();
                        onLineClick();
                    }
                });

        return view;
    }


    public void onLineClick()
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
            ObjectStatusFragment objectMeasureFragment =
                    (ObjectStatusFragment) fm.findFragmentByTag("ObjectMeasure");

            if (objectMeasureFragment == null) {
                objectMeasureFragment = new ObjectStatusFragment();
            }

            objectMeasureFragment.setParams(
                    mFoclStructLayerType, mLineId, mLineNameText, null, null);

            ft.replace(R.id.object_fragment, objectMeasureFragment, "ObjectMeasure");

        } else {
            ObjectListFragment objectListFragment =
                    (ObjectListFragment) fm.findFragmentByTag("ObjectList");

            if (objectListFragment == null) {
                objectListFragment = new ObjectListFragment();
            }

            objectListFragment.setParams(mFoclStructLayerType, mLineId);

            ft.replace(R.id.object_fragment, objectListFragment, "ObjectList");
        }

        ft.addToBackStack(null);
        ft.commit();
    }
}
