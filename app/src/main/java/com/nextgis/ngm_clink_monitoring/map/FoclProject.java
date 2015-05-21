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

package com.nextgis.ngm_clink_monitoring.map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.INGWLayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FoclProject
        extends LayerGroup
        implements INGWLayer
{
    protected static final String JSON_ACCOUNT_KEY  = "account";

    protected NetworkUtil mNet;

    protected String mAccountName = "";


    public FoclProject(
            Context context,
            File path,
            LayerFactory layerFactory)
    {
        super(context, path, layerFactory);

        mNet = new NetworkUtil(context);
        mLayerType = FoclConstants.LAYERTYPE_FOCL_PROJECT;
    }


    public static String getFoclUrl(String server)
    {
        if (!server.startsWith("http")) {
            server = "http://" + server;
        }
        return server + "/compulink/mobile/user_focl_list";
    }


    @Override
    public String getAccountName()
    {
        return mAccountName;
    }


    @Override
    public void setAccountName(String accountName)
    {
        mAccountName = accountName;
    }


    public FoclStruct getFoclStructByRemoteId(long remoteId)
    {
        for (ILayer layer : mLayers) {
            FoclStruct foclStruct = (FoclStruct) layer;

            if (foclStruct.getRemoteId() == remoteId) {
                return foclStruct;
            }
        }

        return null;
    }


    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = super.toJSON();
        rootConfig.put(JSON_ACCOUNT_KEY, mAccountName);
        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        super.fromJSON(jsonObject);
        mAccountName = jsonObject.getString(JSON_ACCOUNT_KEY);
    }


    public FoclStruct addOrUpdateFoclStruct(
            JSONObject jsonStruct,
            JSONArray jsonLayers)
            throws JSONException, SQLiteException
    {
        long structId = jsonStruct.getLong(Constants.JSON_ID_KEY);
        String structName = jsonStruct.isNull(Constants.JSON_NAME_KEY)
                            ? ""
                            : jsonStruct.getString(Constants.JSON_NAME_KEY);
        String structRegion = jsonStruct.isNull(FoclConstants.JSON_REGION_KEY)
                              ? ""
                              : jsonStruct.getString(FoclConstants.JSON_REGION_KEY);
        String structDistrict = jsonStruct.isNull(FoclConstants.JSON_DISTRICT_KEY)
                                ? ""
                                : jsonStruct.getString(FoclConstants.JSON_DISTRICT_KEY);

        FoclStruct foclStruct = getFoclStructByRemoteId(structId);

        if (null != foclStruct) {

            if (!foclStruct.getName().equals(structName)) {
                foclStruct.setName(structName);
            }

            if (!foclStruct.getRegion().equals(structRegion)) {
                foclStruct.setRegion(structRegion);
            }

            if (!foclStruct.getDistrict().equals(structDistrict)) {
                foclStruct.setDistrict(structDistrict);
            }

            List<Long> layerIdList = new ArrayList<>(jsonLayers.length());

            for (int jj = 0; jj < jsonLayers.length(); jj++) {
                JSONObject jsonLayer = jsonLayers.getJSONObject(jj);
                long layerId = jsonLayer.getInt(Constants.JSON_ID_KEY);
                layerIdList.add(layerId);
            }

            List<ILayer> layers = foclStruct.getLayers();
            for (int i = 0; i < layers.size(); ++i) {
                ILayer layer = layers.get(i);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                FoclVectorLayer foclVectorLayer = (FoclVectorLayer) layer;

                if (!layerIdList.contains(foclVectorLayer.getRemoteId())) {
                    foclVectorLayer.delete();
                    --i;
                }
            }

        } else {
            foclStruct = new FoclStruct(getContext(), createLayerStorage(), mLayerFactory);

            foclStruct.setRemoteId(structId);
            foclStruct.setName(structName);
            foclStruct.setRegion(structRegion);
            foclStruct.setDistrict(structDistrict);
            foclStruct.setVisible(true);

            addLayer(foclStruct);
        }

        return foclStruct;
    }


    public void addOrUpdateFoclVectorLayer(
            JSONObject jsonLayer,
            FoclStruct foclStruct)
            throws JSONException, SQLiteException
    {
        int layerId = jsonLayer.getInt(Constants.JSON_ID_KEY);
        String layerName = jsonLayer.isNull(Constants.JSON_NAME_KEY)
                           ? ""
                           : jsonLayer.getString(Constants.JSON_NAME_KEY);
        String layerType = jsonLayer.getString(Constants.JSON_TYPE_KEY);


        FoclVectorLayer foclVectorLayer = foclStruct.getLayerByRemoteId(layerId);
        boolean createNewVectorLayer = false;

        if (foclVectorLayer != null) {
            if (foclVectorLayer.getFoclLayerType() !=
                    FoclVectorLayer.getFoclLayerTypeFromString(layerType)) {

                foclVectorLayer.delete();
                createNewVectorLayer = true;
            }

            if (!createNewVectorLayer) {
                if (!foclVectorLayer.getName().equals(layerName)) {
                    foclVectorLayer.setName(layerName);
                }
            }
        }

        if (createNewVectorLayer || foclVectorLayer == null) {
            foclVectorLayer = new FoclVectorLayer(
                    foclStruct.getContext(), foclStruct.createLayerStorage());

            foclVectorLayer.setRemoteId(layerId);
            foclVectorLayer.setName(layerName);
            foclVectorLayer.setFoclLayerType(
                    FoclVectorLayer.getFoclLayerTypeFromString(layerType));
            foclVectorLayer.setAccountName(mAccountName);
            foclVectorLayer.setVisible(true);
            foclVectorLayer.setSyncType(Constants.SYNC_ATTRIBUTES | Constants.SYNC_ATTACH);
            foclStruct.addLayer(foclVectorLayer);

            String error = foclVectorLayer.download();

            if (null != error && error.length() > 0) {
                // TODO: work error
            }
        }
    }


    public String createOrUpdateFromJson(JSONArray jsonArray)
    {
        if (Thread.currentThread().isInterrupted()) {
            return "";
        }

        try {
            List<Long> structIdList = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonStruct = jsonArray.getJSONObject(i);
                long structId = jsonStruct.getLong(Constants.JSON_ID_KEY);
                structIdList.add(structId);
            }

            for (int i = 0; i < mLayers.size(); ++i) {
                ILayer layer = mLayers.get(i);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                FoclStruct foclStruct = (FoclStruct) layer;

                if (!structIdList.contains(foclStruct.getRemoteId())) {
                    foclStruct.delete();
                    --i;
                }
            }

            for (int i = 0; i < jsonArray.length(); ++i) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                JSONObject jsonStruct = jsonArray.getJSONObject(i);
                JSONArray jsonLayers = jsonStruct.getJSONArray(Constants.JSON_LAYERS_KEY);

                FoclStruct foclStruct = addOrUpdateFoclStruct(jsonStruct, jsonLayers);

                for (int jj = 0; jj < jsonLayers.length(); ++jj) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    JSONObject jsonLayer = jsonLayers.getJSONObject(jj);
                    addOrUpdateFoclVectorLayer(jsonLayer, foclStruct);
                }
            }

            save();
            return "";

        } catch (JSONException | SQLiteException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }


    public String download()
    {
        if (!mNet.isNetworkAvailable()) {
            return getContext().getString(com.nextgis.maplib.R.string.error_network_unavailable);
        }

        IGISApplication app = (IGISApplication) mContext.getApplicationContext();
        AccountManager accountManager = AccountManager.get(mContext.getApplicationContext());
        Account account = app.getAccount(mAccountName);

        String url = accountManager.getUserData(account, "url");
        String login = accountManager.getUserData(account, "login");
        String password = accountManager.getPassword(account);

        try {

            final HttpGet get = new HttpGet(getFoclUrl(url)); //get as GeoJSON
            //basic auth
            if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
                get.setHeader("Accept", "*/*");
                final String basicAuth = "Basic " + Base64.encodeToString(
                        (login + ":" + password).getBytes(), Base64.NO_WRAP);
                get.setHeader("Authorization", basicAuth);
            }

            final DefaultHttpClient HTTPClient = mNet.getHttpClient();
            final HttpResponse response = HTTPClient.execute(get);

            // Check to see if we got success
            final org.apache.http.StatusLine line = response.getStatusLine();
            if (line.getStatusCode() != 200) {
                Log.d(
                        Constants.TAG, "Problem downloading FOCL: " + url + " HTTP response: " +
                                line);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                Log.d(Constants.TAG, "No content downloading FOCL: " + url);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            String data = EntityUtils.toString(entity);
            JSONArray jsonArray = new JSONArray(data);

            return createOrUpdateFromJson(jsonArray);

        } catch (IOException e) {
            Log.d(
                    Constants.TAG, "Problem downloading FOCL: " + url + " Error: " +
                            e.getLocalizedMessage());
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        } catch (JSONException e) {
            e.printStackTrace();
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        }
    }
}
