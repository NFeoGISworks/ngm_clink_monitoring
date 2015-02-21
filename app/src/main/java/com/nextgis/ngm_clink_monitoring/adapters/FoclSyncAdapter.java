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

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;


public class FoclSyncAdapter
        extends SyncAdapter
{
    public static final int NOTIFICATION_START    = 1;
    public static final int NOTIFICATION_FINISH   = 2;
    public static final int NOTIFICATION_CANCELED = 3;
    public static final int NOTIFICATION_ERROR    = 4;

    private static final int NOTIFY_ID = 1;

    protected Context mContext;
    protected String mError = null;


    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize)
    {
        super(context, autoInitialize);
        init(context);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }


    protected void init(Context context)
    {
        mContext = context;
    }


    @Override
    public void onPerformSync(
            Account account,
            Bundle bundle,
            String authority,
            ContentProviderClient contentProviderClient,
            SyncResult syncResult)
    {
        sendNotification(NOTIFICATION_START, null);

        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);

        if (null != mError && mError.length() > 0) {

            if (mError.equals(getContext().getString(R.string.sync_canceled))) {
                sendNotification(NOTIFICATION_CANCELED, mError);
            } else {
                sendNotification(NOTIFICATION_ERROR, mError);
            }

        } else {
            sendNotification(NOTIFICATION_FINISH, null);
        }
    }


    @Override
    protected void sync( // has recursive call in super.sync()
                         LayerGroup layerGroup,
                         String authority,
                         SyncResult syncResult)
    {
        // First, we must upload changes for them saving
        super.sync(layerGroup, authority, syncResult);

        if (isCanceled()) {
            mError = getContext().getString(R.string.sync_canceled);
            return;
        }

        if (layerGroup instanceof FoclProject) {
            // Second, we update FoclProject, can delete some or all layers
            FoclProject foclProject = (FoclProject) layerGroup;
            mError = foclProject.download(this);
        }
    }


    @Override
    protected void onCanceled()
    {
        // we try do this before thread is killed
        super.onCanceled();
        sendNotification(NOTIFICATION_CANCELED, getContext().getString(R.string.sync_canceled));
    }


    public void sendNotification(
            int notificationType,
            String errorMsg)
    {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                mContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        builder.setLargeIcon(
                BitmapFactory.decodeResource(
                        mContext.getResources(), R.drawable.ic_launcher))
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(false);

        switch (notificationType) {
            case NOTIFICATION_START:
                builder.setProgress(0, 0, true)
                        .setSmallIcon(R.drawable.ic_sync_started)
                        .setTicker(mContext.getString(R.string.sync_started))
                        .setContentTitle(mContext.getString(R.string.synchronization))
                        .setContentText(mContext.getString(R.string.sync_progress));
                break;

            case NOTIFICATION_FINISH:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_finished)
                        .setTicker(mContext.getString(R.string.sync_finished))
                        .setContentTitle(mContext.getString(R.string.synchronization))
                        .setContentText(mContext.getString(R.string.sync_finished));
                break;

            case NOTIFICATION_CANCELED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(mContext.getString(R.string.sync_canceled))
                        .setContentTitle(mContext.getString(R.string.synchronization))
                        .setContentText(mContext.getString(R.string.sync_canceled));
                break;

            case NOTIFICATION_ERROR:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(mContext.getString(R.string.sync_error))
                        .setContentTitle(mContext.getString(R.string.sync_error))
                        .setContentText(errorMsg);
                break;
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}
