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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import com.nextgis.ngm_clink_monitoring.R;

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


    public static boolean isOnlyGpsLocationModeEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode;

            try {
                locationMode = Settings.Secure.getInt(
                        context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                return false;
            }

            // GPS must be enabled
            return Settings.Secure.LOCATION_MODE_SENSORS_ONLY == locationMode ||
                    Settings.Secure.LOCATION_MODE_HIGH_ACCURACY == locationMode;

        } else {
            String locationProviders = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            if (TextUtils.isEmpty(locationProviders)) {
                return false;
            }

            String[] providers = locationProviders.split(",");

            for (String provider : providers) {
                // GPS must be enabled
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    return true;
                }
            }

            return false;
        }
    }


    public static void showSettingsLocationAlert(final Context context)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setIcon(R.drawable.ic_action_warning).setCancelable(false)
                .setTitle(context.getResources().getString(R.string.location_off))
                .setMessage(context.getResources().getString(R.string.location_off_message))
                .setPositiveButton(
                        context.getResources().getString(R.string.ok),

                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        })
                .show();
    }
}
