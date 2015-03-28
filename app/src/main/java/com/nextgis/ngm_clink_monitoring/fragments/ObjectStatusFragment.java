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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.nextgis.ngm_clink_monitoring.util.BitmapUtil;
import com.nextgis.ngm_clink_monitoring.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.nextgis.maplib.util.Constants.TAG;


public class ObjectStatusFragment
        extends Fragment
{
    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected Context mContext;

    protected TextView     mLineName;
    protected TextView     mObjectNameCaption;
    protected TextView     mObjectName;
    protected Button       mCompleteStatusButton;
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

    protected ObjectPhotoAdapter mObjectPhotoAdapter;
    protected Cursor mAttachesCursor;

    protected String mCurrentPhotoPath = null;


    public void setParams(
            Context context,
            Integer foclStructLayerType,
            Integer lineId,
            String lineName,
            String objectLayerName,
            Cursor objectCursor)
    {
        mContext = context;
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

            if (null == mObjectStatus) {
                mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;
            }

            objectCursor.close();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

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
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        MainActivity activity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_object_status, null);

        mLineName = (TextView) view.findViewById(R.id.line_name_st);
        mObjectNameCaption = (TextView) view.findViewById(R.id.object_name_caption_st);
        mObjectName = (TextView) view.findViewById(R.id.object_name);
        mCompleteStatusButton = (Button) view.findViewById(R.id.complete_status);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text);
        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo);
        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery);

        registerForContextMenu(mPhotoGallery);

        String toolbarTitle = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                toolbarTitle = activity.getString(R.string.cable_laying);
                mObjectNameCaption.setText(R.string.optical_cable_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                toolbarTitle = activity.getString(R.string.fosc_mounting);
                mObjectNameCaption.setText(R.string.fosc_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                toolbarTitle = activity.getString(R.string.cross_mounting);
                mObjectNameCaption.setText(R.string.cross_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                toolbarTitle = activity.getString(R.string.access_point_mounting);
                mObjectNameCaption.setText(R.string.access_point_colon);
                break;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                toolbarTitle = activity.getString(R.string.line_measuring);
                break;
        }

        activity.setBarsView(MainActivity.FT_OBJECT_STATUS, toolbarTitle);

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
            mObjectNameCaption.setVisibility(View.GONE);
            mObjectName.setVisibility(View.GONE);

        } else {
            mObjectNameCaption.setVisibility(View.VISIBLE);
            mObjectName.setVisibility(View.VISIBLE);
        }

        mCompleteStatusButton.setText(activity.getString(R.string.completed));
        mPhotoHintText.setText(R.string.take_photos_to_confirm);

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            mLineName.setText("");
            mObjectName.setText("");
            setStatusButtonView(false);
            mMakePhotoButton.setEnabled(false);
            mMakePhotoButton.setOnClickListener(null);
            mPhotoGallery.setEnabled(false);
            mPhotoGallery.setAdapter(null);
            setPhotoGalleryVisibility(false);
            return view;
        }


        mLineName.setText(mLineNameText);

        if (FoclConstants.LAYERTYPE_FOCL_ENDPOINT == mFoclStructLayerType) {
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

            if (null != objectCursor) {

                if (objectCursor.getCount() > 0) {
                    objectCursor.moveToFirst();

                    do {
                        String typeEndpoint = objectCursor.getString(
                                objectCursor.getColumnIndex(FoclConstants.FIELD_TYPE_ENDPOINT));

                        if (typeEndpoint.equals(FoclConstants.FIELD_VALUE_POINT_B)) {
                            mObjectId = objectCursor.getLong(
                                    objectCursor.getColumnIndex(VectorLayer.FIELD_ID));

                            mObjectStatus = objectCursor.getString(
                                    objectCursor.getColumnIndex(
                                            FoclConstants.FIELD_STATUS_MEASURE));

                            if (null == mObjectStatus) {
                                mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;
                            }

                            found = true;
                            break;
                        }
                    } while (objectCursor.moveToNext());
                }

                objectCursor.close();
            }


            setStatusButtonView(found);
            mMakePhotoButton.setEnabled(found);
            setPhotoGalleryVisibility(found);

        } else {
            mObjectName.setText(mObjectNameText);
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

        mCompleteStatusButton.setOnClickListener(statusButtonOnClickListener);

        mMakePhotoButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        // Ensure that there's a camera activity to handle the intent
                        if (null !=
                                cameraIntent.resolveActivity(getActivity().getPackageManager())) {

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
        mPhotoGallery.setHasFixedSize(true);
        setPhotoGalleryAdapter();

        setPhotoGalleryVisibility(true);

        return view;
    }


    @Override
    public void onDestroyView()
    {
        mAttachesCursor.close();
        super.onDestroyView();
    }


    public boolean onContextItemSelected(MenuItem menuItem)
    {
        final long itemId;

        try {
            itemId = ((ObjectPhotoAdapter) mPhotoGallery.getAdapter()).getSelectedItemId();

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return super.onContextItemSelected(menuItem);
        }

        switch (menuItem.getItemId()) {
            case R.id.menu_show_photo:
                showPhoto(itemId);
                break;

            case R.id.menu_delete_photo:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setIcon(R.drawable.ic_action_warning)
                        .setTitle(mContext.getResources().getString(R.string.delete_photo_ask))
                        .setMessage(
                                mContext.getResources().getString(R.string.delete_photo_message))
                        .setNegativeButton(mContext.getResources().getString(R.string.cancel), null)
                        .setPositiveButton(
                                mContext.getResources().getString(R.string.ok),

                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which)
                                    {
                                        deletePhoto(itemId);
                                    }
                                })
                        .show();
                break;
        }

        return super.onContextItemSelected(menuItem);
    }


    protected void showPhoto(long itemId)
    {
        Uri attachUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName +
                        "/" + mObjectId + "/attach/" + itemId);

        // get file path of photo file
        String proj[] = {VectorLayer.ATTACH_ID, VectorLayer.ATTACH_DATA};
        Cursor attachCursor = getActivity().getContentResolver().query(
                attachUri, proj, null, null, null);
        attachCursor.moveToFirst();
        int columnIndex = attachCursor.getColumnIndex(VectorLayer.ATTACH_DATA);

        // show photo in system program
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.parse("file://" + attachCursor.getString(columnIndex)), "image/*");
        startActivity(intent);
    }


    protected void deletePhoto(long itemId)
    {
        Uri deleteUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName +
                        "/" + mObjectId + "/attach/" + itemId);

        int result = getActivity().getContentResolver().delete(deleteUri, null, null);

        if (result == 0) {
            Log.d(TAG, "delete failed");
        } else {
            Log.d(TAG, "deleted " + result);
        }

        setPhotoGalleryAdapter();
        setPhotoGalleryVisibility(true);
    }


    protected void setStatusButtonView(boolean enabled)
    {
        if (enabled) {

            switch (mObjectStatus) {
                case FoclConstants.FIELD_VALUE_PROJECT:
                case FoclConstants.FIELD_VALUE_NOT_MEASURE:
                case FoclConstants.FIELD_VALUE_UNKNOWN:
                default:
                    mCompleteStatusButton.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_unchecked_500, 0);
                    break;

                case FoclConstants.FIELD_VALUE_BUILT:
                case FoclConstants.FIELD_VALUE_MEASURE:
                    mCompleteStatusButton.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_checked_500, 0);
                    break;
            }

        } else {
            mCompleteStatusButton.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_unchecked_500, 0);
        }

        mCompleteStatusButton.setEnabled(enabled);
    }


    private void setPhotoGalleryAdapter()
    {
        Uri attachesUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName + "/" +
                        mObjectId + "/attach");

        String proj[] = {VectorLayer.ATTACH_ID, VectorLayer.ATTACH_DISPLAY_NAME};
        String orderBy = VectorLayer.ATTACH_DISPLAY_NAME;

        mAttachesCursor =
                mContext.getContentResolver().query(attachesUri, proj, null, null, orderBy);

        mObjectPhotoAdapter = new ObjectPhotoAdapter(mContext, attachesUri, mAttachesCursor);

        mObjectPhotoAdapter.setOnPhotoClickListener(
                new ObjectPhotoAdapter.OnPhotoClickListener()
                {
                    @Override
                    public void onPhotoClick(long itemId)
                    {
                        showPhoto(itemId);
                    }
                });

        mPhotoGallery.setAdapter(mObjectPhotoAdapter);
    }


    protected void setPhotoGalleryVisibility(boolean visible)
    {
        if (visible) {
            if (mObjectPhotoAdapter.getItemCount() > 0) {
                mPhotoHintText.setVisibility(View.GONE);
                mPhotoGallery.setVisibility(View.VISIBLE);
            } else {
                mPhotoHintText.setVisibility(View.VISIBLE);
                mPhotoGallery.setVisibility(View.GONE);
            }
        } else {
            mPhotoHintText.setVisibility(View.GONE);
            mPhotoGallery.setVisibility(View.GONE);
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
            ContentResolver contentResolver = app.getContentResolver();
            File srcPhotoFile = new File(mCurrentPhotoPath);

            try {
                BitmapUtil.writeLocationToExif(srcPhotoFile, app.getCurrentLocation());
            } catch (IOException e) {
                // TODO: work of error
                e.printStackTrace();
            }

            Uri allAttachesUri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY +
                            "/" + mObjectLayerName + "/" + mObjectId + "/attach");

            ContentValues values = new ContentValues();
            values.put(VectorLayer.ATTACH_DISPLAY_NAME, srcPhotoFile.getName());
            values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");
            values.put(VectorLayer.ATTACH_DESCRIPTION, srcPhotoFile.getName());

            Uri attachUri = contentResolver.insert(allAttachesUri, values);

            if (null != attachUri) {

                try {
                    int exifOrientation = BitmapUtil.getOrientationFromExif(srcPhotoFile);

                    // resize and rotate
                    Bitmap sourceBitmap = BitmapFactory.decodeFile(srcPhotoFile.getPath());
                    Bitmap resizedBitmap = BitmapUtil.getResizedBitmap(
                            sourceBitmap, FoclConstants.PHOTO_MAX_SIZE_PX,
                            FoclConstants.PHOTO_MAX_SIZE_PX);
                    Bitmap rotatedBitmap = BitmapUtil.rotateBitmap(resizedBitmap, exifOrientation);

                    // jpeg compress
                    File tempPhotoFile = File.createTempFile("attach", null, app.getCacheDir());
                    OutputStream tempOutStream = new FileOutputStream(tempPhotoFile);
                    rotatedBitmap.compress(
                            Bitmap.CompressFormat.JPEG, FoclConstants.PHOTO_JPEG_COMPRESS_QUALITY,
                            tempOutStream);
                    tempOutStream.close();

                    // write EXIF to new file
                    BitmapUtil.copyExifData(srcPhotoFile, tempPhotoFile);

                    ExifInterface dstExif = new ExifInterface(tempPhotoFile.getCanonicalPath());

                    dstExif.setAttribute(
                            ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
                    dstExif.setAttribute(
                            ExifInterface.TAG_IMAGE_LENGTH, "" + rotatedBitmap.getHeight());
                    dstExif.setAttribute(
                            ExifInterface.TAG_IMAGE_WIDTH, "" + rotatedBitmap.getWidth());

                    dstExif.saveAttributes();

                    rotatedBitmap.recycle();

                    // attach data from tempPhotoFile
                    OutputStream attachOutStream = contentResolver.openOutputStream(attachUri);
                    FileUtil.copy(new FileInputStream(srcPhotoFile), attachOutStream);
                    attachOutStream.close();

                    tempPhotoFile.delete();

                } catch (IOException e) {
                    // TODO: work of error
                    e.printStackTrace();
                }

                Log.d(TAG, attachUri.toString());

            } else {
                Log.d(TAG, "insert attach failed");
            }

            setPhotoGalleryAdapter();
            setPhotoGalleryVisibility(true);
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

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                prefix = "Access_Point_Mounting_";
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
