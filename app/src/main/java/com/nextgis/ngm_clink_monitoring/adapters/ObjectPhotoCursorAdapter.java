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

package com.nextgis.ngm_clink_monitoring.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.Constants;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class ObjectPhotoCursorAdapter
        extends ObjectPhotoAdapter
{
    protected Cursor mAttachesCursor;
    protected Uri    mAttachesUri;


    public ObjectPhotoCursorAdapter(
            Context context,
            Uri attachesUri,
            Cursor attachesCursor)
    {
        super(context);
        mAttachesUri = attachesUri;
        mAttachesCursor = attachesCursor;
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mAttachesCursor) {
            return super.getItemId(position);
        }

        mAttachesCursor.moveToPosition(position);
        return mAttachesCursor.getLong(mAttachesCursor.getColumnIndex(VectorLayer.ATTACH_ID));
    }


    @Override
    public int getItemCount()
    {
        return (mAttachesCursor == null) ? 0 : mAttachesCursor.getCount();
    }


    @Override
    protected InputStream getPhotoInputStream(int position)
    {
        Uri attachUri = ContentUris.withAppendedId(mAttachesUri, getItemId(position));
        InputStream inputStream;

        try {
            inputStream = mContext.getContentResolver().openInputStream(attachUri);

        } catch (FileNotFoundException e) {
            Log.d(Constants.TAG, "position = " + position + ", ERROR: " + e.getLocalizedMessage());
            return null;
        }

        Log.d(Constants.TAG, "position = " + position + ", URI = " + attachUri.toString());
        return inputStream;
    }
}
