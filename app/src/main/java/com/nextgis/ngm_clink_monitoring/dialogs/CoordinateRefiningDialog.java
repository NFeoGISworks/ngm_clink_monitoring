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
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import com.nextgis.maplib.location.AccurateLocationTaker;
import com.nextgis.ngm_clink_monitoring.R;

import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.*;


public class CoordinateRefiningDialog
        extends DialogFragment
{
    protected ProgressBar           mProgressBar;
    protected AccurateLocationTaker mLocationTaker;

    protected int mTakeCount    = 0;
    protected int mTakeCountPct = 0;
    protected int mTakeTimePct  = 0;

    protected OnGetAccurateLocationListener mOnGetAccurateLocationListener;

    protected final static int MAX_PCT = 100;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mLocationTaker = new AccurateLocationTaker(
                getActivity(), MAX_TAKEN_ACCURACY, MAX_ACCURACY_TAKE_COUNT, MAX_ACCURACY_TAKE_TIME,
                ACCURACY_PUBLISH_PROGRESS_DELAY, ACCURACY_CIRCULAR_ERROR_STR);

        mLocationTaker.setOnGetCurrentAccurateLocationListener(
                new AccurateLocationTaker.OnGetCurrentAccurateLocationListener()
                {
                    @Override
                    public void onGetCurrentAccurateLocation(Location currentAccurateLocation)
                    {
                        if (MIN_ACCURACY_TAKE_COUNT <= mTakeCount &&
                                null != currentAccurateLocation &&
                                // Form getAccuracy() docs:
                                // "If this location does not have an accuracy, then 0.0 is returned."
                                // We must check for 0.
                                0 < currentAccurateLocation.getAccuracy() &&
                                MAX_ACCURACY > currentAccurateLocation.getAccuracy()) {

                            mLocationTaker.stopTaking();
                        }
                    }
                });

        mLocationTaker.startTaking();
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
        View view = inflater.inflate(R.layout.dialog_coordinate_refining, null);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar_refining);
        mProgressBar.setMax(MAX_PCT);
        mProgressBar.setSecondaryProgress(mTakeCountPct);
        mProgressBar.setProgress(mTakeTimePct);


        // TODO: change AlertDialog.Builder to YesNoDialog

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.coordinate_refining))
                .setIcon(R.drawable.ic_action_time)
                .setView(view);

        mLocationTaker.setOnProgressUpdateListener(
                new AccurateLocationTaker.OnProgressUpdateListener()
                {
                    @Override
                    public void onProgressUpdate(Long... values)
                    {
                        mTakeCount = values[0].intValue();
                        mTakeCountPct = mTakeCount * MAX_PCT / MAX_ACCURACY_TAKE_COUNT;
                        mTakeTimePct = (int) (values[1] * MAX_PCT / MAX_ACCURACY_TAKE_TIME);

                        mProgressBar.setSecondaryProgress(mTakeCountPct);
                        mProgressBar.setProgress(mTakeTimePct);
                    }
                });

        mLocationTaker.setOnGetAccurateLocationListener(
                new AccurateLocationTaker.OnGetAccurateLocationListener()
                {
                    @Override
                    public void onGetAccurateLocation(
                            Location accurateLocation,
                            Long... values)
                    {
                        if (null != mOnGetAccurateLocationListener) {
                            mOnGetAccurateLocationListener.onGetAccurateLocation(accurateLocation);
                        }

                        dismiss();
                    }
                });

        return builder.create();
    }


    public void setOnGetAccurateLocationListener(
            OnGetAccurateLocationListener onGetAccurateLocationListener)
    {
        mOnGetAccurateLocationListener = onGetAccurateLocationListener;
    }


    /**
     * Implement the OnGetAccurateLocationListener interface to obtain the accurate location.
     */
    public interface OnGetAccurateLocationListener
    {
        /**
         * @param accurateLocation
         *         The accurate location. May be null.
         */
        void onGetAccurateLocation(Location accurateLocation);
    }
}
