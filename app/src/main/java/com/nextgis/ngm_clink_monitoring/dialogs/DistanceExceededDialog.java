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

package com.nextgis.ngm_clink_monitoring.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import com.nextgis.ngm_clink_monitoring.R;


public class DistanceExceededDialog
        extends DialogFragment
{
    OnRepeatClickedListener mOnRepeatClickedListener;
    OnNewPointClickedListener mOnNewPointClickedListener;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_distance_exceeded, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.warning))
                .setIcon(R.drawable.ic_action_warning)
                .setView(view)
                .setPositiveButton(
                        R.string.repeat, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {
                                if (null != mOnRepeatClickedListener) {
                                    mOnRepeatClickedListener.onRepeatClicked();
                                }

                                dismiss();
                            }
                        })
                .setNegativeButton(
                        R.string.new_starting_point, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {
                                if (null != mOnNewPointClickedListener) {
                                    mOnNewPointClickedListener.onNewPointClicked();
                                }

                                dismiss();
                            }
                        });

        return builder.create();
    }


    public void setOnRepeatClickedListener(OnRepeatClickedListener onRepeatClickedListener)
    {
        mOnRepeatClickedListener = onRepeatClickedListener;
    }


    public interface OnRepeatClickedListener
    {
        void onRepeatClicked();
    }


    public void setOnNewPointClickedListener(OnNewPointClickedListener onNewPointClickedListener)
    {
        mOnNewPointClickedListener = onNewPointClickedListener;
    }


    public interface OnNewPointClickedListener
    {
        void onNewPointClicked();
    }
}
