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

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.mapui.MapViewOverlays;
import com.nextgis.maplibui.overlay.CurrentLocationOverlay;
import com.nextgis.maplibui.util.ConstantsUI;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.dialogs.AttributesDialog;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;


public class MapFragment
        extends Fragment
        implements MapViewEventListener, GpsEventListener
{
    protected final static int mMargins = 10;
    protected float mTolerancePX;

    protected MapViewOverlays mMapView;
    protected ImageView       mivZoomIn;
    protected ImageView       mivZoomOut;

    protected RelativeLayout mMapRelativeLayout;
    protected RelativeLayout mButtonsRelativeLayout;
    protected final int mButtonsRelativeLayoutId = ViewUtil.generateViewId();

    protected GeoPoint               mCurrentCenter;
    protected GpsEventSource         mGpsEventSource;
    protected CurrentLocationOverlay mCurrentLocationOverlay;

    protected boolean onMenuMapClicked;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        GISApplication app = (GISApplication) getActivity().getApplication();

        mTolerancePX =
                app.getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;

        mMapView = new MapViewOverlays(getActivity(), app.getMap());
        mMapView.setId(ViewUtil.generateViewId());

        mGpsEventSource = app.getGpsEventSource();

        mCurrentLocationOverlay = new CurrentLocationOverlay(getActivity(), mMapView);
        mCurrentLocationOverlay.setStandingMarker(R.drawable.ic_location_standing);
        mCurrentLocationOverlay.setMovingMarker(R.drawable.ic_location_moving);

        onMenuMapClicked = true;
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        MainActivity activity = (MainActivity) getActivity();
        activity.setBarsView("");
        activity.switchMenuView();

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            View customActionBarView = actionBar.getCustomView();
            View saveMenuItem = customActionBarView.findViewById(R.id.custom_toolbar_button_layout);
            saveMenuItem.setOnClickListener(
                    new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            getActivity().onBackPressed();
                        }
                    });
        }

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapRelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_map);

        //search relative view of map, if not found - add it
        if (mMapRelativeLayout != null) {
            mMapRelativeLayout.addView(
                    mMapView, 0, new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));
        }
/// TODO: ???
//        mMapView.invalidate();
        mMapView.postInvalidate();

        return view;
    }


    @Override
    public void onDestroyView()
    {
        if (mMapView != null) {
            mMapView.removeListener(this);

            if (mMapRelativeLayout != null) {
                removeMapButtonsInLayout();
                mMapRelativeLayout.removeView(mMapView);
            }
        }

        super.onDestroyView();
    }


    @Override
    public void onPause()
    {
        if (null != mGpsEventSource) {
            mGpsEventSource.removeListener(this);
        }

        if (null != mCurrentLocationOverlay) {
            mCurrentLocationOverlay.stopShowingCurrentLocation();
        }

        final SharedPreferences.Editor edit =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        if (null != mMapView) {
            mMapView.removeListener(this);

            edit.putFloat(FoclSettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, mMapView.getZoomLevel());
            GeoPoint point = mMapView.getMapCenter();
            edit.putLong(
                    FoclSettingsConstantsUI.KEY_PREF_SCROLL_X,
                    Double.doubleToRawLongBits(point.getX()));
            edit.putLong(
                    FoclSettingsConstantsUI.KEY_PREF_SCROLL_Y,
                    Double.doubleToRawLongBits(point.getY()));
        }

        edit.commit();

        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        GISApplication app = (GISApplication) getActivity().getApplication();

        if (null != mGpsEventSource) {
            mGpsEventSource.addListener(this);
        }

        mCurrentCenter = null;

        if (null != mMapView) {
            mMapView.addListener(this);

            final SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            float mapZoom = prefs.getFloat(
                    FoclSettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, mMapView.getMinZoom());
            double mapScrollX = Double.longBitsToDouble(
                    prefs.getLong(FoclSettingsConstantsUI.KEY_PREF_SCROLL_X, 0));
            double mapScrollY = Double.longBitsToDouble(
                    prefs.getLong(FoclSettingsConstantsUI.KEY_PREF_SCROLL_Y, 0));
            mMapView.setZoomAndCenter(mapZoom, new GeoPoint(mapScrollX, mapScrollY));

            //change zoom controls visibility
            boolean showControls =
                    prefs.getBoolean(FoclSettingsConstantsUI.KEY_PREF_SHOW_ZOOM_CONTROLS, false);

            if (showControls) {
                addMapButtons();
            } else {
                removeMapButtons();
            }

            if (null != mCurrentLocationOverlay) {
                mCurrentLocationOverlay.updateMode(app.getLocationOverlayMode());
                mCurrentLocationOverlay.startShowingCurrentLocation();
                mMapView.addOverlay(mCurrentLocationOverlay);
            }

            if (null != mGpsEventSource && onMenuMapClicked) {
                onMenuMapClicked = false;
                Location lastLocation = mGpsEventSource.getLastKnownLocation();
                setCurrentCenter(lastLocation);
                locateCurrentPositionAndZoom(false, lastLocation);
            }

/// TODO: ???
//             mMapView.drawMapDrawable();
        }
    }


    @Override
    public void onLayerAdded(int id)
    {

    }


    @Override
    public void onLayerDeleted(int id)
    {

    }


    @Override
    public void onLayerChanged(int id)
    {

    }


    @Override
    public void onExtentChanged(
            float zoom,
            GeoPoint center)
    {
        setZoomInEnabled(mMapView.canZoomIn());
        setZoomOutEnabled(mMapView.canZoomOut());
    }


    @Override
    public void onLayersReordered()
    {

    }


    @Override
    public void onLayerDrawStarted()
    {

    }


    @Override
    public void onLayerDrawFinished(
            int id,
            float percent)
    {

    }


    @Override
    public void onLongPress(MotionEvent event)
    {

    }


    @Override
    public void onSingleTapUp(MotionEvent event)
    {
        double dMinX = event.getX() - mTolerancePX;
        double dMaxX = event.getX() + mTolerancePX;
        double dMinY = event.getY() - mTolerancePX;
        double dMaxY = event.getY() + mTolerancePX;

        GeoEnvelope mapEnv = mMapView.screenToMap(new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY));
        if (null == mapEnv) {
            return;
        }

        //show actions dialog
        List<ILayer> layers = mMapView.getVectorLayersByType(GeoConstants.GTAnyCheck);

        TreeMap<Integer, Integer> priorityMap = new TreeMap<>();
        List<FoclVectorLayer> foclVectorLayers = new ArrayList<>();

        for (ILayer layer : layers) {
            if (!layer.isValid()) {
                continue;
            }
            ILayerView layerView = (ILayerView) layer;
            if (!layerView.isVisible()) {
                continue;
            }

            FoclVectorLayer foclVectorLayer = (FoclVectorLayer) layer;
            List<Long> items = foclVectorLayer.query(mapEnv);
            if (!items.isEmpty()) {
                foclVectorLayers.add(foclVectorLayer);

                int type = foclVectorLayer.getFoclLayerType();
                int priority;

                switch (type) {
                    case FoclConstants.LAYERTYPE_FOCL_UNKNOWN:
                    default:
                        priority = 0;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                        priority = 1;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
                        priority = 2;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_FOSC:
                        priority = 3;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                        priority = 4;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                        priority = 5;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                        priority = 6;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                        priority = 7;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                        priority = 8;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                        priority = 9;
                        break;

                    case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                        priority = 10;
                        break;
                }

                if (!priorityMap.containsKey(priority)) {
                    priorityMap.put(priority, type);
                }
            }
        }

        Integer type = null;
        if (!priorityMap.isEmpty()) {
            Integer key = priorityMap.lastKey();
            type = priorityMap.get(key);
        }

        if (null != type) {
            for (FoclVectorLayer layer : foclVectorLayers) {
                if (type == layer.getFoclLayerType()) {
                    List<Long> items = layer.query(mapEnv);

                    AttributesDialog attributesDialog = new AttributesDialog();
                    attributesDialog.setKeepInstance(true);
                    attributesDialog.setParams(layer, items.get(0));
                    attributesDialog.show(
                            getActivity().getSupportFragmentManager(), FoclConstants.FRAGMENT_ATTRIBUTES);

                    break;
                }
            }
        }
    }


    @Override
    public void panStart(MotionEvent e)
    {

    }


    @Override
    public void panMoveTo(MotionEvent e)
    {

    }


    @Override
    public void panStop()
    {

    }


    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null) {
            setCurrentCenter(location);
        }
    }


    @Override
    public void onBestLocationChanged(Location location)
    {

    }


    @Override
    public void onGpsStatusChanged(int event)
    {

    }


    protected void setCurrentCenter(Location location)
    {
        if (null == location) {
            return;
        }

        if (mCurrentCenter == null) {
            mCurrentCenter = new GeoPoint();
        }

        mCurrentCenter.setCoordinates(location.getLongitude(), location.getLatitude());
        mCurrentCenter.setCRS(GeoConstants.CRS_WGS84);

        if (!mCurrentCenter.project(GeoConstants.CRS_WEB_MERCATOR)) {
            mCurrentCenter = null;
        }
    }


    public void locateCurrentPositionAndZoom(
            boolean forMenuLocation,
            Location lastLocation)
    {
        GeoPoint center;

        if (forMenuLocation && mCurrentCenter == null) {
            Toast.makeText(getActivity(), R.string.error_no_location, Toast.LENGTH_SHORT).show();
            return;

        } else if (mCurrentCenter != null) {
            center = mCurrentCenter;

        } else if (lastLocation != null) {
            center = new GeoPoint(lastLocation.getLongitude(), lastLocation.getLatitude());
            center.setCRS(CRS_WGS84);
            center.project(CRS_WEB_MERCATOR);

        } else {
            center = new GeoPoint(37.6155600, 55.7522200); // Moscow
            center.setCRS(CRS_WGS84);
            center.project(CRS_WEB_MERCATOR);
        }

        float zoomLevel = 16f;

        if (mMapView.canZoomIn()) {

            if (mMapView.getMaxZoom() < zoomLevel) {
                zoomLevel = mMapView.getMaxZoom();
            }

            mMapView.setZoomAndCenter(zoomLevel, center);

        } else {
            mMapView.setZoomAndCenter(mMapView.getZoomLevel(), center);
        }
    }


    protected void removeMapButtonsInLayout()
    {
        if (null != mMapRelativeLayout && null != mButtonsRelativeLayout) {
            mButtonsRelativeLayout.removeViewInLayout(mivZoomIn);
            mButtonsRelativeLayout.removeViewInLayout(mivZoomOut);
            mMapRelativeLayout.removeViewInLayout(mButtonsRelativeLayout);
        }
    }


    protected void removeMapButtons()
    {
        removeMapButtonsInLayout();

        mivZoomIn = null;
        mivZoomOut = null;
        mButtonsRelativeLayout = null;

        mMapRelativeLayout.invalidate();
    }


    protected void addMapButtons()
    {
        Context context = getActivity();

        if (mivZoomIn == null || mivZoomOut == null) {
            mivZoomIn = new ImageView(context);
            mivZoomIn.setImageResource(R.drawable.ic_plus);
            mivZoomIn.setId(ViewUtil.generateViewId());

            mivZoomOut = new ImageView(context);
            mivZoomOut.setImageResource(R.drawable.ic_minus);
            mivZoomOut.setId(ViewUtil.generateViewId());

            mivZoomIn.setOnClickListener(
                    new OnClickListener()
                    {
                        public void onClick(View v)
                        {
                            mMapView.zoomIn();
                        }
                    });

            mivZoomOut.setOnClickListener(
                    new OnClickListener()
                    {
                        public void onClick(View v)
                        {
                            mMapView.zoomOut();
                        }
                    });
        }


        mButtonsRelativeLayout =
                (RelativeLayout) mMapRelativeLayout.findViewById(mButtonsRelativeLayoutId);

        if (null == mButtonsRelativeLayout) {
            mButtonsRelativeLayout = new RelativeLayout(context);
            mButtonsRelativeLayout.setId(mButtonsRelativeLayoutId);

            RelativeLayout.LayoutParams paramsButtonIn = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams paramsButtonOut = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams paramsButtonsRl = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            paramsButtonIn.setMargins(mMargins + 5, mMargins - 5, mMargins + 5, mMargins + 5);
            paramsButtonOut.setMargins(mMargins + 5, mMargins + 5, mMargins + 5, mMargins - 5);

            paramsButtonOut.addRule(RelativeLayout.BELOW, mivZoomIn.getId());
            paramsButtonsRl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsButtonsRl.addRule(RelativeLayout.CENTER_IN_PARENT);

            mButtonsRelativeLayout.addView(mivZoomIn, paramsButtonIn);
            mButtonsRelativeLayout.addView(mivZoomOut, paramsButtonOut);
            mMapRelativeLayout.addView(mButtonsRelativeLayout, paramsButtonsRl);
        }

        setZoomInEnabled(mMapView.canZoomIn());
        setZoomOutEnabled(mMapView.canZoomOut());
    }


    protected void setZoomInEnabled(boolean isEnabled)
    {
        setZoomEnabled(mivZoomIn, isEnabled);
    }


    protected void setZoomOutEnabled(boolean isEnabled)
    {
        setZoomEnabled(mivZoomOut, isEnabled);
    }


    protected void setZoomEnabled(
            ImageView ivZoom,
            boolean isEnabled)
    {
        if (ivZoom == null) {
            return;
        }
        if (isEnabled) {
            ivZoom.getDrawable().setAlpha(255);
        } else {
            ivZoom.getDrawable().setAlpha(50);
        }
    }


    public void refresh()
    {
        if (null != mMapView) {
            mMapView.drawMapDrawable();
        }
    }
}
