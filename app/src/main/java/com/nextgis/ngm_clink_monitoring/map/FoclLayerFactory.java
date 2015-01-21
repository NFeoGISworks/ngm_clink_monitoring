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
import android.util.Log;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.RemoteTMSLayer;
import com.nextgis.maplib.util.FileUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.nextgis.maplib.util.Constants.*;
import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.LAYERTYPE_FOCL_VECTOR;


public class FoclLayerFactory
        extends LayerFactory
{
    public FoclLayerFactory(File mapPath)
    {
        super(mapPath);
    }


    @Override
    public ILayer createLayer(
            Context context,
            File path)
    {
        File config_file = new File(path, CONFIG);
        ILayer layer = null;

        try {
            String sData = FileUtil.readFromFile(config_file);
            JSONObject rootObject = new JSONObject(sData);
            int nType = rootObject.getInt(JSON_TYPE_KEY);

            switch (nType) {
                case LAYERTYPE_REMOTE_TMS:
                    layer = new RemoteTMSLayer(context, path);
                    break;
                case LAYERTYPE_FOCL_VECTOR:
                    layer = new FoclVectorLayer(context, path);
                    break;
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return layer;
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
}
