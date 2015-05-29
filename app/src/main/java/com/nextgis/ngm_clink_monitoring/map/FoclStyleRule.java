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

package com.nextgis.ngm_clink_monitoring.map;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.nextgis.maplib.display.IStyleRule;
import com.nextgis.maplib.display.SimpleMarkerStyle;
import com.nextgis.maplib.display.SimpleTextLineStyle;
import com.nextgis.maplib.display.SimpleTextMarkerStyle;
import com.nextgis.maplib.display.Style;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import static com.nextgis.maplib.util.Constants.FIELD_ID;
import static com.nextgis.maplib.util.Constants.TAG;


public class FoclStyleRule
        implements IStyleRule
{
    protected VectorLayer mVectorLayer;
    protected int         mFoclLayerType;


    public FoclStyleRule(
            VectorLayer vectorLayer,
            int foclLayerType)
    {
        mVectorLayer = vectorLayer;
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
                SimpleMarkerStyle apStyle = new SimpleMarkerStyle();
                apStyle.setType(SimpleMarkerStyle.MarkerStyleCircle);
                apStyle.setColor(Color.BLACK);
                apStyle.setOutlineColor(Color.TRANSPARENT);
                apStyle.setSize(9);
                apStyle.setWidth(3);
                return apStyle;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                SimpleTextMarkerStyle epStyle = new SimpleTextMarkerStyle();
                epStyle.setType(SimpleTextMarkerStyle.MarkerStyleTextCircle);
                epStyle.setColor(Color.WHITE);
                epStyle.setOutlineColor(Color.BLUE);
                epStyle.setMarkerText("?");
                epStyle.setSize(9);
                epStyle.setWidth(3);
                return epStyle;

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
                "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                        mVectorLayer.getPath().getName() + "/" + objectId);
        String type = null;
        String status = null;

        switch (mFoclLayerType) {

            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CABLE:
                select = new String[] {
                        FIELD_ID,
                        FoclConstants.FIELD_LAYING_METHOD,
                        FoclConstants.FIELD_STATUS_BUILT};

                try {
                    cursor = mVectorLayer.query(uri, select, null, null, null);

                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                    cursor = null;
                }

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        type = cursor.getString(
                                cursor.getColumnIndex(FoclConstants.FIELD_LAYING_METHOD));
                        status = cursor.getString(
                                cursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
                    }
                    cursor.close();
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_FOSC:
            case FoclConstants.LAYERTYPE_FOCL_OPTICAL_CROSS:
            case FoclConstants.LAYERTYPE_FOCL_ACCESS_POINT:
                select = new String[] {FIELD_ID, FoclConstants.FIELD_STATUS_BUILT};

                try {
                    cursor = mVectorLayer.query(uri, select, null, null, null);

                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                    cursor = null;
                }

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        status = cursor.getString(
                                cursor.getColumnIndex(FoclConstants.FIELD_STATUS_BUILT));
                    }
                    cursor.close();
                }

                break;

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                select = new String[] {
                        FIELD_ID, FoclConstants.FIELD_TYPE_ENDPOINT};

                try {
                    cursor = mVectorLayer.query(uri, select, null, null, null);

                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                    cursor = null;
                }

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        type = cursor.getString(
                                cursor.getColumnIndex(FoclConstants.FIELD_TYPE_ENDPOINT));
                    }
                    cursor.close();
                }

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
                        ocStyle.setType(SimpleTextLineStyle.LineStyleDash);
                        break;
                    case FoclConstants.FIELD_VALUE_BUILT:
                        ocStyle.setType(SimpleTextLineStyle.LineStyleEdgingSolid);
                        break;
                    default:
                        notDefined = true;
                        break;
                }

                if (!notDefined) {
                    switch (type) {
                        case FoclConstants.FIELD_VALUE_GROUND:
                            ocStyle.setColor(0xFF9C7900);
                            break;
                        case FoclConstants.FIELD_VALUE_AIR_LINK:
                            ocStyle.setColor(0xFF63DFD6);
                            break;
                        case FoclConstants.FIELD_VALUE_TRANSMISSION_TOWERS:
                            ocStyle.setColor(Color.BLACK);
                            break;
                        case FoclConstants.FIELD_VALUE_CANALIZATION:
                            ocStyle.setColor(0xFFFF8A00);
                            break;
                        case FoclConstants.FIELD_VALUE_SEWER:
                            ocStyle.setColor(Color.MAGENTA);
                            break;
                        case FoclConstants.FIELD_VALUE_BUILDING:
                            ocStyle.setColor(Color.BLUE);
                            break;
                        default:
                            notDefined = true;
                            break;
                    }
                }

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

            case FoclConstants.LAYERTYPE_FOCL_ENDPOINT:
                SimpleTextMarkerStyle epStyle = (SimpleTextMarkerStyle) style;

                switch (type) {
                    case FoclConstants.FIELD_VALUE_POINT_A:
                        epStyle.setOutlineColor(Color.RED);
                        epStyle.setMarkerText("A");
                        break;
                    case FoclConstants.FIELD_VALUE_POINT_B:
                        epStyle.setOutlineColor(Color.BLUE);
                        epStyle.setMarkerText("B");
                        break;
                    default:
                        epStyle.setOutlineColor(Color.BLUE);
                        epStyle.setMarkerText("?");
                        break;
                }

                break;
        }
    }
}