package com.graftel.www.graftel;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;
import com.graftel.www.graftel.mail.SendMail;

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
 * Created by Shorabh on 6/16/2016.
 */
public class DeviceInfoActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener
{
    private ListView mListView1;
    static ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    ArrayList<HashMap<String, String>> tempList;

    private String url = "https://www.graftel.com/appport/webGetAllCalInfoProd.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_CID = "Calibration ID #";
    private static final String TAG_PATH = "Calibration PDF Path";
    private static final String TAG_MTE = "MT&E #";
    private static final String TAG_DATE = "Date";
    private static final String TAG_PO = "Customer PO #";
    private static final String TAG_STATUS = "Calibration Status";
    ArrayList<HashMap<String, String>> list1;
    JSONArray temp = null;
    String cal;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        invalidateOptionsMenu();
        mListView1 = (ListView)findViewById(R.id.list1);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("Calibration Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        System.out.println(mListView1.getChildCount());
        cal = getIntent().getStringExtra("result");
        list1 = new ArrayList<HashMap<String, String>>();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.request_all, menu);
        if(list1.size()>1) {
            menu.findItem(R.id.request_all).setVisible(true);
        }
        this.invalidateOptionsMenu();
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.request_all).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    new AlertDialog.Builder(DeviceInfoActivity.this)
                            .setMessage("Do you want a Requote?")
                            .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String subject = "Quote Request From " + User.getCompanyName() + " (" + User.getLoginEmail() + ")";
                                    final String body = "\nThis a confirmation that your quote request has been successfully sent to Graftel, we will respond to you within 24 hours.\n" +
                                            "\nThank you for contacting Graftel LLC. Below is the original message.<br>\n" +
                                            "\n---------------------------------------------------------------------------------------------------------------------------------\n" +
                                            "\nQuote request from"+ User.getContactPersonName ()+" ( "+User.getLoginEmail()+" ) \n" +
                                            "\nCustomer is requesting a quote for calibration ID: "+ cal;

                                    //final String body = " Customer is requesting a quote for calibration IDs: " + cal + ". Please respond to Customer ASAP.";
                                    System.out.println(body);
                                    new AsyncTask<String, Void, String>() {
                                        @Override
                                        protected String doInBackground(String... params) {
                                            if (ConnectivityReceiver.isConnected()) {
                                                try {
                                                    SendMail sender = new SendMail("graftel", "calibrate1");
                                                    sender.sendMail(subject, body, "kangmin@graftel.com","");
                                                } catch (Exception e) {
                                                    Log.e("SendMail", e.getMessage(), e);
                                                }
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Quote Request Sent!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "No Internet Connection! Request Not Sent!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            return null;
                                        }
                                    }.execute();
                                }
                            })
                            .setNegativeButton("NO!", null)
                            .create()
                            .show();
                    return false;
                }
            });

        return true;
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
        //register connection status listener
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

    class TestAdapter extends SimpleAdapter {

        private Context context;
        private int resource;
        ArrayList<HashMap<String, String>> data;


        public TestAdapter(Context context, ArrayList<HashMap<String,String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context=context;
            this.resource=resource;
            this.data=data;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final View row = super.getView(position, convertView, parent);
            final ImageButton imageButton= (ImageButton) row.findViewById(R.id.img);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!list1.get(position).get(TAG_PATH).equals("null"))
                    {
                        imageButton.setVisibility(View.VISIBLE);
                        imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FTPConnect(list1.get(position).get(TAG_PATH)).execute();
                            }
                        });
                    }
                    else
                    {
                        row.findViewById(R.id.l6).setVisibility(View.VISIBLE);
                        imageButton.setVisibility(View.GONE);

                    }
                }
            });
            final Button button= (Button) row.findViewById(R.id.requote);
            sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sp.edit();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long timestamp = sp.getLong(list1.get(position).get(TAG_CID), 0);
                    if(timestamp==0 || ((System.currentTimeMillis()-timestamp)>=(24*60*60*1000))) {
                        editor.putLong(list1.get(position).get(TAG_CID), System.currentTimeMillis());
                        editor.commit();
                        new AlertDialog.Builder(DeviceInfoActivity.this)
                                .setMessage("Do you want a Re-Quote?")
                                .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String subject = "[Graftel APP] Your quote request has been sent to Graftel";
                                        final String body = "\nThis a confirmation that your quote request has been successfully sent to Graftel, we will respond to you within 24 hours.\n" +
                                                "\nThank you for contacting Graftel LLC. Below is the original message.\n" +
                                                "\n---------------------------------------------------------------------------------------------------------------------------------\n" +
                                                "\nQuote request from "+ User.getContactPersonName ()+" ( "+User.getLoginEmail()+" ) \n" +
                                                "\nCustomer is requesting a quote for calibration ID: "+ list1.get(position).get(TAG_CID);
                                        //final String body = " Customer is requesting a quote for calibration ID " + list1.get(position).get(TAG_CID) + ". Please respond to Customer ASAP.";
                                        new AsyncTask<String, Void, String>() {
                                            @Override
                                            protected String doInBackground(String... params) {
                                                if (ConnectivityReceiver.isConnected()) {
                                                    try {
                                                        SendMail sender = new SendMail("graftel", "calibrate1");
                                                        sender.sendMail(subject, body, User.getLoginEmail(),"");
                                                    } catch (Exception e) {
                                                        Log.e("SendMail", e.getMessage(), e);
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), "Quote Request Sent!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), "No Internet Connection! Request Not Sent!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                return null;
                                            }
                                        }.execute();
                                    }
                                })
                                .setNegativeButton("NO!", null)
                                .create()
                                .show();
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Cannot Send a Re-Quote request within 24 hours.",Toast.LENGTH_SHORT).show();
                }
            });
            return row;
        }

    }


    class Load extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DeviceInfoActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("cal",cal));
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            //Log.d("Temp: ", json.toString());

            try
            {
                int success = json.getInt(TAG_SUCCESS);
                if(success == 1)
                {
                    temp = json.getJSONArray("temp");
                    for (int i = 0; i < temp.length(); i++)
                    {
                        JSONObject c = temp.getJSONObject(i);
                        String line1 = c.getString(TAG_CID);
                        String line2 = c.getString(TAG_MTE);
                        String line3 = c.getString(TAG_PO);
                        String line4 = c.getString(TAG_STATUS);
                        String line5 = c.getString(TAG_PATH);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_CID, line1);
                        map.put(TAG_MTE, line2);
                        map.put(TAG_PO, line3);
                        map.put(TAG_STATUS, line4);
                        map.put(TAG_PATH, line5);
                        map.put("Name","");
                        list1.add(map);
                    }
                }
                else
                {
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            DeviceInfoActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    mListView1.setAdapter(new TestAdapter(DeviceInfoActivity.this, list1,
                            R.layout.device_item,new String[]{TAG_CID,TAG_MTE,TAG_PO,
                            TAG_STATUS},
                            new int[]{R.id.line1, R.id.line2,R.id.line3,R.id.line4}));
                }
            });

        }
    }

    class FTPConnect extends AsyncTask<String, String, String> {

        private String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
        private String path=null;

        public FTPConnect(String path)
        {
            mFileName = mFileName+path;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DeviceInfoActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try
            {
                FTPSClient mFtpClient = User.getmFtpClient();
                File download = new File(mFileName);
                if(!download.getParentFile().isDirectory())
                    download.getParentFile().mkdir();
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(download));
                mFtpClient.retrieveFile(path,outputStream);
                outputStream.close();

            }
            catch (SocketException e)
            {
                e.printStackTrace();
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            int hasPDFReadPermission = ContextCompat.checkSelfPermission(DeviceInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPDFReadPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeviceInfoActivity.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                return;
            }
            Uri fileURI = null;
            try {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setDataAndType(Uri.fromFile(new File(mFileName)), "application/pdf");
//                startActivity(i);
                Intent i = new Intent(Intent.ACTION_VIEW);
                fileURI = FileProvider.getUriForFile(DeviceInfoActivity.this, getString(R.string.file_provider_authority), new File(mFileName));
                i.setDataAndType(fileURI, "application/pdf");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(i);
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getApplicationContext(), "No Application to view PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
