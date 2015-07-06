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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.AccurateLocationTaker;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.VectorCacheItem;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.adapters.ObjectPhotoFileAdapter;
import com.nextgis.ngm_clink_monitoring.dialogs.DistanceExceededDialog;
import com.nextgis.ngm_clink_monitoring.dialogs.YesNoDialog;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.BitmapUtil;
import com.nextgis.ngm_clink_monitoring.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;
import com.nextgis.ngm_clink_monitoring.util.ViewUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.nextgis.maplib.util.Constants.*;
import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;
import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.*;


public class CreateObjectFragment
        extends Fragment
        implements GpsEventListener
{
    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected Context mContext;

    protected TextView mTypeWorkTitle;
    protected TextView mLineName;

    protected RelativeLayout mRefiningLayout;
    protected ProgressBar    mRefiningProgress;
    protected TextView       mRefiningText;

    protected TextView mCoordinates;
    protected TextView mDistanceFromPrevPointCaption;
    protected TextView mDistanceFromPrevPoint;

    protected AccurateLocationTaker mAccurateLocationTaker;
    protected              int     mTakeCount                = 0;
    protected              int     mTakeCountPct             = 0;
    protected              int     mTakeTimePct              = 0;
    protected              int     mTakingLoopCount          = 0;
    protected static final int     MAX_PCT                   = 100;
    protected              Float   mDistance                 = null;
    protected              boolean mNewStartPoint            = false;
    protected              boolean mShowChangeLocationDialog = false;

    protected TextView        mLayingMethodCaption;
    protected ComboboxControl mLayingMethod;

    protected TextView        mFoscTypeCaption;
    protected ComboboxControl mFoscType;
    protected TextView        mFoscPlacementCaption;
    protected ComboboxControl mFoscPlacement;

    protected TextView        mOpticalCrossTypeCaption;
    protected ComboboxControl mOpticalCrossType;

    protected TextView        mSpecialLayingMethodCaption;
    protected ComboboxControl mSpecialLayingMethod;
    protected TextView        mMarkTypeCaption;
    protected ComboboxControl mMarkType;

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

    protected String mTempPhotoPath = null;

    protected GpsEventSource            mGpsEventSource;
    protected OnDistanceChangedListener mOnDistanceChangedListener;

    protected int mObjectCount;

    protected FoclProject     mFoclProject;
    protected FoclStruct      mFoclStruct;
    protected FoclVectorLayer mFoclVectorLayer;


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

        GISApplication app = (GISApplication) getActivity().getApplication();
        mGpsEventSource = app.getGpsEventSource();

        if (FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT == mFoclStructLayerType) {
            if (null != mGpsEventSource) {
                mGpsEventSource.addListener(this);
            }

            setObjectCount();
        }

        mAccurateLocationTaker = new AccurateLocationTaker(
                getActivity(), MAX_ACCURACY_TAKE_COUNT, MAX_ACCURACY_TAKE_TIME,
                ACCURACY_PUBLISH_PROGRESS_DELAY, ACCURACY_CIRCULAR_ERROR_STR);

        mAccurateLocationTaker.setTakeOnBestLocation(true);

        mAccurateLocationTaker.setOnGetCurrentAccurateLocationListener(
                new AccurateLocationTaker.OnGetCurrentAccurateLocationListener()
                {
                    @Override
                    public void onGetCurrentAccurateLocation(Location currentAccurateLocation)
                    {
                        if (MIN_ACCURACY_TAKE_COUNT <= mTakeCount &&
                                null != currentAccurateLocation &&
                                MAX_ACCURACY > currentAccurateLocation.getAccuracy()) {

                            mAccurateLocationTaker.stopTaking();
                        }
                    }
                });

        mAccurateLocationTaker.startTaking();
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_object, null);

        // Common
        mTypeWorkTitle = (TextView) view.findViewById(R.id.type_work_title_cr);
        mLineName = (TextView) view.findViewById(R.id.line_name_cr);

        setCoordinatesRefiningView(view);

        // Optical cable
        mLayingMethodCaption = (TextView) view.findViewById(R.id.laying_method_caption_cr);
        mLayingMethod = (ComboboxControl) view.findViewById(R.id.laying_method_cr);

        // FOSC
        mFoscTypeCaption = (TextView) view.findViewById(R.id.fosc_type_caption_cr);
        mFoscType = (ComboboxControl) view.findViewById(R.id.fosc_type_cr);
        mFoscPlacementCaption = (TextView) view.findViewById(R.id.fosc_placement_caption_cr);
        mFoscPlacement = (ComboboxControl) view.findViewById(R.id.fosc_placement_cr);

        // Optical cross
        mOpticalCrossTypeCaption = (TextView) view.findViewById(R.id.optical_cross_type_caption_cr);
        mOpticalCrossType = (ComboboxControl) view.findViewById(R.id.optical_cross_type_cr);

        // Access point
        // nothing

        // Special transition
        mSpecialLayingMethodCaption =
                (TextView) view.findViewById(R.id.special_laying_method_caption_cr);
        mSpecialLayingMethod = (ComboboxControl) view.findViewById(R.id.special_laying_method_cr);
        mMarkTypeCaption = (TextView) view.findViewById(R.id.mark_type_caption_cr);
        mMarkType = (ComboboxControl) view.findViewById(R.id.mark_type_cr);


        // Common
        mDescription = (EditText) view.findViewById(R.id.description_cr);
        mPhotoHintText = (TextView) view.findViewById(R.id.photo_hint_text_cr);
        mMakePhotoButton = (Button) view.findViewById(R.id.btn_make_photo_cr);
        mPhotoGallery = (RecyclerView) view.findViewById(R.id.photo_gallery_cr);

        MainActivity activity = (MainActivity) getActivity();

        setBarsView(activity);
        setTitleView(activity);
        setFieldVisibility();
        registerForContextMenu(mPhotoGallery);

        mPhotoHintText.setText(R.string.take_photos_to_confirm);

        final GISApplication app = (GISApplication) activity.getApplication();

        if (!setFoclProjectData(app)) {
            setBlockedView();
            return view;
        }

        setObjectCount();
        mLineName.setText(Html.fromHtml(mFoclStruct.getHtmlFormattedName()));
        setFieldDicts();


        View.OnClickListener doneButtonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewUtil.hideSoftKeyboard(getActivity());

                if (mAccurateLocationTaker.isTaking()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(getActivity().getString(R.string.warning))
                            .setMessage(R.string.coordinates_refining_process)
                            .setIcon(R.drawable.ic_action_warning)
                            .setPositiveButton(
                                    R.string.ok, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which)
                                        {
                                            // cancel
                                        }
                                    })
                            .show();

                } else if (null == mAccurateLocation) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(getActivity().getString(R.string.warning))
                            .setMessage(R.string.coordinates_not_defined_try_again)
                            .setIcon(R.drawable.ic_action_warning)
                            .setPositiveButton(
                                    R.string.repeat, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which)
                                        {
                                            startLocationTaking();
                                            dialog.dismiss();
                                        }
                                    })
                            .show();

                } else if (0 < mObjectCount &&
                        FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT ==
                                mFoclStructLayerType && null != mDistance &&
                        FoclConstants.MAX_DISTANCE_FROM_PREV_POINT < mDistance) {

                    showDistanceExceededDialog();

                } else {
                    createObject();
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
    public void onDestroyView()
    {
        mAccurateLocationTaker.setOnProgressUpdateListener(null);
        mAccurateLocationTaker.setOnGetAccurateLocationListener(null);
        super.onDestroyView();
    }


    @Override
    public void onDestroy()
    {
        mAccurateLocationTaker.stopTaking();

        if (FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT == mFoclStructLayerType) {
            if (null != mGpsEventSource) {
                mGpsEventSource.removeListener(this);
            }
        }

        deleteTempFiles();
        super.onDestroy();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (mShowChangeLocationDialog) {
            mShowChangeLocationDialog = false;
            showChangeLocationDialog();
        }
    }


    protected void setBarsView(MainActivity activity)
    {
        activity.setBarsView("");
        activity.switchMenuView();
    }


    protected void setTitleView(MainActivity activity)
    {
        String title = "";

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                title = activity.getString(R.string.cable_laying);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                title = activity.getString(R.string.fosc_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                title = activity.getString(R.string.cross_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                title = activity.getString(R.string.access_point_mounting);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                title = activity.getString(R.string.special_transition_laying);
                break;
        }

        mTypeWorkTitle.setText(title);
    }


    protected void setBlockedView()
    {
        mLineName.setText("");

        mLayingMethod.setEnabled(false);
        mFoscType.setEnabled(false);
        mFoscPlacement.setEnabled(false);
        mOpticalCrossType.setEnabled(false);
        mSpecialLayingMethod.setEnabled(false);
        mMarkType.setEnabled(false);

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
        mDistanceFromPrevPointCaption.setVisibility(View.GONE);
        mDistanceFromPrevPoint.setVisibility(View.GONE);
        mLayingMethodCaption.setVisibility(View.GONE);
        mLayingMethod.setVisibility(View.GONE);
        mFoscTypeCaption.setVisibility(View.GONE);
        mFoscType.setVisibility(View.GONE);
        mFoscPlacementCaption.setVisibility(View.GONE);
        mFoscPlacement.setVisibility(View.GONE);
        mOpticalCrossTypeCaption.setVisibility(View.GONE);
        mOpticalCrossType.setVisibility(View.GONE);
        mSpecialLayingMethodCaption.setVisibility(View.GONE);
        mSpecialLayingMethod.setVisibility(View.GONE);
        mMarkTypeCaption.setVisibility(View.GONE);
        mMarkType.setVisibility(View.GONE);

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                if (0 < mObjectCount) {
                    mDistanceFromPrevPointCaption.setVisibility(View.VISIBLE);
                    mDistanceFromPrevPoint.setVisibility(View.VISIBLE);
                }
                mLayingMethodCaption.setVisibility(View.VISIBLE);
                mLayingMethod.setVisibility(View.VISIBLE);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                mFoscTypeCaption.setVisibility(View.VISIBLE);
                mFoscType.setVisibility(View.VISIBLE);
                mFoscPlacementCaption.setVisibility(View.VISIBLE);
                mFoscPlacement.setVisibility(View.VISIBLE);
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
                mMarkTypeCaption.setVisibility(View.VISIBLE);
                mMarkType.setVisibility(View.VISIBLE);
                break;
        }
    }


    protected void setFieldDicts()
    {
        FoclDictItem dictItem;

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                dictItem = mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_LAYING_METHOD);
                mLayingMethod.setValues(dictItem);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                dictItem = mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_FOSC_TYPE);
                mFoscType.setValues(dictItem);

                dictItem = mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_FOSC_PLACEMENT);
                mFoscPlacement.setValues(dictItem);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                dictItem = mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_OPTICAL_CROSS_TYPE);
                mOpticalCrossType.setValues(dictItem);
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                dictItem =
                        mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_SPECIAL_LAYING_METHOD);
                mSpecialLayingMethod.setValues(dictItem);

                dictItem = mFoclProject.getFoclDitcs().get(FoclConstants.FIELD_MARK_TYPE);
                mMarkType.setValues(dictItem);
                break;
        }
    }


    protected void setCoordinatesRefiningView(View paretntView)
    {
        mRefiningLayout = (RelativeLayout) paretntView.findViewById(R.id.refining_layout_cr);
        mRefiningProgress = (ProgressBar) paretntView.findViewById(R.id.refining_progress_cr);
        mRefiningText = (TextView) paretntView.findViewById(R.id.refining_text_cr);

        mCoordinates = (TextView) paretntView.findViewById(R.id.coordinates_cr);
        mDistanceFromPrevPointCaption =
                (TextView) paretntView.findViewById(R.id.distance_from_prev_point_caption_cr);
        mDistanceFromPrevPoint =
                (TextView) paretntView.findViewById(R.id.distance_from_prev_point_cr);

        mRefiningProgress.setMax(MAX_PCT);
        mRefiningProgress.setSecondaryProgress(mTakeCountPct);
        mRefiningProgress.setProgress(mTakeTimePct);

        setCoordinatesText();
        setCoordinatesVisibility(!mAccurateLocationTaker.isTaking());

        mAccurateLocationTaker.setOnProgressUpdateListener(
                new AccurateLocationTaker.OnProgressUpdateListener()
                {
                    @Override
                    public void onProgressUpdate(Long... values)
                    {
                        mTakeCount = values[0].intValue();
                        mTakeCountPct = mTakeCount * MAX_PCT / MAX_ACCURACY_TAKE_COUNT;
                        mTakeTimePct = (int) (values[1] * MAX_PCT / MAX_ACCURACY_TAKE_TIME);

                        mRefiningProgress.setSecondaryProgress(mTakeCountPct);
                        mRefiningProgress.setProgress(mTakeTimePct);
                    }
                });

        mAccurateLocationTaker.setOnGetAccurateLocationListener(
                new AccurateLocationTaker.OnGetAccurateLocationListener()
                {
                    @Override
                    public void onGetAccurateLocation(
                            Location accurateLocation,
                            Long... values)
                    {
                        ++mTakingLoopCount;

                        if (null != accurateLocation) {
                            mAccurateLocation = accurateLocation;
                            setCoordinatesText();
                            setCoordinatesVisibility(true);

                        } else {
                            mAccurateLocation = null;

                            if (1 == mTakingLoopCount) {
                                if (CreateObjectFragment.this.isResumed()) {
                                    showChangeLocationDialog();
                                } else {
                                    mShowChangeLocationDialog = true;
                                }
                            }

                            startLocationTaking();
                        }
                    }
                });
    }


    protected void showChangeLocationDialog()
    {
        YesNoDialog changeLocationDialog = new YesNoDialog();
        changeLocationDialog.setKeepInstance(true)
                .setIcon(R.drawable.ic_action_warning)
                .setTitle(R.string.warning)
                .setMessage(
                        R.string.coordinates_not_defined_change_location)
                .setPositiveText(R.string.ok)
                .setOnPositiveClickedListener(
                        new YesNoDialog.OnPositiveClickedListener()
                        {
                            @Override
                            public void onPositiveClicked()
                            {
                                // close
                            }
                        })
                .show(
                        getActivity().getSupportFragmentManager(),
                        FoclConstants.FRAGMENT_YES_NO_DIALOG + "ChangeLocation");
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
                            mAccurateLocation.getLatitude(), nFormat, getResources()) +
                    getString(R.string.coord_lat);

            String longText = getString(R.string.longitude_caption) + " " +
                    LocationUtil.formatLongitude(
                            mAccurateLocation.getLongitude(), nFormat, getResources()) +
                    getString(R.string.coord_lon);

            mCoordinates.setText(latText + ",  " + longText);

            if (FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT == mFoclStructLayerType) {
                mDistance = getMinDistanceFromPrevPoints(mAccurateLocation);
                mDistanceFromPrevPoint.setText(getDistanceText(mDistance));
                mDistanceFromPrevPoint.setTextColor(getDistanceTextColor(mDistance));
            }

        } else {
            mDistance = null;
            mCoordinates.setText(getText(R.string.coordinates_not_defined));
            mDistanceFromPrevPoint.setText("--");
            mDistanceFromPrevPoint.setTextColor(
                    getResources().getColor(R.color.selected_object_text_color));
        }
    }


    protected void setCoordinatesVisibility(boolean isRefined)
    {
        if (isRefined) {
            mRefiningLayout.setVisibility(View.GONE);
            mCoordinates.setVisibility(View.VISIBLE);
        } else {
            mRefiningLayout.setVisibility(View.VISIBLE);
            mCoordinates.setVisibility(View.GONE);
        }
    }


    protected boolean setFoclProjectData(GISApplication app)
    {
        mFoclProject = app.getFoclProject();
        if (null == mFoclProject) {
            return false;
        }

        try {
            mFoclStruct = (FoclStruct) mFoclProject.getLayer(mLineId);
        } catch (Exception e) {
            mFoclStruct = null;
        }

        if (null == mFoclStruct) {
            return false;
        }

        mFoclVectorLayer = (FoclVectorLayer) mFoclStruct.getLayerByFoclType(
                mFoclStructLayerType);

        if (null == mFoclVectorLayer) {
            return false;
        }

        mObjectLayerName = mFoclVectorLayer.getPath().getName();
        return true;
    }


    protected void setObjectCount()
    {
        final GISApplication app = (GISApplication) getActivity().getApplication();
        mObjectCount = 0;

        if (setFoclProjectData(app)) {

            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mObjectLayerName);
            String proj[] = {FIELD_ID};

            Cursor objectCursor;

            try {
                objectCursor =
                        getActivity().getContentResolver().query(uri, proj, null, null, null);
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                objectCursor = null;
            }

            if (null != objectCursor) {
                mObjectCount = objectCursor.getCount();
                objectCursor.close();
            }
        }
    }


    protected void startLocationTaking()
    {
        mAccurateLocation = null;
        mDistance = null;

        mTakeCount = 0;
        mTakeCountPct = 0;
        mTakeTimePct = 0;
        mRefiningProgress.setSecondaryProgress(mTakeCountPct);
        mRefiningProgress.setProgress(mTakeTimePct);

        setCoordinatesText();
        setCoordinatesVisibility(false);

        mAccurateLocationTaker.startTaking();
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
            setPhotoGalleryAdapter();
            setPhotoGalleryVisibility(true);
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            tempPhotoFile.delete();
        }
    }


    protected void writePhotoAttach(File tempPhotoFile)
            throws IOException
    {
        BitmapUtil.writeLocationToExif(tempPhotoFile, mAccurateLocation);

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


    protected void createObject()
    {
        Uri uri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                        mObjectLayerName);

        ContentValues values = new ContentValues();

        values.put(FoclConstants.FIELD_DESCRIPTION, mDescription.getText().toString());

//        Calendar calendar = Calendar.getInstance();
//        values.put(FoclConstants.FIELD_BUILT_DATE, calendar.getTimeInMillis());
        values.put(FoclConstants.FIELD_BUILT_DATE, mAccurateLocation.getTime());

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

        switch (mFoclStructLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                if (0 == mObjectCount || mNewStartPoint) {
                    values.put(FIELD_START_POINT, true);
                }
                values.put(FIELD_LAYING_METHOD, mLayingMethod.getValue());
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                values.put(FIELD_FOSC_TYPE, mFoscType.getValue());
                values.put(FIELD_FOSC_PLACEMENT, mFoscPlacement.getValue());
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                values.put(FIELD_OPTICAL_CROSS_TYPE, mOpticalCrossType.getValue());
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                break;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                values.put(FIELD_SPECIAL_LAYING_METHOD, mSpecialLayingMethod.getValue());
                values.put(FIELD_MARK_TYPE, mMarkType.getValue());
                break;
        }

        Uri result = getActivity().getContentResolver().insert(uri, values);
        if (result == null) {
            Log.d(
                    TAG, "Layer: " + mObjectLayerName + ", insert FAILED");
            Toast.makeText(
                    getActivity(), R.string.object_creation_error, Toast.LENGTH_LONG).show();

        } else {

            if (mFoclStruct.getStatus().equals(FoclConstants.FIELD_VALUE_STATUS_PROJECT)) {
                mFoclStruct.setStatus(FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS);
                mFoclStruct.save();
            }

            mObjectId = Long.parseLong(result.getLastPathSegment());
            Log.d(
                    TAG, "Layer: " + mObjectLayerName + ", id: " + mObjectId +
                            ", insert result: " + result);
            writePhotoAttaches();
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
    }


    protected Float getMinDistanceFromPrevPoints(Location location)
    {
        List<VectorCacheItem> cache = mFoclVectorLayer.getVectorCache();
        Float minDist = null;

        if (0 < cache.size()) {

            int ii = 0;
            do {
                VectorCacheItem item = cache.get(ii);
                GeoMultiPoint mpt = (GeoMultiPoint) item.getGeoGeometry();

                if (0 < mpt.size()) {

                    int kk = 0;
                    do {
                        GeoPoint pt = new GeoPoint(mpt.get(kk));
                        pt.setCRS(GeoConstants.CRS_WEB_MERCATOR);
                        pt.project(GeoConstants.CRS_WGS84);

                        Location dstLocation = new Location("");
                        dstLocation.setLatitude(pt.getY());
                        dstLocation.setLongitude(pt.getX());

                        float dist = location.distanceTo(dstLocation);
                        minDist = null == minDist ? dist : Math.min(minDist, dist);

                        ++kk;
                    } while (kk < mpt.size());
                }

                ++ii;
            } while (ii < cache.size());
        }

        return minDist;
    }


    public String getDistanceText(Float distance)
    {
        if (null == distance) {
            return "--";
        }

        DecimalFormat df = new DecimalFormat("0");
        return df.format(distance) + " " + getString(R.string.distance_unit);
    }


    public int getDistanceTextColor(Float distance)
    {
        if (null == distance) {
            return 0xFF000000;
        }

        return FoclConstants.MAX_DISTANCE_FROM_PREV_POINT < distance ? 0xFF880000 : 0xFF008800;
    }


    protected void showDistanceExceededDialog()
    {
        DistanceExceededDialog distanceExceededDialog = new DistanceExceededDialog();
        distanceExceededDialog.setParams(this, mDistance);
        setOnOnDistanceChangedListener(distanceExceededDialog);

        distanceExceededDialog.setOnCancelListener(
                new DistanceExceededDialog.OnCancelListener()
                {
                    @Override
                    public void onCancel()
                    {
                        setOnOnDistanceChangedListener(null);
                    }
                });

        distanceExceededDialog.setOnRepeatClickedListener(
                new DistanceExceededDialog.OnRepeatClickedListener()
                {
                    @Override
                    public void onRepeatClicked()
                    {
                        setOnOnDistanceChangedListener(null);
                        startLocationTaking();
                    }
                });

        distanceExceededDialog.setOnNewPointClickedListener(
                new DistanceExceededDialog.OnNewPointClickedListener()
                {
                    @Override
                    public void onNewPointClicked()
                    {
                        setOnOnDistanceChangedListener(null);

                        YesNoDialog newPointDialog = new YesNoDialog();
                        newPointDialog.setKeepInstance(true)
                                .setIcon(R.drawable.ic_action_warning)
                                .setTitle(R.string.confirmation)
                                .setMessage(R.string.confirm_new_start_point_creating)
                                .setPositiveText(R.string.yes)
                                .setNegativeText(R.string.no)
                                .setOnPositiveClickedListener(
                                        new YesNoDialog.OnPositiveClickedListener()
                                        {
                                            @Override
                                            public void onPositiveClicked()
                                            {
                                                mNewStartPoint = true;
                                                createObject();
                                            }
                                        })
                                .setOnNegativeClickedListener(
                                        new YesNoDialog.OnNegativeClickedListener()
                                        {
                                            @Override
                                            public void onNegativeClicked()
                                            {
                                                // cancel
                                            }
                                        });

                        newPointDialog.show(
                                getActivity().getSupportFragmentManager(),
                                FoclConstants.FRAGMENT_YES_NO_DIALOG + "NewPointDialog");
                    }
                });

        distanceExceededDialog.show(
                getActivity().getSupportFragmentManager(),
                FoclConstants.FRAGMENT_DISTANCE_EXCEEDED);
    }


    @Override
    public void onLocationChanged(Location location)
    {
    }


    @Override
    public void onBestLocationChanged(Location location)
    {
        if (null != mOnDistanceChangedListener) {
            mOnDistanceChangedListener.onDistanceChanged(getMinDistanceFromPrevPoints(location));
        }
    }


    @Override
    public void onGpsStatusChanged(int event)
    {
    }


    public void setOnOnDistanceChangedListener(OnDistanceChangedListener onOnDistanceChangedListener)
    {
        mOnDistanceChangedListener = onOnDistanceChangedListener;
    }


    public interface OnDistanceChangedListener
    {
        void onDistanceChanged(float distance);
    }
}
