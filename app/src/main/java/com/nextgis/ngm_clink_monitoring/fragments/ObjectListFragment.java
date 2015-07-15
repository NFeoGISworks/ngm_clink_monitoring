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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectCursorAdapter;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import static com.nextgis.maplib.util.Constants.FIELD_ID;
import static com.nextgis.maplib.util.Constants.TAG;


public class ObjectListFragment
        extends Fragment
{
    protected TextView mLineName;
    protected TextView mObjectListCaption;
    protected ListView mObjectList;

    protected Integer mLineId;
    protected String  mObjectLayerName;
    protected Long    mObjectId;
    protected Cursor  mAdapterCursor;

    protected Integer mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;


    public void setParams(
            Integer lineId,
            Integer foclStructLayerType)
    {
        mLineId = lineId;
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
        MainActivity activity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_object_list, null);

        mLineName = (TextView) view.findViewById(R.id.line_name_ls);
        mObjectListCaption = (TextView) view.findViewById(R.id.object_list_caption_ls);
        mObjectList = (ListView) view.findViewById(R.id.object_list_ls);

        String toolbarTitle = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                toolbarTitle = activity.getString(R.string.cable_laying);
                mObjectListCaption.setText(R.string.select_optical_cables_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                toolbarTitle = activity.getString(R.string.fosc_mounting);
                mObjectListCaption.setText(R.string.select_fosc_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                toolbarTitle = activity.getString(R.string.cross_mounting);
                mObjectListCaption.setText(R.string.select_cross_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                toolbarTitle = activity.getString(R.string.access_point_mounting);
                mObjectListCaption.setText(R.string.select_access_points_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_UNKNOWN:
                // TODO: for FoclConstants.LAYERTYPE_FOCL_UNKNOWN
                break;
        }

        activity.setBarsView(toolbarTitle);

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            setBlockedView();
            return view;
        }

        FoclStruct foclStruct;
        try {
            foclStruct = (FoclStruct) foclProject.getLayer(mLineId);
        } catch (Exception e) {
            foclStruct = null;
        }

        if (null == foclStruct) {
            setBlockedView();
            return view;
        }

        FoclVectorLayer layer =
                (FoclVectorLayer) foclStruct.getLayerByFoclType(mFoclStructLayerType);

        if (null == layer) {
            setBlockedView();
            return view;
        }

        mLineName.setText(Html.fromHtml(foclStruct.getHtmlFormattedName(false)));
        mObjectLayerName = layer.getPath().getName();

        Uri uri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName);

        String proj[] = {FIELD_ID, FoclConstants.FIELD_NAME, FoclConstants.FIELD_STATUS_BUILT};

        try {
            mAdapterCursor = getActivity().getContentResolver().query(uri, proj, null, null, null);

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            mAdapterCursor = null;
        }

        if (null != mAdapterCursor && mAdapterCursor.getCount() > 0) {
            mObjectList.setEnabled(true);
        } else {
            setBlockedView();
            return view;
        }


        ObjectCursorAdapter cursorAdapter = new ObjectCursorAdapter(
                getActivity(), mAdapterCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mObjectList.setAdapter(cursorAdapter);
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
                        mObjectId = cursor.getLong(cursor.getColumnIndex(FIELD_ID));
                        cursor.close();
                        onObjectClick();
                    }
                });

        return view;
    }


    protected void setBlockedView()
    {
        mLineName.setText("");
        mObjectList.setEnabled(false);
        mObjectList.setAdapter(null);
    }


    @Override
    public void onDestroyView()
    {
        if (null != mAdapterCursor) {
            mAdapterCursor.close();
        }

        super.onDestroyView();
    }


    public void onObjectClick()
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ObjectStatusFragment objectStatusFragment =
                (ObjectStatusFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_OBJECT_STATUS);

        if (objectStatusFragment == null) {
            objectStatusFragment = new ObjectStatusFragment();
        }

        objectStatusFragment.setParams(getActivity(), mLineId, mFoclStructLayerType, mObjectId);

        ft.replace(R.id.main_fragment, objectStatusFragment, FoclConstants.FRAGMENT_OBJECT_STATUS);
        ft.addToBackStack(null);
        ft.commit();
    }
}
