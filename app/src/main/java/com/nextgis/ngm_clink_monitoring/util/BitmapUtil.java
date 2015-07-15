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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;
import com.nextgis.maplib.util.Constants;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


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
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
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
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

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
            Location location,
            long gpsTimeOffset)
            throws IOException
    {
        if (location == null) {
            return;
        }

        ExifInterface exif = new ExifInterface(imgFile.getCanonicalPath());


        // write GPSLatitude
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


        // write GPSLongitude
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


        // write GPSDateStamp and GPSTimeStamp
        TimeZone timeZone = TimeZone.getDefault();
        timeZone.setRawOffset(0);
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(location.getTime() + gpsTimeOffset);
        Log.d(Constants.TAG, "write EXIF, AccurateGpsTime: " + location.getTime() + gpsTimeOffset);

        int timeYear = calendar.get(Calendar.YEAR);
        int timeMonth = calendar.get(Calendar.MONTH);
        ++timeMonth;
        int timeDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        int timeHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int timeMinutes = calendar.get(Calendar.MINUTE);
        int timeSeconds = calendar.get(Calendar.SECOND);

        String exifGPSDatestamp = timeYear + ":" + timeMonth + ":" + timeDayOfMonth;
        String exifGPSTimestamp = timeHourOfDay + "/1," + timeMinutes + "/1," + timeSeconds + "/1";

        exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, exifGPSDatestamp);
        exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, exifGPSTimestamp);

        Log.d(Constants.TAG, "write EXIF, exifGPSDatestamp: " + exifGPSDatestamp);
        Log.d(Constants.TAG, "write EXIF, exifGPSTimestamp: " + exifGPSTimestamp);

        exif.saveAttributes();
    }


    public static void copyExifData(
            File srcImgFile,
            File dstImgFile)
            throws IOException
    {
        ExifInterface srcExif = new ExifInterface(srcImgFile.getCanonicalPath());
        ExifInterface dstExif = new ExifInterface(dstImgFile.getCanonicalPath());

        int buildSDKVersion = Build.VERSION.SDK_INT;

        // From API 11
        if (buildSDKVersion >= Build.VERSION_CODES.HONEYCOMB) {
            if (srcExif.getAttribute(ExifInterface.TAG_APERTURE) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_APERTURE,
                        srcExif.getAttribute(ExifInterface.TAG_APERTURE));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_EXPOSURE_TIME,
                        srcExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_ISO) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_ISO, srcExif.getAttribute(ExifInterface.TAG_ISO));
            }
        }

        // From API 9
        if (buildSDKVersion >= Build.VERSION_CODES.GINGERBREAD) {
            if (srcExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_GPS_ALTITUDE,
                        srcExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_GPS_ALTITUDE_REF,
                        srcExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF));
            }
        }

        // From API 8
        if (buildSDKVersion >= Build.VERSION_CODES.FROYO) {
            if (srcExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_FOCAL_LENGTH,
                        srcExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_GPS_DATESTAMP,
                        srcExif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        srcExif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));
            }
            if (srcExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP) != null) {
                dstExif.setAttribute(
                        ExifInterface.TAG_GPS_TIMESTAMP,
                        srcExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
            }
        }

        if (srcExif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_DATETIME, srcExif.getAttribute(ExifInterface.TAG_DATETIME));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_FLASH) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_FLASH, srcExif.getAttribute(ExifInterface.TAG_FLASH));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_GPS_LATITUDE,
                    srcExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    srcExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_GPS_LONGITUDE,
                    srcExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    srcExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_IMAGE_LENGTH,
                    srcExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_IMAGE_WIDTH,
                    srcExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_MAKE) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_MAKE, srcExif.getAttribute(ExifInterface.TAG_MAKE));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_MODEL) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_MODEL, srcExif.getAttribute(ExifInterface.TAG_MODEL));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_ORIENTATION) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_ORIENTATION,
                    srcExif.getAttribute(ExifInterface.TAG_ORIENTATION));
        }
        if (srcExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE) != null) {
            dstExif.setAttribute(
                    ExifInterface.TAG_WHITE_BALANCE,
                    srcExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
        }

        dstExif.saveAttributes();
    }


    public static Date getExifDate(File imgFile)
            throws IOException
    {
        ExifInterface imgFileExif = new ExifInterface(imgFile.getCanonicalPath());

        if (imgFileExif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
            String imgDateTime = imgFileExif.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);

            try {
                return simpleDateFormat.parse(imgDateTime);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
