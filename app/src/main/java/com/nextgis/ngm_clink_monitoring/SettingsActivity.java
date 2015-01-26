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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import static com.nextgis.ngm_clink_monitoring.util.SettingsConstants.ACTION_PREFS_GENERAL;
import static com.nextgis.ngm_clink_monitoring.util.SettingsConstants.ACTION_PREFS_MAP;


public class SettingsActivity
        extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ViewGroup root = ((ViewGroup) findViewById(android.R.id.content));
        LinearLayout content = (LinearLayout) root.getChildAt(0);
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
                        SettingsActivity.this.finish();
                    }
                });

        boolean bAddPrefXML = false;
        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_PREFS_GENERAL)) {
            addPreferencesFromResource(R.xml.preferences_general);
            bAddPrefXML = true;
        } else if (action != null && action.equals(ACTION_PREFS_MAP)) {
            addPreferencesFromResource(R.xml.preferences_map);
            bAddPrefXML = true;
        }
        /*else if (action != null && action.equals(ACTION_PREFS_USER)) {
            addPreferencesFromResource(R.xml.preferences_user);
            bAddPrefXML = true;
        }
        else if (action != null && action.equals(ACTION_PREFS_SCANEX)) {
            addPreferencesFromResource(R.xml.preferences_scanex);
            bAddPrefXML = true;
        }*/
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Load the legacy preferences headers
            addPreferencesFromResource(R.xml.preference_headers_legacy);
            bAddPrefXML = true;
        }
        if (bAddPrefXML) {
            //support = new SettingsSupport(this, this.getPreferenceScreen());
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


    @Override
    public void onResume()
    {
        super.onResume();
        //    if(support != null)
        //        support.registerListener();
    }


    @Override
    public void onPause()
    {
        super.onPause();
        //    if(support != null)
        //        support.unregisterListener();
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return SettingsFragment.class.getName().equals(fragmentName);
        //return super.isValidFragment(fragmentName);
    }
}
