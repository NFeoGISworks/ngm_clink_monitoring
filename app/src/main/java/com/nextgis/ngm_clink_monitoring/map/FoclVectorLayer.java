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

package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class FoclVectorLayer
        extends NGWVectorLayer
{
    protected static final String JSON_FOCL_TYPE_KEY = "focl_type";

    protected int mFoclLayerType;


    public FoclVectorLayer(
            Context context,
            File path)
    {
        super(context, path);
        mLayerType = FoclConstants.LAYERTYPE_FOCL_VECTOR;
    }


    public static int getFoclLayerTypeFromString(String type)
    {
        if (type.equals("fosc")) {
            return FoclConstants.LAYERTYPE_FOCL_FOSC;
        }
        if (type.equals("optical_cross")) {
            return FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS;
        }
        if (type.equals("pole")) {
            return FoclConstants.LAYERTYPE_FOCL_POLE;
        }
        if (type.equals("optical_cable")) {
            return FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE;
        }
        if (type.equals("telecom_cabinet")) {
            return FoclConstants.LAYERTYPE_FOCL_TELECOM_CABINET;
        }
        if (type.equals("endpoint")) {
            return FoclConstants.LAYERTYPE_FOCL_ENDPOINT;
        }

        return FoclConstants.LAYERTYPE_FOCL_UNKNOWN;
    }


    public int getFoclLayerType()
    {
        return mFoclLayerType;
    }


    public void setFoclLayerType(int foclLayerType)
    {
        mFoclLayerType = foclLayerType;
    }


    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = super.toJSON();
        rootConfig.put(JSON_FOCL_TYPE_KEY, mFoclLayerType);
        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException, SQLiteException
    {
        super.fromJSON(jsonObject);
        mFoclLayerType = jsonObject.getInt(JSON_FOCL_TYPE_KEY);
    }
}
