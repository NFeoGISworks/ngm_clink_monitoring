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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.dialogs.YesNoDialog;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class ObjectTypesFragment
        extends Fragment
{
    protected Integer mLineId;

    protected TextView mLineName;

    protected StatusComboboxControl mLineStatus;
    protected int                   mOldStatusSelection;
    protected int                   mNewStatusSelection;
    protected long                  mComboboxRestartTime;
    protected boolean mIsOnInitStatus   = false;
    protected boolean mIsOnYesStatus    = false;
    protected boolean mIsOnNoStatus     = false;
    protected boolean mIsOnCancelStatus = false;

    protected AdapterView.OnItemSelectedListener mOnStatusItemSelectedListener;

    protected Button mBtnCableLaying;
    protected Button mBtnFoscMounting;
    protected Button mBtnCrossMounting;
    protected Button mBtnAccessPointMounting;
    protected Button mBtnSpecialTransitionLaying;

    protected FoclStruct mFoclStruct;


    public void setParams(Integer lineId)
    {
        mLineId = lineId;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mComboboxRestartTime = System.currentTimeMillis();
        mOnStatusItemSelectedListener = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    final int position,
                    long id)
            {
                if (mIsOnInitStatus) {
                    mIsOnInitStatus = false;
                    return;
                }

                if (mIsOnYesStatus) {
                    mIsOnYesStatus = false;
                    return;
                }

                if (mIsOnNoStatus) {
                    mIsOnNoStatus = false;
                    return;
                }

                if (mIsOnCancelStatus) {
                    mIsOnCancelStatus = false;
                    return;
                }

                // http://stackoverflow.com/a/7922086/4727406
                if (System.currentTimeMillis() - mComboboxRestartTime < 200) {
                    // System generated event e.g. orientation change, activity startup. So ignore
                    return;
                }


                mNewStatusSelection = mLineStatus.getSelectedItemPosition();

                YesNoDialog yesNoDialog = new YesNoDialog();
                yesNoDialog.setIcon(R.drawable.ic_action_warning)
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.change_line_status_confirmation)
                        .setPositiveText(R.string.yes)
                        .setNegativeText(R.string.no)
                        .setOnPositiveClickedListener(
                                new YesNoDialog.OnPositiveClickedListener()
                                {
                                    @Override
                                    public void onPositiveClicked()
                                    {
                                        mIsOnYesStatus = true;
                                        mFoclStruct.setStatus(mLineStatus.getValue());
                                    }
                                })
                        .setOnNegativeClickedListener(
                                new YesNoDialog.OnNegativeClickedListener()
                                {
                                    @Override
                                    public void onNegativeClicked()
                                    {
                                        mIsOnNoStatus = true;
                                    }
                                })
                        .setOnCancelListener(
                                new YesNoDialog.OnCancelListener()
                                {
                                    @Override
                                    public void onCancel()
                                    {
                                        mIsOnCancelStatus = true;
                                    }
                                })
                        .setOnDismissListener(
                                new YesNoDialog.OnDismissListener()
                                {
                                    @Override
                                    public void onDismiss()
                                    {
                                        int currPos = mLineStatus.getSelectedItemPosition();

                                        if (mIsOnYesStatus) {
                                            mOldStatusSelection = mNewStatusSelection;

                                            if (currPos == mNewStatusSelection) {
                                                mIsOnYesStatus = false;
                                            } else {
                                                mLineStatus.setSelection(mNewStatusSelection);
                                            }
                                        }

                                        if (mIsOnNoStatus) {
                                            if (currPos == mOldStatusSelection) {
                                                mIsOnNoStatus = false;
                                            } else {
                                                mLineStatus.setSelection(mOldStatusSelection);
                                            }
                                        }

                                        if (mIsOnCancelStatus) {
                                            if (currPos == mOldStatusSelection) {
                                                mIsOnCancelStatus = false;
                                            } else {
                                                mLineStatus.setSelection(mOldStatusSelection);
                                            }
                                        }
                                    }
                                });

                yesNoDialog.show(
                        getActivity().getSupportFragmentManager(),
                        FoclConstants.FRAGMENT_YES_NO_DIALOG + "NewStatusSelected");
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        };
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        mComboboxRestartTime = System.currentTimeMillis();

        final MainActivity activity = (MainActivity) getActivity();
        activity.setBarsView(null);

        final View view = inflater.inflate(R.layout.fragment_object_types, null);

        mLineName = (TextView) view.findViewById(R.id.line_name_ot);
        mLineStatus = (StatusComboboxControl) view.findViewById(R.id.line_status_ot);

        mBtnCableLaying = (Button) view.findViewById(R.id.btn_cable_laying_ot);
        mBtnFoscMounting = (Button) view.findViewById(R.id.btn_fosc_mounting_ot);
        mBtnCrossMounting = (Button) view.findViewById(R.id.btn_cross_mounting_ot);
        mBtnAccessPointMounting = (Button) view.findViewById(R.id.btn_access_point_mounting_ot);
        mBtnSpecialTransitionLaying =
                (Button) view.findViewById(R.id.btn_special_transition_laying_ot);

        GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            setBlockedView();
            return view;
        }

        try {
            mFoclStruct = (FoclStruct) foclProject.getLayer(mLineId);
        } catch (Exception e) {
            mFoclStruct = null;
        }

        if (null == mFoclStruct) {
            setBlockedView();
            return view;
        }

        mLineName.setText(Html.fromHtml(mFoclStruct.getHtmlFormattedName()));

        FoclDictItem dictItem = foclProject.getFoclDitcs().get(FoclConstants.FIELD_PROJ_STATUSES);
        mLineStatus.setValues(dictItem);
        mLineStatus.setOnItemSelectedListener(mOnStatusItemSelectedListener);
        mOldStatusSelection = StatusComboboxControl.getStausId(mFoclStruct.getStatus());

        // workaround, http://stackoverflow.com/a/17370964/4727406
        mLineStatus.post(
                new Runnable()
                {
                    public void run()
                    {
                        mIsOnInitStatus = true;
                        mLineStatus.setSelection(mOldStatusSelection);
                    }
                });

        View.OnClickListener buttonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int foclStructLayerType;

                switch (v.getId()) {
                    case R.id.btn_cable_laying_ot:
                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT;
                        break;
                    case R.id.btn_fosc_mounting_ot:
                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_REAL_FOSC;
                        break;
                    case R.id.btn_cross_mounting_ot:
                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS;
                        break;
                    case R.id.btn_access_point_mounting_ot:
                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT;
                        break;
                    case R.id.btn_special_transition_laying_ot:
                        foclStructLayerType =
                                FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT;
                        break;

                    // TODO: for layer editing
//                    case R.id.btn_cable_laying:
//                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE;
//                        break;
//                    case R.id.btn_fosc_mounting:
//                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_FOSC;
//                        break;
//                    case R.id.btn_cross_mounting:
//                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS;
//                        break;
//                    case R.id.btn_access_point_mounting:
//                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT;
//                        break;
//                    case R.id.btn_hid_mounting:
//                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION;
//                        break;
//
                    default:
                        foclStructLayerType = FoclConstants.LAYERTYPE_FOCL_UNKNOWN;
                        break;
                }

                onButtonClick(foclStructLayerType);
            }
        };

        mBtnCableLaying.setOnClickListener(buttonOnClickListener);
        mBtnFoscMounting.setOnClickListener(buttonOnClickListener);
        mBtnCrossMounting.setOnClickListener(buttonOnClickListener);
        mBtnAccessPointMounting.setOnClickListener(buttonOnClickListener);
        mBtnSpecialTransitionLaying.setOnClickListener(buttonOnClickListener);

        return view;
    }


    protected void setBlockedView()
    {
        mLineName.setText("");
        mLineStatus.setEnabled(false);
        mBtnCableLaying.setEnabled(false);
        mBtnFoscMounting.setEnabled(false);
        mBtnCrossMounting.setEnabled(false);
        mBtnAccessPointMounting.setEnabled(false);
        mBtnSpecialTransitionLaying.setEnabled(false);
    }


    public void onButtonClick(int foclStructLayerType)
    {
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        CreateObjectFragment createObjectFragment =
                (CreateObjectFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_CREATE_OBJECT);

        if (createObjectFragment == null) {
            createObjectFragment = new CreateObjectFragment();
        }

        createObjectFragment.setParams(getActivity(), mLineId, foclStructLayerType);

        ft.replace(R.id.main_fragment, createObjectFragment, FoclConstants.FRAGMENT_CREATE_OBJECT);
        ft.addToBackStack(null);
        ft.commit();


        // TODO: for layer editing
//        final FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//
//        ObjectListFragment objectListFragment =
//                (ObjectListFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_OBJECT_LIST);
//
//        if (objectListFragment == null) {
//            objectListFragment = new ObjectListFragment();
//        }
//
//        objectListFragment.setParams(mLineId, foclStructLayerType);
//
//        ft.replace(R.id.main_fragment, objectListFragment, FoclConstants.FRAGMENT_OBJECT_LIST);
//        ft.addToBackStack(null);
//        ft.commit();
    }
}
