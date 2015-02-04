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

package com.nextgis.ngm_clink_monitoring.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.nextgis.maplib.map.Layer;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;


public class FoclProjectAdapter
        extends BaseAdapter
{
    protected Context     mContext;
    protected FoclProject mFoclProject;


    public FoclProjectAdapter(
            Context context,
            FoclProject foclProject)
    {
        mContext = context;
        mFoclProject = foclProject;
    }


    @Override
    public int getCount()
    {
        return mFoclProject.getLayerCount();
    }


    @Override
    public Object getItem(int position)
    {
        return mFoclProject.getLayer(position);
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.layout_focl_struct_row, null);
        }

        Layer layer = (Layer) getItem(position);

        TextView tvFoclStructName = (TextView) convertView.findViewById(R.id.focl_struct_name);
        tvFoclStructName.setText(layer.getName());

        return convertView;
    }
}
