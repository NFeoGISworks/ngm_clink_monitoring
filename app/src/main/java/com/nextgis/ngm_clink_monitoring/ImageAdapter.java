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

package com.nextgis.ngm_clink_monitoring;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.List;


public class ImageAdapter
        extends BaseAdapter
{
    protected static final int IMAGE_SIZE_PX = 120;
    protected final int IMAGE_SIZE_DP;

    protected Context      mContext;
    protected List<String> mImagePathList;

    protected int mItemBackground;


    public ImageAdapter(
            Context context,
            List<String> imagePathList)
    {
        mContext = context;
        mImagePathList = imagePathList;

        IMAGE_SIZE_DP = (int) (IMAGE_SIZE_PX * mContext.getResources().getDisplayMetrics().density);

        // sets a grey background; wraps around the images
        TypedArray typedArray = mContext.obtainStyledAttributes(R.styleable.photo_gallery);
        mItemBackground =
                typedArray.getResourceId(R.styleable.photo_gallery_android_galleryItemBackground,
                                         0);
        typedArray.recycle();
    }


    public int getCount()
    {
        return mImagePathList.size();
    }


    public Object getItem(int position)
    {
        return position;
    }


    public long getItemId(int position)
    {
        return position;
    }


    public View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        ImageView imageView = new ImageView(mContext);

        @SuppressWarnings("deprecation")
        Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(IMAGE_SIZE_DP, IMAGE_SIZE_DP);
        imageView.setLayoutParams(layoutParams);

        imageView.setImageBitmap(createImagePreview(mImagePathList.get(position), imageView));
        imageView.setBackgroundResource(mItemBackground);
        return imageView;
    }


    protected Bitmap createImagePreview(
            String imagePath,
            ImageView imageView)
    {
        int targetW = IMAGE_SIZE_DP;
        int targetH = IMAGE_SIZE_DP;

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
}
