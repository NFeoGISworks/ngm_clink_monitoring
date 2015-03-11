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

package com.nextgis.ngm_clink_monitoring.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nextgis.ngm_clink_monitoring.R;

import java.util.List;


public class ObjectPhotoAdapter
        extends RecyclerView.Adapter<ObjectPhotoAdapter.ViewHolder>
{
    protected static final int IMAGE_SIZE_DP = 100;
    protected final int IMAGE_SIZE_PX;

    protected Context      mContext;
    protected List<String> mImagePathList;


    public ObjectPhotoAdapter(
            Context context,
            List<String> imagePathList)
    {
        mContext = context;
        mImagePathList = imagePathList;

        IMAGE_SIZE_PX = (int) (IMAGE_SIZE_DP * mContext.getResources().getDisplayMetrics().density);
    }


    @Override
    public ObjectPhotoAdapter.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup,
            int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_object_photo, viewGroup, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            ViewHolder viewHolder,
            int i)
    {
        ViewGroup.LayoutParams layoutParams = viewHolder.mImageView.getLayoutParams();
        layoutParams.height = IMAGE_SIZE_PX;
        layoutParams.width = IMAGE_SIZE_PX;

        viewHolder.mImageView.setLayoutParams(layoutParams);
        viewHolder.mImageView.setImageBitmap(createImagePreview(mImagePathList.get(i)));
    }


    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public int getItemCount()
    {
        return mImagePathList.size();
    }


    protected Bitmap createImagePreview(String imagePath)
    {
        int targetW = IMAGE_SIZE_PX;
        int targetH = IMAGE_SIZE_PX;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public ImageView mImageView;


        public ViewHolder(View itemView)
        {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.photo_item);
        }
    }
}
