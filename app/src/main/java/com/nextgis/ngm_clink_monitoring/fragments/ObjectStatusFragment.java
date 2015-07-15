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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectCursorAdapter;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectPhotoCursorAdapter;
import com.nextgis.ngm_clink_monitoring.dialogs.CoordinateRefiningDialog;
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

import static com.nextgis.maplib.util.Constants.FIELD_ID;
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
    protected String  mObjectLayerName;
    protected Long    mObjectId;

    protected String mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;

    protected ObjectPhotoCursorAdapter mObjectPhotoCursorAdapter;
    protected Cursor                   mAttachesCursor;

    protected String  mTempPhotoPath         = null;
    protected boolean mHasAccurateCoordinate = false;


    public void setParams(
            Context context,
            Integer lineId,
            Integer foclStructLayerType,
            Long objectId)
    {
        mContext = context;
        mLineId = lineId;
        mFoclStructLayerType = foclStructLayerType;
        mObjectId = objectId;
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
        View view = inflater.inflate(R.layout.fragment_object_status, null);

        mLineName = (TextView) view.findViewById(R.id.line_name_st);
        mObjectNameCaption = (TextView) view.findViewById(R.id.object_name_caption_st);
        mObjectName = (TextView) view.findViewById(R.id.object_name_st);
        mCompleteStatusButton = (Button) view.findViewById(R.id.complete_status_st);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text_st);
        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo_st);
        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery_st);

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

            case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
                toolbarTitle = activity.getString(R.string.special_transition_laying);
                break;
        }

        activity.setBarsView(toolbarTitle);

        mCompleteStatusButton.setText(activity.getString(R.string.completed));
        mPhotoHintText.setText(R.string.take_photos_to_confirm);

        final GISApplication app = (GISApplication) getActivity().getApplication();
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

        FoclVectorLayer layer = (FoclVectorLayer) foclStruct.getLayerByFoclType(
                mFoclStructLayerType);

        if (null == layer) {
            setBlockedView();
            return view;
        }

        if (null == mObjectId) {
            setBlockedView();
            return view;
        }


        mLineName.setText(Html.fromHtml(foclStruct.getHtmlFormattedName(false)));
        mObjectLayerName = layer.getPath().getName();

        Uri uri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                        mObjectLayerName + "/" + mObjectId);

        String proj[] = {
                FIELD_ID, FoclConstants.FIELD_NAME, FoclConstants.FIELD_STATUS_BUILT};

        Cursor objectCursor;

        try {
            objectCursor = getActivity().getContentResolver().query(uri, proj, null, null, null);

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            objectCursor = null;
        }

        if (null != objectCursor && objectCursor.getCount() == 1 &&
                objectCursor.moveToFirst()) {

            String objectNameText = ObjectCursorAdapter.getObjectName(mContext, objectCursor);
            mObjectStatus = objectCursor.getString(
                    objectCursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
            objectCursor.close();

            if (TextUtils.isEmpty(mObjectStatus)) {
                mObjectStatus = FoclConstants.FIELD_VALUE_UNKNOWN;
            }

            mObjectName.setText(objectNameText);
            setStatusButtonView(true);

        } else {
            setBlockedView();
            return view;
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

                    case FoclConstants.FIELD_VALUE_UNKNOWN:
                    default:
                        mObjectStatus = FoclConstants.FIELD_VALUE_BUILT;
                        break;
                }

                Uri uri = Uri.parse(
                        "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName);
                Uri updateUri = ContentUris.withAppendedId(uri, mObjectId);

                ContentValues values = new ContentValues();
                Calendar calendar = Calendar.getInstance();

                values.put(FoclConstants.FIELD_STATUS_BUILT, mObjectStatus);
                values.put(FoclConstants.FIELD_STATUS_BUILT_CH, calendar.getTimeInMillis());

                int result = 0;
                try {
                    result = getActivity().getContentResolver()
                            .update(updateUri, values, null, null);

                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }

                if (result == 0) {
                    Log.d(
                            TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                                    ", update FAILED");
                } else {
                    Log.d(
                            TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                                    ", update result: " + result);
                    setStatusButtonView(true);
                }
            }
        };

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            View customActionBarView = actionBar.getCustomView();
            View saveMenuItem = customActionBarView.findViewById(R.id.custom_toolbar_button_layout);
            saveMenuItem.setOnClickListener(statusButtonOnClickListener); // TODO: it is test
        }

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

                            try {
                                File tempFile = new File(app.getDataDir(), "temp-photo.jpg");

                                if (!tempFile.exists() && tempFile.createNewFile() ||
                                        tempFile.exists() && tempFile.delete() &&
                                                tempFile.createNewFile()) {

                                    mTempPhotoPath = tempFile.getAbsolutePath();

                                    cameraIntent.putExtra(
                                            MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                                }

                            } catch (IOException e) {
                                Toast.makeText(
                                        getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG)
                                        .show();
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


    protected void setBlockedView()
    {
        mLineName.setText("");
        mObjectName.setText("");
        setStatusButtonView(false);
        mMakePhotoButton.setEnabled(false);
        mMakePhotoButton.setOnClickListener(null);
        mPhotoGallery.setEnabled(false);
        mPhotoGallery.setAdapter(null);
        setPhotoGalleryVisibility(false);
    }


    @Override
    public void onDestroyView()
    {
        if (null != mAttachesCursor) {
            mAttachesCursor.close();
        }
        super.onDestroyView();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (!mHasAccurateCoordinate) {
            mHasAccurateCoordinate = true;
            DialogFragment coordRefiningDialog = new CoordinateRefiningDialog();
            coordRefiningDialog.show(
                    getActivity().getSupportFragmentManager(), "CoordinateRefining");
        }
    }


    public boolean onContextItemSelected(MenuItem menuItem)
    {
        final long itemId;

        try {
            itemId = ((ObjectPhotoCursorAdapter) mPhotoGallery.getAdapter()).getSelectedItemId();

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

        Cursor attachCursor;
        try {
            attachCursor = getActivity().getContentResolver().query(
                    attachUri, proj, null, null, null);

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return;
        }

        attachCursor.moveToFirst();
        String data = attachCursor.getString(attachCursor.getColumnIndex(VectorLayer.ATTACH_DATA));
        attachCursor.close();

        if (TextUtils.isEmpty(data)) {
            return;
        }

        // show photo in system program
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + data), "image/*");

        startActivity(intent);
    }


    protected void deletePhoto(long itemId)
    {
        Uri deleteUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName +
                        "/" + mObjectId + "/attach/" + itemId);

        int result = 0;
        try {
            result = getActivity().getContentResolver().delete(deleteUri, null, null);

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

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
                case FoclConstants.FIELD_VALUE_UNKNOWN:
                default:
                    mCompleteStatusButton.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_unchecked_500, 0);
                    break;

                case FoclConstants.FIELD_VALUE_BUILT:
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


    protected void setPhotoGalleryAdapter()
    {
        if (null != mAttachesCursor) {
            mAttachesCursor.close();
        }

        Uri attachesUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName + "/" +
                        mObjectId + "/attach");

        String proj[] = {VectorLayer.ATTACH_ID, VectorLayer.ATTACH_DISPLAY_NAME};
        String orderBy = VectorLayer.ATTACH_DISPLAY_NAME;

        try {
            mAttachesCursor =
                    mContext.getContentResolver().query(attachesUri, proj, null, null, orderBy);

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            mAttachesCursor = null;
            mObjectPhotoCursorAdapter = null;
        }

        if (null != mAttachesCursor) {
            mObjectPhotoCursorAdapter =
                    new ObjectPhotoCursorAdapter(mContext, attachesUri, mAttachesCursor);
            mObjectPhotoCursorAdapter.setOnPhotoClickListener(
                    new ObjectPhotoCursorAdapter.OnPhotoClickListener()
                    {
                        @Override
                        public void onPhotoClick(long itemId)
                        {
                            showPhoto(itemId);
                        }
                    });
        }

        mPhotoGallery.setAdapter(mObjectPhotoCursorAdapter);
    }


    protected void setPhotoGalleryVisibility(boolean visible)
    {
        if (visible) {

            if (null != mObjectPhotoCursorAdapter && mObjectPhotoCursorAdapter.getItemCount() > 0) {
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
        File tempPhotoFile = new File(mTempPhotoPath);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            GISApplication app = (GISApplication) getActivity().getApplication();
            ContentResolver contentResolver = app.getContentResolver();
            String photoFileName = getPhotoFileName();

            try {
                BitmapUtil.writeLocationToExif(tempPhotoFile, app.getCurrentLocation(), 0);
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            Uri allAttachesUri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY +
                            "/" + mObjectLayerName + "/" + mObjectId + "/attach");

            ContentValues values = new ContentValues();
            values.put(VectorLayer.ATTACH_DISPLAY_NAME, photoFileName);
            values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");
            //values.put(VectorLayer.ATTACH_DESCRIPTION, photoFileName);

            Uri attachUri = null;
            try {
                attachUri = contentResolver.insert(allAttachesUri, values);

            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            if (null != attachUri) {

                try {
                    int exifOrientation = BitmapUtil.getOrientationFromExif(tempPhotoFile);

                    // resize and rotate
                    Bitmap sourceBitmap = BitmapFactory.decodeFile(tempPhotoFile.getPath());
                    Bitmap resizedBitmap = BitmapUtil.getResizedBitmap(
                            sourceBitmap, FoclConstants.PHOTO_MAX_SIZE_PX,
                            FoclConstants.PHOTO_MAX_SIZE_PX);
                    Bitmap rotatedBitmap = BitmapUtil.rotateBitmap(resizedBitmap, exifOrientation);

                    // jpeg compress
                    File tempAttachFile = File.createTempFile("attach", null, app.getCacheDir());
                    OutputStream tempOutStream = new FileOutputStream(tempAttachFile);
                    rotatedBitmap.compress(
                            Bitmap.CompressFormat.JPEG, FoclConstants.PHOTO_JPEG_COMPRESS_QUALITY,
                            tempOutStream);
                    tempOutStream.close();

                    int newHeight = rotatedBitmap.getHeight();
                    int newWidth = rotatedBitmap.getWidth();

                    rotatedBitmap.recycle();

                    // write EXIF to new file
                    BitmapUtil.copyExifData(tempPhotoFile, tempAttachFile);

                    ExifInterface attachExif = new ExifInterface(tempAttachFile.getCanonicalPath());

                    attachExif.setAttribute(
                            ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
                    attachExif.setAttribute(
                            ExifInterface.TAG_IMAGE_LENGTH, "" + newHeight);
                    attachExif.setAttribute(
                            ExifInterface.TAG_IMAGE_WIDTH, "" + newWidth);

                    attachExif.saveAttributes();

                    // attach data from tempAttachFile
                    OutputStream attachOutStream = contentResolver.openOutputStream(attachUri);
                    FileUtil.copy(new FileInputStream(tempAttachFile), attachOutStream);
                    attachOutStream.close();

                    tempAttachFile.delete();

                } catch (IOException e) {
                    Toast.makeText(
                            getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                Log.d(TAG, attachUri.toString());

            } else {
                Log.d(TAG, "insert attach failed");
            }

            try {
                if (app.isOriginalPhotoSaving()) {
                    File origPhotoFile = new File(getDailyPhotoFolder(), photoFileName);

                    if (!com.nextgis.maplib.util.FileUtil.move(tempPhotoFile, origPhotoFile)) {
                        Toast.makeText(
                                getActivity(), "Save original photo failed", Toast.LENGTH_LONG)
                                .show();
                    }

                } else {
                    tempPhotoFile.delete();
                }

                setPhotoGalleryAdapter();
                setPhotoGalleryVisibility(true);

            } catch (IOException e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            tempPhotoFile.delete();
        }
    }


    protected String getPhotoFileName()
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

            case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
                prefix = "Special_Transition_Laying_";
                break;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return prefix + timeStamp + ".jpg";
    }


    protected File getDailyPhotoFolder()
            throws IOException
    {
        final GISApplication app = (GISApplication) getActivity().getApplication();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return FileUtil.getDirWithCreate(app.getPhotoPath() + File.separator + timeStamp);
    }
}
