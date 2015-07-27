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

package com.nextgis.ngm_clink_monitoring.dialogs;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class SetLineStatusDialog
        extends YesNoDialog
{
    protected TextView    mLineName;
    protected RadioButton mRbProject;
    protected RadioButton mRbInProgress;
    protected RadioButton mRbBuilt;

    protected String mLineStatus;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_set_status_line, null);
        mLineName = (TextView) view.findViewById(R.id.line_name_sl);
        mRbProject = (RadioButton) view.findViewById(R.id.status_project_sl);
        mRbInProgress = (RadioButton) view.findViewById(R.id.status_in_progress_sl);
        mRbBuilt = (RadioButton) view.findViewById(R.id.status_built_sl);


        final GISApplication app = (GISApplication) getActivity().getApplication();
        final FoclProject foclProject = app.getFoclProject();
        final FoclStruct foclStruct = app.getSelectedFoclStruct();
        mLineStatus = foclStruct.getStatus();


        mLineName.setText(Html.fromHtml(foclStruct.getHtmlFormattedName(false)));


        FoclDictItem dictItem = foclProject.getFoclDitcs().get(FoclConstants.FIELD_PROJ_STATUSES);
        String value;
        String value_alias;

        value = FoclConstants.FIELD_VALUE_STATUS_PROJECT;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mRbProject.setText(value_alias);

        value = FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mRbInProgress.setText(value_alias);

        value = FoclConstants.FIELD_VALUE_STATUS_BUILT;
        value_alias = null;
        if (null != dictItem) {
            value_alias = dictItem.get(value);
        }
        if (null == value_alias) {
            value_alias = value;
        }
        mRbBuilt.setText(value_alias);


        switch (mLineStatus) {
            case FoclConstants.FIELD_VALUE_STATUS_PROJECT:
            default:
                mRbProject.setChecked(true);
                break;

            case FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS:
                mRbInProgress.setChecked(true);
                break;

            case FoclConstants.FIELD_VALUE_STATUS_BUILT:
                mRbBuilt.setChecked(true);
                break;
        }


        View.OnClickListener radioListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RadioButton rb = (RadioButton) v;

                switch (rb.getId()) {
                    case R.id.status_project_sl:
                    default:
                        mLineStatus = FoclConstants.FIELD_VALUE_STATUS_PROJECT;
                        break;

                    case R.id.status_in_progress_sl:
                        mLineStatus = FoclConstants.FIELD_VALUE_STATUS_IN_PROGRESS;
                        break;

                    case R.id.status_built_sl:
                        mLineStatus = FoclConstants.FIELD_VALUE_STATUS_BUILT;
                        break;
                }
            }
        };

        mRbProject.setOnClickListener(radioListener);
        mRbInProgress.setOnClickListener(radioListener);
        mRbBuilt.setOnClickListener(radioListener);


        setIcon(R.drawable.ic_action_warning);
        setTitle(R.string.status_setting);
        setView(view);

        setPositiveText(R.string.ok);
        setNegativeText(R.string.cancel);

        setOnPositiveClickedListener(
                new YesNoDialog.OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        foclStruct.setStatus(mLineStatus);
                        foclStruct.setIsStatusChanged(true);

                        GpsEventSource gps = app.getGpsEventSource();
                        Location lastLoc = gps.getLastKnownLocation();

                        long time;
                        if (null != lastLoc) {
                            time = lastLoc.getTime();
                        } else {
                            time = System.currentTimeMillis();
                        }

                        foclStruct.setStatusUpdateTime(time);
                        foclStruct.save();
                    }
                });

        setOnNegativeClickedListener(
                new YesNoDialog.OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });


        return super.onCreateDialog(savedInstanceState);
    }
}
