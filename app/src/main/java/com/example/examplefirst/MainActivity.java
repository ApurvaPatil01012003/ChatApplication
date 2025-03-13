package com.example.examplefirst;

import io.socket.client.IO;
import io.socket.client.Socket;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private ArrayList<String> waIdList;

    private TextView textView;
    // private ArrayList<String> contactList;
    private HashMap<String, Integer> messageCountMap = new HashMap<>();
    private String lastFetchedJson = "";
    HashMap<String, String> lastMessageTimes = new HashMap<>();


    private static final String API_URL = "https://waba.mpocket.in/api/phone/get/chats/361462453714220?accessToken=Vpv6mesdUaY3XHS6BKrM0XOdIoQu4ygTVaHmpKMNb29bc1c7";


    private Socket mSocket;
    // private TextView logTextView;  // TextView for displaying logs
    private ImageView imageView;
    private ArrayList<Contact> contactList;
    private ContactAdapter contactAdapter;
    Toolbar ToolbarContactList;
    private String selectedUserName = "";  // Store last selected user
    private SearchView searchContact;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // logTextView = findViewById(R.id.logTextView);  // Initialize the TextView

        //textView = findViewById(R.id.textView);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        searchContact=findViewById(R.id.searchContact);
        searchContact.clearFocus();
        searchContact.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ToolbarContactList=findViewById(R.id.ToolbarContactList);
        setSupportActionBar(ToolbarContactList);

        listView = findViewById(R.id.listView);
        waIdList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, waIdList);
        listView.setAdapter(adapter);


        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, contactList);
        listView.setAdapter(contactAdapter);

        fetchData();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected contact from contactList instead of waIdList
            Contact selectedContact = contactList.get(position);

            // Extract details
            String selectedWaId = selectedContact.getWaId();
            String selectedContactName = selectedContact.getName();
            boolean isActive = selectedContact.isActive();
            Log.d("CONTACT_CLICK", "Clicked: " + selectedContactName + " (" + selectedWaId + ")");


            Intent intent = new Intent(MainActivity.this, LoadDataActivity.class);
            intent.putExtra("contact_name", selectedContactName); // Pass contact name
            intent.putExtra("waId", selectedWaId); // Pass waId
            intent.putExtra("isActive",isActive ); // Pass waId
            intent.putExtra("pageNo", 1);

            startActivityForResult(intent, 1);
        });


        try {
            IO.Options options = new IO.Options();
            options.query = "phone_number_id=" + URLEncoder.encode("361462453714220", "UTF-8");
            mSocket = IO.socket("https://waba.mpocket.in", options);
            // setupSocketEvents();
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
            // appendLog("Error connecting to server: " + e.getMessage());
        }




    }
    private void fetchData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    lastFetchedJson = response.body().string(); // Store JSON

                    runOnUiThread(() -> {
                        parseJSON(lastFetchedJson); // Refresh the data

                        // Apply filter only if a user is selected
                        if (!selectedUserName.isEmpty()) {
                            filterContactsByUser(selectedUserName);
                        }
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void parseJSON(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            waIdList.clear(); // Clear list before populating again
            messageCountMap.clear(); // Clear message count to avoid old counts
            contactList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String waId = jsonObject.optString("wa_id_or_sender", "N/A");
                String contactName = jsonObject.optString("contact_name", "Unknown");
                String lastMessageTime = jsonObject.optString("last_message_date", "").trim();
                int messageCount = jsonObject.optInt("message_count", 0); // Extract message count from API
                long lastActiveTime = jsonObject.optLong("last_active_time", 0);
                boolean isActive = jsonObject.optBoolean("active_last_24_hours", false);
                String userName = jsonObject.optString("user_name", "Unknown");
                // Log.d("FULL_JSON", "Response: " + jsonData)


                if (lastMessageTime.isEmpty() || lastMessageTime.equalsIgnoreCase("null")) {
                    lastMessageTime = "No messages";
                } else {
                    try {
                        lastMessageTime = formatTimestamp(lastMessageTime); // Convert timestamp
                    } catch (Exception e) {
                        Log.e("TimestampError", "Error parsing timestamp: " + lastMessageTime, e);
                        lastMessageTime = "Invalid Time";
                    }

                }


                Contact contact = new Contact(contactName, waId, lastMessageTime, messageCount,isActive,userName);
                contactList.add(contact);
                Log.d("PARSE_JSON", "Added: " + contactName + " (" + waId + ")");

                if (waId != null && !waId.equals("null")) {
                    messageCountMap.put(waId, messageCount);
                    // Store message count

                    // Updating display text to include last message time
                    String displayText = contactName + " (" + waId + ") - " + messageCount + " messages\nLast Message: " + lastMessageTime;

                    waIdList.add(displayText);
                }
            }
            contactAdapter.notifyDataSetChanged();
            //adapter.notifyDataSetChanged(); // Notify adapter about updated data
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }


    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            fetchData(); // Fetch latest data
            handler.postDelayed(this, 5000); // Fetch every 5 seconds
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(fetchRunnable); // Start periodic fetching
        updateUserActiveStatus();

    }
    private void updateUserActiveStatus() {
        long currentTime = System.currentTimeMillis(); // Get current time in milliseconds

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastActiveTime", currentTime);
        editor.apply();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(fetchRunnable); // Stop fetching when activity is not visible
    }



    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("TimestampError", "Error parsing timestamp: " + timestamp, e);
            return "Invalid Time"; // Handle parsing errors
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String waId = data.getStringExtra("waId");
            int messageCount = data.getIntExtra("messageCount", 0);

            messageCountMap.put(waId, messageCount);  // Update message count
            fetchData(); // Refresh data instead of using old JSON
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        fetchUserNames(menu);
        return true;
    }

    private void fetchUserNames(Menu menu) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    Set<String> uniqueUsers = new HashSet<>();

                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String userName = jsonObject.optString("user_name", "Unknown");

                            if (userName != null && !userName.equalsIgnoreCase("null")) {
                                uniqueUsers.add(userName);
                            }
                        }

                        runOnUiThread(() -> {
                            menu.clear(); // Clear previous menu items
                            getMenuInflater().inflate(R.menu.menu_main, menu); // Reinflate default menu

                            int itemId = 1000; // Unique ID for each menu item
                            for (String user : uniqueUsers) {
                                menu.add(Menu.NONE, itemId++, Menu.NONE, user);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "JSON Parsing Error", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
    private void filterContactsByUser(String userName) {
        ArrayList<Contact> filteredList = new ArrayList<>();

        for (Contact contact : contactList) {
            if (contact.getUserName().equalsIgnoreCase(userName)) {
                filteredList.add(contact);
            }
        }

        Log.d("FILTER", "Filtered List Size: " + filteredList.size());  // Debugging log

        contactAdapter.updateData(filteredList); // Update ListView with filtered contacts
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            selectedUserName = "";
            fetchData();  // Refresh data
            return true;
        } else {
            selectedUserName = item.getTitle().toString();
            filterContactsByUser(selectedUserName);
            return true;
        }
    }



}