package com.example.examplefirst;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapterLoadData extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_INCOMING = 1;
    private static final int VIEW_TYPE_OUTGOING = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 2;


    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapterLoadData(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }


    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);

        // Debugging Logs


        // Properly handle null or empty waId
        if (message.getWaId() == null || message.getWaId().trim().isEmpty() || "null".equals(message.getWaId())) {

            return VIEW_TYPE_INCOMING; // Incoming message (left)
        } else {

            return VIEW_TYPE_OUTGOING; // Outgoing message (right)
        }
    }
    public void addMessages(List<ChatMessage> newMessages) {
        int startPosition = messages.size();
        messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_INCOMING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingMessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof IncomingMessageHolder) {
            ((IncomingMessageHolder) holder).bind(message);
        } else if (holder instanceof OutgoingMessageHolder) {
            ((OutgoingMessageHolder) holder).bind(message);
        }
    }




    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for Incoming Messages (Left Side)
    class IncomingMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText;
        ImageView messageImage;

        IncomingMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body_incoming);
            timestampText = itemView.findViewById(R.id.text_message_time_incoming);
            messageImage = itemView.findViewById(R.id.image_message);

        }

        void bind(ChatMessage message) {

            String fileUrl = message.getFileUrl();
            String fileType = message.getMessageType().toLowerCase();
            String messageBody = message.getMessageBody();

            String latitude = message.getLatitude();
            String longitude = message.getLongitude();
            Log.d("Sample",latitude );
            Log.d("Sample",longitude );

          //  if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                // If it's an image, display it
            if (latitude != null && longitude != null && !latitude.trim().isEmpty() && !longitude.trim().isEmpty()&& latitude.length()>4 && longitude.length()>4) {
                try {
                    double lat = Double.parseDouble(latitude);
                    double lon = Double.parseDouble(longitude);

                    // Handle location message
                    messageText.setVisibility(View.VISIBLE);
                    messageImage.setVisibility(View.GONE);
                    String locationUrl = "https://maps.google.com/?q=" + lat + "," + lon;
                    //String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
                    messageText.setText(locationUrl);
                    messageText.setTextColor(Color.BLUE);
                    messageText.setPaintFlags(messageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    messageText.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl));
                        intent.setPackage("com.google.android.apps.maps"); // Open in Google Maps
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // If Google Maps is not installed, open in browser
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl));
                            context.startActivity(browserIntent);
                        }
                    });

                } catch (NumberFormatException e) {
                    Log.e("ChatAdapter", "Invalid latitude/longitude format: " + latitude + ", " + longitude);
                }
            } else if ("image".equals(fileType)) {
                    messageText.setVisibility(View.GONE);
                    messageImage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(fileUrl).into(messageImage);

                    // Open image on click
                    messageImage.setOnClickListener(v -> {
                        openFile(fileUrl, "image/*");
                    });

                } else if(!fileUrl.isEmpty() && fileUrl.length() > 4) {
                    // Show "Open File" button for other file types
                    messageText.setVisibility(View.VISIBLE);
                    messageImage.setVisibility(View.GONE);
                    messageText.setText(fileUrl);
                    messageText.setTextColor(Color.BLUE);  // Make it look like a link
                    messageText.setPaintFlags(messageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    // Open file when clicked
                    messageText.setOnClickListener(v -> {
                        openFile(fileUrl, getMimeType(fileUrl));
                    });
                }
            else if (messageBody != null && !messageBody.trim().isEmpty()) {
                    // If there's no file but a text message, show the message
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(messageBody);
                    messageText.setTextColor(Color.BLACK);
                    messageText.setPaintFlags(0);
                    messageImage.setVisibility(View.GONE);
                } else {
                // If both fileUrl and messageBody are empty, hide everything
                messageText.setVisibility(View.GONE);
                messageImage.setVisibility(View.GONE);
            }

            timestampText.setText(message.getFormattedTimestamp());
        }

        private void openFile(String fileUrl, String mimeType) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fileUrl), mimeType);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        private String getMimeType(String url) {
            String mimeType = "*/*";  // Default type
            if (url.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                mimeType = "image/*";
            } else if (url.endsWith(".mp4")) {
                mimeType = "video/mp4";
            } else if (url.endsWith(".mp3")) {
                mimeType = "audio/mpeg";
            } else if (url.endsWith(".doc") || url.endsWith(".docx")) {
                mimeType = "application/msword";
            } else if (url.endsWith(".xls") || url.endsWith(".xlsx")) {
                mimeType = "application/vnd.ms-excel";
            }
            return mimeType;
        }

}



        // ViewHolder for Outgoing Messages (Right Side)
        class OutgoingMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timestampText;
            ImageView image_message_outgoing;


            OutgoingMessageHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.text_message_body_outgoing);
                timestampText = itemView.findViewById(R.id.text_message_time_outgoing);
                image_message_outgoing = itemView.findViewById(R.id.image_message_outgoing);

            }

            void bind(ChatMessage message) {
                String fileUrl = message.getFileUrl();
                String fileType = message.getMessageType().toLowerCase();
                String messageBody = message.getMessageBody();

                String latitude = message.getLatitude();
                String longitude = message.getLongitude();
                Log.d("Sample", latitude);
                Log.d("Sample", longitude);

                //  if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                // If it's an image, display it
                if (latitude != null && longitude != null && !latitude.trim().isEmpty() && !longitude.trim().isEmpty() && latitude.length() > 4 && longitude.length() > 4) {
                    try {
                        double lat = Double.parseDouble(latitude);
                        double lon = Double.parseDouble(longitude);

                        // Handle location message
                        messageText.setVisibility(View.VISIBLE);
                        image_message_outgoing.setVisibility(View.GONE);
                        String locationUrl = "https://maps.google.com/?q=" + lat + "," + lon;
                        messageText.setText(locationUrl);
                        messageText.setTextColor(Color.BLUE);
                        messageText.setPaintFlags(messageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                        messageText.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl));
                            intent.setPackage("com.google.android.apps.maps"); // Open in Google Maps
                            try {
                                context.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                // If Google Maps is not installed, open in browser
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl));
                                context.startActivity(browserIntent);
                            }
                        });

                    } catch (NumberFormatException e) {
                        Log.e("ChatAdapter", "Invalid latitude/longitude format: " + latitude + ", " + longitude);
                    }
                } else if ("image".equals(fileType)) {
                    messageText.setVisibility(View.GONE);
                    image_message_outgoing.setVisibility(View.VISIBLE);
                    Glide.with(context).load(fileUrl).into(image_message_outgoing);

                    // Open image on click
                    image_message_outgoing.setOnClickListener(v -> {
                        openFile(fileUrl, "image/*");
                    });

                } else if (!fileUrl.isEmpty() && fileUrl.length() > 4) {
                    // Show "Open File" button for other file types
                    messageText.setVisibility(View.VISIBLE);
                    image_message_outgoing.setVisibility(View.GONE);
                    messageText.setText(fileUrl);
                    messageText.setTextColor(Color.BLUE);  // Make it look like a link
                    messageText.setPaintFlags(messageText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    // Open file when clicked
                    messageText.setOnClickListener(v -> {
                        openFile(fileUrl, getMimeType(fileUrl));
                    });
                } else if (messageBody != null && !messageBody.trim().isEmpty()) {
                    // If there's no file but a text message, show the message
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(messageBody);
                    messageText.setTextColor(Color.BLACK);
                    messageText.setPaintFlags(0);
                    image_message_outgoing.setVisibility(View.GONE);
                } else {
                    // If both fileUrl and messageBody are empty, hide everything
                    messageText.setVisibility(View.GONE);
                    image_message_outgoing.setVisibility(View.GONE);
                }

                timestampText.setText(message.getFormattedTimestamp());
            }

            private void openFile(String fileUrl, String mimeType) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(fileUrl), mimeType);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            private String getMimeType(String url) {
                String mimeType = "*/*";  // Default type
                if (url.endsWith(".pdf")) {
                    mimeType = "application/pdf";
                } else if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                    mimeType = "image/*";
                } else if (url.endsWith(".mp4")) {
                    mimeType = "video/mp4";
                } else if (url.endsWith(".mp3")) {
                    mimeType = "audio/mpeg";
                } else if (url.endsWith(".doc") || url.endsWith(".docx")) {
                    mimeType = "application/msword";
                } else if (url.endsWith(".xls") || url.endsWith(".xlsx")) {
                    mimeType = "application/vnd.ms-excel";
                }
                return mimeType;
            }
        }



}
