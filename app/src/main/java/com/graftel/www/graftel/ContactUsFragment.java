package com.graftel.www.graftel;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Shorabh on 6/30/2016.
 */
public class ContactUsFragment extends ListFragment {

    ArrayList<HashMap<String, String>> tempList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        tempList = new ArrayList<HashMap<String, String>>();

        String[] name = {"Calibration Sales","Calibration Technical","Product Sales","Product Technical"};
        String[] email = {"esther@graftel.com","scott@graftel.com","hector@graftel.com","narayan@graftel.com"};
        String[] contact = {"224-279-8169","224-279-4823","224-279-8337","224-279-4077"};

        for (int i = 0; i < 4; i++)
        {
            String line1 = " "+name[i];
            String line2 = " "+email[i];
            String line3 = " "+contact[i];
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", line1);
            map.put("email", line2);
            map.put("contact", line3);
            tempList.add(map);
        }

        ListAdapter adapter = new TestAdapter(
                getActivity(), tempList,
                R.layout.contact_item, new String[]{"name","email",
                "contact"},
                new int[]{R.id.line1, R.id.line2,R.id.line3});
        setListAdapter(adapter);
        return view;
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

            View row = super.getView(position, convertView, parent);
            final String mail = data.get(position).get("email");
            final String phone = data.get(position).get("contact");

            Button btn1= (Button) row.findViewById(R.id.line2);
            Button btn2= (Button) row.findViewById(R.id.line3);

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Send an EMAIL?")
                            .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Intent.ACTION_SENDTO).putExtra(Intent.EXTRA_EMAIL,mail).setData(Uri.parse("mailto:"+mail)));
                                }
                            })
                            .setNegativeButton("NO!",null)
                            .create()
                            .show();
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int hasMailPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);
                    if (hasMailPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.CALL_PHONE},1);
                        return;
                    }
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Place a Call?")
                            .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:"+phone)));
                                }
                            })
                            .setNegativeButton("NO!",null)
                            .create()
                            .show();
                }
            });
            return row;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity(), "Call Access Denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

