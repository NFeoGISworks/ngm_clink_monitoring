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
    String FRAGMENT_SYNC_LOGIN             = "NGWLogin";
    String FRAGMENT_PERFORM_1ST_SYNC       = "Perform1stSync";
    String FRAGMENT_STATUS_BAR             = "StatusBar";
    String FRAGMENT_OBJECT_TYPES           = "ObjectTypes";
    String FRAGMENT_LINE_LIST              = "LineList";
    String FRAGMENT_OBJECT_LIST            = "ObjectList";
    String FRAGMENT_OBJECT_STATUS          = "ObjectStatus";
    String FRAGMENT_CREATE_OBJECT          = "CreateObject";
    String FRAGMENT_MAP                    = "Map";
    String FRAGMENT_ATTRIBUTES             = "Attributes";
    String FRAGMENT_DISTANCE_EXCEEDED      = "DistanceExceeded";
    String FRAGMENT_YES_NO_DIALOG          = "YesNoDialog";
    String FRAGMENT_SET_LINE_STATUS_DIALOG = "SetLineStatusDialog";
    String FRAGMENT_SYNC_DIALOG            = "SyncDialog";

    String JSON_STATUS_KEY            = "status";
    String JSON_UPDATE_DT_KEY         = "update_dt";
    String JSON_IS_STATUS_CHANGED_KEY = "is_status_changed";
    String JSON_REGION_KEY            = "region";
    String JSON_DISTRICT_KEY          = "district";

    String JSON_DEVICE_UUID_KEY = "device_uuid";
    String JSON_DATE_KEY        = "date";
    String JSON_SERVER_URL_KEY  = "server_url";
    String JSON_LOGIN_KEY       = "login";
    String JSON_MODEL_NAME_KEY  = "model_name";
    String JSON_REPORT_TYPE_KEY = "message_type";
    String JSON_LOGCAT_KEY      = "logcat";
    String JSON_FILE_UPLOAD_KEY = "file_upload";

    String JSON_MAIN_REPORT_TYPE_VALUE       = "main";
    String JSON_MAIN_ERROR_REPORT_TYPE_VALUE = "main_error";
    String JSON_SYNC_REPORT_TYPE_VALUE       = "sync";
    String JSON_SYNC_ERROR_REPORT_TYPE_VALUE = "sync_error";
    String JSON_WORK_DATA_REPORT_TYPE_VALUE  = "work_data";

    String FOCL_REPORTS_DIR                 = "reports";
    String FOCL_MAIN_LOGCAT_FILE_NAME       = "main_logcat";
    String FOCL_MAIN_ERROR_LOGCAT_FILE_NAME = "main_error_logcat";
    String FOCL_SYNC_LOGCAT_FILE_NAME       = "sync_logcat";
    String FOCL_SYNC_ERROR_LOGCAT_FILE_NAME = "sync_error_logcat";
    String FOCL_REPORT_FILE_EXT             = ".log";

    String FOCL_SEND_REPORT_FROM_MAIN = "send_report_from_main";
    String FOCL_SEND_WORK_DATA        = "send_work_data";

    String JSON_OPTICAL_CABLE_VALUE      = "optical_cable";
    String JSON_FOSC_VALUE               = "fosc";
    String JSON_OPTICAL_CROSS_VALUE      = "optical_cross";
    String JSON_ACCESS_POINT_VALUE       = "access_point";
    String JSON_SPECIAL_TRANSITION_VALUE = "special_transition";

    String JSON_REAL_OPTICAL_CABLE_POINT_VALUE      = "real_optical_cable_point";
    String JSON_REAL_FOSC_VALUE                     = "real_fosc";
    String JSON_REAL_OPTICAL_CROSS_VALUE            = "real_optical_cross";
    String JSON_REAL_ACCESS_POINT_VALUE             = "real_access_point";
    String JSON_REAL_SPECIAL_TRANSITION_POINT_VALUE = "real_special_transition_point";

    int LAYERTYPE_FOCL_VECTOR  = 1001;
    int LAYERTYPE_FOCL_STRUCT  = 1002;
    int LAYERTYPE_FOCL_PROJECT = 1003;

    int LAYERTYPE_FOCL_UNKNOWN            = 1100;
    int LAYERTYPE_FOCL_OPTICAL_CABLE      = 1101;
    int LAYERTYPE_FOCL_FOSC               = 1102;
    int LAYERTYPE_FOCL_OPTICAL_CROSS      = 1103;
    int LAYERTYPE_FOCL_ACCESS_POINT       = 1104;
    int LAYERTYPE_FOCL_SPECIAL_TRANSITION = 1105;

    int LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT      = 1201;
    int LAYERTYPE_FOCL_REAL_FOSC                     = 1202;
    int LAYERTYPE_FOCL_REAL_OPTICAL_CROSS            = 1203;
    int LAYERTYPE_FOCL_REAL_ACCESS_POINT             = 1204;
    int LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT = 1205;

    String FIELD_NAME        = "name";
    String FIELD_DESCRIPTION = "description";
    String FIELD_BUILT_DATE  = "built_date";

    String FIELD_PROJ_STATUSES         = "proj_statuses";
    String FIELD_START_POINT           = "start_point";
    String FIELD_LAYING_METHOD         = "laying_method";
    String FIELD_FOSC_TYPE             = "type_fosc";
    String FIELD_FOSC_PLACEMENT        = "fosc_placement";
    String FIELD_OPTICAL_CROSS_TYPE    = "type_optical_cross";
    String FIELD_SPECIAL_LAYING_METHOD = "special_laying_method";
    String FIELD_MARK_TYPE             = "special_laying_number";

    String FIELD_VALUE_STATUS_PROJECT     = "project";
    String FIELD_VALUE_STATUS_IN_PROGRESS = "in_progress";
    String FIELD_VALUE_STATUS_BUILT       = "built";

    int STATUS_PROJECT_INDEX     = 0;
    int STATUS_IN_PROGRESS_INDEX = 1;
    int STATUS_BUILT_INDEX       = 2;

    String FIELD_STATUS_BUILT    = "status_built";
    String FIELD_STATUS_BUILT_CH = "status_built_ch";

    String FIELD_VALUE_UNKNOWN = "unknown";
    String FIELD_VALUE_PROJECT = "project";
    String FIELD_VALUE_BUILT   = "built";

    String FIELD_VALUE_GROUND              = "ground";
    String FIELD_VALUE_AIR_LINK            = "air_link";
    String FIELD_VALUE_TRANSMISSION_TOWERS = "transmission_towers";
    String FIELD_VALUE_CANALIZATION        = "canalization";
    String FIELD_VALUE_SEWER               = "sewer";
    String FIELD_VALUE_BUILDING            = "building";

    String FOCL_PROJECT      = "FOCL_project";
    String FOCL_ACCOUNT_NAME = "Compulink Monitoring";

    //TODO: remove it
//     String FOCL_DEFAULT_ACCOUNT_URL = "http://176.9.38.120/cl2";
    String FOCL_DEFAULT_ACCOUNT_URL = "https://gis.compulink.ru";
    String FOCL_USER_FOCL_LIST_URL  = "/compulink/mobile/user_focl_list";
    String FOCL_ALL_DICTS_URL       = "/compulink/mobile/all_dicts";
    String FOCL_SET_FOCL_STATUS_URL = "/compulink/mobile/set_focl_status";
    String FOCL_REPORT_URL          = "/mobile_debug/message/append";

    String FOCL_UPDATE_URL =
            "https://play.google.com/store/apps/details?id=com.nextgis.ngm_clink_monitoring";

    String FOCL_NTP_URL            = "pool.ntp.org";
    int    FOCL_NTP_TIMEOUT_MILLIS = 10000;

    // If you change it, change it in aFileDialog/res/values/strings/daidalos_confirm_move_to_folder
    String FOCL_DATA_DIR  = "ngm_clink_monitoring";
    String FOCL_PHOTO_DIR = "photo";

    String FOCL_ZIP_WORK_DATA_FILE_NAME = "work_data";
    String FOCL_ZIP_WORK_DATA_FILE_EXT  = ".zip";

    long DEFAULT_SYNC_PERIOD_SEC_LONG = 3600;

    int PHOTO_MAX_SIZE_PX           = 640;
    int PHOTO_JPEG_COMPRESS_QUALITY = 75;

    int MAX_DISTANCE_FROM_PREV_POINT      = 300; // R.strings.distance_from_prev_point_exceeded
    int MAX_DISTANCE_FROM_OBJECT_TO_PHOTO = 100; // R.strings.photo_not_saved_distance_exceed

    float  MAX_TAKEN_ACCURACY              = 100;
    int    MAX_ACCURACY                    = 10;
    int    MIN_ACCURACY_TAKE_COUNT         = 8;
    int    MAX_ACCURACY_TAKE_COUNT         = 12;
    long   MAX_ACCURACY_TAKE_TIME          = 60000;
    long   ACCURACY_PUBLISH_PROGRESS_DELAY = 500;
    String ACCURACY_CIRCULAR_ERROR_STR     = "CE95";

    String TEMP_PHOTO_FILE_PREFIX = "temp-photo-";

    String KEY_IS_FULL_SYNC = "is_full_sync";

    String NO_SDCARD = "no_sdcard";

    String FOCL_STRUCT_REMOTE_ID  = "focl_struct_remote_id";
    String FOCL_STRUCT_LAYER_TYPE = "focl_struct_layer_type";
    String TEMP_PHOTO_PATH        = "temp_photo_path";
    String ACCURATE_LOCATION      = "accurate_location";
    String VIEW_STATE             = "view_state";
    String IS_ACCURATE_TAKING     = "is_accurate_taking";
    String OBJECT_LAYER_NAME      = "object_layer_name";
    String DISTANCE               = "distance";
}
