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

package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.nextgis.maplib.util.Constants.JSON_ID_KEY;
import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.LAYERTYPE_FOCL_STRUCT;


public class FoclStruct
        extends LayerGroup
{
    protected long mRemoteId;


    public FoclStruct(
            Context context,
            File path,
            LayerFactory layerFactory)
    {
        super(context, path, layerFactory);
        mLayerType = LAYERTYPE_FOCL_STRUCT;
    }


    public void setRemoteId(long remoteId)
    {
        mRemoteId = remoteId;
    }


    public ILayer getLayerByFoclType(int type)
    {
        for (ILayer layer : mLayers) {
            FoclVectorLayer foclLayer = (FoclVectorLayer) layer;
            if (foclLayer.getFoclLayerType() == type) {
                return foclLayer;
            }
        }

        return null;
    }


    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = super.toJSON();
        rootConfig.put(JSON_ID_KEY, mRemoteId);

        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        super.fromJSON(jsonObject);
        mRemoteId = jsonObject.getLong(JSON_ID_KEY);
    }
}
