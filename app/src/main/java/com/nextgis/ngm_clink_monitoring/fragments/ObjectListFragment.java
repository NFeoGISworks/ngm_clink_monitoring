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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.adapters.LineNameAdapter;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectCursorAdapter;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;


public class ObjectListFragment
        extends Fragment
{
    protected TextView mWorkTypeName;
    protected TextView mObjectListCaption;

    protected Spinner  mLineNameSpinner;
    protected ListView mObjectList;

    protected int mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;


    public void setParams(int foclStructLayerType)
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

        View view = inflater.inflate(R.layout.fragment_object_list, null);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.object_list_toolbar);
        toolbar.getBackground().setAlpha(255);
        toolbar.setTitle(activity.getString(R.string.backward) + "  -  " + activity.getTitle());
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        activity.setSupportActionBar(toolbar);

        mWorkTypeName = (TextView) view.findViewById(R.id.work_type_name);
        mObjectListCaption = (TextView) view.findViewById(R.id.object_list_caption);

        mLineNameSpinner = (Spinner) view.findViewById(R.id.line_name_spinner);
        mObjectList = (ListView) view.findViewById(R.id.object_list);

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                mWorkTypeName.setText(R.string.cable_laying);
                mObjectListCaption.setText(R.string.optical_cables);
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                mWorkTypeName.setText(R.string.fosc_mounting);
                mObjectListCaption.setText(R.string.foscs);
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                mWorkTypeName.setText(R.string.cross_mounting);
                mObjectListCaption.setText(R.string.crosses);
                break;

            case FoclConstants.LAYERTYPE_FOCL_TELECOM_CABINET:
                mWorkTypeName.setText(R.string.cabinet_mounting);
                mObjectListCaption.setText(R.string.telecom_cabinets);
                break;

            case FoclConstants.LAYERTYPE_FOCL_POLE:
                mWorkTypeName.setText(R.string.pole_mounting);
                mObjectListCaption.setText(R.string.poles);
                break;

            case FoclConstants.LAYERTYPE_FOCL_LINE_MEASURING:
                mWorkTypeName.setText(R.string.line_measuring);
                mObjectListCaption.setVisibility(View.INVISIBLE);
                mObjectList.setVisibility(View.INVISIBLE);
                break;
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            mLineNameSpinner.setEnabled(false);
            mLineNameSpinner.setAdapter(null);
            mObjectList.setEnabled(false);
            mObjectList.setAdapter(null);
            return view;
        }

        final String[] lineName = {null};
        final String[] objectLayerName = {null};

        LineNameAdapter projectAdapter = new LineNameAdapter(getActivity(), foclProject);

        mLineNameSpinner.setAdapter(projectAdapter);
        mLineNameSpinner.setSelection(0);
        mLineNameSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id)
                    {
                        FoclStruct foclStruct = (FoclStruct) foclProject.getLayer(position);
                        lineName[0] = foclStruct.getName();

                        FoclVectorLayer layer = (FoclVectorLayer) foclStruct.getLayerByFoclType(
                                mFoclStructLayerType);
                        objectLayerName[0] = layer.getPath().getName();

                        Uri uri = Uri.parse(
                                "content://" + FoclSettingsConstants.AUTHORITY + "/" +
                                layer.getPath().getName());

                        String proj[] = {
                                VectorLayer.FIELD_ID,
                                FoclConstants.FIELD_NAME,
                                FoclConstants.FIELD_STATUS_BUILT};

                        Cursor cursor = getActivity().getContentResolver()
                                .query(uri, proj, null, null, null);

                        if (cursor.getCount() > 0) {
                            mObjectList.setEnabled(true);
                        } else {
                            mObjectList.setAdapter(null);
                            mObjectList.setEnabled(false);
                            return;
                        }

                        ObjectCursorAdapter cursorAdapter = new ObjectCursorAdapter(
                                getActivity(), cursor,
                                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

                        mObjectList.setAdapter(cursorAdapter);
                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });

        mObjectList.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id)
                    {
                        Cursor cursor = (Cursor) mObjectList.getAdapter().getItem(position);
                        OnObjectClick(
                                mFoclStructLayerType, lineName[0], objectLayerName[0], cursor);
                    }
                });

        return view;
    }


    public void OnObjectClick(
            int foclStructLayerType,
            String lineName,
            String objectLayerName,
            Cursor objectCursor)
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();

        ObjectStatusFragment objectStatusFragment =
                (ObjectStatusFragment) fm.findFragmentByTag("ObjectStatus");

        if (objectStatusFragment == null) {
            objectStatusFragment = new ObjectStatusFragment();
        }

        objectStatusFragment.setParams(
                foclStructLayerType, lineName, objectLayerName, objectCursor);

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.object_fragment, objectStatusFragment, "ObjectStatus");
        ft.addToBackStack(null);
        ft.commit();
        fm.executePendingTransactions();
    }
}
