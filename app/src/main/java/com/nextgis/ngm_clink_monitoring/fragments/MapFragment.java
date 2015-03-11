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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplibui.MapView;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.ViewUtil;


public class MapFragment
        extends Fragment
        implements MapEventListener
{

    protected static final int mMargings = 10;

    protected static final String KEY_PREF_WAS_ZOOM_CONTROLS_SHOWN = "was_zoom_controls_shown";

    protected MapView        mMap;
    protected ImageView      mivZoomIn;
    protected ImageView      mivZoomOut;
    protected RelativeLayout mMapRelativeLayout;

    protected boolean mShowZoomControls;


    public MapFragment()
    {
        mShowZoomControls = false;
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FrameLayout layout = (FrameLayout) view.findViewById(R.id.mapholder);

        //search relative view of map, if not found - add it
        if (mMap != null) {
            mMapRelativeLayout = (RelativeLayout) layout.findViewById(R.id.maprl);
            if (mMapRelativeLayout != null) {
                mMapRelativeLayout.addView(
                        mMap, 0, new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT));

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (sharedPreferences.getBoolean(
                        FoclSettingsConstantsUI.KEY_PREF_SHOW_ZOOM_CONTROLS, false)) {
                    addMapButtons(view.getContext(), mMapRelativeLayout);
                }
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
        mShowZoomControls = false;
        rl.removeViewInLayout(mivZoomIn);
        rl.removeViewInLayout(mivZoomOut);
        mivZoomIn = null;
        mivZoomOut = null;
    }


    protected void addMapButtons(
            Context context,
            RelativeLayout rl)
    {
        mShowZoomControls = true;
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

        final RelativeLayout.LayoutParams RightParams4 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RightParams4.setMargins(mMargings + 5, mMargings - 5, mMargings + 5, mMargings - 5);
        RightParams4.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        RightParams4.addRule(RelativeLayout.CENTER_IN_PARENT);//ALIGN_PARENT_TOP
        rl.addView(mivZoomIn, RightParams4);

        final RelativeLayout.LayoutParams RightParams2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RightParams2.setMargins(mMargings + 5, mMargings - 5, mMargings + 5, mMargings - 5);
        RightParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        RightParams2.addRule(RelativeLayout.CENTER_IN_PARENT);//ALIGN_PARENT_TOP
        // TODO:
//        RightParams2.addRule(RelativeLayout.BELOW, mivZoomIn.getId());
        rl.addView(mivZoomOut, RightParams2);

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
        edit.putBoolean(KEY_PREF_WAS_ZOOM_CONTROLS_SHOWN, mShowZoomControls);
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
        if (prefs.getBoolean(KEY_PREF_WAS_ZOOM_CONTROLS_SHOWN, false) != showControls) {
            if (showControls) {
                addMapButtons(getActivity(), mMapRelativeLayout);
            } else {
                removeMapButtons(mMapRelativeLayout);
            }
        }
    }
}
