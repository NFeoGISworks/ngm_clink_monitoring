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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.R;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ObjectPhotoAdapter
        extends RecyclerView.Adapter<ObjectPhotoAdapter.ViewHolder>
{
    protected static final int IMAGE_SIZE_DP = 100;
    protected final int IMAGE_SIZE_PX;

    protected Context mContext;
    protected Cursor  mAttachesCursor;
    protected Uri     mAttachesUri;
    protected int     mSelectedItemPosition;
    protected long    mSelectedItemId;

    protected OnPhotoClickListener mOnPhotoClickListener;


    public ObjectPhotoAdapter(
            Context context,
            Uri attachesUri,
            Cursor attachesCursor)
    {
        mContext = context;
        mAttachesUri = attachesUri;
        mAttachesCursor = attachesCursor;

        IMAGE_SIZE_PX = (int) (IMAGE_SIZE_DP * mContext.getResources().getDisplayMetrics().density);
    }


    @Override
    public ObjectPhotoAdapter.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup,
            int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_object_photo, viewGroup, false);

        return new ViewHolder(mContext, view);
    }


    @Override
    public void onBindViewHolder(
            final ViewHolder viewHolder,
            final int position)
    {
        ViewGroup.LayoutParams layoutParams = viewHolder.mImageView.getLayoutParams();
        layoutParams.height = IMAGE_SIZE_PX;
        layoutParams.width = IMAGE_SIZE_PX;

        viewHolder.mPosition = position;
        viewHolder.mImageView.setLayoutParams(layoutParams);
        viewHolder.mImageView.setImageBitmap(null);

        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (null != mOnPhotoClickListener) {
                            mOnPhotoClickListener.onPhotoClick(
                                    getItemId(viewHolder.getAdapterPosition()));
                        }
                    }
                });

        viewHolder.itemView.setOnLongClickListener(
                new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        setSelectedItemPosition(viewHolder.getAdapterPosition());
                        setSelectedItemId(getItemId(viewHolder.getAdapterPosition()));
                        return false;
                    }
                });

        new AsyncTask<Void, Void, Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(Void... params)
            {
                InputStream attachInputStream = getAttachInputStream(position);

                if (null == attachInputStream) {
                    return null;
                }

                Bitmap bitmap = createImagePreview(attachInputStream);

                try {
                    attachInputStream.close();
                } catch (IOException e) {
                    Log.d(Constants.TAG, e.getLocalizedMessage());
                }

                return bitmap;
            }


            @Override
            protected void onPostExecute(Bitmap result)
            {
                super.onPostExecute(result);
                if (viewHolder.mPosition == position) {
                    viewHolder.mImageView.setImageBitmap(result);
                }
            }
        }.execute();
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mAttachesCursor) {
            return -1;
        }

        mAttachesCursor.moveToPosition(position);
        return mAttachesCursor.getLong(mAttachesCursor.getColumnIndex(VectorLayer.ATTACH_ID));
    }


    @Override
    public int getItemCount()
    {
        return (mAttachesCursor == null) ? 0 : mAttachesCursor.getCount();
    }


    private InputStream getAttachInputStream(int position)
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


    protected Bitmap createImagePreview(InputStream inputStream)
    {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;

        try {
            bis.mark(bis.available());

            int targetW = IMAGE_SIZE_PX;
            int targetH = IMAGE_SIZE_PX;

            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bis, null, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bis.reset();
            bitmap = BitmapFactory.decodeStream(bis, null, bmOptions);
            bis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    public int getSelectedItemPosition()
    {
        return mSelectedItemPosition;
    }


    public void setSelectedItemPosition(int selectedItemPosition)
    {
        mSelectedItemPosition = selectedItemPosition;
    }


    public long getSelectedItemId()
    {
        return mSelectedItemId;
    }


    public void setSelectedItemId(long selectedItemId)
    {
        mSelectedItemId = selectedItemId;
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener)
    {
        mOnPhotoClickListener = onPhotoClickListener;
    }


    public interface OnPhotoClickListener
    {
        public void onPhotoClick(long itemId);
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener
    {
        public int       mPosition;
        public ImageView mImageView;

        private Context mContext;


        public ViewHolder(
                Context context,
                View itemView)
        {
            super(itemView);

            mContext = context;
            mImageView = (ImageView) itemView.findViewById(R.id.photo_item);

            itemView.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(
                ContextMenu menu,
                View v,
                ContextMenu.ContextMenuInfo menuInfo)
        {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            inflater.inflate(R.menu.menu_context_photo_gallery, menu);
            menu.setHeaderTitle(R.string.select_action);
        }
    }
}
