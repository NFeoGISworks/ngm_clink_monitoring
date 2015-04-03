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

public interface FoclConstants
{
    public static final int LAYERTYPE_FOCL_VECTOR  = 1001;
    public static final int LAYERTYPE_FOCL_STRUCT  = 1002;
    public static final int LAYERTYPE_FOCL_PROJECT = 1003;

    public static final int LAYERTYPE_FOCL_UNKNOWN       = 1100;
    public static final int LAYERTYPE_FOCL_OPTICAL_CABLE = 1101;
    public static final int LAYERTYPE_FOCL_FOSC          = 1102;
    public static final int LAYERTYPE_FOCL_OPTICAL_CROSS = 1103;
    public static final int LAYERTYPE_FOCL_ACCESS_POINT  = 1104;
    public static final int LAYERTYPE_FOCL_ENDPOINT      = 1105;

    public static final String FIELD_NAME              = "name";
    public static final String FIELD_TYPE_ENDPOINT     = "type_endpoint";
    public static final String FIELD_STATUS_BUILT      = "status_built";
    public static final String FIELD_STATUS_BUILT_CH   = "status_built_ch";
    public static final String FIELD_STATUS_MEASURE    = "status_measure";
    public static final String FIELD_STATUS_MEASURE_CH = "status_measure_ch";

    public static final String FIELD_VALUE_UNKNOWN     = "unknown";
    public static final String FIELD_VALUE_POINT_B     = "point_b";
    public static final String FIELD_VALUE_PROJECT     = "project";
    public static final String FIELD_VALUE_BUILT       = "built";
    public static final String FIELD_VALUE_NOT_MEASURE = "not_measure";
    public static final String FIELD_VALUE_MEASURE     = "measure";

    public static final String FOCL_PROJECT             = "FOCL_project";
    public static final String FOCL_ACCOUNT_NAME        = "Compulink Monitoring Account";
    //TODO: remove it
    public static final String FOCL_DEFAULT_ACCOUNT_URL = "http://176.9.38.120/cl2";
    //    public static final String FOCL_DEFAULT_ACCOUNT_URL = "https://gis.compulink.ru";
    public static final String FOCL_DATA_DIR            = "ngm_clink_monitoring";
    public static final String FOCL_PHOTO_DIR           = "photo";

    public static final long DEFAULT_SYNC_PERIOD_SEC_LONG = 300;

    public static final int PHOTO_MAX_SIZE_PX           = 640;
    public static final int PHOTO_JPEG_COMPRESS_QUALITY = 75;
}
