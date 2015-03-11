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

package com.nextgis.ngm_clink_monitoring.util;

import android.content.res.Resources;
import android.location.Location;
import android.media.ExifInterface;
import com.nextgis.ngm_clink_monitoring.R;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


public class LocationUtil
{
    public static final char DEGREE_CHAR = (char) 0x00B0;


    public static String formatLatitude(
            double latitude,
            int outputType,
            Resources res)
    {
        String direction = (String) res.getText(R.string.compas_N);

        if (latitude < 0) {
            direction = (String) res.getText(R.string.compas_S);
            latitude = -latitude;
        }

        return formatCoordinate(latitude, outputType) + direction;
    }


    public static String formatLongitude(
            double longitude,
            int outputType,
            Resources res)
    {
        String direction = (String) res.getText(R.string.compas_E);

        if (longitude < 0) {
            direction = (String) res.getText(R.string.compas_W);
            longitude = -longitude;
        }

        return formatCoordinate(longitude, outputType) + direction;
    }


    public static String formatCoordinate(
            double coordinate,
            int outputType)
    {
        StringBuilder sb = new StringBuilder();
        char endChar = DEGREE_CHAR;

        DecimalFormat df = new DecimalFormat("###.####");
        if (outputType == Location.FORMAT_MINUTES || outputType == Location.FORMAT_SECONDS) {

            df = new DecimalFormat("##.###");

            int degrees = (int) Math.floor(coordinate);
            sb.append(degrees);
            sb.append(DEGREE_CHAR); // degrees sign
            endChar = '\''; // minutes sign
            coordinate -= degrees;
            coordinate *= 60.0;

            if (outputType == Location.FORMAT_SECONDS) {

                df = new DecimalFormat("##.##");

                int minutes = (int) Math.floor(coordinate);
                sb.append(minutes);
                sb.append('\''); // minutes sign
                endChar = '\"'; // seconds sign
                coordinate -= minutes;
                coordinate *= 60.0;
            }
        }

        sb.append(df.format(coordinate));
        sb.append(endChar);

        return sb.toString();
    }


    public static void writeLocationToExif(
            File imgFile,
            Location location)
            throws IOException
    {
        if (location == null) {
            return;
        }

        ExifInterface exif = new ExifInterface(imgFile.getCanonicalPath());

        double lat = location.getLatitude();
        double absLat = Math.abs(lat);
        String dms = Location.convert(absLat, Location.FORMAT_SECONDS);
        String[] splits = dms.split(":");
        String[] secondsArr = (splits[2]).split("\\.");
        String seconds;

        if (secondsArr.length == 0) {
            seconds = splits[2];
        } else {
            seconds = secondsArr[0];
        }

        String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat > 0 ? "N" : "S");

        double lon = location.getLongitude();
        double absLon = Math.abs(lon);
        dms = Location.convert(absLon, Location.FORMAT_SECONDS);
        splits = dms.split(":");
        secondsArr = (splits[2]).split("\\.");

        if (secondsArr.length == 0) {
            seconds = splits[2];
        } else {
            seconds = secondsArr[0];
        }

        String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lon > 0 ? "E" : "W");

        exif.saveAttributes();
    }
}
