package com.example.myapplication;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class PhonenumberAdaptor extends ArrayAdapter<Phonenumber> implements Filterable {
    private Context context;
    private ArrayList<Phonenumber> phonenumbers;
    public ArrayList<Phonenumber> filteredphonenumbers;
    Filter listFilter;


    public PhonenumberAdaptor(Context context, ArrayList<Phonenumber> phonenumbers) {
        super(context,-1,phonenumbers);
        this.context = context;
        this.phonenumbers = phonenumbers;
        this.filteredphonenumbers = phonenumbers;
    }

    @Override
    public int getCount() {
        return filteredphonenumbers.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_contact, parent, false);

        TextView nameTextView = (TextView) view.findViewById(R.id.txt_name);
        nameTextView.setText(filteredphonenumbers.get(position).getName());
        final TextView numberTextView = (TextView) view.findViewById(R.id.number);
        numberTextView.setText(filteredphonenumbers.get(position).getNumber());

        LinearLayout hidden = (LinearLayout) view.findViewById(R.id.hidden);
        hidden.setVisibility(view.GONE);

        //Button close_hidden = (Button) view.findViewById(R.id.close_hidden);
        Button call = (Button) view.findViewById(R.id.call);
        Button msg = (Button) view.findViewById(R.id.msg);
        Button update = (Button) view.findViewById(R.id.update);
        call.setBackgroundColor(Color.WHITE);
        msg.setBackgroundColor(Color.WHITE);
        update.setBackgroundColor(Color.WHITE);
        //Button update = (Button) view.findViewById(R.id.update);

        call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+filteredphonenumbers.get(position).getNumber()));
                context.startActivity(call);
            }
        });

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+filteredphonenumbers.get(position).getNumber()));
                context.startActivity(msg);
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filteredphonenumbers.get(position).getAtLocal()) {
                    Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                    intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, filteredphonenumbers.get(position).getName())
                            .putExtra(ContactsContract.Intents.Insert.EMAIL, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .putExtra(ContactsContract.Intents.Insert.PHONE, filteredphonenumbers.get(position).getNumber())
                            .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
                    context.startActivity(intent);
                }
                else {
                    Toast.makeText(getContext(), "이미 존재합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView iv_local = view.findViewById(R.id.at_local);
        if (filteredphonenumbers.get(position).getAtLocal()) {
            iv_local.setImageResource(R.drawable.v);
        }
        else {
            iv_local.setImageResource(R.drawable.x);
        }

        ImageView iv_server = view.findViewById(R.id.at_server);
        if (filteredphonenumbers.get(position).getAtServer()) {
            iv_server.setImageResource(R.drawable.v);
        }
        else {
            iv_server.setImageResource(R.drawable.x);
        }

        ImageView iv_contact = (ImageView)view.findViewById(R.id.image_contact);
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);


        if ((cur != null ? cur.getCount() : 0) > 0) {
            Uri u= null;
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (name.equalsIgnoreCase(filteredphonenumbers.get(position).getName()))
                {
                    try {
                        Cursor cur1 = cr.query(
                                ContactsContract.Data.CONTENT_URI,
                                null,
                                ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                                        + ContactsContract.Data.MIMETYPE + "='"
                                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                                null);
                        if (cur1 != null) {
                            if (!cur1.moveToFirst()) {
                                break; // no photo
                            }
                        } else {
                            break; // error in cursor process
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                            .parseLong(id));
                    u = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                    break;
                }
            }

            if (u != null) {
                iv_contact.setImageURI(u);
            }
        }
        return view;
    }

    @Override
    public Phonenumber getItem(int position) {
        return filteredphonenumbers.get(position);
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new PhonenumberAdaptor.ListFilter() ;
        }
        return listFilter ;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = phonenumbers;
                results.count = phonenumbers.size() ;
            }
            else {
                ArrayList<Phonenumber> itemList = new ArrayList<>() ;

                for (Phonenumber item : phonenumbers) {
                    if (item.getName().toUpperCase().contains(constraint.toString().toUpperCase()))
                    {
                        itemList.add(item) ;
                    }
                }
                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            // update listview by filtered data list.
            filteredphonenumbers = (ArrayList<Phonenumber>) results.values ;

            // notify
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
