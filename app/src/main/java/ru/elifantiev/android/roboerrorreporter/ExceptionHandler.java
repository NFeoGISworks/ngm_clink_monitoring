/*
 * Copyright 2011 Oleg Elifantiev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.elifantiev.android.roboerrorreporter;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


// http://habrahabr.ru/post/129582/
// https://github.com/Olegas/RoboErrorReporter
final class ExceptionHandler
        implements Thread.UncaughtExceptionHandler
{
    private final GISApplication mApplication;
    private final DateFormat mFormatter     =
            new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US);
    private final DateFormat mFileFormatter =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);

    private String mDeviceModel        = "unknown";
    private String mVersionName        = "0";
    private int    mVersionCode        = 0;
    private String mCurrentProcessName = "";

    private final String mStacktraceDirPath;

    private final Thread.UncaughtExceptionHandler mPreviousHandler;


    private ExceptionHandler(
            GISApplication application,
            String stacktraceDirPath,
            boolean chained)
    {
        mApplication = application;
        mStacktraceDirPath = stacktraceDirPath;
        mCurrentProcessName = getCurrentProcessName();

        PackageManager packageManager = mApplication.getPackageManager();
        PackageInfo packageInfo;

        try {
            packageInfo = packageManager.getPackageInfo(mApplication.getPackageName(), 0);
            mVersionName = packageInfo.versionName;
            mVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }

        mDeviceModel = Build.MODEL;
        if (!mDeviceModel.startsWith(Build.MANUFACTURER)) {
            mDeviceModel = Build.MANUFACTURER + " " + mDeviceModel;
        }


        if (chained) {
            mPreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        } else {
            mPreviousHandler = null;
        }
    }


    static ExceptionHandler inContext(
            GISApplication application,
            String stacktraceDirPath)
    {
        return new ExceptionHandler(application, stacktraceDirPath, true);
    }


    static ExceptionHandler reportOnlyHandler(
            GISApplication application,
            String stacktraceDirPath)
    {
        return new ExceptionHandler(application, stacktraceDirPath, false);
    }


    public String getCurrentProcessName()
    {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) mApplication.getSystemService(
                Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }


    @Override
    public void uncaughtException(
            Thread thread,
            Throwable exception)
    {
        final String state = Environment.getExternalStorageState();
        final Date dumpDate = new Date(System.currentTimeMillis());

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            Account account = mApplication.getAccount();

            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append("\n\n\n")
                    .append(
                            String.format(
                                    "Device UUID: %s\n", Utils.getDeviceUniqueID(mApplication)))
                    .append(String.format("Date: %s\n", mFormatter.format(dumpDate)))
                    .append(String.format("Server URL: %s\n", mApplication.getAccountUrl(account)))
                    .append(String.format("Login: %s\n", mApplication.getAccountLogin(account)))
                    .append(String.format("Device model: %s\n", mDeviceModel))
                    .append(String.format("Android version: %s\n", Build.VERSION.SDK_INT))
                    .append(
                            String.format(
                                    "Application version: %s, rev. %d\n\n", mVersionName,
                                    mVersionCode))
                    .append(String.format("Process name: %s\n\n", mCurrentProcessName))
                    .append("Thread info:\n")
                    .append(thread.toString())
                    .append("\n");
            processThrowable(exception, reportBuilder);

            File stacktraceFile = new File(
                    mStacktraceDirPath, String.format(
                    "stacktrace-%s.txt", mFileFormatter.format(dumpDate)));
            File dumpDir = stacktraceFile.getParentFile();
            boolean dirReady = dumpDir.isDirectory() || dumpDir.mkdirs();

            if (dirReady) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(stacktraceFile, true);
                    writer.write(reportBuilder.toString());
                } catch (IOException e) {
                    // ignore
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        if (mPreviousHandler != null) {
            mPreviousHandler.uncaughtException(thread, exception);
        }
    }


    private void processThrowable(
            Throwable exception,
            StringBuilder builder)
    {
        if (exception == null) {
            return;
        }
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        builder.append("\n\nException: ")
                .append(exception.getClass().getName())
                .append("\nMessage: ")
                .append(exception.getMessage())
                .append("\n\nStacktrace:\n");
        for (StackTraceElement element : stackTraceElements) {
            builder.append("\t").append(element.toString()).append("\n");
        }
        processThrowable(exception.getCause(), builder);
    }
}
