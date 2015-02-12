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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;
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
    private static final int REQUEST_TAKE_PHOTO = 1;

    protected TextView mWorkTypeName;
    protected TextView mLineName;
    protected TextView mObjectNameCaption;
    protected TextView mObjectName;
    protected TextView mStatusButtonNotBuilted;
    protected TextView mStatusButtonBuilted;
    protected TextView mPhotoHintText;
    protected Button   mMakePhotoButton;

    protected int mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;
    protected String mLineNameText;
    protected String mObjectLayerName;
    protected Cursor mObjectCursor;
    protected long   mObjectId;
    protected String mObjectNameText;
    protected String mObjectStatus;

    protected RecyclerView       mPhotoGallery;
    protected List<String>       mPhotoList;
    protected ObjectPhotoAdapter mObjectPhotoAdapter;

    protected String mCurrentPhotoPath = null;


    public void setParams(
            int foclStructLayerType,
            String lineName,
            String objectLayerName,
            Cursor objectCursor)
    {
        mFoclStructLayerType = foclStructLayerType;
        mLineNameText = lineName;
        mObjectLayerName = objectLayerName;
        mObjectCursor = objectCursor;
        mObjectId = objectCursor.getLong(objectCursor.getColumnIndex(VectorLayer.FIELD_ID));
        mObjectNameText = ObjectCursorAdapter.getObjectName(objectCursor);
        mObjectStatus = mObjectCursor.getString(
                mObjectCursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
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
        View view = inflater.inflate(R.layout.fragment_object_status, null);

        mWorkTypeName = (TextView) view.findViewById(R.id.work_type_name_st);
        mLineName = (TextView) view.findViewById(R.id.line_name);
        mObjectNameCaption = (TextView) view.findViewById(R.id.object_name_caption);
        mObjectName = (TextView) view.findViewById(R.id.object_name);
        mStatusButtonNotBuilted = (TextView) view.findViewById(R.id.status_not_builted);
        mStatusButtonBuilted = (TextView) view.findViewById(R.id.status_builted);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text);

        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery);

        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo);

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

            case FoclConstants.LAYERTYPE_FOCL_LINE_MEASURING:
                mWorkTypeName.setText(R.string.line_measuring);
                mObjectNameCaption.setVisibility(View.INVISIBLE);
                mObjectName.setVisibility(View.INVISIBLE);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            mLineName.setText("");
            mObjectName.setText("");
            mStatusButtonNotBuilted.setEnabled(false);
            mStatusButtonBuilted.setEnabled(false);
            mPhotoGallery.setEnabled(false);
            mPhotoGallery.setAdapter(null);
            mMakePhotoButton.setEnabled(false);
            mMakePhotoButton.setOnClickListener(null);
            return view;
        }

        mLineName.setText(mLineNameText);
        mObjectName.setText(mObjectNameText);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mPhotoGallery.setLayoutManager(layoutManager);
        mPhotoGallery.setAdapter(mObjectPhotoAdapter);
        mPhotoGallery.setHasFixedSize(true);

// TODO:
/*
        mCancelButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                });
*/

        setStatusButtonView();

        View.OnClickListener statusButtonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (mObjectStatus) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                    default:
                        mObjectStatus = FoclConstants.FIELD_VALUE_BUILT;
                        break;

                    case FoclConstants.FIELD_VALUE_BUILT:
                        mObjectStatus = FoclConstants.FIELD_VALUE_PROJECT;
                        break;
                }

                Uri uri = Uri.parse(
                        "content://" + FoclSettingsConstants.AUTHORITY + "/" + mObjectLayerName);
                Uri updateUri = ContentUris.withAppendedId(uri, mObjectId);

                ContentValues values = new ContentValues();
                values.put(FoclConstants.FIELD_STATUS_BUILT, mObjectStatus);

                Calendar calendar = Calendar.getInstance();
                values.put(FoclConstants.FIELD_STATUS_BUILT_CH, calendar.getTimeInMillis());

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
                    setStatusButtonView();
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

        return view;
    }


    protected void setStatusButtonView()
    {
        Drawable backgroundNotBuilted;
        Drawable backgroundBuilted;

        switch (mObjectStatus) {
            case FoclConstants.FIELD_VALUE_PROJECT:
            default:
                backgroundNotBuilted =
                        getActivity().getResources().getDrawable(R.drawable.border_status_red);
                backgroundBuilted =
                        getActivity().getResources().getDrawable(R.drawable.border_status_grey);
                break;

            case FoclConstants.FIELD_VALUE_BUILT:
                backgroundNotBuilted =
                        getActivity().getResources().getDrawable(R.drawable.border_status_grey);
                backgroundBuilted =
                        getActivity().getResources().getDrawable(R.drawable.border_status_green);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mStatusButtonNotBuilted.setBackground(backgroundNotBuilted);
            mStatusButtonBuilted.setBackground(backgroundBuilted);
        } else {
            mStatusButtonNotBuilted.setBackgroundDrawable(backgroundNotBuilted);
            mStatusButtonBuilted.setBackgroundDrawable(backgroundBuilted);
        }
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

            case FoclConstants.LAYERTYPE_FOCL_LINE_MEASURING:
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
