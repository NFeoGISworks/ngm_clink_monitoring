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
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.map.FoclVectorLayer;

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


        // set attributes
        String selection = FIELD_ID + " = ?";
        Cursor attributes =
                mFoclVectorLayer.query(null, selection, new String[] {mObjectId + ""}, null, null);

        if (null != attributes) {
            try {
                if (attributes.moveToFirst()) {
                    for (int i = 0; i < attributes.getColumnCount(); i++) {
                        String column = attributes.getColumnName(i);

                        if (column.equals(FIELD_GEOM)) {
                            continue;
                        }

                        String dataText = null;

                        try {
                            dataText = attributes.getString(i);
                        } catch (Exception ignored) {
                            // do nothing
                        }

                        if (TextUtils.isEmpty(dataText)) {
                            continue;
                        }

                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        LinearLayout row =
                                (LinearLayout) inflater.inflate(R.layout.item_attribute_row, null);

                        TextView columnName = (TextView) row.findViewById(R.id.column_name);
                        columnName.setText(column);

                        TextView columnData = (TextView) row.findViewById(R.id.column_data);
                        columnData.setText(dataText);

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
