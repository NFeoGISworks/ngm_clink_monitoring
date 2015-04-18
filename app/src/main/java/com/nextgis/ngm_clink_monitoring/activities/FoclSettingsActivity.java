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

package com.nextgis.ngm_clink_monitoring.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import ar.com.daidalos.afiledialog.FileChooserActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.FoclSettingsFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import java.io.File;
import java.util.List;


public class FoclSettingsActivity
        extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final int DATA_FOLDER_SELECT_CODE = 1232;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ViewGroup root = ((ViewGroup) findViewById(android.R.id.content));
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer =
                (LinearLayout) View.inflate(this, R.layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        Toolbar toolbar = (Toolbar) toolbarContainer.findViewById(R.id.main_toolbar);
        toolbar.getBackground().setAlpha(255);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        FoclSettingsActivity.this.finish();
                    }
                });

        String action = getIntent().getAction();

        if (action != null) {

            switch (action) {
                case FoclSettingsConstantsUI.ACTION_PREFS_GENERAL:
                    addPreferencesFromResource(R.xml.preferences_general);
                    final Preference dataPathPreference =
                            findPreference(FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH);
                    initDataPathPreference(this, null, dataPathPreference);
                    break;

                case FoclSettingsConstantsUI.ACTION_PREFS_MAP:
                    addPreferencesFromResource(R.xml.preferences_map);
                    break;
            }

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Load the legacy preferences headers
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return FoclSettingsFragment.class.getName().equals(fragmentName);
        //return super.isValidFragment(fragmentName);
    }


    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences,
            String key)
    {
        if (key.equals(FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH)) {
            GISApplication app = (GISApplication) getApplication();

            Preference preference = findPreference(key);
            preference.setSummary(app.getDataPath());

            PreferenceScreen prefScr = (PreferenceScreen) findPreference(
                    "general_prefs_root");

            if (prefScr != null) {
                ((BaseAdapter) prefScr.getRootAdapter()).notifyDataSetChanged();
            }
        }
    }


    public static void initDataPathPreference(
            final PreferenceActivity activity,
            final FoclSettingsFragment fragment,
            final Preference dataPathPreference)
    {
        if (null != dataPathPreference) {
            final GISApplication app = (GISApplication) activity.getApplication();
            dataPathPreference.setSummary(app.getDataPath());

            dataPathPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener()
                    {
                        @Override
                        public boolean onPreferenceClick(Preference preference)
                        {
                            showFolderChooser(activity, fragment);
                            return true;
                        }
                    });
        }
    }


    public static void showFolderChooser(
            final Activity activity,
            final Fragment fragment)
    {
// TODO: remove it
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
//        alertDialog.setTitle(activity.getResources().getString(R.string.hint))
//
//                .setMessage(
//                        String.format(
//                                activity.getResources().getString(R.string.hint_folder_moving),
//                                FoclConstants.FOCL_DATA_DIR))
//
//                .setPositiveButton(
//                        activity.getResources().getString(R.string.ok),
//
//                        new DialogInterface.OnClickListener()
//                        {
//                            public void onClick(
//                                    DialogInterface dialog,
//                                    int which)
//                            {
                                GISApplication app = (GISApplication) activity.getApplication();
                                Intent intent = new Intent(activity, FileChooserActivity.class);

                                intent.putExtra(
                                        FileChooserActivity.INPUT_START_FOLDER,
                                        app.getDataParentPath());

                                intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
                                intent.putExtra(
                                        FileChooserActivity.INPUT_SHOW_ONLY_SELECTABLE, true);
                                intent.putExtra(FileChooserActivity.INPUT_CAN_CREATE_FILES, true);
                                intent.putExtra(
                                        FileChooserActivity.INPUT_SHOW_CONFIRMATION_ON_SELECT,
                                        true);
                                intent.putExtra(
                                        FileChooserActivity.INPUT_SHOW_CONFIRMATION_ON_CREATE,
                                        true);
                                intent.putExtra(
                                        FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
                                intent.putExtra(
                                        FileChooserActivity.INPUT_USE_BACK_BUTTON_TO_NAVIGATE,
                                        false);
        intent.putExtra(
                FileChooserActivity.INPUT_USE_STORAGE_DEVICES, true);

                                if (null == fragment) {
                                    activity.startActivityForResult(
                                            intent, DATA_FOLDER_SELECT_CODE);
                                } else {
                                    // for API > 11
                                    fragment.startActivityForResult(
                                            intent, DATA_FOLDER_SELECT_CODE);
                                }
// TODO: remove it
//                            }
//                        }).show();
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == DATA_FOLDER_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            executeBackgroundMoveTask(this, null, data.getExtras());
        }
    }


    public static void executeBackgroundMoveTask(
            FoclSettingsActivity activity,
            FoclSettingsFragment fragment,
            Bundle bundle)
    {
        if (bundle != null) {

                /*
                * Note that if a file has been created, then the value, inside the Bundle object,
                * represented by the key FileChooserActivity.OUTPUT_NEW_FILE_NAME
                * is going to contain the name of the file (or folder) and
                * the value represented by the key FileChooserActivity.OUTPUT_FILE_OBJECT
                * is going to contain the folder in which the file must be created.
                * Otherwise, if a file has only been selected,
                * FileChooserActivity.OUTPUT_NEW_FILE_NAME is going to be null and
                * FileChooserActivity.OUTPUT_FILE_OBJECT is going to contain
                * the file (or folder) selected.
                * */

            File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
            String newDataParentPath = folder.getAbsolutePath();

            if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                newDataParentPath += File.separator + name;
            }

            GISApplication app = (GISApplication) activity.getApplication();

            String oldDataParentPath = app.getDataParentPath(); // TODO: remove it
            app.setDataParentPath(newDataParentPath);

            // workaround for onSharedPreferenceChanged()
            refreshPreferences(activity, fragment);

// TODO: remove it
            new BackgroundMoveTask(activity, oldDataParentPath, newDataParentPath).execute();
        }
    }


    // workaround for onSharedPreferenceChanged()
    private static void refreshPreferences(
            FoclSettingsActivity activity,
            FoclSettingsFragment fragment)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        PreferenceScreen preferenceScreen;

        if (null == fragment) {
            preferenceScreen = activity.getPreferenceScreen();

            for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
                activity.onSharedPreferenceChanged(sp, preferenceScreen.getPreference(i).getKey());
            }

        } else { // for API > 11
            preferenceScreen = fragment.getPreferenceScreen();

            for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
                fragment.onSharedPreferenceChanged(sp, preferenceScreen.getPreference(i).getKey());
            }
        }
    }


    // TODO: remove it
    private static class BackgroundMoveTask
            extends AsyncTask<Void, Void, Void>
    {
        protected Activity       mActivity;
        protected ProgressDialog mProgressDialog;
        protected String         mOldDataParentPath;
        protected String         mNewDataParentPath;


        public BackgroundMoveTask(
                Activity activity,
                String oldDataParentPath,
                String newDataParentPath)
        {
            mActivity = activity;
            mOldDataParentPath = oldDataParentPath;
            mNewDataParentPath = newDataParentPath;
        }


        @Override
        protected Void doInBackground(Void... voids)
        {
            GISApplication app = (GISApplication) mActivity.getApplication();
            app.moveProgramData(mOldDataParentPath, mNewDataParentPath);
            return null;
        }


        @Override
        protected void onPreExecute()
        {
            //not good solution but rare used so let it be
            lockScreenOrientation();
            mProgressDialog = ProgressDialog.show(
                    mActivity, mActivity.getString(R.string.moving),
                    mActivity.getString(R.string.warning_data_moving), true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIcon(R.drawable.ic_action_warning);
        }


        @Override
        protected void onPostExecute(Void aVoid)
        {
            mProgressDialog.dismiss();
            unlockScreenOrientation();
        }


        protected void lockScreenOrientation()
        {
            int currentOrientation = mActivity.getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }


        protected void unlockScreenOrientation()
        {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}
