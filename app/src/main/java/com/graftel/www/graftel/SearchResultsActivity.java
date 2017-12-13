package com.graftel.www.graftel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Shorabh on 7/25/16.
 */
public class SearchResultsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {


    static ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    ArrayList<HashMap<String, String>> tempList;

    private static String url = "http://www.graftel.com/appport/webSearchCalInfoProd.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_CID = "Calibration ID #";
    private static final String TAG_PATH = "Calibration PDF Path";
    private static final String TAG_MTE = "MT&E #";
    private static final String TAG_DATE = "Date";
    private static final String TAG_PO = "Customer PO #";
    private static final String TAG_STATUS = "Calibration Status";
    TextView t1;
    JSONArray temp = null;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        t1 = (TextView) findViewById(R.id.noorder);
        tempList = new ArrayList<HashMap<String, String>>();

        listView = (ListView) findViewById(R.id.list1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), DeviceInfoActivity.class);
                i.putExtra("result", tempList.get(position).get(TAG_CID).substring(tempList.get(position).get(TAG_CID).indexOf(":") + 1, tempList.get(position).get(TAG_CID).length()));
                startActivity(i);
            }
        });
        new Load().execute();
        new Runnable(){
            @Override
            public void run() {
                try {
                    new Load().get(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    Toast.makeText(getApplicationContext(),"Timeout",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Graftel.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    class Load extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SearchResultsActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            System.out.println(ViewCertiFragment.start+" "+ViewCertiFragment.end+" "+ViewCertiFragment.serial+" "+ViewCertiFragment.mte+" "+ViewCertiFragment.po+" "+User.getUID());
            params.add(new BasicNameValuePair("user",User.getUID()));
            if(!ViewCertiFragment.start.equals(""))
                params.add(new BasicNameValuePair("start",ViewCertiFragment.start));
            if(!ViewCertiFragment.end.equals(""))
                params.add(new BasicNameValuePair("end",ViewCertiFragment.end));
            if(!ViewCertiFragment.serial.equals(""))
                params.add(new BasicNameValuePair("serial",ViewCertiFragment.serial));
            if(!ViewCertiFragment.mte.equals(""))
                params.add(new BasicNameValuePair("mte",ViewCertiFragment.mte));
            if(!ViewCertiFragment.po.equals(""))
                params.add(new BasicNameValuePair("cpo",ViewCertiFragment.po));
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            System.out.println("URL: "+url);
            if(json!=null) {
                //Log.d("Temp: ", json.toString());
                //Log.d("JSON Parser", String.valueOf(json));
                try {
                    int success = json.getInt(TAG_SUCCESS);
                    System.out.println("Success: " + success);
                    if (success == 1) {
                        temp = json.getJSONArray("temp");
                        for (int i = 0; i < temp.length(); i++) {
                            JSONObject c = temp.getJSONObject(i);
                            String line1 = "Calibration ID: " + c.getString(TAG_CID);
                            String line2 = "";
                            if(!c.getString(TAG_DATE).equals("null"))
                                line2 = "Date: " + c.getString(TAG_DATE).substring(9, 19);
                            String line4 = c.getString(TAG_STATUS);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_CID, line1);
                            map.put(TAG_DATE, line2.trim());
                            map.put(TAG_STATUS, line4.trim());
                            tempList.add(map);
                        }
                    } else {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
                runOnUiThread(new Runnable() {
                    public void run() {
                        t1.setVisibility(View.VISIBLE);
                    }
                });
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            getApplicationContext(), tempList,
                            R.layout.order_item, new String[]{TAG_CID,TAG_DATE,TAG_STATUS},
                            new int[]{R.id.line1, R.id.line2,R.id.line3});
                    listView.setAdapter(adapter);
                }
            });
        }
    }
}
