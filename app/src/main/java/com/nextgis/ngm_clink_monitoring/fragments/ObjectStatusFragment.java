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

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectCursorAdapter;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectPhotoAdapter;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ObjectStatusFragment
        extends Fragment
{
    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected TextView     mWorkTypeName;
    protected TextView     mLineName;
    protected TextView     mObjectNameCaption;
    protected TextView     mObjectName;
    protected TextView     mStatusButtonNotBuilted;
    protected TextView     mStatusButtonBuilted;
    protected TextView     mPhotoHintText;
    protected Button       mMakePhotoButton;
    protected RecyclerView mPhotoGallery;

    protected Integer mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;

    protected Integer mLineId;
    protected String  mLineNameText;
    protected String  mObjectLayerName;
    protected Long    mObjectId;
    protected String  mObjectNameText;

    protected String mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;

    protected List<String>       mPhotoList;
    protected ObjectPhotoAdapter mObjectPhotoAdapter;

    protected String mCurrentPhotoPath = null;


    public void setParams(
            Integer foclStructLayerType,
            Integer lineId,
            String lineName,
            String objectLayerName,
            Cursor objectCursor)
    {
        mFoclStructLayerType = foclStructLayerType;
        mLineNameText = lineName;

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
            mLineId = lineId;

        } else {
            mObjectLayerName = objectLayerName;
            mObjectId = objectCursor.getLong(objectCursor.getColumnIndex(VectorLayer.FIELD_ID));
            mObjectNameText = ObjectCursorAdapter.getObjectName(objectCursor);
            mObjectStatus = objectCursor.getString(
                    objectCursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mPhotoList = new ArrayList<>();

        // TODO
/*
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getActivity(), "SDCard is not mounted", Toast.LENGTH_LONG).show();

        } else {
            File dataDir = new File(MainActivity.PHOTO_DIR_PATH);

            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File[] files = dataDir.listFiles();

            for (File file : files) {
                mPhotoList.add(file.getAbsolutePath());
            }
        }
*/

        mObjectPhotoAdapter = new ObjectPhotoAdapter(getActivity(), mPhotoList);
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

        View view = inflater.inflate(R.layout.fragment_object_status, null);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.object_status_toolbar);
        toolbar.getBackground().setAlpha(255);
        toolbar.setTitle(
                activity.getString(R.string.backward) + "  -  " + activity.getString(
                        R.string.set_status));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        activity.setSupportActionBar(toolbar);

        mWorkTypeName = (TextView) view.findViewById(R.id.work_type_name_st);
        mLineName = (TextView) view.findViewById(R.id.line_name_st);
        mObjectNameCaption = (TextView) view.findViewById(R.id.object_name_caption_st);
        mObjectName = (TextView) view.findViewById(R.id.object_name);
        mStatusButtonNotBuilted = (TextView) view.findViewById(R.id.status_not_builted);
        mStatusButtonBuilted = (TextView) view.findViewById(R.id.status_builted);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text);
        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo);
        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery);

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                mWorkTypeName.setText(R.string.cable_laying);
                mObjectNameCaption.setText(R.string.optical_cable);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                mWorkTypeName.setText(R.string.fosc_mounting);
                mObjectNameCaption.setText(R.string.fosc);
                mPhotoHintText.setText(R.string.take_photos_to_confirm_fosc);
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                mWorkTypeName.setText(R.string.cross_mounting);
                mObjectNameCaption.setText(R.string.cross);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case FoclConstants.LAYERTYPE_FOCL_TELECOM_CABINET:
                mWorkTypeName.setText(R.string.cabinet_mounting);
                mObjectNameCaption.setText(R.string.telecom_cabinet);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case FoclConstants.LAYERTYPE_FOCL_POLE:
                mWorkTypeName.setText(R.string.pole_mounting);
                mObjectNameCaption.setText(R.string.pole);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                mWorkTypeName.setText(R.string.line_measuring);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;
        }

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
            mObjectNameCaption.setVisibility(View.GONE);
            mObjectName.setVisibility(View.GONE);

        } else {
            mObjectNameCaption.setVisibility(View.VISIBLE);
            mObjectName.setVisibility(View.VISIBLE);
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            mLineName.setText("");
            mObjectName.setText("");
            mMakePhotoButton.setEnabled(false);
            mMakePhotoButton.setOnClickListener(null);
            mPhotoGallery.setEnabled(false);
            mPhotoGallery.setAdapter(null);
            setStatusButtonView(false);
            return view;
        }


        mLineName.setText(mLineNameText);

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
            mStatusButtonNotBuilted.setText(activity.getString(R.string.not_complete));
            mStatusButtonBuilted.setText(activity.getString(R.string.complete));

            FoclStruct foclStruct = (FoclStruct) foclProject.getLayer(mLineId);
            FoclVectorLayer layer = (FoclVectorLayer) foclStruct.getLayerByFoclType(
                    mFoclStructLayerType);

            mObjectLayerName = layer.getPath().getName();

            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName);

            String proj[] = {
                    VectorLayer.FIELD_ID,
                    FoclConstants.FIELD_TYPE_ENDPOINT,
                    FoclConstants.FIELD_STATUS_MEASURE};

            Cursor objectCursor =
                    getActivity().getContentResolver().query(uri, proj, null, null, null);

            boolean found = false;

            if (null != objectCursor && objectCursor.getCount() > 0) {
                objectCursor.moveToFirst();

                do {
                    String typeEndpoint = objectCursor.getString(
                            objectCursor.getColumnIndex(FoclConstants.FIELD_TYPE_ENDPOINT));

                    if (typeEndpoint.equals(FoclConstants.FIELD_VALUE_POINT_B)) {
                        mObjectId = objectCursor.getLong(
                                objectCursor.getColumnIndex(VectorLayer.FIELD_ID));

                        mObjectStatus = objectCursor.getString(
                                objectCursor.getColumnIndex(FoclConstants.FIELD_STATUS_MEASURE));

                        if (null == mObjectStatus) {
                            mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;
                        }

                        found = true;
                        break;
                    }
                } while (objectCursor.moveToNext());
            }

            setStatusButtonView(found);

        } else {
            mObjectName.setText(mObjectNameText);
            mStatusButtonNotBuilted.setText(activity.getString(R.string.not_builted));
            mStatusButtonBuilted.setText(activity.getString(R.string.builted));
            setStatusButtonView(true);
        }

        View.OnClickListener statusButtonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (mObjectStatus) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                        mObjectStatus = FoclConstants.FIELD_VALUE_BUILT;
                        break;

                    case FoclConstants.FIELD_VALUE_BUILT:
                        mObjectStatus = FoclConstants.FIELD_VALUE_PROJECT;
                        break;

                    case FoclConstants.FIELD_VALUE_NOT_MEASURE:
                        mObjectStatus = FoclConstants.FIELD_VALUE_MEASURE;
                        break;

                    case FoclConstants.FIELD_VALUE_MEASURE:
                        mObjectStatus = FoclConstants.FIELD_VALUE_NOT_MEASURE;
                        break;

                    case FoclConstants.FIELD_VALUE_UNKNOWN:
                    default:
                        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
                            mObjectStatus = FoclConstants.FIELD_VALUE_MEASURE;
                        } else {
                            mObjectStatus = FoclConstants.FIELD_VALUE_BUILT;
                        }
                        break;
                }

                Uri uri = Uri.parse(
                        "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName);
                Uri updateUri = ContentUris.withAppendedId(uri, mObjectId);

                ContentValues values = new ContentValues();
                Calendar calendar = Calendar.getInstance();

                if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
                    values.put(FoclConstants.FIELD_STATUS_MEASURE, mObjectStatus);
                    values.put(FoclConstants.FIELD_STATUS_MEASURE_CH, calendar.getTimeInMillis());

                } else {
                    values.put(FoclConstants.FIELD_STATUS_BUILT, mObjectStatus);
                    values.put(FoclConstants.FIELD_STATUS_BUILT_CH, calendar.getTimeInMillis());
                }

                int result =
                        getActivity().getContentResolver().update(updateUri, values, null, null);

                if (result == 0) {
                    Log.d(
                            Constants.TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                                           ", update FAILED");
                } else {
                    Log.d(
                            Constants.TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                                           ", update result: " + result);
                    setStatusButtonView(true);
                }
            }
        };

        mStatusButtonNotBuilted.setOnClickListener(statusButtonOnClickListener);
        mStatusButtonBuilted.setOnClickListener(statusButtonOnClickListener);

        mMakePhotoButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        // Ensure that there's a camera activity to handle the intent
                        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) !=
                            null) {
                            File photoFile = null;

                            try {
                                photoFile = createImageFile();
                            } catch (IOException e) {
                                Toast.makeText(
                                        getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }

                            if (photoFile != null) {
                                mCurrentPhotoPath = photoFile.getAbsolutePath();

                                cameraIntent.putExtra(
                                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                            }
                        }
                    }
                });

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mPhotoGallery.setLayoutManager(layoutManager);
        mPhotoGallery.setAdapter(mObjectPhotoAdapter);
        mPhotoGallery.setHasFixedSize(true);

        return view;
    }


    protected void setStatusButtonView(boolean enabled)
    {
        Drawable backgroundNotBuilted;
        Drawable backgroundBuilted;

        if (enabled) {

            switch (mObjectStatus) {
                case FoclConstants.FIELD_VALUE_PROJECT:
                case FoclConstants.FIELD_VALUE_NOT_MEASURE:
                case FoclConstants.FIELD_VALUE_UNKNOWN:
                default:
                    backgroundNotBuilted =
                            getActivity().getResources().getDrawable(R.drawable.border_status_red);
                    backgroundBuilted =
                            getActivity().getResources().getDrawable(R.drawable.border_status_grey);
                    break;

                case FoclConstants.FIELD_VALUE_BUILT:
                case FoclConstants.FIELD_VALUE_MEASURE:
                    backgroundNotBuilted =
                            getActivity().getResources().getDrawable(R.drawable.border_status_grey);
                    backgroundBuilted = getActivity().getResources()
                            .getDrawable(R.drawable.border_status_green);
                    break;
            }

        } else {
            backgroundNotBuilted =
                    getActivity().getResources().getDrawable(R.drawable.border_status_grey);
            backgroundBuilted =
                    getActivity().getResources().getDrawable(R.drawable.border_status_grey);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mStatusButtonNotBuilted.setBackground(backgroundNotBuilted);
            mStatusButtonBuilted.setBackground(backgroundBuilted);
        } else {
            mStatusButtonNotBuilted.setBackgroundDrawable(backgroundNotBuilted);
            mStatusButtonBuilted.setBackgroundDrawable(backgroundBuilted);
        }

        mStatusButtonNotBuilted.setEnabled(enabled);
        mStatusButtonBuilted.setEnabled(enabled);
    }


    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            GISApplication app = (GISApplication) getActivity().getApplication();

            try {
                LocationUtil.writeLocationToExif(
                        new File(mCurrentPhotoPath), app.getCurrentLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }

            mPhotoList.add(mCurrentPhotoPath);
            mObjectPhotoAdapter.notifyDataSetChanged();
        }
    }


    private File createImageFile()
            throws IOException
    {
        String prefix = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                prefix = "Optical_Cable_Laying_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                prefix = "FOSC_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                prefix = "Cross_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_TELECOM_CABINET:
                prefix = "Telecom_Cabinet_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_POLE:
                prefix = "Pole_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                prefix = "Line_Measuring_";
                break;
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dataDir = new File(MainActivity.PHOTO_DIR_PATH + File.separator + timeStamp);

        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = prefix + timeStamp + ".jpg";
        File emptyFile = new File(dataDir, imageFileName);
        emptyFile.createNewFile();

        return emptyFile;
    }
}
