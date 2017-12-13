package com.graftel.www.graftel;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shorabh on 6/8/2016.
 */
public class LoginActivity extends AppCompatActivity
{
    Button b1,b2,b3;
    EditText ed1,ed2;
    static ProgressDialog pDialog; // Progress Dialog
    JSONParser jParser = new JSONParser(); // Creating JSON Parser object
    User u;

    private String url = "http://www.graftel.com/appport/webGetUserInfoProd.php";

    private static final String TAG_SUCCESS = "success";
    boolean email,valid;
    String username=null,pass=null;
    JSONArray temp = null;
    static SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        b1=(Button)findViewById(R.id.btnLogin);
        b2=(Button)findViewById(R.id.btnRegister);
        b3 = (Button)findViewById(R.id.btnForgot);
        ed1=(EditText)findViewById(R.id.email);
        ed2=(EditText)findViewById(R.id.password);
        email = false;


        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        boolean status = sharedpreferences.getBoolean("LoggedIn", false);
        if (status)
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    username = ed1.getText().toString();
                    pass = ed2.getText().toString();
                    //username = "sdhandha@hawk.iit.edu";
                    //pass = "Shorabh123";
                    editor.putString("Username", username);
                    editor.putString("Password", pass);
                    //editor.commit();
                    if (ed1.getText().toString().equals("") || ed2.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(), "Enter Username/Password", Toast.LENGTH_SHORT).show();
                    else {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(ed1.getText().toString()).matches())
                            email = true;
                        new Load().execute();
                    }
                    //email = true;
                    //new Load().execute();
                }
                else
                    Toast.makeText(getApplicationContext(), "No Internet Connection",Toast.LENGTH_SHORT).show();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GuestActivity.class).putExtra("Guest","Yes"));
                finish();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                int hasInternetPermission = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET);
                if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.INTERNET},1);
                    return;
                }
                Intent i = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                startActivity(i);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        //pDialog.dismiss();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    class Load extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(email) {
                params.add(new BasicNameValuePair("email", username));
            }
            else
            {
                params.add(new BasicNameValuePair("user",username));
            }

            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            Log.d("Temp: ", json.toString());

            try
            {
                int success = json.getInt(TAG_SUCCESS);
                if(success == 1)
                {
                    temp = json.getJSONArray("temp");
                    valid=false;
                    for (int i = 0; i < temp.length(); i++)
                    {
                        JSONObject c = temp.getJSONObject(i);
                        addUser(c);
                        String password = c.getString("Password");
                        if(email)
                        {
                            try {
                                valid = Password.verifyHashedValue(password,pass);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            valid = pass.equals(password);
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Username/Password not Valid!",Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void addUser(JSONObject c)
        {
            try {
                if(email){
                    editor.putString("UID",c.getString("UID"));
                    editor.putString("IsTempUser",c.getString("IsTempUser"));
                    editor.putString("LoginEmail",c.getString("LoginEmail"));
                    editor.putString("Password",c.getString("Password"));
                    editor.putString("CompanyName",c.getString("CompanyName"));
                    editor.putString("ContactPersonName",c.getString("ContactPersonName"));
                    editor.putString("Phone",c.getString("Phone"));
                    editor.putString("Fax",c.getString("Fax"));
                    editor.putString("ShippingAddress",c.getString("ShippingAddress"));
                    editor.putString("AdditionalEmail1",c.getString("AdditionalEmail1"));
                    editor.putString("AdditionalEmail2",c.getString("AdditionalEmail2"));
                    editor.putString("AdditionalEmail3",c.getString("AdditionalEmail3"));
                    editor.putString("AdditionalEmail4",c.getString("AdditionalEmail4"));
                    editor.putBoolean("LoggedIn",true);
                    editor.commit();
                }else {
                    User.setUID(c.getString("UID"));
                    User.setLoginEmail(c.getString("LoginEmail"));
                    User.setIsTempUser(c.getString("IsTempUser"));
                    User.setPassword(c.getString("Password"));
                    User.setCompanyName(c.getString("CompanyName"));
                    User.setContactPersonName(c.getString("ContactPersonName"));
                    User.setPhone(c.getString("Phone"));
                    User.setFax(c.getString("Fax"));
                    User.setShippingAddress(c.getString("ShippingAddress"));
                    User.setAddEmail1(c.getString("AdditionalEmail1"));
                    User.setAddEmail2(c.getString("AdditionalEmail2"));
                    User.setAddEmail3(c.getString("AdditionalEmail3"));
                    User.setAddEmail4(c.getString("AdditionalEmail4"));
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if(valid)
            {
                if(email)
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                else
                {
                    startActivity(new Intent(getApplicationContext(),TempUserActivity.class).putExtra("call","temp"));
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();
        }
    }
}
