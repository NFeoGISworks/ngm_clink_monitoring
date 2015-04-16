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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.ngm_clink_monitoring.R;


public class AttributesDialog
        extends DialogFragment
{
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


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        getDialog().setTitle(getActivity().getString(R.string.object_attributes));

        View view = inflater.inflate(R.layout.fragment_attributes, container, false);
        mAttributesLayout = (LinearLayout) view.findViewById(R.id.ll_attributes);

        Button buttonOk = (Button) view.findViewById(R.id.btn_attr_ok);
        buttonOk.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dismiss();
                    }
                });

        setAttributes();

        return view;
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

        String selection = VectorLayer.FIELD_ID + " = ?";
        Cursor attributes = mLayer.query(null, selection, new String[] {mItemId + ""}, null);

        if (attributes.moveToFirst()) {
            for (int i = 0; i < attributes.getColumnCount(); i++) {
                String column = attributes.getColumnName(i);

                if (column.equals(VectorLayer.FIELD_GEOM)) {
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
