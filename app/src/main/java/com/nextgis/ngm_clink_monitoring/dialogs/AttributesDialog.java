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

package com.nextgis.ngm_clink_monitoring.dialogs;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nextgis.maplib.datasource.Field;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclDictItem;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;
import static com.nextgis.maplib.util.Constants.FIELD_ID;


public class AttributesDialog
        extends YesNoDialog
{
    protected FoclVectorLayer mFoclVectorLayer;
    protected long            mObjectId;

    protected LinearLayout mAttributesLayout;


    public YesNoDialog setParams(
            FoclVectorLayer selectedLayer,
            long selectedObjectId)
    {
        mFoclVectorLayer = selectedLayer;
        mObjectId = selectedObjectId;
        return this;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_attributes, null);

        mAttributesLayout = (LinearLayout) view.findViewById(R.id.ll_attributes);
        setAttributes();

        setTitle(getActivity().getString(R.string.object_attributes));
        setView(view, true);
        setPositiveText(getActivity().getString(R.string.ok));
        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        // do nothing
                    }
                });

        return super.onCreateDialog(savedInstanceState);
    }


    private void setAttributes()
    {
        FoclStruct struct = (FoclStruct) mFoclVectorLayer.getParent();
        FoclProject project = (FoclProject) struct.getParent();

        TextView title = (TextView) mAttributesLayout.findViewById(R.id.title);
        title.setText(mFoclVectorLayer.getName());


        // set line name
        LayoutInflater lineNameInflater = LayoutInflater.from(getActivity());
        LinearLayout lineNameRow =
                (LinearLayout) lineNameInflater.inflate(R.layout.item_attribute_row, null);

        TextView lineNameCaption = (TextView) lineNameRow.findViewById(R.id.column_name);
        lineNameCaption.setText(R.string.communication_line_colon);

        TextView lineName = (TextView) lineNameRow.findViewById(R.id.column_data);
        lineName.setText(Html.fromHtml(struct.getHtmlFormattedNameThreeStringsNormal()));

        mAttributesLayout.addView(lineNameRow);

        List<Field> fields = mFoclVectorLayer.getFields();

        // set attributes
        String selection = FIELD_ID + " = ?";
        Cursor attributes =
                mFoclVectorLayer.query(null, selection, new String[] {mObjectId + ""}, null, null);

        if (null != attributes) {
            try {
                if (attributes.moveToFirst()) {
                    for (int i = 0; i < attributes.getColumnCount(); ++i) {

                        if (attributes.isNull(i)) {
                            continue;
                        }

                        String fieldName = attributes.getColumnName(i);
                        if (fieldName.equals(FIELD_GEOM)) {
                            continue;
                        }

                        String fieldAlias = null;
                        int fieldType = Constants.NOT_FOUND;
                        for (Field field : fields) {
                            if (field.getName().equals(fieldName)) {
                                fieldAlias = field.getAlias();
                                fieldType = field.getType();
                                break;
                            }
                        }

                        if (TextUtils.isEmpty(fieldAlias)) {
                            fieldAlias = fieldName;
                        }


                        String valueText = null;
                        Long timestamp = null;

                        try {
                            switch (fieldType) {
                                case GeoConstants.FTInteger:
                                case GeoConstants.FTReal:
                                case GeoConstants.FTString:
                                    valueText = attributes.getString(i);
                                    break;

                                case GeoConstants.FTDateTime:
                                case GeoConstants.FTDate:
                                case GeoConstants.FTTime:
                                    timestamp = attributes.getLong(i);
                                    break;

                                default:
                                    continue;
                            }
                        } catch (Exception ignored) {
                            continue;
                        }

                        if (TextUtils.isEmpty(valueText) && null == timestamp) {
                            continue;
                        }


                        SimpleDateFormat sdf = null;

                        switch (fieldType) {
                            case GeoConstants.FTString:
                                String valueAlias = null;
                                FoclDictItem dictItem = project.getFoclDitcs().get(fieldName);

                                if (null != dictItem) {
                                    valueAlias = dictItem.get(valueText);
                                }
                                if (!TextUtils.isEmpty(valueAlias)) {
                                    valueText = valueAlias;
                                }
                                break;

                            case GeoConstants.FTDateTime:
                                sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                                break;

                            case GeoConstants.FTDate:
                                sdf = (SimpleDateFormat) DateFormat.getDateInstance();
                                break;

                            case GeoConstants.FTTime:
                                sdf = (SimpleDateFormat) DateFormat.getTimeInstance();
                                break;
                        }

                        switch (fieldType) {
                            case GeoConstants.FTDateTime:
                            case GeoConstants.FTDate:
                            case GeoConstants.FTTime:
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(timestamp);
                                valueText = sdf.format(calendar.getTime());
                                break;
                        }


                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        LinearLayout row =
                                (LinearLayout) inflater.inflate(R.layout.item_attribute_row, null);

                        TextView columnName = (TextView) row.findViewById(R.id.column_name);
                        columnName.setText(fieldAlias);

                        TextView columnData = (TextView) row.findViewById(R.id.column_data);
                        columnData.setText(valueText);

                        mAttributesLayout.addView(row);
                    }
                }
            } catch (Exception e) {
                //Log.d(TAG, e.getLocalizedMessage());
            } finally {
                attributes.close();
            }
        }
    }
}
