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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.nextgis.maplib.api.INGWLayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.NetworkUtil;
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

import static com.nextgis.maplib.util.Constants.SYNC_ATTRIBUTES;
import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.LAYERTYPE_FOCL_PROJECT;


public class FoclProject
        extends LayerGroup
        implements INGWLayer
{
    protected static final String JSON_ACCOUNT_KEY  = "account";
    protected static final String JSON_URL_KEY      = "url";
    protected static final String JSON_LOGIN_KEY    = "login";
    protected static final String JSON_PASSWORD_KEY = "password";

    protected String      mAccountName;
    protected NetworkUtil mNet;
    protected String      mURL;
    protected String      mLogin;
    protected String      mPassword;


    public FoclProject(
            Context context,
            File path,
            LayerFactory layerFactory)
    {
        super(context, path, layerFactory);

        mNet = new NetworkUtil(context);
        mLayerType = LAYERTYPE_FOCL_PROJECT;
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


    public String createFromJson(JSONArray jsonArray)
    {
        try {

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonStruct = jsonArray.getJSONObject(i);

                int structId = jsonStruct.getInt("id");
                String structName = jsonStruct.getString("name");

                FoclStruct foclStruct =
                        new FoclStruct(getContext(), cretateLayerStorage(), mLayerFactory);

                foclStruct.setRemoteId(structId);
                foclStruct.setName(structName);
                foclStruct.setVisible(true);

                addLayer(foclStruct);

                JSONArray jsonLayers = jsonStruct.getJSONArray("layers");

                for (int jj = 0; jj < jsonLayers.length(); jj++) {
                    JSONObject jsonLayer = jsonLayers.getJSONObject(jj);

                    int layerId = jsonLayer.getInt("id");
                    String layerName = jsonLayer.getString("name");
                    String layerType = jsonLayer.getString("type");

                    FoclVectorLayer foclVectorLayer = new FoclVectorLayer(
                            foclStruct.getContext(), foclStruct.cretateLayerStorage());

                    foclVectorLayer.setRemoteId(layerId);
                    foclVectorLayer.setName(layerName);
                    foclVectorLayer.setFoclLayerType(
                            FoclVectorLayer.getFoclLayerTypeFromString(layerType));
                    foclVectorLayer.setAccountName(mAccountName);
                    foclVectorLayer.setURL(mURL);
                    foclVectorLayer.setLogin(mLogin);
                    foclVectorLayer.setPassword(mPassword);
                    foclVectorLayer.setVisible(true);
                    foclVectorLayer.setSyncType(SYNC_ATTRIBUTES);

                    foclStruct.addLayer(foclVectorLayer);

                    //init in separate thread
                    foclVectorLayer.downloadAsync();
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
                        TAG, "Problem downloading FOCL: " + mURL + " HTTP response: " +
                             line);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                Log.d(TAG, "No content downloading FOCL: " + mURL);
                return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
            }

            String data = EntityUtils.toString(entity);
            JSONArray jsonArray = new JSONArray(data);

            return createFromJson(jsonArray);

        } catch (IOException e) {
            Log.d(
                    TAG, "Problem downloading FOCL: " + mURL + " Error: " +
                         e.getLocalizedMessage());
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        } catch (JSONException e) {
            e.printStackTrace();
            return getContext().getString(com.nextgis.maplib.R.string.error_download_data);
        }
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
            if (null != error && error.length() > 0) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
