package com.graftel.www.graftel;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.graftel.www.graftel.mail.SendMail;

/**
 * Created by Shorabh on 6/13/2016.
 */
public class SendMessageFragment extends Fragment
{
    EditText ed1,ed2,ed3,ed4;
    Button b1;
    String from,guest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.contact, container, false);

        ed1=(EditText)rootView.findViewById(R.id.editText1);
        ed2=(EditText)rootView.findViewById(R.id.editText2);
        ed3=(EditText)rootView.findViewById(R.id.editText3);
        ed4=(EditText)rootView.findViewById(R.id.editText4);

        guest = getArguments().getString("Guest","No");

        b1=(Button)rootView.findViewById(R.id.btnSend);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = ed3.getText().toString();
                final String body = "This a confirmation that your message has been successfully sent to Graftel, we will respond to you within 24 hours.\n" +
                        "\nThank you for contacting Graftel LLC. Below is the original message.\n" +
                        "\n---------------------------------------------------------------------------------------------------------------------------------\n" +
                        "\nMessage from "+ed1.getText().toString()+" ( "+ed3.getText().toString()+" ) \n"+
                        "\n"+ed4.getText().toString();
                final String subject = "[Graftel APP] Your message has been sent to Graftel";
                if (!ed1.getText().toString().equals("") && !ed2.getText().toString().equals("") && !ed3.getText().toString().equals("") && !ed4.getText().toString().equals("")) {
                    if(android.util.Patterns.EMAIL_ADDRESS.matcher(ed3.getText().toString()).matches()) {
                        new AsyncTask<String, Void, String>() {
                            @Override
                            protected String doInBackground(String... params) {
                                if (ConnectivityReceiver.isConnected()) {
                                    try {
                                        SendMail sender = new SendMail("graftel", "calibrate1");
                                        sender.sendMail(subject, body, from, "");
                                    } catch (Exception e) {
                                        Log.e("SendMail", e.getMessage(), e);
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "Message Sent!",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    if(guest.equals("Yes"))
                                        startActivity(new Intent(getActivity(), LoginActivity.class));
                                    else
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                }
                                return null;
                            }
                        }.execute();
                    }
                    else
                        Toast.makeText(getActivity(), "Email Address not Valid!",Toast.LENGTH_SHORT).show();

                }
                else
                    Toast.makeText(getActivity(), "Give all required Fields!",Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
