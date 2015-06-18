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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.R;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;
import static com.nextgis.maplib.util.Constants.FIELD_ID;


public class AttributesDialog
        extends DialogFragment
{
    protected static final String KEY_ITEM_ID = "item_id";

    protected VectorLayer mLayer;
    protected long        mItemId;

    protected LinearLayout mAttributesLayout;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_attributes, null);

        mAttributesLayout = (LinearLayout) view.findViewById(R.id.ll_attributes);
        setAttributes();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.object_attributes))
                .setView(view)
                .setPositiveButton(
                        getActivity().getString(R.string.ok), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {
                                dismiss();
                            }
                        });

        return builder.create();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_ITEM_ID, mItemId);
    }


    @Override
    public void onViewStateRestored(
            @Nullable
            Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mItemId = savedInstanceState.getLong(KEY_ITEM_ID);
        }

        setAttributes();
    }


    public void setSelectedFeature(
            VectorLayer selectedLayer,
            long selectedItemId)
    {
        mItemId = selectedItemId;
        mLayer = selectedLayer;

        if (mLayer == null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }

        setAttributes();
    }


    private void setAttributes()
    {
        if (mAttributesLayout == null) {
            return;
        }

        TextView title = (TextView) mAttributesLayout.findViewById(R.id.title);
        title.setText(mLayer.getName());

        String selection = FIELD_ID + " = ?";
        Cursor attributes = mLayer.query(null, selection, new String[] {mItemId + ""}, null);

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
    }
}
