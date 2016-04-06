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

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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
        return getDirWithCreate(dir);
    }


    public static File getDirWithCreate(File dir)
            throws IOException
    {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create directory " + dir.getPath());
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


    /**
     * Zips a file at a location and places the resulting zip file at the toLocation.
     * <p/>
     * Example:
     * zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     * <p/>
     * http://stackoverflow.com/a/14868161
     */
    public static void zipFileAtPath(
            String sourcePath,
            String toLocation)
            throws IOException
    {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        FileOutputStream fos = new FileOutputStream(toLocation);
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos));

        if (sourceFile.isDirectory()) {
            zipSubFolder(zipOut, sourceFile, sourceFile.getParent().length() + 1); // ??

        } else {
            byte data[] = new byte[BUFFER];
            FileInputStream fis = new FileInputStream(sourcePath);
            BufferedInputStream bis = new BufferedInputStream(fis, BUFFER);

            String lastPathComponent = sourcePath.substring(sourcePath.lastIndexOf("/"));

            ZipEntry zipEntry = new ZipEntry(lastPathComponent);
            zipOut.putNextEntry(zipEntry);

            int count;
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zipOut.write(data, 0, count);
            }
        }

        zipOut.close();
    }


    /**
     * Zips a subfolder
     */
    protected static void zipSubFolder(
            ZipOutputStream zipOut,
            File srcFolder,
            int basePathLength)
            throws IOException
    {
        final int BUFFER = 2048;

        File[] fileList = srcFolder.listFiles();
        BufferedInputStream bis = null;

        for (File file : fileList) {

            if (file.isDirectory()) {
                zipSubFolder(zipOut, file, basePathLength);

            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);
                Log.d("ZIP SUBFOLDER", "Relative Path : " + relativePath);

                FileInputStream fis = new FileInputStream(unmodifiedFilePath);
                bis = new BufferedInputStream(fis, BUFFER);

                ZipEntry zipEntry = new ZipEntry(relativePath);
                zipOut.putNextEntry(zipEntry);

                int count;
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    zipOut.write(data, 0, count);
                }

                bis.close();
            }
        }
    }
}
