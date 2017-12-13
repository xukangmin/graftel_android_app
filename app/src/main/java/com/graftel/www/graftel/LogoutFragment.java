package com.graftel.www.graftel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Shorabh on 6/14/2016.
 */
public class LogoutFragment extends Fragment
{
    Button b1;
    TextView t1;
    SharedPreferences sharedpreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logout, container, false);
        t1 = (TextView)view.findViewById(R.id.textView);
        t1.setText(t1.getText().toString()+User.getContactPersonName()+"!");
        b1 = (Button)view.findViewById(R.id.button);
        sharedpreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);;
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        return view;
    }
}
