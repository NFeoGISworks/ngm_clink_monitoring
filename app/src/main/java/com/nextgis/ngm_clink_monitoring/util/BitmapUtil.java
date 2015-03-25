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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;


public class BitmapUtil
{
    public static Bitmap getResizedBitmap(
            Bitmap bm,
            int newWidth,
            int newHeight)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        if (scaleWidth < scaleHeight) {
            scaleHeight = scaleWidth;
        } else {
            scaleWidth = scaleHeight;
        }

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // "recreate" the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    public static Bitmap rotateBitmap(
            Bitmap bitmap,
            int exifOrientation)
    {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
        }

        try {
            Bitmap bmRotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    public static int getOrientationFromExif(File imgFile)
            throws IOException
    {
        ExifInterface exif = new ExifInterface(imgFile.getCanonicalPath());
        return exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
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
