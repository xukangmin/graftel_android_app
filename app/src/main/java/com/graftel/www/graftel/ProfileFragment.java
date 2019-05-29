package com.graftel.www.graftel;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;
import com.graftel.www.graftel.mail.SendMail;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Shorabh on 6/13/2016.
 */
public class ProfileFragment extends Fragment
{

    Button b1,b2;
    TextView t1,t2;

    private ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    String call;
    private String url = "https://www.graftel.com/appport/webUpdateCustomerInfo.php";
    private static final String TAG_SUCCESS = "success";
    String email,pwd,company,person,ship,phone,fax,addEmail1,addEmail2,addEmail3,addEmail4;
    static EditText e1,e2,e3,e4,e5,e6,e7,e8,e9,e10,e11,e12;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        editor = LoginActivity.sharedpreferences.edit();

        call = getArguments().getString("call");
        t1 = (TextView)view.findViewById(R.id.textview2);
        t2 = (TextView)view.findViewById(R.id.textview3);

        e1 = (EditText)view.findViewById(R.id.editText1);
        e2 = (EditText)view.findViewById(R.id.editText2);
        e3 = (EditText)view.findViewById(R.id.editText3);
        e4 = (EditText)view.findViewById(R.id.editText4);
        e5 = (EditText)view.findViewById(R.id.editText5);
        e6 = (EditText)view.findViewById(R.id.editText6);
        e7 = (EditText)view.findViewById(R.id.editText7);
        e8 = (EditText)view.findViewById(R.id.editText8);
        e9 = (EditText)view.findViewById(R.id.editText9);
        e10 = (EditText)view.findViewById(R.id.editText10);
        e11 = (EditText)view.findViewById(R.id.editText11);
        e12 = (EditText)view.findViewById(R.id.editText12);

        b1 = (Button)view.findViewById(R.id.button);
        b2 = (Button)view.findViewById(R.id.button2);


        e4.setText(User.getCompanyName());


        if(call.equals("exist"))
        {
            e1.setText(User.getLoginEmail());
            e5.setText(User.getContactPersonName());
            e6.setText(User.getShippingAddress());
            e7.setText(User.getPhone());
            e8.setText(User.getFax());
            e9.setText(User.getAddEmail1());
            e10.setText(User.getAddEmail2());
            e11.setText(User.getAddEmail3());
            e12.setText(User.getAddEmail4());
            e1.setFocusable(false);
            e1.setClickable(false);
            e2.setVisibility(View.GONE);
            t1.setVisibility(View.GONE);
            e3.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.VISIBLE);
        }
        else
        {
            b1.setGravity(Gravity.CENTER_HORIZONTAL);
            //MainActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //MainActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
        }

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e2.setVisibility(View.VISIBLE);
                t1.setVisibility(View.VISIBLE);
                e3.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(ConnectivityReceiver.isConnected()) {
                    email = e1.getText().toString();
                    company = e4.getText().toString();
                    person = e5.getText().toString();
                    phone = e6.getText().toString();
                    fax = e7.getText().toString();
                    ship = e8.getText().toString();
                    addEmail1 = e9.getText().toString();
                    addEmail2 = e10.getText().toString();
                    addEmail3 = e11.getText().toString();
                    addEmail4 = e12.getText().toString();
                    System.out.println(person);
                    if((email.equals(""))||(company.equals(""))||(person.equals(""))||(phone.equals("")))
                        Toast.makeText(getActivity(), "Please Fill All Required Fields!",Toast.LENGTH_SHORT).show();
                    else if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (e2.isShown() && e3.isShown()) {
                            if (!e2.getText().toString().equals("") && !e3.getText().toString().equals("")) {
                                if (e2.getText().toString().equals(e3.getText().toString())) {
                                    try {
                                        pwd = Password.hashValue(e2.getText().toString());
                                    } catch (Exception e) {
                                    }
                                    new Load().execute();
                                } else
                                    Toast.makeText(getActivity(), "Passwords Doesn't Match!", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getActivity(), "Password Fields are Empty!", Toast.LENGTH_SHORT).show();
                        } else {
                            pwd = User.getPassword();
                            new Load().execute();
                        }
                    }
                    else
                        Toast.makeText(getActivity(), "Enter a valid Email Address!",Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();

            }
        });

        return view;

    }

    class Load extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        boolean flag=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            System.out.println(User.getUID());
            params.add(new BasicNameValuePair("user",User.getUID()));
            params.add(new BasicNameValuePair("temp","0"));
            params.add(new BasicNameValuePair("email",email));
            params.add(new BasicNameValuePair("pwd",pwd));
            params.add(new BasicNameValuePair("name",company));
            params.add(new BasicNameValuePair("contact",person));
            params.add(new BasicNameValuePair("ship",ship));
            params.add(new BasicNameValuePair("phone",phone));
            params.add(new BasicNameValuePair("fax",fax));
            params.add(new BasicNameValuePair("email1",addEmail1));
            params.add(new BasicNameValuePair("email2",addEmail2));
            params.add(new BasicNameValuePair("email3",addEmail3));
            params.add(new BasicNameValuePair("email4",addEmail4));
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            //Log.d("All Products: ", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1){
                    flag = true;
                }
                else if(success == -1){
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Email Address already Exist!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            final String body = "UID "+User.getUID()+"\n" +
                    "\nLogin Email "+email+"\n" +
                    "\nCompany "+company+"\n" +
                    "\nContact Person Name  "+person+"\n" +
                    "\nPhone "+phone;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if(flag) {
                        if(!call.equals("exist"))
                        {
                            if(ConnectivityReceiver.isConnected()) {
                                try {
                                    SendMail sender = new SendMail("graftel", "calibrate1");
                                    sender.sendMail("[Graftel APP] New Customer Creation Notification", body, email,"");
                                } catch (Exception e) {
                                    Log.e("SendMail", e.getMessage(), e);
                                }
                            }
                            else
                                Toast.makeText(getActivity(), "No Internet Connection! Mail Not Sent!", Toast.LENGTH_SHORT).show();
                        }
                        editor.putString("UID",User.getUID());
                        editor.putString("LoginEmail",email);
                        editor.putString("Password",pwd);
                        editor.putString("CompanyName",company);
                        editor.putString("ContactPersonName",person);
                        editor.putString("Phone",phone);
                        editor.putString("Fax",fax);
                        editor.putString("ShippingAddress",ship);
                        editor.putString("AdditionalEmail1",addEmail1);
                        editor.putString("AdditionalEmail2",addEmail2);
                        editor.putString("AdditionalEmail3",addEmail3);
                        editor.putString("AdditionalEmail4",addEmail4);
                        editor.putBoolean("LoggedIn",true);
                        editor.commit();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                }
            });
        }
    }
}
