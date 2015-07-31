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
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.LogcatWriter;

import java.io.IOException;


public class FoclSyncAdapter
        extends SyncAdapter
{
    public static final int IS_OK       = 0;
    public static final int IS_STARTED  = 1;
    public static final int IS_FINISHED = 2;
    public static final int IS_CANCELED = 3;
    public static final int IS_ERROR    = 4;

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
        // For service debug
//        android.os.Debug.waitForDebugger();

        int syncStatus = FoclSyncAdapter.IS_OK;

        GISApplication app = (GISApplication) getContext().getApplicationContext();

        LogcatWriter logcatWriter = new LogcatWriter(app);
        try {
            logcatWriter.startLogcat();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            app.sendReportsOnServer(false, false);
        } catch (IOException e) {
            e.printStackTrace();
        }


        sendNotification(app, FoclSyncAdapter.IS_STARTED, null);
        app.setFullSync(bundle.getBoolean(FoclConstants.KEY_IS_FULL_SYNC, false));

        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);

        if (isCanceled()) {
            syncStatus = FoclSyncAdapter.IS_CANCELED;
            sendNotification(
                    app, FoclSyncAdapter.IS_CANCELED, app.getString(R.string.sync_canceled));
            Log.d(Constants.TAG, "FoclSyncAdapter - notification IS_CANCELED is sent");
        }

        if (syncResult.hasError() || null != mError && mError.length() > 0) {
            syncStatus = FoclSyncAdapter.IS_ERROR;
            sendNotification(app, FoclSyncAdapter.IS_ERROR, mError);
        }

        if (FoclSyncAdapter.IS_OK == syncStatus) {
            sendNotification(app, FoclSyncAdapter.IS_FINISHED, null);
        }

        try {
            String logcatFilePath;

            switch (syncStatus) {
                case FoclSyncAdapter.IS_OK:
                    logcatFilePath = app.getSyncLogcatFilePath();
                    break;
                case FoclSyncAdapter.IS_ERROR:
                case FoclSyncAdapter.IS_CANCELED:
                default:
                    logcatFilePath = app.getSyncErrorLogcatFilePath();
                    break;
            }

            logcatWriter.writeLogcat(logcatFilePath);
            logcatWriter.stopLogcat();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            app.sendReportsOnServer(false, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            mError = foclProject.sync();

            Log.d(Constants.TAG, "FoclSyncAdapter - downloading is finished");
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
            case FoclSyncAdapter.IS_STARTED:
                builder.setProgress(0, 0, true)
                        .setSmallIcon(R.drawable.ic_sync_started)
                        .setTicker(context.getString(R.string.sync_started))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_progress));
                break;

            case FoclSyncAdapter.IS_FINISHED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_finished)
                        .setTicker(context.getString(R.string.sync_finished))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_finished));
                break;

            case FoclSyncAdapter.IS_CANCELED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(context.getString(R.string.sync_canceled))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_canceled));
                break;

            case FoclSyncAdapter.IS_ERROR:
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
