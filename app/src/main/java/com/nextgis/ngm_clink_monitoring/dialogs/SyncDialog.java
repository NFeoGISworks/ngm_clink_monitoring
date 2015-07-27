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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;


public class SyncDialog
        extends YesNoDialog
{
    protected RadioButton mSync;
    protected RadioButton mFullSync;

    protected boolean mIsFullSync = false;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_sync, null);
        mSync = (RadioButton) view.findViewById(R.id.sync_sy);
        mFullSync = (RadioButton) view.findViewById(R.id.full_sync_sy);


        final GISApplication app = (GISApplication) getActivity().getApplication();

        View.OnClickListener radioListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RadioButton rb = (RadioButton) v;

                switch (rb.getId()) {
                    case R.id.sync_sy:
                    default:
                        mIsFullSync = false;
                        break;

                    case R.id.full_sync_sy:
                        mIsFullSync = true;
                        break;
                }
            }
        };

        mSync.setOnClickListener(radioListener);
        mFullSync.setOnClickListener(radioListener);


        setIcon(R.drawable.ic_action_refresh);
        setTitle(R.string.synchronization);
        setView(view);

        setPositiveText(R.string.ok);
        setNegativeText(R.string.cancel);


        setOnPositiveClickedListener(
                new YesNoDialog.OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        app.runSyncManually(mIsFullSync);
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
