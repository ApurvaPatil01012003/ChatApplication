package com.example.examplefirst;



import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContactAdapter extends BaseAdapter {
    private Context context;
    private List<Contact> contactList;
    //private int[] colors = {0xFFE57373, 0xFF81C784, 0xFF64B5F6, 0xFFFFD54F, 0xFFBA68C8};

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        }

        TextView circleTextView = convertView.findViewById(R.id.circleTextView);
        TextView contactName = convertView.findViewById(R.id.contactName);
        TextView lastMessage = convertView.findViewById(R.id.lastMessage);
        TextView messageCount = convertView.findViewById(R.id.messageCount);

        Contact contact = contactList.get(position);
        int count = contact.getMessageCount();
        if (count > 0) {
            messageCount.setVisibility(View.VISIBLE);
            messageCount.setText(String.valueOf(count));
        } else {
            messageCount.setVisibility(View.GONE);
        }



        // Set contact name and last message
        contactName.setText(contact.getName());
        lastMessage.setText("Last Message: " + contact.getLastMessageTime());
        messageCount.setText("Message count : "+contact.getMessageCount());
        // Generate initials
        String initials = getInitials(contact.getName());
        circleTextView.setText(initials);


        if (contact.isActive()) {
            circleTextView.setBackgroundResource(R.drawable.green_circle);
        } else {
            circleTextView.setBackgroundResource(R.drawable.gray_circle);
        }

        return convertView;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return parts[0].substring(0, 2).toUpperCase();
        }
    }
    private boolean isUserActive(long lastActiveTime) {
        long currentTime = System.currentTimeMillis();
        boolean isActive = (currentTime - lastActiveTime) <= (24 * 60 * 60 * 1000);

        Log.d("ActiveCheck", "LastActiveTime: " + lastActiveTime + ", CurrentTime: " + currentTime + ", Active: " + isActive);
        return isActive;
    }
    public void updateData(ArrayList<Contact> newContacts) {
        contactList.clear();
        contactList.addAll(newContacts);
        notifyDataSetChanged();
    }




}