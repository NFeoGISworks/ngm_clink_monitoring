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

package com.nextgis.ngm_clink_monitoring.map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.INGWLayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;
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


public class FoclProject
        extends LayerGroup
        implements INGWLayer
{
    protected static final String JSON_ACCOUNT_KEY  = "account";
    protected static final String JSON_URL_KEY      = "url";
    protected static final String JSON_LOGIN_KEY    = "login";
    protected static final String JSON_PASSWORD_KEY = "password";

    protected NetworkUtil mNet;

    protected String mAccountName = "";
    protected String mURL         = "";
    protected String mLogin       = "";
    protected String mPassword    = "";

    protected int mCreatedVectorLayers    = 0;
    protected int mDownloadedVectorLayers = 0;

    protected boolean mNeedSync            = false;
    protected boolean mNewLayersNotCreated = false;
    protected boolean mDownloadedWithError = false;

    protected OnDownloadFinishedListener mOnDownloadFinishedListener = null;


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


    public void setURL(String URL)
    {
        mURL = URL;
    }


    public void setLogin(String login)
    {
        mLogin = login;
    }


    public void setPassword(String password)
    {
        mPassword = password;
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
        rootConfig.put(JSON_URL_KEY, mURL);
        rootConfig.put(JSON_LOGIN_KEY, mLogin);
        rootConfig.put(JSON_PASSWORD_KEY, mPassword);

        return rootConfig;
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        super.fromJSON(jsonObject);

        mAccountName = jsonObject.getString(JSON_ACCOUNT_KEY);
        mURL = jsonObject.getString(JSON_URL_KEY);
        if (jsonObject.has(JSON_LOGIN_KEY)) {
            mLogin = jsonObject.getString(JSON_LOGIN_KEY);
        }
        if (jsonObject.has(JSON_PASSWORD_KEY)) {
            mPassword = jsonObject.getString(JSON_PASSWORD_KEY);
        }
    }


    public FoclStruct addOrUpdateFoclStruct(JSONObject jsonStruct)
            throws JSONException
    {
        int structId = jsonStruct.getInt(Constants.JSON_ID_KEY);
        String structName = jsonStruct.getString(Constants.JSON_NAME_KEY);

        FoclStruct foclStruct = getFoclStructByRemoteId(structId);

        if (foclStruct != null) {
            if (!foclStruct.getName().equals(structName)) {
                foclStruct.setName(structName);
            }

        } else {
            foclStruct = new FoclStruct(getContext(), createLayerStorage(), mLayerFactory);

            foclStruct.setRemoteId(structId);
            foclStruct.setName(structName);
            foclStruct.setVisible(true);

            addLayer(foclStruct);
        }

        return foclStruct;
    }


    public void addOrUpdateFoclVectorLayer(
            JSONObject jsonLayer,
            FoclStruct foclStruct)
            throws JSONException
    {
        int layerId = jsonLayer.getInt(Constants.JSON_ID_KEY);
        String layerName = jsonLayer.getString(Constants.JSON_NAME_KEY);
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

                mNeedSync = true;
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
            foclVectorLayer.setURL(mURL);
            foclVectorLayer.setLogin(mLogin);
            foclVectorLayer.setPassword(mPassword);
            foclVectorLayer.setVisible(true);
            foclVectorLayer.setSyncType(Constants.SYNC_ATTRIBUTES);
            foclStruct.addLayer(foclVectorLayer);

            ++mCreatedVectorLayers;

            foclVectorLayer.setOnDownloadFinishedListener(
                    new NGWVectorLayer.OnDownloadFinishedListener()
                    {
                        @Override
                        public void OnDownloadFinished(boolean withError)
                        {
                            mDownloadedWithError = withError;
                            ++mDownloadedVectorLayers;

                            if (mCreatedVectorLayers == mDownloadedVectorLayers) {
                                mCreatedVectorLayers = 0;
                                mDownloadedVectorLayers = 0;

                                if (mOnDownloadFinishedListener != null) {
                                    mOnDownloadFinishedListener.OnDownloadFinished(
                                            false, mDownloadedWithError);
                                }

                                mDownloadedWithError = false;

                                if (mNeedSync) {
                                    mNeedSync = false;
                                    runSync();
                                }
                            }
                        }
                    });

            // in separate thread
            foclVectorLayer.downloadAsync();
        }
    }


    public void runSync()
    {
        AccountManager accountManager = AccountManager.get(mContext);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        // we work only with one account
        Account account = accountManager.getAccountsByType(Constants.NGW_ACCOUNT_TYPE)[0];
        ContentResolver.requestSync(account, FoclSettingsConstants.AUTHORITY, settingsBundle);
    }


    public String createOrUpdateFromJson(JSONArray jsonArray)
    {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonStruct = jsonArray.getJSONObject(i);

                FoclStruct foclStruct = addOrUpdateFoclStruct(jsonStruct);
                JSONArray jsonLayers = jsonStruct.getJSONArray(Constants.JSON_LAYERS_KEY);

                for (int jj = 0; jj < jsonLayers.length(); jj++) {
                    addOrUpdateFoclVectorLayer(jsonLayers.getJSONObject(jj), foclStruct);
                }
            }

            if (0 == mCreatedVectorLayers) {
                mDownloadedVectorLayers = 0;
                mDownloadedWithError = false;

                mNewLayersNotCreated = true;

                if (mNeedSync) {
                    mNeedSync = false;
                    runSync();
                }
            }

            save();
            return "";

        } catch (JSONException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }


    public void downloadAsync()
    {
        new DownloadTask().execute();
    }


    public String download()
    {
        if (!mNet.isNetworkAvailable()) {
            return getContext().getString(com.nextgis.maplib.R.string.error_network_unavailable);
        }

        try {

            final HttpGet get = new HttpGet(getFoclUrl(mURL)); //get as GeoJSON
            //basic auth
            if (null != mLogin && mLogin.length() > 0 && null != mPassword &&
                mPassword.length() > 0) {
                get.setHeader("Accept", "*/*");
                final String basicAuth = "Basic " + Base64.encodeToString(
                        (mLogin + ":" + mPassword).getBytes(), Base64.NO_WRAP);
                get.setHeader("Authorization", basicAuth);
            }

            final DefaultHttpClient HTTPClient = mNet.getHttpClient();
            final HttpResponse response = HTTPClient.execute(get);

            // Check to see if we got success
            final org.apache.http.StatusLine line = response.getStatusLine();
            if (line.getStatusCode() != 200) {
                Log.d(
                        Constants.TAG, "Problem downloading FOCL: " + mURL + " HTTP response: " +
                                       line);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                Log.d(Constants.TAG, "No content downloading FOCL: " + mURL);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            String data = EntityUtils.toString(entity);
            JSONArray jsonArray = new JSONArray(data);

            return createOrUpdateFromJson(jsonArray);

        } catch (IOException e) {
            Log.d(
                    Constants.TAG, "Problem downloading FOCL: " + mURL + " Error: " +
                                   e.getLocalizedMessage());
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        } catch (JSONException e) {
            e.printStackTrace();
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        }
    }


    public void setOnDownloadFinishedListener(OnDownloadFinishedListener listener)
    {
        mOnDownloadFinishedListener = listener;
    }


    public interface OnDownloadFinishedListener
    {
        void OnDownloadFinished(
                boolean newLayersNotCreated,
                boolean withError);
    }


    protected class DownloadTask
            extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... voids)
        {
            return download();
        }


        @Override
        protected void onPostExecute(String error)
        {
            boolean withError = false;

            if (null != error && error.length() > 0) {
                withError = true;
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }

            if (mNewLayersNotCreated && mOnDownloadFinishedListener != null) {
                mNewLayersNotCreated = false;
                mOnDownloadFinishedListener.OnDownloadFinished(true, withError);
            }
        }
    }
}
