package com.graftel.www.graftel;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Shorabh on 6/15/2016.
 */
public class QuoteDetailActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private ListView mListView1, mListView2;


    private ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    ArrayList<HashMap<String, String>> list1,list2;

    private String info = "https://www.graftel.com/appport/webGetCalInfoProd.php";
    private String docs = "https://www.graftel.com/appport/webGetDocPathProd.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_CID = "Calibration ID #";
    private static final String TAG_PATH = "Calibration PDF Path";
    private static final String TAG_MTE = "MT&E #";
    private static final String TAG_DATE = "Date";
    private static final String TAG_PO = "Customer PO #";
    private static final String TAG_STATUS = "Calibration Status";
    private static final String TAG_DNAME = "Document Name";
    private static final String TAG_DPATH = "Document Path";
    TextView t1;
    String QID = null;
    JSONArray products = null;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<HashMap<String, String>>> listDataChild;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quote_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        t1 = (TextView)findViewById(R.id.noorder);

        QID = getIntent().getStringExtra("QID");

        list1 = new ArrayList<HashMap<String, String>>();
        list2 = new ArrayList<HashMap<String, String>>();

        expListView = (ExpandableListView) findViewById(R.id.lvExp);

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

    @Override
    protected void onPause() {
        super.onPause();
        pDialog.dismiss();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), "Storage Access Denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    class Load extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(QuoteDetailActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("quoteId",QID));
            JSONObject json = jParser.makeHttpRequest(info, "GET", params);
            JSONObject json2 = jParser.makeHttpRequest(docs, "GET", params);
            if(json!=null) {
                //Log.d("All Products: ", json.toString());
                try {
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        products = json.getJSONArray("temp");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);
                            String line1 = "Calibration ID: " + c.getString(TAG_CID);
                            String line2 = "Customer PO#: " + c.getString(TAG_PO);
                            String line3 = c.getString(TAG_STATUS);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_CID, line1);
                            map.put(TAG_PO, line2);
                            map.put(TAG_STATUS, line3);
                            list1.add(map);
                        }
                    } else {
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            t1.setVisibility(View.VISIBLE);
                            t1.setText("No Calibration Info");
                        }
                    });
                }
            }
            else
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        t1.setVisibility(View.VISIBLE);
                        t1.setText("No Calibration Info");
                    }
                });
            }
            if(json2!=null) {
                //Log.d("All Products: ", json.toString());
                try {
                    int success = json2.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        products = json2.getJSONArray("temp");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);
                            String line1 = c.getString(TAG_DNAME) + " "/*+c.getString(TAG_DPATH)*/;
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_DNAME, line1);
                            map.put(TAG_DPATH, c.getString(TAG_DPATH));
                            list2.add(map);
                        }
                    } else {
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            t1.setVisibility(View.VISIBLE);
                            t1.setText("No Reports Found");
                        }
                    });
                }
            }
            else
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        System.out.println("Here");
                        t1.setVisibility(View.VISIBLE);
                        t1.setText("No Reports Found");
                    }
                });
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {

                    listDataHeader = new ArrayList<String>();
                    listDataChild = new HashMap<String, List<HashMap<String,String>>>();

                    listDataHeader.add("Calibration Info");
                    listDataHeader.add("Reports");

                    listDataChild.put(listDataHeader.get(0), list1); // Header, Child data
                    listDataChild.put(listDataHeader.get(1), list2);

                    System.out.println(list1.size());
                    System.out.println(list2.size());
                    listAdapter = new ExpandableListAdapter(getApplicationContext(), listDataHeader, listDataChild);

                    expListView.setAdapter(listAdapter);

                    expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v,
                                                    int groupPosition, long id) {
                            return false;
                        }
                    });

                    expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v,
                                                    int groupPosition, int childPosition, long id) {
                            if(groupPosition==0) {
                                Intent i = new Intent(QuoteDetailActivity.this,DeviceInfoActivity.class);
                                System.out.println(list1.get(childPosition).get(TAG_CID));
                                i.putExtra("result",list1.get(childPosition).get(TAG_CID).substring(list1.get(childPosition).get(TAG_CID).indexOf(":")+1,list1.get(childPosition).get(TAG_CID).length()));
                                startActivity(i);
                            }
                            else {
                                if (!list2.get(childPosition).get(TAG_DPATH).equals("null")) {
                                    new FTPConnect(list2.get(childPosition).get(TAG_DPATH)).execute();
                                }
                            }
                            return false;
                        }
                    });
                }
            });
        }
    }

    class FTPConnect extends AsyncTask<String, String, String> {

        private String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
        private String path = null;

        public FTPConnect(String path) {
            mFileName = mFileName + path;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(QuoteDetailActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                FTPSClient mFtpClient = User.getmFtpClient();
                File download = new File(mFileName);
                System.out.println(download.getParentFile());
                if (!download.getParentFile().isDirectory())
                    download.getParentFile().mkdir();
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(download));
                mFtpClient.retrieveFile(path, outputStream);
                outputStream.close();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            int hasPDFReadPermission = ContextCompat.checkSelfPermission(QuoteDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPDFReadPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(QuoteDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
            Uri fileURI = null;
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                fileURI = FileProvider.getUriForFile(QuoteDetailActivity.this, getString(R.string.file_provider_authority), new File(mFileName));
                i.setDataAndType(fileURI, "application/pdf");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "No Application to view PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
