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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class FoclDictItem
        extends HashMap<String, String>
{
    public FoclDictItem(JSONObject jsonObject)
            throws JSONException
    {
        super(jsonObject.length());
        fromJSON(jsonObject);
    }


    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = new JSONObject();

        for (Map.Entry<String, String> entry : this.entrySet()) {
            rootConfig.put(entry.getKey(), entry.getValue());
        }

        return rootConfig;
    }


    public FoclDictItem fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = iter.next();
            String value = jsonObject.getString(key);
            this.put(key, value);
        }

        return this;
    }
}
