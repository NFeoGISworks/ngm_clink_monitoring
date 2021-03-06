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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.MainActivity;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class SyncLoginFragment
        extends NGWLoginFragment
{
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable
            ViewGroup container,
            @Nullable
            Bundle savedInstanceState)
    {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.setBarsView(activity.getString(R.string.account_setup));
        }


        final View view = inflater.inflate(R.layout.fragment_focl_login, container, false);

        mURL = (EditText) view.findViewById(R.id.focl_url);
        mLogin = (EditText) view.findViewById(R.id.focl_login);
        mPassword = (EditText) view.findViewById(R.id.focl_password);
        mSignInButton = (Button) view.findViewById(R.id.focl_sign_in);

        TextWatcher watcher = new LocalTextWatcher();
        mURL.addTextChangedListener(watcher);
        mLogin.addTextChangedListener(watcher);
        mPassword.addTextChangedListener(watcher);


        if (!mForNewAccount) {
            mURL.setText(mUrlText);
            mLogin.setText(mLoginText);
            mURL.setEnabled(mChangeAccountUrl);
            mLogin.setEnabled(mChangeAccountLogin);
        }


        TextView loginDescription = (TextView) view.findViewById(R.id.focl_login_description);
        if (mForNewAccount) {
            loginDescription.setText(R.string.focl_login_description);
            mURL.setText(FoclConstants.FOCL_DEFAULT_ACCOUNT_URL);
        } else {
            loginDescription.setText(R.string.focl_edit_login_description);
        }

        mURL.setEnabled(mForNewAccount); // TODO: remove it
//        mURL.setEnabled(false); // TODO: uncomment it
        mLogin.setEnabled(mForNewAccount);

        // button's theme applying
        View viewStyle = inflater.inflate(R.layout.button_login_style, null, false);
        Button buttonStyle = (Button) viewStyle.findViewById(R.id.btn_login_style);
        ViewGroup.LayoutParams styleParams = buttonStyle.getLayoutParams();
        mSignInButton.setLayoutParams(styleParams);
        mSignInButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        // 14sp == android:textAppearance="?android:attr/textAppearanceSmall"
        // 18sp == android:textAppearance="?android:attr/textAppearanceMedium"
        // 22sp == android:textAppearance="?android:attr/textAppearanceLarge"

        return view;
    }


    @Override
    public void onClick(View v)
    {
        if (mForNewAccount) {
            mLogin.setText(mLogin.getText().toString().trim());
        }
        super.onClick(v);
    }


    @Override
    public void onTokenReceived(
            String accountName,
            String token)
    {
        accountName = FoclConstants.FOCL_ACCOUNT_NAME;
        super.onTokenReceived(accountName, token);
    }
}
