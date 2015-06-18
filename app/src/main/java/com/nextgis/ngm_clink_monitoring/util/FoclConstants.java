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

public interface FoclConstants
{
    String FRAGMENT_SYNC_LOGIN       = "NGWLogin";
    String FRAGMENT_PERFORM_1ST_SYNC = "Perform1stSync";
    String FRAGMENT_STATUS_BAR       = "StatusBar";
    String FRAGMENT_OBJECT_TYPES     = "ObjectTypes";
    String FRAGMENT_LINE_LIST        = "LineList";
    String FRAGMENT_OBJECT_LIST      = "ObjectList";
    String FRAGMENT_OBJECT_STATUS    = "ObjectStatus";
    String FRAGMENT_OBJECT_MEASURE   = "ObjectMeasure";
    String FRAGMENT_MAP              = "Map";
    String FRAGMENT_ATTRIBUTES       = "Attributes";

    String JSON_REGION_KEY   = "region";
    String JSON_DISTRICT_KEY = "district";

    String JSON_OPTICAL_CABLE_VALUE = "optical_cable";
    String JSON_FOSC_VALUE          = "fosc";
    String JSON_OPTICAL_CROSS_VALUE = "optical_cross";
    String JSON_ACCESS_POINT_VALUE  = "access_point";
    String JSON_ENDPOINT_VALUE      = "endpoint";

    int LAYERTYPE_FOCL_VECTOR  = 1001;
    int LAYERTYPE_FOCL_STRUCT  = 1002;
    int LAYERTYPE_FOCL_PROJECT = 1003;

    int LAYERTYPE_FOCL_UNKNOWN       = 1100;
    int LAYERTYPE_FOCL_OPTICAL_CABLE = 1101;
    int LAYERTYPE_FOCL_FOSC          = 1102;
    int LAYERTYPE_FOCL_OPTICAL_CROSS = 1103;
    int LAYERTYPE_FOCL_ACCESS_POINT  = 1104;
    int LAYERTYPE_FOCL_ENDPOINT      = 1105;

    String FIELD_NAME              = "name";
    String FIELD_LAYING_METHOD     = "laying_method";
    String FIELD_TYPE_ENDPOINT     = "type_endpoint";
    String FIELD_STATUS_BUILT      = "status_built";
    String FIELD_STATUS_BUILT_CH   = "status_built_ch";
    String FIELD_STATUS_MEASURE    = "status_measure";
    String FIELD_STATUS_MEASURE_CH = "status_measure_ch";

    String FIELD_VALUE_UNKNOWN     = "unknown";
    String FIELD_VALUE_POINT_A     = "point_a";
    String FIELD_VALUE_POINT_B     = "point_b";
    String FIELD_VALUE_PROJECT     = "project";
    String FIELD_VALUE_BUILT       = "built";
    String FIELD_VALUE_NOT_MEASURE = "not_measure";
    String FIELD_VALUE_MEASURE     = "measure";

    String FIELD_VALUE_GROUND              = "ground";
    String FIELD_VALUE_AIR_LINK            = "air_link";
    String FIELD_VALUE_TRANSMISSION_TOWERS = "transmission_towers";
    String FIELD_VALUE_CANALIZATION        = "canalization";
    String FIELD_VALUE_SEWER               = "sewer";
    String FIELD_VALUE_BUILDING            = "building";

    String FOCL_PROJECT      = "FOCL_project";
    String FOCL_ACCOUNT_NAME = "Compulink Monitoring";
    //TODO: remove it
    //String FOCL_DEFAULT_ACCOUNT_URL = "http://176.9.38.120/cl2";
    String FOCL_DEFAULT_ACCOUNT_URL = "https://gis.compulink.ru";

    // If you change it, change it in aFileDialog/res/values/strings/daidalos_confirm_move_to_folder
    String FOCL_DATA_DIR  = "ngm_clink_monitoring";
    String FOCL_PHOTO_DIR = "photo";

    long DEFAULT_SYNC_PERIOD_SEC_LONG = 300;

    int PHOTO_MAX_SIZE_PX           = 640;
    int PHOTO_JPEG_COMPRESS_QUALITY = 75;
}
