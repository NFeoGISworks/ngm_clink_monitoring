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

package com.nextgis.ngm_clink_monitoring.util;

import android.accounts.Account;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.nextgis.ngm_clink_monitoring.GISApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LogcatWriter
{
    protected GISApplication mApplication;
    protected Process        mLogcatProcess;
    protected BufferedReader mLogcatReader;

    protected String mProcessName     = "";
    protected String mProcessId       = "";
    private   String mDeviceModelName = "";
    private   String mVersionName     = "0";
    private   int    mVersionCode     = 0;

    private final DateFormat mFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US);


    public LogcatWriter(GISApplication application)
    {
        mApplication = application;
        mProcessName = mApplication.getCurrentProcessName();
        mProcessId = Integer.toString(android.os.Process.myPid());

        PackageManager packageManager = mApplication.getPackageManager();
        PackageInfo packageInfo;

        try {
            packageInfo = packageManager.getPackageInfo(mApplication.getPackageName(), 0);
            mVersionName = packageInfo.versionName;
            mVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }

        mDeviceModelName = mApplication.getDeviceModelName();
    }


    public void startLogcat()
            throws IOException
    {
        Process clearProcess = Runtime.getRuntime().exec("logcat -c");
        try {
            clearProcess.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void writeLogcat(String logcatFilePath)
            throws IOException
    {
        mLogcatProcess = Runtime.getRuntime().exec("logcat -d -v time");
        mLogcatReader = new BufferedReader(new InputStreamReader(mLogcatProcess.getInputStream()));

        File logcatFile = new File(logcatFilePath);
        File logcatDir = logcatFile.getParentFile();
        boolean dirReady = logcatDir.isDirectory() || logcatDir.mkdirs();

        if (dirReady) {
            FileOutputStream fos = new FileOutputStream(logcatFile, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            final Date dumpDate = new Date(System.currentTimeMillis());
            Account account = mApplication.getAccount();

            osw.append("\n\n\n")
                    .append(
                            String.format(
                                    "Device UUID: %s\n", Utils.getDeviceUniqueID(mApplication)))
                    .append(String.format("Date: %s\n", mFormatter.format(dumpDate)))
                    .append(String.format("Server URL: %s\n", mApplication.getAccountUrl(account)))
                    .append(String.format("Login: %s\n", mApplication.getAccountLogin(account)))
                    .append(String.format("Device model: %s\n", mDeviceModelName))
                    .append(String.format("Android version: %s\n", Build.VERSION.SDK_INT))
                    .append(
                            String.format(
                                    "Application version: %s, rev. %d\n\n", mVersionName,
                                    mVersionCode))
                    .append(String.format("Process name: %s\n", mProcessName))
                    .append(String.format("Process ID: %s\n\n\n", mProcessId))
                    .append("Logcat:\n\n");

            String line = "";
            while ((line = mLogcatReader.readLine()) != null) {
                if (line.contains(mProcessId)) {
                    osw.append(line);
                    osw.append("\n");
                }
            }

            osw.close();

            File readyLogFile = new File(logcatFilePath + FoclConstants.FOCL_REPORT_FILE_EXT);

            if (!logcatFile.renameTo(readyLogFile)) {
                throw new IOException(
                        "can not rename logcatFile: " + logcatFile.getAbsolutePath() +
                                " to readyLogFile: " + readyLogFile.getAbsolutePath());
            }
        }
    }


    public void stopLogcat()
            throws IOException
    {
        mLogcatProcess.destroy();
        mLogcatReader.close();
    }
}
