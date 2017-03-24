package xyz.digzdigital.cunavigator.navigator;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Digz on 24/03/2017.
 */

public class AssetsFileReader {
    private List<double[]> points;
    private Context context;

private String filePath;

    private String jsonNode;



    AssetsFileReader(Context context) {
        this.context = context;
    }

    public void setJsonNode(String jsonNode) {
        this.jsonNode = jsonNode;
    }

    public List<double[]> getPointsJsonFromFile() {
        try {
            String jsonString = readAssets();
            interpreteJson(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return points;
    }

    private String readAssets()throws IOException{
        AssetManager assetManager = context.getAssets();
        StringBuilder buf = new StringBuilder();
        InputStream json = null;
            json = assetManager.open(filePath);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
        return buf.toString();
    }

    private void interpreteJson(String jsonString)throws JSONException{

    }
}
