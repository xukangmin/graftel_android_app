package com.graftel.www.graftel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
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
 * Created by Shorabh on 6/14/2016.
 */
public class OrderFragment extends ListFragment {

    static ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    ArrayList<HashMap<String, String>> tempList;
    HashMap<Integer, String> qid = new HashMap<Integer, String>();
    boolean flag=false;
    private String url = "http://www.graftel.com/appport/webGetOrderInfoProd.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_QID = "QuoteID";
    private static final String TAG_DESC = "Description";
    private static final String TAG_NOI = "Number of Instruments";
    private static final String TAG_DATE = "Received Date";
    private static final String TAG_STATUS = "Status";
    TextView t1;
    JSONArray temp = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        t1 = (TextView)view.findViewById(R.id.noorder);
        tempList = new ArrayList<HashMap<String, String>>();
        if(TabFragment.viewPager.getCurrentItem()==0) {
            new LoadAllProducts().execute();
            new Runnable(){
                @Override
                public void run() {
                    try {
                        new LoadAllProducts().get(5000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        Toast.makeText(getActivity(),"Timeout",Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(),QuoteDetailActivity.class);
                i.putExtra("QID",qid.get(position));
                startActivity(i);
            }
        });
    }

    class LoadAllProducts extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //System.out.println(User.getUID());
            params.add(new BasicNameValuePair("user",User.getUID()));
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            if(json!=null)
            {
                Log.d("Temp: ", json.toString());
                try
                {
                    int success = json.getInt(TAG_SUCCESS);
                    if(success == 1)
                    {
                        temp = json.getJSONArray("temp");
                        for (int i = 0; i < temp.length(); i++)
                        {
                            JSONObject c = temp.getJSONObject(i);
                            qid.put(i,c.getString(TAG_QID));
                            String line1 = "Quote ID: "+c.getString(TAG_QID);
                            String desc[] = c.getString(TAG_DESC).split(",");
                            String line2 = "";
                            for(int j=0;j<desc.length;j++)
                            {
                                line2 += desc[j];
                                if(j!=desc.length-1)
                                    line2 += "\n";
                            }
                            String line3 = c.getString(TAG_STATUS)+" on "+c.getString(TAG_DATE).substring(9,19);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_QID, line1);
                            map.put(TAG_DESC, line2.trim());
                            map.put(TAG_STATUS, line3.trim());
                            tempList.add(map);
                        }
                    }
                    else
                    {}
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            else
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        t1.setVisibility(View.VISIBLE);
                    }});
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            getActivity(), tempList,
                            R.layout.order_item, new String[]{TAG_QID,
                            TAG_DESC,TAG_STATUS},
                            new int[]{R.id.line1, R.id.line2,R.id.line3});
                    setListAdapter(adapter);
                }
            });
        }
    }
}
