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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;


public class ObjectPhotoFileAdapter
        extends ObjectPhotoAdapter
{
    protected File[] mPhotoFiles;


    public ObjectPhotoFileAdapter(
            Context context,
            File photoDirectory)
    {
        super(context);

        if (null != photoDirectory) {

            if (!photoDirectory.isDirectory()) {
                throw new IllegalArgumentException("photoDirectory is not directory");
            }

            mPhotoFiles = photoDirectory.listFiles(
                    new FilenameFilter()
                    {
                        @Override
                        public boolean accept(
                                final File dir,
                                final String name)
                        {
                            return name.matches(FoclConstants.TEMP_PHOTO_FILE_PREFIX + ".*\\.jpg");
                        }
                    });

        } else {
            mPhotoFiles = null;
        }
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mPhotoFiles) {
            return super.getItemId(position);
        }

        return position;
    }


    @Override
    public int getItemCount()
    {
        if (null == mPhotoFiles) {
            return 0;
        }

        return mPhotoFiles.length;
    }


    @Override
    protected InputStream getPhotoInputStream(int position)
    {
        if (null == mPhotoFiles) {
            return null;
        }

        long itemId = getItemId(position);

        if (RecyclerView.NO_ID == itemId) {
            return null;
        }

        File photoFile = mPhotoFiles[(int) itemId];
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(photoFile);

        } catch (FileNotFoundException e) {
            Log.d(Constants.TAG, "position = " + position + ", ERROR: " + e.getLocalizedMessage());
            return null;
        }

        Log.d(Constants.TAG, "position = " + position + ", file = " + photoFile.getAbsolutePath());
        return inputStream;
    }


    public File getItemPhotoFile(int itemId)
    {
        return null == mPhotoFiles ? null : mPhotoFiles[itemId];
    }
}
