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

package com.nextgis.ngm_clink_monitoring;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.widget.Toast;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class FoclSyncAdapter
        extends SyncAdapter
{
    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize)
    {
        super(context, autoInitialize);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    protected void sync(
            LayerGroup layerGroup,
            SyncResult syncResult)
    {
        FoclProject foclProject = null;

        for (int i = 0; i < layerGroup.getLayerCount(); i++) {
            ILayer layer = layerGroup.getLayer(i);
            if (layer.getType() == FoclConstants.LAYERTYPE_FOCL_PROJECT) {
                foclProject = (FoclProject) layer;
            }
        }

        if (null != foclProject) {
            String error = foclProject.download();

            if (null != error && error.length() > 0) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            super.sync(layerGroup, syncResult);
        }
    }
}
