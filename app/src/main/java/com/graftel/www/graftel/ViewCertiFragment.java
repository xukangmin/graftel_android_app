package com.graftel.www.graftel;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.graftel.www.graftel.database.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Shorabh on 6/14/2016.
 */
public class ViewCertiFragment extends Fragment
{

    EditText ed1,ed2,ed3;

    static String start,end,po,serial,mte;

    static Button b1,b2,b3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.certi, container, false);

        b1=(Button)rootView.findViewById(R.id.start);
        b2=(Button)rootView.findViewById(R.id.end);

        ed1=(EditText)rootView.findViewById(R.id.po);
        ed2=(EditText)rootView.findViewById(R.id.serial);
        ed3=(EditText)rootView.findViewById(R.id.mte);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "start");
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "end");
            }
        });

        b3=(Button)rootView.findViewById(R.id.view);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = b1.getText().toString();
                end = b2.getText().toString();
                po = ed1.getText().toString();
                serial=ed2.getText().toString();
                mte = ed3.getText().toString();
                if(po.equals("")&&serial.equals("")&&mte.equals("")&&(start.equals("")&&end.equals("")))
                    Toast.makeText(getActivity(),"Please give at least one Search Criteria",Toast.LENGTH_SHORT).show();
                else {
                    if ((start.equals("") && !end.equals("")) || (!start.equals("") && end.equals("")))
                        Toast.makeText(getActivity(), "Please Select Start & End Date!", Toast.LENGTH_SHORT).show();
                    else
                        startActivity(new Intent(getActivity(), SearchResultsActivity.class));
                }
            }
        });

        return rootView;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if(getTag()=="start")
            {
                b1.setText(new StringBuilder().append(year).append("-")
                        .append(month+1).append("-").append(day));
            }
            else
            {
                b2.setText(new StringBuilder().append(year).append("-")
                        .append(month+1).append("-").append(day));
            }
        }
    }
}
