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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.Constants;
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

    protected String mError = null;


    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize)
    {
        super(context, autoInitialize);
    }


    public FoclSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    public void onPerformSync(
            Account account,
            Bundle bundle,
            String authority,
            ContentProviderClient contentProviderClient,
            SyncResult syncResult)
    {
        Context context = getContext();
        sendNotification(context, NOTIFICATION_START, null);

        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);

        if (isCanceled()) {
            sendNotification(
                    context, FoclSyncAdapter.NOTIFICATION_CANCELED,
                    context.getString(R.string.sync_canceled));
            Log.d(Constants.TAG, "FoclSyncAdapter - NOTIFICATION_CANCELED is sent");
            return;
        }

        if (null != mError && mError.length() > 0) {
            sendNotification(context, NOTIFICATION_ERROR, mError);
            return;
        }

        sendNotification(context, NOTIFICATION_FINISH, null);
    }


    // has recursive call in super.sync()
    @Override
    protected void sync(
            LayerGroup layerGroup,
            String authority,
            SyncResult syncResult)
    {
        // First, we must upload changes for them saving
        super.sync(layerGroup, authority, syncResult);

        if (layerGroup instanceof FoclProject) {

            if (isCanceled()) {
                Log.d(Constants.TAG, "FoclSyncAdapter - downloading is canceled");
                return;
            }

            Log.d(Constants.TAG, "FoclSyncAdapter - downloading is in progress");

            // Second, we update FoclProject, can delete some or all layers
            FoclProject foclProject = (FoclProject) layerGroup;
            mError = foclProject.download();
        }
    }


    public static void sendNotification(
            Context context,
            int notificationType,
            String errorMsg)
    {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setLargeIcon(
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.ic_launcher))
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(false);

        switch (notificationType) {
            case NOTIFICATION_START:
                builder.setProgress(0, 0, true)
                        .setSmallIcon(R.drawable.ic_sync_started)
                        .setTicker(context.getString(R.string.sync_started))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_progress));
                break;

            case NOTIFICATION_FINISH:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_finished)
                        .setTicker(context.getString(R.string.sync_finished))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_finished));
                break;

            case NOTIFICATION_CANCELED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(context.getString(R.string.sync_canceled))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_canceled));
                break;

            case NOTIFICATION_ERROR:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(context.getString(R.string.sync_error))
                        .setContentTitle(context.getString(R.string.sync_error))
                        .setContentText(errorMsg);
                break;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}
