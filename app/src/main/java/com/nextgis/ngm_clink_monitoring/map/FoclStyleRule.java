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

package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import com.nextgis.maplib.api.IStyleRule;
import com.nextgis.maplib.display.SimpleMarkerStyle;
import com.nextgis.maplib.display.SimpleTextLineStyle;
import com.nextgis.maplib.display.SimpleTextMarkerStyle;
import com.nextgis.maplib.display.Style;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import static com.nextgis.maplib.util.Constants.FIELD_ID;


public class FoclStyleRule
        implements IStyleRule
{
    protected Context mContext;
    protected String  mLayerPathName;
    protected int     mFoclLayerType;


    public FoclStyleRule(
            Context context,
            String layerPathName,
            int foclLayerType)
    {
        mContext = context;
        mLayerPathName = layerPathName;
        mFoclLayerType = foclLayerType;
    }


    public static Style getDefaultStyle(int foclLayerType)
            throws Exception
    {
        switch (foclLayerType) {

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                SimpleTextLineStyle ocStyle = new SimpleTextLineStyle();
                ocStyle.setType(SimpleTextLineStyle.LineStyleSolid);
                ocStyle.setColor(Color.BLACK);
                ocStyle.setOutColor(Color.GREEN);
                ocStyle.setLineText("?");
                ocStyle.setWidth(3);
                return ocStyle;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                SimpleMarkerStyle foscStyle = new SimpleMarkerStyle();
                foscStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                foscStyle.setColor(Color.WHITE);
                foscStyle.setOutlineColor(Color.BLACK);
                foscStyle.setSize(9);
                foscStyle.setWidth(3);
                return foscStyle;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                SimpleMarkerStyle crossStyle = new SimpleMarkerStyle();
                crossStyle.setType(SimpleMarkerStyle.MarkerStyleCrossedBox);
                crossStyle.setColor(Color.WHITE);
                crossStyle.setOutlineColor(Color.BLACK);
                crossStyle.setSize(9);
                crossStyle.setWidth(3);
                return crossStyle;

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                SimpleMarkerStyle aPointStyle = new SimpleMarkerStyle();
                aPointStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                aPointStyle.setColor(Color.BLACK);
                aPointStyle.setOutlineColor(Color.TRANSPARENT);
                aPointStyle.setSize(9);
                aPointStyle.setWidth(3);
                return aPointStyle;

            case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
                SimpleTextMarkerStyle epStyle = new SimpleTextMarkerStyle();
                epStyle.setType(SimpleTextMarkerStyle.MarkerStyleTextCircle);
                epStyle.setColor(Color.WHITE);
                epStyle.setOutlineColor(Color.BLUE);
                epStyle.setText("?");
                epStyle.setSize(9);
                epStyle.setWidth(3);
                return epStyle;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CABLE_POINT:
                SimpleMarkerStyle realOcPointStyle = new SimpleMarkerStyle();
                realOcPointStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                realOcPointStyle.setColor(Color.BLUE);
                realOcPointStyle.setOutlineColor(Color.GREEN);
                realOcPointStyle.setSize(9);
                realOcPointStyle.setWidth(3);
                return realOcPointStyle;

            case FoclConstants.LAYERTYPE_FOCL_REAL_FOSC:
                SimpleMarkerStyle realFoscStyle = new SimpleMarkerStyle();
                realFoscStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                realFoscStyle.setColor(Color.GREEN);
                realFoscStyle.setOutlineColor(Color.BLACK);
                realFoscStyle.setSize(9);
                realFoscStyle.setWidth(3);
                return realFoscStyle;

            case FoclConstants.LAYERTYPE_FOCL_REAL_OPTICAL_CROSS:
                SimpleMarkerStyle realCrossStyle = new SimpleMarkerStyle();
                realCrossStyle.setType(SimpleMarkerStyle.MarkerStyleCrossedBox);
                realCrossStyle.setColor(Color.GREEN);
                realCrossStyle.setOutlineColor(Color.BLACK);
                realCrossStyle.setSize(9);
                realCrossStyle.setWidth(3);
                return realCrossStyle;

            case FoclConstants.LAYERTYPE_FOCL_REAL_ACCESS_POINT:
                SimpleMarkerStyle realApointStyle = new SimpleMarkerStyle();
                realApointStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                realApointStyle.setColor(Color.BLACK);
                realApointStyle.setOutlineColor(Color.GREEN);
                realApointStyle.setSize(9);
                realApointStyle.setWidth(3);
                return realApointStyle;

            case FoclConstants.LAYERTYPE_FOCL_REAL_SPECIAL_TRANSITION_POINT:
                SimpleMarkerStyle realStPointStyle = new SimpleMarkerStyle();
                realStPointStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                realStPointStyle.setColor(Color.RED);
                realStPointStyle.setOutlineColor(Color.GREEN);
                realStPointStyle.setSize(9);
                realStPointStyle.setWidth(3);
                return realStPointStyle;

            default:
                throw new Exception("Unknown value of foclLayerType: " + foclLayerType);
        }
    }


    @Override
    public void setStyleParams(
            Style style,
            long objectId)
    {
        Cursor cursor;
        String[] select;
        Uri uri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" + mLayerPathName + "/" +
                        objectId);
        String type = null;
        String status = null;

        switch (mFoclLayerType) {

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                select = new String[] {
                        FIELD_ID, FoclConstants.FIELD_LAYING_METHOD/*,
                        FoclConstants.FIELD_STATUS_BUILT*/};

                try {
                    cursor = mContext.getContentResolver().query(uri, select, null, null, null);

                } catch (Exception e) {
                    //Log.d(TAG, e.getLocalizedMessage());
                    cursor = null;
                }

                if (null != cursor) {
                    try {
                        if (cursor.moveToFirst()) {
                            type = cursor.getString(
                                    cursor.getColumnIndex(FoclConstants.FIELD_LAYING_METHOD));
//                            status = cursor.getString(
//                                    cursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
                        }
                    } catch (Exception e) {
                        //Log.d(TAG, e.getLocalizedMessage());
                    } finally {
                        cursor.close();
                    }
                }
                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
            case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
//                select = new String[] {FIELD_ID/*, FoclConstants.FIELD_STATUS_BUILT*/};
//
//                try {
//                    cursor = mVectorLayer.query(uri, select, null, null, null, null);
//
//                } catch (Exception e) {
//                    //Log.d(TAG, e.getLocalizedMessage());
//                    cursor = null;
//                }
//
//                if (null != cursor) {
//                    try {
//                        if (cursor.moveToFirst()) {
////                            status = cursor.getString(
////                                    cursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
//                        }
//                        cursor.close();
//                    } catch (Exception e) {
//                        //Log.d(TAG, e.getLocalizedMessage());
//                    } finally {
//                        cursor.close();
//                    }
//                }
                break;
        }


        if (TextUtils.isEmpty(type)) {
            type = "";
        }

        if (TextUtils.isEmpty(status)) {
            status = "";
        }


        switch (mFoclLayerType) {
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                SimpleTextLineStyle ocStyle = (SimpleTextLineStyle) style;

                boolean notDefined = false;

                switch (status) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                    default:
                        ocStyle.setType(SimpleTextLineStyle.LineStyleDash);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILT:
                        ocStyle.setType(SimpleTextLineStyle.LineStyleEdgingSolid);
                        break;
//                    default:
//                        notDefined = true;
//                        break;
                }

//                if (!notDefined) {
                switch (type) {
                    case FoclConstants.FIELD_VALUE_GROUND:
                        ocStyle.setColor(0xFF9C7900);
                        break;
                    case FoclConstants.FIELD_VALUE_OVERPASS:
                        ocStyle.setColor(0xFF63DFD6);
                        break;
                    case FoclConstants.FIELD_VALUE_TRANSMISSION_TOWERS:
                        ocStyle.setColor(Color.BLACK);
                        break;
                    case FoclConstants.FIELD_VALUE_CANALIZATION:
                        ocStyle.setColor(0xFFFF8A00);
                        break;
                    case FoclConstants.FIELD_VALUE_OTHER:
                        ocStyle.setColor(Color.MAGENTA);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILDING:
                        ocStyle.setColor(Color.BLUE);
                        break;
                    default:
                        notDefined = true;
                        break;
                }
//                }

                if (!notDefined) {
                    ocStyle.setOutColor(Color.GREEN);

                } else {
                    ocStyle.setColor(Color.BLACK);
                    ocStyle.setOutColor(Color.RED);
                    ocStyle.setType(SimpleTextLineStyle.LineStyleTextSolid);
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
                SimpleMarkerStyle foscStyle = (SimpleMarkerStyle) style;

                switch (status) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                    default:
                        foscStyle.setColor(Color.WHITE);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILT:
                        foscStyle.setColor(Color.GREEN);
                        break;
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
                SimpleMarkerStyle crossStyle = (SimpleMarkerStyle) style;

                switch (status) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                    default:
                        crossStyle.setColor(Color.WHITE);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILT:
                        crossStyle.setColor(Color.GREEN);
                        break;
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                SimpleMarkerStyle apStyle = (SimpleMarkerStyle) style;

                switch (status) {
                    case FoclConstants.FIELD_VALUE_PROJECT:
                    default:
                        apStyle.setOutlineColor(Color.TRANSPARENT);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILT:
                        apStyle.setOutlineColor(Color.GREEN);
                        break;
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_SPECIAL_TRANSITION:
                SimpleTextMarkerStyle epStyle = (SimpleTextMarkerStyle) style;

                switch (type) {
                    default:
                        epStyle.setOutlineColor(Color.BLUE);
                        epStyle.setText("?");
                        break;
                }

                break;

            default:
//                Log.d(TAG, "Unknown value of mFoclLayerType: " + mFoclLayerType);
                break;
        }
    }
}