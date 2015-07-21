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

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.UUID;


public class Utils
{
    /**
     * Return pseudo unique ID
     * <p/>
     * http://stackoverflow.com/a/17625641/4727406
     * <p/>
     * http://stackoverflow.com/a/2853253/4727406
     *
     * @return ID
     */
    public static String getDeviceUniqueID(Context context)
    {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        final String tmDevice = "" + tm.getDeviceId();
        final String tmSerial = "" + tm.getSimSerialNumber();
        final String androidId = "" + android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        String m_szDevIDShort =
                tmDevice + tmSerial + androidId + "35" + (Build.BOARD.length() % 10) +
                        (Build.BRAND.length() % 10) +
                        (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) +
                        (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) +
                        (Build.PRODUCT.length() % 10);

        String serial;
        try {
            // Go ahead and return the serial for api => 9
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial_unknown";
        }

        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
}
