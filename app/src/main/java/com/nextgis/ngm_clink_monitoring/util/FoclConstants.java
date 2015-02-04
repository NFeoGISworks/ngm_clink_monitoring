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
 * the Free Software Foundation, either version 2 of the License, or
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

    public static final int LAYERTYPE_FOCL_UNKNOWN         = 1100;
    public static final int LAYERTYPE_FOCL_OPTICAL_CABLE   = 1101;
    public static final int LAYERTYPE_FOCL_FOSC            = 1102;
    public static final int LAYERTYPE_FOCL_OPTICAL_CROSS   = 1103;
    public static final int LAYERTYPE_FOCL_TELECOM_CABINET = 1104;
    public static final int LAYERTYPE_FOCL_POLE            = 1105;
    public static final int LAYERTYPE_FOCL_ENDPOINT        = 1106;
    public static final int LAYERTYPE_FOCL_LINE_MEASURING  = 1107;

    public static final String FIELD_NAME            = "name";
    public static final String FIELD_STATUS_BUILT    = "status_built";
    public static final String FIELD_STATUS_BUILT_CH = "status_built_ch";

    public static final String FIELD_VALUE_BUILT = "built";
}
