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

package com.nextgis.ngm_clink_monitoring;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;
import com.nextgis.ngm_clink_monitoring.util.SettingsConstants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.*;


public class LineWorkFragment
        extends Fragment
{
    private static final int REQUEST_TAKE_PHOTO = 1;

    protected TextView mWorkTypeName;
    protected TextView mObjectCaption;
    protected TextView mPhotoHintText;

    protected Spinner mLineName;
    protected Spinner mObjectName;

    @SuppressWarnings("deprecation")
    protected Gallery mPhotoGallery;

    protected Button mMakePhotoButton;
    protected Button mSaveButton;
    protected Button mCancelButton;

    protected String mCurrentPhotoPath    = null;
    protected int    mFoclStructLayerType = LAYERTYPE_FOCL_UNKNOWN;

    protected List<String> mPhotoList;
    protected ImageAdapter mImageAdapter;


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

        mImageAdapter = new ImageAdapter(getActivity(), mPhotoList);
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_line_work, null);

        mWorkTypeName = (TextView) view.findViewById(R.id.work_type_name);
        mObjectCaption = (TextView) view.findViewById(R.id.object_caption);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text);

        mLineName = (Spinner) view.findViewById(R.id.line_name);
        mObjectName = (Spinner) view.findViewById(R.id.object_name);

        mPhotoGallery = (Gallery) view.findViewById(R.id.photo_gallery);
        mPhotoGallery.setAdapter(mImageAdapter);

        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo);
        mSaveButton = (Button) view.findViewById(R.id.btn_save);
        mCancelButton = (Button) view.findViewById(R.id.btn_cancel);

        GISApplication app = (GISApplication) getActivity().getApplication();
        MapBase map = app.getMap();
        FoclProject foclProject = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            if (map.getLayer(i) instanceof FoclProject) {
                foclProject = (FoclProject) map.getLayer(i);
            }
        }

        final FoclProject foclProjectFin = foclProject;

        mLineName.setAdapter(new FoclProjectAdapter(getActivity(), foclProject));
        mLineName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id)
            {
                if (null == foclProjectFin) {
                    return;
                }

                FoclStruct foclStruct = (FoclStruct) foclProjectFin.getLayer(position);
                FoclVectorLayer layer =
                        (FoclVectorLayer) foclStruct.getLayerByFoclType(mFoclStructLayerType);

                Uri uri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/" +
                                    layer.getPath().getName());

                String proj[] = {VectorLayer.FIELD_ID, "name"};

                Cursor cursor = null;
                try {
                    cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (null == cursor) {
                    mObjectName.setAdapter(null);
                    mObjectName.setEnabled(false);
                    return;
                } else {
                    mObjectName.setEnabled(true);
                }

                getActivity().startManagingCursor(cursor);

                String from[] = {"name"};
                int to[] = {R.id.focl_layer_name};
                SimpleCursorAdapter adapter =
                        new SimpleCursorAdapter(getActivity(), R.layout.layout_focl_layer_row,
                                                cursor, from, to,
                                                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

                mObjectName.setAdapter(adapter);
                mObjectName.setSelection(0);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        mLineName.setSelection(0);

        mObjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id)
            {

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        switch (mFoclStructLayerType) {
            case LAYERTYPE_FOCL_OPTICAL_CABLE:
                mWorkTypeName.setText(R.string.optical_cable_laying);
                mObjectCaption.setText(R.string.optical_cable);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case LAYERTYPE_FOCL_FOSC:
                mWorkTypeName.setText(R.string.fosc_mounting);
                mObjectCaption.setText(R.string.fosc);
                mPhotoHintText.setText(R.string.take_photos_to_confirm_fosc);
                break;

            case LAYERTYPE_FOCL_OPTICAL_CROSS:
                mWorkTypeName.setText(R.string.cross_mounting);
                mObjectCaption.setText(R.string.cross);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case LAYERTYPE_FOCL_TELECOM_CABINET:
                mWorkTypeName.setText(R.string.telecom_cabinet_mounting);
                mObjectCaption.setText(R.string.telecom_cabinet);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case LAYERTYPE_FOCL_POLE:
                mWorkTypeName.setText(R.string.pole_mounting);
                mObjectCaption.setText(R.string.pole);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case LAYERTYPE_FOCL_LINE_MEASURING:
                mWorkTypeName.setText(R.string.line_measuring);
                mObjectCaption.setVisibility(View.INVISIBLE);
                mObjectName.setVisibility(View.INVISIBLE);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;
        }

        mMakePhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Ensure that there's a camera activity to handle the intent
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG)
                             .show();
                    }

                    if (photoFile != null) {
                        mCurrentPhotoPath = photoFile.getAbsolutePath();

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                    }
                }

            }
        });

        return view;
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
                LocationUtil.writeLocationToExif(new File(mCurrentPhotoPath),
                                                 app.getCurrentLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }

            mPhotoList.add(mCurrentPhotoPath);
            mImageAdapter.notifyDataSetChanged();
        }
    }


    private File createImageFile()
            throws IOException
    {
        String prefix = "";

        switch (mFoclStructLayerType) {
            case LAYERTYPE_FOCL_OPTICAL_CABLE:
                prefix = "Optical_Cable_Laying_";
                break;

            case LAYERTYPE_FOCL_FOSC:
                prefix = "FOSC_Mounting_";
                break;

            case LAYERTYPE_FOCL_OPTICAL_CROSS:
                prefix = "Cross_Mounting_";
                break;

            case LAYERTYPE_FOCL_TELECOM_CABINET:
                prefix = "Telecom_Cabinet_Mounting_";
                break;

            case LAYERTYPE_FOCL_POLE:
                prefix = "Pole_Mounting_";
                break;

            case LAYERTYPE_FOCL_LINE_MEASURING:
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


    public void setParams(int workType)
    {
        mFoclStructLayerType = workType;
    }
}
