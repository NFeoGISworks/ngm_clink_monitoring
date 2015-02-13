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

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.ViewUtil;


public class ObjectTypesFragment
        extends Fragment
{
    protected FoclProject mFoclProject = null;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final ActionBarActivity activity = (ActionBarActivity) getActivity();

        final ViewGroup rootView =
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.object_types_toolbar);
        toolbar.setVisibility(View.VISIBLE);

        final View view = inflater.inflate(R.layout.fragment_object_types, null);

        final LinearLayout buttonsLayout = (LinearLayout) view.findViewById(R.id.buttons_layout);

        final Button btnCableLaying = (Button) view.findViewById(R.id.btn_cable_laying);
        final Button btnFoscMounting = (Button) view.findViewById(R.id.btn_fosc_mounting);
        final Button btnCrossMounting = (Button) view.findViewById(R.id.btn_cross_mounting);
        final Button btnCabinetMounting = (Button) view.findViewById(R.id.btn_cabinet_mounting);
        final Button btnPoleMounting = (Button) view.findViewById(R.id.btn_pole_mounting);
        final Button btnLineMeasuring = (Button) view.findViewById(R.id.btn_line_measuring);

        // Set button height to 1/3 of button's display part.
        // Set a global layout listener which will be called
        // when the layout pass is completed and the view is drawn
        buttonsLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    public void onGlobalLayout()
                    {
                        //Remove the listener before proceeding
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            buttonsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            buttonsLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        // measure your views here

                        int rootViewH = rootView.getHeight();

                        int[] locations = new int[2];
                        buttonsLayout.getLocationInWindow(locations);
                        int buttonsLayoutTop = locations[1];

                        int buttonMinH = btnCableLaying.getHeight(); // with layout_margins
                        int buttonMaxH = (rootViewH - buttonsLayoutTop) / 3; // with layout_margins

                        if (buttonMaxH > buttonMinH) {
                            ViewUtil.setViewHeight(btnCableLaying, buttonMaxH);
                            ViewUtil.setViewHeight(btnFoscMounting, buttonMaxH);
                            ViewUtil.setViewHeight(btnCrossMounting, buttonMaxH);
                            ViewUtil.setViewHeight(btnCabinetMounting, buttonMaxH);
                            ViewUtil.setViewHeight(btnPoleMounting, buttonMaxH);
                            ViewUtil.setViewHeight(btnLineMeasuring, buttonMaxH);
                        }
                    }
                });


        GISApplication app = (GISApplication) getActivity().getApplication();
        mFoclProject = app.getFoclProject();

        if (null == mFoclProject) {
            btnCableLaying.setEnabled(false);
            btnFoscMounting.setEnabled(false);
            btnCrossMounting.setEnabled(false);
            btnCabinetMounting.setEnabled(false);
            btnPoleMounting.setEnabled(false);
            btnLineMeasuring.setEnabled(false);
            return view;
        }


        // TODO: remove it
        btnLineMeasuring.setEnabled(false);


        btnCableLaying.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE);
                    }
                });

        btnFoscMounting.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_FOSC);
                    }
                });

        btnCrossMounting.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS);
                    }
                });

        btnCabinetMounting.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_TELECOM_CABINET);
                    }
                });

        btnPoleMounting.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_POLE);
                    }
                });

        btnLineMeasuring.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        OnButtonClick(FoclConstants.LAYERTYPE_FOCL_LINE_MEASURING);
                    }
                });

        return view;
    }


    public void OnButtonClick(int workType)
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();

        StatusBarFragment statusBarFragment = (StatusBarFragment) fm.findFragmentByTag("StatusBar");

        ObjectListFragment objectListFragment =
                (ObjectListFragment) fm.findFragmentByTag("ObjectList");

        if (objectListFragment == null) {
            objectListFragment = new ObjectListFragment();
        }

        objectListFragment.setParams(workType);

        FragmentTransaction ft = fm.beginTransaction();
        if (null != statusBarFragment) {
            ft.hide(statusBarFragment);
        }
        ft.replace(R.id.object_fragment, objectListFragment, "ObjectList");
        ft.addToBackStack(null);
        ft.commit();
        fm.executePendingTransactions();
    }
}
