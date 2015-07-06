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
import android.widget.Button;
import com.nextgis.ngm_clink_monitoring.R;


public class YesNoDialog
        extends DialogFragment
{
    protected Integer mIconId;

    protected Integer mTitleId;
    protected Integer mMessageId;
    protected Integer mPositiveTextId;
    protected Integer mNegativeTextId;

    protected CharSequence mTitleText;
    protected CharSequence mMessageText;
    protected CharSequence mPositiveText;
    protected CharSequence mNegativeText;

    protected Button mBtnPositive;
    protected Button mBtnNegative;

    protected OnPositiveClickedListener mOnPositiveClickedListener;
    protected OnNegativeClickedListener mOnNegativeClickedListener;
    protected OnCancelListener          mOnCancelListener;
    protected OnDismissListener         mOnDismissListener;

    protected boolean mKeepInstance = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(mKeepInstance);
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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_yes_no, null);

        mBtnPositive = (Button) view.findViewById(R.id.btn_positive_yn);
        mBtnNegative = (Button) view.findViewById(R.id.btn_negative_yn);

        if (null != mPositiveTextId) {
            mBtnPositive.setVisibility(View.VISIBLE);
            mBtnPositive.setText(mPositiveTextId);
        }

        if (null != mNegativeTextId) {
            mBtnNegative.setVisibility(View.VISIBLE);
            mBtnNegative.setText(mNegativeTextId);
        }


        if (null != mPositiveText) {
            mBtnPositive.setVisibility(View.VISIBLE);
            mBtnPositive.setText(mPositiveText);
        }

        if (null != mNegativeText) {
            mBtnNegative.setVisibility(View.VISIBLE);
            mBtnNegative.setText(mNegativeText);
        }


        if (null != mOnPositiveClickedListener) {
            mBtnPositive.setVisibility(View.VISIBLE);
            mBtnPositive.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (null != mOnPositiveClickedListener) {
                                mOnPositiveClickedListener.onPositiveClicked();
                            }
                            dismiss();
                        }
                    });
        }

        if (null != mOnNegativeClickedListener) {
            mBtnNegative.setVisibility(View.VISIBLE);
            mBtnNegative.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (null != mOnNegativeClickedListener) {
                                mOnNegativeClickedListener.onNegativeClicked();
                            }
                            dismiss();
                        }
                    });
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        if (null != mIconId) {
            builder.setIcon(mIconId);
        }

        if (null != mTitleId) {
            builder.setTitle(mTitleId);
        }

        if (null != mTitleText) {
            builder.setTitle(mTitleText);
        }

        if (null != mMessageId) {
            builder.setMessage(mMessageId);
        }

        if (null != mMessageText) {
            builder.setMessage(mMessageText);
        }

        return builder.create();
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (null != mOnCancelListener) {
            mOnCancelListener.onCancel();
        }
        super.onCancel(dialog);
    }


    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (null != mOnDismissListener) {
            mOnDismissListener.onDismiss();
        }
        super.onDismiss(dialog);
    }


    public YesNoDialog setKeepInstance(boolean keepInstance)
    {
        mKeepInstance = keepInstance;
        return this;
    }


    public YesNoDialog setIcon(int iconId)
    {
        mIconId = iconId;
        return this;
    }


    public YesNoDialog setTitle(int titleId)
    {
        mTitleId = titleId;
        return this;
    }


    public YesNoDialog setMessage(int messageId)
    {
        mMessageId = messageId;
        return this;
    }


    public YesNoDialog setPositiveText(int positiveTextId)
    {
        mPositiveTextId = positiveTextId;
        return this;
    }


    public YesNoDialog setNegativeText(int negativeTextId)
    {
        mNegativeTextId = negativeTextId;
        return this;
    }


    public YesNoDialog setTitle(CharSequence titleText)
    {
        mTitleText = titleText;
        return this;
    }


    public YesNoDialog setMessage(CharSequence messageText)
    {
        mMessageText = messageText;
        return this;
    }


    public YesNoDialog setPositiveText(CharSequence positiveText)
    {
        mPositiveText = positiveText;
        return this;
    }


    public YesNoDialog setNegativeText(CharSequence negativeText)
    {
        mNegativeText = negativeText;
        return this;
    }


    public YesNoDialog setOnPositiveClickedListener(OnPositiveClickedListener onPositiveClickedListener)
    {
        mOnPositiveClickedListener = onPositiveClickedListener;
        return this;
    }


    public YesNoDialog setOnNegativeClickedListener(OnNegativeClickedListener onNegativeClickedListener)
    {
        mOnNegativeClickedListener = onNegativeClickedListener;
        return this;
    }


    public YesNoDialog setOnCancelListener(OnCancelListener onCancelListener)
    {
        mOnCancelListener = onCancelListener;
        return this;
    }


    public YesNoDialog setOnDismissListener(OnDismissListener onDismissListener)
    {
        mOnDismissListener = onDismissListener;
        return this;
    }


    public interface OnPositiveClickedListener
    {
        void onPositiveClicked();
    }


    public interface OnNegativeClickedListener
    {
        void onNegativeClicked();
    }


    public interface OnCancelListener
    {
        void onCancel();
    }


    public interface OnDismissListener
    {
        void onDismiss();
    }
}
