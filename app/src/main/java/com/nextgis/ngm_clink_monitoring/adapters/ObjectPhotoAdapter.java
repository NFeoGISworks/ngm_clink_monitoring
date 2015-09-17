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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;


public abstract class ObjectPhotoAdapter
        extends RecyclerView.Adapter<ObjectPhotoAdapter.ViewHolder>
{
    protected final static int CREATE_PREVIEW_DONE   = 0;
    protected final static int CREATE_PREVIEW_OK     = 1;
    protected final static int CREATE_PREVIEW_FAILED = 2;

    protected static final int IMAGE_SIZE_DP = 100;
    protected final int IMAGE_SIZE_PX;

    protected Context mContext;
    protected int     mSelectedItemPosition;
    protected long    mSelectedItemId;

    protected OnPhotoClickListener mOnPhotoClickListener;


    protected abstract InputStream getPhotoInputStream(int position)
            throws IOException;


    public ObjectPhotoAdapter(Context context)
    {
        mContext = context;
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


        final Handler handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case CREATE_PREVIEW_DONE:
                        break;

                    case CREATE_PREVIEW_OK:
                        if (viewHolder.mPosition == position) {
                            viewHolder.mImageView.setImageBitmap((Bitmap) msg.obj);
                        }
                        break;

                    case CREATE_PREVIEW_FAILED:
                        Toast.makeText(
                                mContext, "onBindViewHolder() ERROR: " + msg.obj, Toast.LENGTH_LONG)
                                .show();
                        break;
                }
            }
        };

        RunnableFuture<Bitmap> future = new FutureTask<Bitmap>(
                new Callable<Bitmap>()
                {
                    @Override
                    public Bitmap call()
                            throws Exception
                    {
                        InputStream attachInputStream = getPhotoInputStream(position);

                        if (null == attachInputStream) {
                            String error = "onBindViewHolder() ERROR: null == attachInputStream";
                            Log.d(Constants.TAG, error);
                            throw new IOException(error);
                        }

                        Bitmap bitmap = createImagePreview(attachInputStream);

                        try {
                            attachInputStream.close();
                        } catch (IOException e) {
                            String error = "onBindViewHolder() ERROR: " + e.getLocalizedMessage();
                            Log.d(Constants.TAG, error);
                            e.printStackTrace();
                            throw new IOException(error);
                        }

                        return bitmap;
                    }
                })
        {
            @Override
            protected void done()
            {
                super.done();
                handler.sendEmptyMessage(CREATE_PREVIEW_DONE);
            }


            @Override
            protected void set(Bitmap result)
            {
                super.set(result);
                Message msg = handler.obtainMessage(CREATE_PREVIEW_OK, result);
                msg.sendToTarget();
            }


            @Override
            protected void setException(Throwable t)
            {
                super.setException(t);
                Message msg = handler.obtainMessage(CREATE_PREVIEW_FAILED, t.getLocalizedMessage());
                msg.sendToTarget();
            }
        };

        new Thread(future).start();
    }


    protected Bitmap createImagePreview(InputStream inputStream)
            throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buffer[] = new byte[10240];
            int len;
            while ((len = bis.read(buffer, 0, buffer.length)) > 0) {
                baos.write(buffer, 0, len);
            }
            bis.close();

            byte[] imageData = baos.toByteArray();
            baos.close();

            int targetW = IMAGE_SIZE_PX;
            int targetH = IMAGE_SIZE_PX;

            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);

        } catch (IOException e) {
            String error = "ObjectPhotoAdapter ERROR: " + e.getLocalizedMessage();
            Log.d(Constants.TAG, error);
            e.printStackTrace();
            throw new IOException(error);
        }

        if (null == bitmap) {
            String error = "ObjectPhotoAdapter ERROR: null == bitmap";
            Log.d(Constants.TAG, error);
            throw new IOException(error);
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
        void onPhotoClick(long itemId);
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
