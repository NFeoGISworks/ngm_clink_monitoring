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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectPhotoFileAdapter;
import com.nextgis.ngm_clink_monitoring.dialogs.CoordinateRefiningDialog;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.BitmapUtil;
import com.nextgis.ngm_clink_monitoring.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;
import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;


public class CreateObjectFragment
        extends Fragment
{
    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected Context mContext;

    protected TextView mLineName;
    protected TextView mCoordinates;

    protected TextView mLayingMethodCaption;
    protected TextView mLayingMethod;

    protected TextView mFoscTypeCaption;
    protected TextView mFoscType;

    protected TextView mOpticalCrossTypeCaption;
    protected TextView mOpticalCrossType;

    protected TextView mSpecialLayingMethodCaption;
    protected TextView mSpecialLayingMethod;

    protected EditText     mDescription;
    protected TextView     mPhotoHintText;
    protected Button       mMakePhotoButton;
    protected RecyclerView mPhotoGallery;

    protected Integer mFoclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;

    protected Integer mLineId;
    protected String  mObjectLayerName;
    protected Long    mObjectId;

    protected Location mAccurateLocation;

    protected ObjectPhotoFileAdapter mObjectPhotoFileAdapter;

    protected String  mTempPhotoPath         = null;
    protected boolean mHasAccurateCoordinate = false;


    public void setParams(
            Context context,
            Integer lineId,
            Integer foclStructLayerType)
    {
        mContext = context;
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
        View view = inflater.inflate(R.layout.fragment_create_object, null);

        mLineName = (TextView) view.findViewById(R.id.line_name_cr);
        mCoordinates = (TextView) view.findViewById(R.id.coordinates_cr);

        mLayingMethodCaption = (TextView) view.findViewById(R.id.laying_method_caption_cr);
        mLayingMethod = (TextView) view.findViewById(R.id.laying_method_cr);

        mFoscTypeCaption = (TextView) view.findViewById(R.id.fosc_type_caption_cr);
        mFoscType = (TextView) view.findViewById(R.id.fosc_type_cr);

        mOpticalCrossTypeCaption = (TextView) view.findViewById(R.id.optical_cross_type_caption_cr);
        mOpticalCrossType = (TextView) view.findViewById(R.id.optical_cross_type_cr);

        mSpecialLayingMethodCaption =
                (TextView) view.findViewById(R.id.special_laying_method_caption_cr);
        mSpecialLayingMethod = (TextView) view.findViewById(R.id.special_laying_method_cr);

        mDescription = (EditText) view.findViewById(R.id.description_cr);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text_cr);
        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo_cr);
        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery_cr);

        MainActivity activity = (MainActivity) getActivity();

        setBarsView(activity);
        setFieldVisibility();
        setCoordinatesText();
        registerForContextMenu(mPhotoGallery);

        mPhotoHintText.setText(R.string.take_photos_to_confirm);

        final GISApplication app = (GISApplication) activity.getApplication();
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


        mLineName.setText(Html.fromHtml(foclStruct.getHtmlFormattedName()));
        mObjectLayerName = layer.getPath().getName();


        View.OnClickListener doneButtonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri uri = Uri.parse(
                        "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                                mObjectLayerName);

                ContentValues values = new ContentValues();

                values.put(FoclConstants.FIELD_DESCRIPTION, mDescription.getText().toString());

                Calendar calendar = Calendar.getInstance();
                values.put(FoclConstants.FIELD_BUILT_DATE, calendar.getTimeInMillis());

                try {
                    GeoPoint pt = new GeoPoint(
                            mAccurateLocation.getLongitude(), mAccurateLocation.getLatitude());
                    pt.setCRS(CRS_WGS84);
                    pt.project(CRS_WEB_MERCATOR);
                    GeoMultiPoint mpt = new GeoMultiPoint();
                    mpt.add(pt);
                    values.put(FIELD_GEOM, mpt.toBlob());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri result = getActivity().getContentResolver().insert(uri, values);
                if (result == null) {
                    Log.d(
                            TAG, "Layer: " + mObjectLayerName + ", insert FAILED");
                    Toast.makeText(
                            getActivity(), R.string.object_creation_error,
                            Toast.LENGTH_LONG).show();

                } else {
                    mObjectId = Long.parseLong(result.getLastPathSegment());
                    Log.d(
                            TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                                    ", insert result: " + result);
                    writePhotoAttaches();
                    getActivity().onBackPressed();
                }
            }
        };

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            View customActionBarView = actionBar.getCustomView();
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(doneButtonOnClickListener);
        }


        mMakePhotoButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showCameraActivity(app);
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
    public void onResume()
    {
        super.onResume();

        if (!mHasAccurateCoordinate) {
            mHasAccurateCoordinate = true;
            showCoordinateRefiningDialog();
        }
    }


    @Override
    public void onDestroy()
    {
        deleteTempFiles();
        super.onDestroy();
    }


    protected void setBarsView(MainActivity activity)
    {
        String toolbarTitle = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                toolbarTitle = activity.getString(R.string.cable_laying);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                toolbarTitle = activity.getString(R.string.fosc_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                toolbarTitle = activity.getString(R.string.cross_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                toolbarTitle = activity.getString(R.string.access_point_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                toolbarTitle = activity.getString(R.string.special_transition_laying);
                break;
        }

        activity.setBarsView(toolbarTitle);
    }


    protected void setBlockedView()
    {
        mLineName.setText("");

        mLayingMethod.setEnabled(false);
        mFoscType.setEnabled(false);
        mOpticalCrossType.setEnabled(false);
        mSpecialLayingMethod.setEnabled(false);

        mDescription.setText("");
        mDescription.setEnabled(false);
        mMakePhotoButton.setEnabled(false);
        mMakePhotoButton.setOnClickListener(null);
        mPhotoGallery.setEnabled(false);
        mPhotoGallery.setAdapter(null);
        setPhotoGalleryVisibility(false);
    }


    protected void setFieldVisibility()
    {
        mLayingMethodCaption.setVisibility(View.GONE);
        mLayingMethod.setVisibility(View.GONE);
        mFoscTypeCaption.setVisibility(View.GONE);
        mFoscType.setVisibility(View.GONE);
        mOpticalCrossTypeCaption.setVisibility(View.GONE);
        mOpticalCrossType.setVisibility(View.GONE);
        mSpecialLayingMethodCaption.setVisibility(View.GONE);
        mSpecialLayingMethod.setVisibility(View.GONE);

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                mLayingMethodCaption.setVisibility(View.VISIBLE);
                mLayingMethod.setVisibility(View.VISIBLE);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                mFoscTypeCaption.setVisibility(View.VISIBLE);
                mFoscType.setVisibility(View.VISIBLE);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                mOpticalCrossTypeCaption.setVisibility(View.VISIBLE);
                mOpticalCrossType.setVisibility(View.VISIBLE);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                mSpecialLayingMethodCaption.setVisibility(View.VISIBLE);
                mSpecialLayingMethod.setVisibility(View.VISIBLE);
                break;
        }
    }


    protected void setCoordinatesText()
    {
        if (null != mAccurateLocation) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    getActivity());

            int nFormat = prefs.getInt(
                    FoclSettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int",
                    Location.FORMAT_DEGREES);

            String latText = getString(R.string.latitude_caption) + " " +
                    LocationUtil.formatLatitude(
                            mAccurateLocation.getLatitude(), nFormat,
                            getResources()) +
                    getString(R.string.coord_lat);

            String longText = getString(R.string.longitude_caption) + " " +
                    LocationUtil.formatLongitude(
                            mAccurateLocation.getLongitude(), nFormat,
                            getResources()) +
                    getString(R.string.coord_lon);

            mCoordinates.setText(latText + ",  " + longText);
        }
    }


    protected void showCoordinateRefiningDialog()
    {
        CoordinateRefiningDialog coordRefiningDialog = new CoordinateRefiningDialog();

        coordRefiningDialog.setOnGetAccurateLocationListener(
                new CoordinateRefiningDialog.OnGetAccurateLocationListener()
                {
                    @Override
                    public void onGetAccurateLocation(Location accurateLocation)
                    {
                        if (null != accurateLocation) {
                            mAccurateLocation = accurateLocation;
                            setCoordinatesText();

                        } else {
                            Toast.makeText(
                                    getActivity(), R.string.coordinates_not_defined,
                                    Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        }
                    }
                });

        coordRefiningDialog.setCancelable(true); // TODO: true -> false
        coordRefiningDialog.show(
                getActivity().getSupportFragmentManager(), "CoordinateRefining");
    }


    public boolean onContextItemSelected(MenuItem menuItem)
    {
        final long itemId;

        try {
            itemId = ((ObjectPhotoFileAdapter) mPhotoGallery.getAdapter()).getSelectedItemId();

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
        // get file path of photo file
        ObjectPhotoFileAdapter adapter = (ObjectPhotoFileAdapter) mPhotoGallery.getAdapter();
        File photoFile = adapter.getItemPhotoFile((int) itemId);
        String absolutePath = photoFile.getAbsolutePath();

        if (TextUtils.isEmpty(absolutePath)) {
            return;
        }

        // show photo in system program
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + absolutePath), "image/*");

        startActivity(intent);
    }


    protected void deletePhoto(long itemId)
    {
        // get file path of photo file
        ObjectPhotoFileAdapter adapter = (ObjectPhotoFileAdapter) mPhotoGallery.getAdapter();
        File photoFile = adapter.getItemPhotoFile((int) itemId);

        photoFile.delete();

        setPhotoGalleryAdapter();
        setPhotoGalleryVisibility(true);
    }


    protected void setPhotoGalleryAdapter()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();

        try {
            mObjectPhotoFileAdapter = new ObjectPhotoFileAdapter(mContext, app.getDataDir());

            mObjectPhotoFileAdapter.setOnPhotoClickListener(
                    new ObjectPhotoFileAdapter.OnPhotoClickListener()
                    {
                        @Override
                        public void onPhotoClick(long itemId)
                        {
                            showPhoto(itemId);
                        }
                    });

        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
            mObjectPhotoFileAdapter = null;
        }

        mPhotoGallery.setAdapter(mObjectPhotoFileAdapter);
    }


    protected void setPhotoGalleryVisibility(boolean visible)
    {
        if (visible) {

            if (null != mObjectPhotoFileAdapter && mObjectPhotoFileAdapter.getItemCount() > 0) {
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


    protected void showCameraActivity(GISApplication app)
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (null != cameraIntent.resolveActivity(getActivity().getPackageManager())) {

            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File tempFile = new File(
                        app.getDataDir(),
                        FoclConstants.TEMP_PHOTO_FILE_PREFIX + timeStamp + ".jpg");

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
                        getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
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
            try {
                BitmapUtil.writeLocationToExif(tempPhotoFile, mAccurateLocation);
                setPhotoGalleryAdapter();
                setPhotoGalleryVisibility(true);

            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            tempPhotoFile.delete();
        }
    }


    protected void writePhotoAttach(File tempPhotoFile)
            throws IOException
    {
        GISApplication app = (GISApplication) getActivity().getApplication();
        ContentResolver contentResolver = app.getContentResolver();
        String photoFileName = getPhotoFileName(tempPhotoFile);

        Uri allAttachesUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY +
                        "/" + mObjectLayerName + "/" + mObjectId + "/attach");

        ContentValues values = new ContentValues();
        values.put(VectorLayer.ATTACH_DISPLAY_NAME, photoFileName);
        values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");
        //values.put(VectorLayer.ATTACH_DESCRIPTION, photoFileName);

        Uri attachUri = null;
        String insertAttachError = null;
        try {
            attachUri = contentResolver.insert(allAttachesUri, values);
            Log.d(TAG, attachUri.toString());
        } catch (Exception e) {
            Log.d(TAG, "Insert attach failed: " + e.getLocalizedMessage());
            insertAttachError = "Insert attach failed: " + e.getLocalizedMessage();
        }

        if (null != attachUri) {
            int exifOrientation = BitmapUtil.getOrientationFromExif(tempPhotoFile);

            // resize and rotate
            Bitmap sourceBitmap = BitmapFactory.decodeFile(tempPhotoFile.getPath());
            Bitmap resizedBitmap = BitmapUtil.getResizedBitmap(
                    sourceBitmap, FoclConstants.PHOTO_MAX_SIZE_PX, FoclConstants.PHOTO_MAX_SIZE_PX);
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
        }

        if (app.isOriginalPhotoSaving()) {
            File origPhotoFile = new File(getDailyPhotoFolder(), photoFileName);
            if (!com.nextgis.maplib.util.FileUtil.move(tempPhotoFile, origPhotoFile)) {
                throw new IOException(
                        "Save original photo failed, tempPhotoFile: " +
                                tempPhotoFile.getAbsolutePath());
            }
        } else {
            tempPhotoFile.delete();
        }

        if (null != insertAttachError) {
            throw new IOException(insertAttachError);
        }
    }


    protected void writePhotoAttaches()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();

        try {
            File dataDir = app.getDataDir();

            for (File tempPhotoFile : dataDir.listFiles()) {
                if (tempPhotoFile.getName().matches(
                        FoclConstants.TEMP_PHOTO_FILE_PREFIX + ".*\\.jpg")) {

                    writePhotoAttach(tempPhotoFile);
                }
            }

        } catch (IOException e) {
            String msg = "Write photo attaches failed, " + e.getLocalizedMessage();
            Log.d(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    }


    protected void deleteTempFiles()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();

        try {
            File dataDir = app.getDataDir();

            for (File tempPhotoFile : dataDir.listFiles()) {
                if (tempPhotoFile.getName().matches(
                        FoclConstants.TEMP_PHOTO_FILE_PREFIX + ".*\\.jpg")) {

                    tempPhotoFile.delete();
                }
            }

        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
            Toast.makeText(
                    getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }


    protected String getPhotoFileName(File tempPhotoFile)
            throws IOException
    {
        String prefix = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                prefix = "Optical_Cable_Laying_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                prefix = "FOSC_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                prefix = "Cross_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                prefix = "Access_Point_Mounting_";
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                prefix = "Special_Transition_Point_";
                break;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(
                BitmapUtil.getExifDate(tempPhotoFile));

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
