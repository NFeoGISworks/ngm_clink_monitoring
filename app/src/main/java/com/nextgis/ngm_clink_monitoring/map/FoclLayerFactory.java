package com.nextgis.ngm_clink_monitoring.map;

import android.content.Context;
import android.util.Log;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.nextgis.maplib.util.Constants.CONFIG;
import static com.nextgis.maplib.util.Constants.JSON_TYPE_KEY;
import static com.nextgis.maplib.util.Constants.TAG;


/**
 * Created by bishop on 22.01.15.
 */
public class FoclLayerFactory
        extends LayerFactory
{
    public FoclLayerFactory(File mapPath)
    {
        super(mapPath);
    }


    @Override
    public ILayer createLayer(
            Context context,
            File path)
    {
        File config_file = new File(path, CONFIG);
        try {
            String sData = FileUtil.readFromFile(config_file);
            JSONObject rootObject = new JSONObject(sData);
            int nType = rootObject.getInt(JSON_TYPE_KEY);
            switch (nType) {
                case FoclConstants.LAYERTYPE_FOCL_PROJECT:
                    return new FoclProject(context, path, this);
                case FoclConstants.LAYERTYPE_FOCL_STRUCT:
                    return new FoclStruct(context, path, this);
                case FoclConstants.LAYERTYPE_FOCL_VECTOR:
                    return new FoclVectorLayerUI(context, path);
            }

        } catch (IOException| JSONException e){
            Log.d(TAG, e.getLocalizedMessage());
        }

        return super.createLayer(context, path);
    }


    @Override
    public void createNewRemoteTMSLayer(
            Context context,
            LayerGroup groupLayer)
    {

    }


    @Override
    public void createNewNGWLayer(
            Context context,
            LayerGroup groupLayer)
    {

    }
}
