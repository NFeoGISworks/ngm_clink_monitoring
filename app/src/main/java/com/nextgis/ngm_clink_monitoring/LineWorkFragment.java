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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    protected String mCurrentPhotoPath = null;
    protected int    mWorkType         = MainActivity.UNKNOWN_WORK;

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

        switch (mWorkType) {
            case MainActivity.LAYING_WORK:
                mWorkTypeName.setText(R.string.construction_length_laying);
                mObjectCaption.setText(R.string.construction_length);
                mPhotoHintText.setText(R.string.take_photos_to_confirm);
                break;

            case MainActivity.MOUNTING_WORK:
                mWorkTypeName.setText(R.string.clutch_or_cross_mounting);
                mObjectCaption.setText(R.string.clutch_or_cross);
                mPhotoHintText.setText(R.string.take_photos_to_confirm_clutch);
                break;

            case MainActivity.MEASURING_WORK:
                mWorkTypeName.setText(R.string.construction_length_laying);
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

        switch (mWorkType) {
            case MainActivity.LAYING_WORK:
                prefix = "Laying_";
                break;

            case MainActivity.MOUNTING_WORK:
                prefix = "Mounting_";
                break;

            case MainActivity.MEASURING_WORK:
                prefix = "Measuring_";
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
        mWorkType = workType;
    }
}
