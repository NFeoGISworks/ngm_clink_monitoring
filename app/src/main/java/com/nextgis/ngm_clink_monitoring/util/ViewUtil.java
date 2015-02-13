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

package com.nextgis.ngm_clink_monitoring.util;

import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import java.util.concurrent.atomic.AtomicInteger;


public class ViewUtil
{
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);


    /**
     * Generate a value suitable for use in View.setId(int). This value will not collide with ID
     * values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId()
    {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    /**
     * Generate and set the identifier for given view. This identifier will not collide with ID
     * values generated at build time by aapt for R.id.
     *
     * @param view
     *         a view for which to set the generated identifier
     */
    public static void setGeneratedId(View view)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setId(generateViewId());

        } else {
            view.setId(View.generateViewId());
        }
    }


    public static void makingSquareView(final View view)
    {
        view.post(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LinearLayout.LayoutParams params =
                                (LinearLayout.LayoutParams) view.getLayoutParams();

                        int width = view.getWidth();
                        int height = view.getHeight();

                        if (width > height) {
                            params.height = width;
                        } else {
                            params.width = height;
                        }

                        view.setLayoutParams(params);
                        view.postInvalidate();
                    }
                });
    }


    public static void setViewHeight(
            View view,
            int height)
    {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }
}
