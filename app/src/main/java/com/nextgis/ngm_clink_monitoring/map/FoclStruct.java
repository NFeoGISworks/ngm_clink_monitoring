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

package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import android.text.TextUtils;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;


public class FoclStruct
        extends LayerGroup
{
    protected long mRemoteId;
    protected String mRegion;
    protected String mDistrict;
    protected String mStatus;
    protected Long mStatusUpdateTime = null;


    public FoclStruct(
            Context context,
            File path,
            LayerFactory layerFactory)
    {
        super(context, path, layerFactory);
        mLayerType = FoclConstants.LAYERTYPE_FOCL_STRUCT;
    }


    public long getRemoteId()
    {
        return mRemoteId;
    }


    public void setRemoteId(long remoteId)
    {
        mRemoteId = remoteId;
    }


    public String getStatus()
    {
        return mStatus;
    }


    public void setStatus(String status)
    {
        mStatus = status;
    }


    public Long getStatusUpdateTime()
    {
        return mStatusUpdateTime;
    }


    public void setStatusUpdateTime(Long statusUpdateTime)
    {
        mStatusUpdateTime = statusUpdateTime;
    }


    public String getRegion()
    {
        return mRegion;
    }


    public void setRegion(String region)
    {
        mRegion = region;
    }


    public String getDistrict()
    {
        return mDistrict;
    }


    public void setDistrict(String district)
    {
        mDistrict = district;
    }


    public String getHtmlFormattedName()
    {
        String lineName = getName();

        if (TextUtils.isEmpty(lineName) || TextUtils.isEmpty(lineName.trim())) {
            lineName = mContext.getString(R.string.no_name);
        }

        String region = getRegion();
        String district = getDistrict();

        boolean isEmptyRegion = TextUtils.isEmpty(region) || TextUtils.isEmpty(region.trim());
        boolean isEmptyDistrict = TextUtils.isEmpty(district) || TextUtils.isEmpty(district.trim());

        if (!isEmptyRegion || !isEmptyDistrict) {
            lineName += "<br><small>";
        }

        if (!isEmptyRegion) {
            lineName += region;
        }

        if (!isEmptyRegion && !isEmptyDistrict) {
            lineName += ", ";
        }

        if (!isEmptyDistrict) {
            lineName += district;
        }

        if (!isEmptyRegion || !isEmptyDistrict) {
            lineName += "</small>";
        }

        return lineName;
    }


    public List<ILayer> getLayers()
    {
        return mLayers;
    }


    public ILayer getLayerByFoclType(int type)
    {
        for (ILayer layer : mLayers) {
            FoclVectorLayer foclLayer = (FoclVectorLayer) layer;
            if (foclLayer.getFoclLayerType() == type) {
                return foclLayer;
            }
        }

        return null;
    }


    public FoclVectorLayer getLayerByRemoteId(int remoteId)
    {
        for (ILayer layer : mLayers) {
            FoclVectorLayer foclLayer = (FoclVectorLayer) layer;
            if (foclLayer.getRemoteId() == remoteId) {
                return foclLayer;
            }
        }

        return null;
    }


    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = super.toJSON();
        rootConfig.put(Constants.JSON_ID_KEY, mRemoteId);
        rootConfig.put(FoclConstants.JSON_STATUS_KEY, mStatus);
        rootConfig.put(FoclConstants.JSON_REGION_KEY, mRegion);
        rootConfig.put(FoclConstants.JSON_DISTRICT_KEY, mDistrict);

        if (null != mStatusUpdateTime) {
            rootConfig.put(FoclConstants.JSON_UPDATE_DT_KEY, mStatusUpdateTime);
        }

        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        super.fromJSON(jsonObject);
        mRemoteId = jsonObject.getLong(Constants.JSON_ID_KEY);
        mStatus = jsonObject.getString(FoclConstants.JSON_STATUS_KEY);
        mRegion = jsonObject.getString(FoclConstants.JSON_REGION_KEY);
        mDistrict = jsonObject.getString(FoclConstants.JSON_DISTRICT_KEY);

        if (jsonObject.has(FoclConstants.JSON_UPDATE_DT_KEY)) {
            mStatusUpdateTime = jsonObject.getLong(FoclConstants.JSON_UPDATE_DT_KEY);
        }
    }
}
