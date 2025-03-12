package com.example.examplefirst;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class LoadDataActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapterLoadData adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private int totalMessages = 0;  // 🔹 Variable to store total messages count
    private ImageView contactImage,backButton,btnSend;
    private TextView contactName, contactNumber,contactInitials;
   private EditText messageInput;
   private Toolbar toolbarLoadData;
   boolean isFirstLoad =true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolbarLoadData=findViewById(R.id.toolbarLoadData);
        backButton = findViewById(R.id.backButton);

        contactName = findViewById(R.id.contactName);
        contactNumber = findViewById(R.id.contactNumber);
        contactInitials = findViewById(R.id.contactInitials);

        contactName.setText(getIntent().getStringExtra("contact_name"));
        contactNumber.setText(getIntent().getStringExtra("wa_id"));


        String contactName = getIntent().getStringExtra("contact_name");
        contactInitials.setText(getInitials(contactName));

        // Handle Back Button Click
       setSupportActionBar(toolbarLoadData);
        backButton.setOnClickListener(view -> onBackPressed());

        // Get the waId and pageNo passed from MainActivity (if needed)
        String waId = getIntent().getStringExtra("waId");
        int pageNo = getIntent().getIntExtra("pageNo", 1);


        String url = "https://waba.mpocket.in/api/phone/get/361462453714220/" + waId + "/" + pageNo;


        // Fetch data from the URL
        //loadData(url);
        //loadAllData(waId);
        loadChatPage(waId,isFirstLoad);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0 && !isLoading) {
                    loadChatPage(waId, false); // Load older messages
                }
            }
        });




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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void loadData(String urlString) {
//        new Thread(() -> {
//
//
//            try {
//                URL url = new URL(urlString);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setConnectTimeout(30000);
//                connection.setReadTimeout(30000);
//
//                int responseCode = connection.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        response.append(line);
//                    }
//                    reader.close();
//
//                    // Parse JSON
//                    JSONArray jsonArray = new JSONArray(response.toString());
//                    messageList.clear();
//                    totalMessages = 0;  // 🔹 Reset message count
//
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject obj = jsonArray.getJSONObject(i);
//                        ChatMessage message = new ChatMessage();
//
//
//                        String message_type = obj.optString("message_type", "");
//                      //  Log.d("Sample",message_type );
//                        if(message_type.equals("location"))
//                        {
//                            String latitude = obj.optString("latitude", "");
//                            String longitude = obj.optString("longitude", "");
//
//
//                        }
//
//                        message.setLatitude(obj.optString("latitude", ""));
//                        message.setLongitude(obj.optString("longitude", ""));
//
//
//
//                        message.setMessageBody(obj.optString("message_body", ""));
//                        message.setTimestamp(obj.optString("timestamp", ""));
//                        message.setSender(obj.optString("sender", ""));
//                        message.setWaId(obj.optString("wa_id", ""));
//                        message.setMessageType(obj.optString("message_type", ""));
//                        message.setFileUrl(obj.optString("file_url", null));
//
//                        // Add message to list
//                        messageList.add(message);
//
//
//                        totalMessages++;
//                    }
//
//                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra("waId", getIntent().getStringExtra("waId"));
//                    resultIntent.putExtra("messageCount", totalMessages);
//                    setResult(RESULT_OK, resultIntent);
//
//
//                    // Reverse message order (latest at bottom)
//                    List<ChatMessage> reversedList = new ArrayList<>();
//                    for (int i = messageList.size() - 1; i >= 0; i--) {
//                        reversedList.add(messageList.get(i));
//                    }
//                    messageList = reversedList;
//
//                    // Update UI on main thread
//                    runOnUiThread(() -> {
//                        adapter = new ChatAdapterLoadData(this, messageList);
//                        recyclerView.setAdapter(adapter);
//
//                        // Scroll to last message
//                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//
//                        // Show message count in Toast
//                        Toast.makeText(LoadDataActivity.this, "Total messages: " + totalMessages, Toast.LENGTH_LONG).show();
//                    });
//
//                } else {
//                    runOnUiThread(() ->
//                            Toast.makeText(LoadDataActivity.this, "Failed to fetch data: " + responseCode, Toast.LENGTH_SHORT).show()
//                    );
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(LoadDataActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//            }
//        }).start();
//    }



    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;


    private void loadChatPage(String waId, boolean isFirstLoad) {
        if (isLoading || !hasMoreData) return;
        isLoading = true;

        new Thread(() -> {
            List<ChatMessage> newMessages = new ArrayList<>();
            List<Object> newItems = new ArrayList<>();
            HashMap<String, Boolean> dateAdded = new HashMap<>();


            try {
                String urlString = "https://waba.mpocket.in/api/phone/get/361462453714220/" + waId + "/" + currentPage;
                HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = reader.lines().collect(Collectors.joining());
                    reader.close();

                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() == 0) {
                        hasMoreData = false; // No more data available
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            ChatMessage message = new ChatMessage();
                            message.setLatitude(obj.optString("latitude", ""));
                            message.setLongitude(obj.optString("longitude", ""));
                            message.setMessageBody(obj.optString("message_body", ""));
                            message.setTimestamp(obj.optString("timestamp", "")); // Make sure timestamp is being set correctly
                            message.setSender(obj.optString("sender", ""));
                            message.setWaId(obj.optString("wa_id", ""));
                            message.setMessageType(obj.optString("message_type", ""));
                            message.setFileUrl(obj.optString("file_url", null));

                            newMessages.add(message);
                        }
                    }
                } else {
                    hasMoreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                hasMoreData = false;
            }

            isLoading = false;

            runOnUiThread(() -> {
                if (newMessages.isEmpty()) return;

                // **Step 2: Sort messages by timestamp**
                Collections.sort(newMessages, Comparator.comparing(ChatMessage::getTimestamp));

                for (ChatMessage msg : newMessages) {
                    String dateHeader = msg.getFormattedTimestamp();
                    if (!dateAdded.containsKey(dateHeader)) {
                        newItems.add(dateHeader); // Add date header
                        dateAdded.put(dateHeader, true);
                    }
                    newItems.add(msg); // Add message
                }

                if (isFirstLoad) {
                    messageList = new ArrayList<>(newMessages);
                    adapter = new ChatAdapterLoadData(this, messageList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1); // Scroll to bottom on first load
                } else {
                    int currentFirstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    int previousMessageCount = messageList.size();

                    // **Step 3: Add sorted messages to the top**
                    messageList.addAll(0, newMessages);
                    adapter.notifyItemRangeInserted(0, newMessages.size());

                    // Maintain scroll position
                    int newMessageCount = messageList.size();
                    int scrollOffset = newMessageCount - previousMessageCount;
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(currentFirstVisiblePosition + scrollOffset, 0);
                }

                currentPage++;
            });
        }).start();
    }





//    private void loadAllData(String waId) {
//        new Thread(() -> {
//            int pageNo = 1;
//            boolean hasMoreData = true;
//            List<ChatMessage> allMessages = new ArrayList<>();
//
//            while (hasMoreData) {
//                try {
//                    String urlString = "https://waba.mpocket.in/api/phone/get/361462453714220/" + waId + "/" + pageNo;
//                    URL url = new URL(urlString);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("GET");
//                    connection.setConnectTimeout(15000);
//                    connection.setReadTimeout(15000);
//
//                    int responseCode = connection.getResponseCode();
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                        StringBuilder response = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            response.append(line);
//                        }
//                        reader.close();
//
//                        // Parse JSON
//                        JSONArray jsonArray = new JSONArray(response.toString());
//
//                        if (jsonArray.length() == 0) {
//                            hasMoreData = false; // No more messages, stop fetching
//                            break;
//                        }
//
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject obj = jsonArray.getJSONObject(i);
//                            ChatMessage message = new ChatMessage();
//
//                            message.setLatitude(obj.optString("latitude", ""));
//                            message.setLongitude(obj.optString("longitude", ""));
//                            message.setMessageBody(obj.optString("message_body", ""));
//                            message.setTimestamp(obj.optString("timestamp", ""));
//                            message.setSender(obj.optString("sender", ""));
//                            message.setWaId(obj.optString("wa_id", ""));
//                            message.setMessageType(obj.optString("message_type", ""));
//                            message.setFileUrl(obj.optString("file_url", null));
//
//                            allMessages.add(message);
//                        }
//
//                        pageNo++; // Move to the next page
//                    } else {
//                        hasMoreData = false; // Stop fetching on error
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    hasMoreData = false; // Stop on exception
//                }
//            }
//
//            // Reverse message order (latest at bottom)
//            List<ChatMessage> reversedList = new ArrayList<>();
//            for (int i = allMessages.size() - 1; i >= 0; i--) {
//                reversedList.add(allMessages.get(i));
//            }
//            messageList = reversedList;
//
//            // Update UI on main thread
//            runOnUiThread(() -> {
//                adapter = new ChatAdapterLoadData(this, messageList);
//                recyclerView.setAdapter(adapter);
//                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                Toast.makeText(LoadDataActivity.this, "Total messages: " + messageList.size(), Toast.LENGTH_LONG).show();
//            });
//
//        }).start();
//    }



}


