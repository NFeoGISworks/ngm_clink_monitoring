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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.LineNameAdapter;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class LineListFragment
        extends Fragment
{
    protected ListView mLineNameList;


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
        View view = inflater.inflate(R.layout.fragment_line_list, null);

        mLineNameList = (ListView) view.findViewById(R.id.line_list_ln);

        activity.setBarsView(null);

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
                        onLineClick(id);
                    }
                });

        return view;
    }


    public void onLineClick(long lineId)
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ObjectTypesFragment objectTypesFragment =
                (ObjectTypesFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_OBJECT_TYPES);

        if (null == objectTypesFragment) {
            objectTypesFragment = new ObjectTypesFragment();
        }

        objectTypesFragment.setParams(lineId);

        ft.replace(R.id.main_fragment, objectTypesFragment, FoclConstants.FRAGMENT_OBJECT_TYPES);
        ft.addToBackStack(null);
        ft.commit();
    }
}
