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

package com.nextgis.ngm_clink_monitoring.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import com.nextgis.ngm_clink_monitoring.R;


public class AboutActivity
        extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        TextView txtVersion = (TextView) findViewById(R.id.app_version);
        try {
            String pkgName = this.getPackageName();
            PackageManager pm = this.getPackageManager();
            String versionName = pm.getPackageInfo(pkgName, 0).versionName;
            String versionCode =
                    Integer.toString(pm.getPackageInfo(this.getPackageName(), 0).versionCode);
            txtVersion.setText("v. " + versionName + " (rev. " + versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            txtVersion.setText("");
        }

        TextView txtCreditsText = (TextView) findViewById(R.id.credits);
        txtCreditsText.setText(Html.fromHtml(getString(R.string.credits)));
        txtCreditsText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView txtCopyrightText = (TextView) findViewById(R.id.copyright);
        txtCopyrightText.setText(Html.fromHtml(getString(R.string.copyright)));
        txtCopyrightText.setMovementMethod(LinkMovementMethod.getInstance());

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
