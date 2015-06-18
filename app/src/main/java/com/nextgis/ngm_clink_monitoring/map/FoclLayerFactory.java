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

package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class FoclLayerFactory
        extends LayerFactory
{
    @Override
    public ILayer createLayer(
            Context context,
            File path)
    {
        File config_file = new File(path, Constants.CONFIG);
        try {
            String sData = FileUtil.readFromFile(config_file);
            JSONObject rootObject = new JSONObject(sData);
            int nType = rootObject.getInt(Constants.JSON_TYPE_KEY);

            switch (nType) {
                case FoclConstants.LAYERTYPE_FOCL_PROJECT:
                    return new FoclProject(context, path, this);
                case FoclConstants.LAYERTYPE_FOCL_STRUCT:
                    return new FoclStruct(context, path, this);
                case FoclConstants.LAYERTYPE_FOCL_VECTOR:
                    return new FoclVectorLayerUI(context, path);
            }

        } catch (IOException | JSONException e) {
            Log.d(Constants.TAG, e.getLocalizedMessage());
        }

        return super.createLayer(context, path);
    }


    @Override
    public void createNewRemoteTMSLayer(
            Context context,
            LayerGroup groupLayer)
    {

    }


    @Override
    public void createNewNGWLayer(
            Context context,
            LayerGroup groupLayer)
    {

    }


    @Override
    public void createNewLocalTMSLayer(
            Context context,
            LayerGroup groupLayer,
            Uri uri)
    {

    }


    @Override
    public void createNewVectorLayer(
            Context context,
            LayerGroup groupLayer,
            Uri uri)
    {

    }


    @Override
    public void createNewVectorLayerWithForm(
            Context context,
            LayerGroup groupLayer,
            Uri uri)
    {

    }
}
