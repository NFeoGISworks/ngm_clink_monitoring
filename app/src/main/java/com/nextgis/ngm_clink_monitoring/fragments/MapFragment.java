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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.VectorCacheItem;
import com.nextgis.maplibui.MapView;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.util.ConstantsUI;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.ViewUtil;

import java.util.List;


public class MapFragment
        extends Fragment
        implements MapViewEventListener
{
    protected final static int mMargins = 10;
    protected float mTolerancePX;

    protected MapView   mMap;
    protected ImageView mivZoomIn;
    protected ImageView mivZoomOut;

    protected RelativeLayout mMapRelativeLayout;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mTolerancePX =
                getActivity().getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //search relative view of map, if not found - add it
        if (mMap != null) {
            mMapRelativeLayout = (RelativeLayout) view.findViewById(R.id.maprl);
            if (mMapRelativeLayout != null) {
                mMapRelativeLayout.addView(
                        mMap, 0, new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT));
            }
            mMap.invalidate();
        }

        return view;
    }


    @Override
    public void onDestroyView()
    {
        if (mMap != null) {
            mMap.removeListener(this);
            if (mMapRelativeLayout != null) {
                mMapRelativeLayout.removeView(mMap);
            }
        }

        super.onDestroyView();
    }


    protected void removeMapButtons(RelativeLayout rl)
    {
        rl.removeViewInLayout(mivZoomIn);
        rl.removeViewInLayout(mivZoomOut);
        mivZoomIn = null;
        mivZoomOut = null;
        rl.invalidate();
    }


    protected void addMapButtons(
            Context context,
            RelativeLayout rl)
    {
        mivZoomIn = new ImageView(context);
        mivZoomIn.setImageResource(R.drawable.ic_plus);
        ViewUtil.setGeneratedId(mivZoomIn);

        mivZoomOut = new ImageView(context);
        mivZoomOut.setImageResource(R.drawable.ic_minus);
        ViewUtil.setGeneratedId(mivZoomOut);

        mivZoomIn.setOnClickListener(
                new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        mMap.zoomIn();
                    }
                });

        mivZoomOut.setOnClickListener(
                new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        mMap.zoomOut();
                    }
                });


        RelativeLayout buttonsRl = new RelativeLayout(context);

        RelativeLayout.LayoutParams paramsButtonIn = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsButtonOut = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsButtonsRl = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        paramsButtonIn.setMargins(mMargins + 5, mMargins - 5, mMargins + 5, mMargins + 5);
        paramsButtonOut.setMargins(mMargins + 5, mMargins + 5, mMargins + 5, mMargins - 5);

        paramsButtonOut.addRule(RelativeLayout.BELOW, mivZoomIn.getId());
        paramsButtonsRl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramsButtonsRl.addRule(RelativeLayout.CENTER_IN_PARENT);

        buttonsRl.addView(mivZoomIn, paramsButtonIn);
        buttonsRl.addView(mivZoomOut, paramsButtonOut);
        rl.addView(buttonsRl, paramsButtonsRl);


        setZoomInEnabled(mMap.canZoomIn());
        setZoomOutEnabled(mMap.canZoomOut());
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
        setZoomInEnabled(mMap.canZoomIn());
        setZoomOutEnabled(mMap.canZoomOut());
    }


    @Override
    public void onLayersReordered()
    {

    }


    @Override
    public void onLayerDrawFinished(
            int id,
            float percent)
    {
        //TODO: invalidate map or listen event in map?
    }


    protected void setZoomInEnabled(boolean bEnabled)
    {
        if (mivZoomIn == null) {
            return;
        }
        if (bEnabled) {
            mivZoomIn.getDrawable().setAlpha(255);
        } else {
            mivZoomIn.getDrawable().setAlpha(50);
        }
    }


    protected void setZoomOutEnabled(boolean bEnabled)
    {
        if (mivZoomOut == null) {
            return;
        }
        if (bEnabled) {
            mivZoomOut.getDrawable().setAlpha(255);
        } else {
            mivZoomOut.getDrawable().setAlpha(50);
        }
    }


    public boolean onInit(MapView map)
    {
        mMap = map;
        mMap.addListener(this);
        return true;
    }


    @Override
    public void onPause()
    {
        final SharedPreferences.Editor edit =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        if (null != mMap) {
            edit.putFloat(FoclSettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, mMap.getZoomLevel());
            GeoPoint point = mMap.getMapCenter();
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

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (null != mMap) {
            float mMapZoom =
                    prefs.getFloat(FoclSettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, mMap.getMinZoom());
            double mMapScrollX = Double.longBitsToDouble(
                    prefs.getLong(FoclSettingsConstantsUI.KEY_PREF_SCROLL_X, 0));
            double mMapScrollY = Double.longBitsToDouble(
                    prefs.getLong(FoclSettingsConstantsUI.KEY_PREF_SCROLL_Y, 0));
            mMap.setZoomAndCenter(mMapZoom, new GeoPoint(mMapScrollX, mMapScrollY));
        }

        //change zoom controls visibility
        boolean showControls =
                prefs.getBoolean(FoclSettingsConstantsUI.KEY_PREF_SHOW_ZOOM_CONTROLS, false);
        if (showControls) {
            if (mivZoomIn == null || mivZoomOut == null) {
                addMapButtons(getActivity(), mMapRelativeLayout);
            }
        } else {
            removeMapButtons(mMapRelativeLayout);
        }
    }


    @Override
    public void onLongPress(MotionEvent event)
    {
        double dMinX = event.getX() - mTolerancePX;
        double dMaxX = event.getX() + mTolerancePX;
        double dMinY = event.getY() - mTolerancePX;
        double dMaxY = event.getY() + mTolerancePX;

        GeoEnvelope mapEnv = mMap.screenToMap(new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY));
        if (null == mapEnv) {
            return;
        }

        //show actions dialog
        List<ILayer> layers = mMap.getVectorLayersByType(GeoConstants.GTAnyCheck);
        List<VectorCacheItem> items = null;
        VectorLayer vectorLayer = null;
        boolean intersects = false;
        for (ILayer layer : layers) {
            if (!layer.isValid()) {
                continue;
            }
            ILayerView layerView = (ILayerView) layer;
            if (!layerView.isVisible()) {
                continue;
            }

            vectorLayer = (VectorLayer) layer;
            items = vectorLayer.query(mapEnv);
            if (!items.isEmpty()) {
                intersects = true;
                break;
            }
        }

        if (intersects) {
            mMap.postInvalidate();
        }
    }


    @Override
    public void onSingleTapUp(MotionEvent event)
    {

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
}
