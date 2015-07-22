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

package com.nextgis.ngm_clink_monitoring.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class FoclFileUtil
{
    public static void copy(
            InputStream inputStream,
            OutputStream outputStream)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }


    public static File getDirWithCreate(String path)
            throws IOException
    {
        File dir = new File(path);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create directory " + path);
            }
        }

        return dir;
    }


    public static JSONArray readJsonArrayLinesFromFile(File filePath)
            throws IOException, JSONException
    {
        JSONArray jsonArray = new JSONArray();

        FileInputStream inputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        JSONObject jsonFileName = new JSONObject();
        jsonFileName.put("file_name", filePath.getName());
        jsonArray.put(jsonFileName);

        String receiveString;
        while ((receiveString = bufferedReader.readLine()) != null) {
            JSONObject jsonLine = new JSONObject();
            jsonLine.put("line", receiveString);
            jsonArray.put(jsonLine);
        }

        inputStream.close();

        return jsonArray;
    }

}
