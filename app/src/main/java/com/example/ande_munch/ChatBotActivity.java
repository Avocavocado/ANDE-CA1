package com.example.ande_munch;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatBotActivity extends AppCompatActivity  {

    EditText msg;
    ImageButton sendMsg;
    ImageButton closeChat;
    ScrollView chatScroll;
    LinearLayout chat;
    List<String> conversationHistory = new ArrayList<>();
    String location;
    private final String TAG = "CHATBOT";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        msg = (EditText) findViewById(R.id.message);
        sendMsg = (ImageButton) findViewById(R.id.sendMessage);
        chat = (LinearLayout) findViewById(R.id.chat);
        chatScroll = (ScrollView) findViewById(R.id.chatScroll);
        closeChat = (ImageButton) findViewById(R.id.closeChat);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Intent intent = getIntent();

        location = intent.getStringExtra("Location");
        Log.i(TAG, " location: " + location);
        conversationHistory.add("For context, my location is " + location + ". Keep your sentences to maximum 100 words.");
        addToChat("Hi, I'm FoodBot! How can I help you today?", false);
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String req = msg.getText().toString();
                if (req.equals("")) {
                    return;
                }
                addToChat(req, true);
                msg.setText("");
                try {
                    JSONObject resObject = new JSONObject(chatGPT(req));
                    JSONArray choicesArray = resObject.getJSONArray("choices");
                    JSONObject firstChoice = choicesArray.getJSONObject(0);
                    JSONObject messageObject = firstChoice.getJSONObject("message");
                    String res = messageObject.getString("content");
                    Log.i(TAG,"OBJECT " + resObject.toString());
                    Log.i(TAG,"CONTENT " + res);
                    addToChat(res, false);
                    conversationHistory.add(req);
                    conversationHistory.add(res);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        closeChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public void addToChat(String msg, Boolean isYou) {
        CardView chatbox = new CardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Set background color, gravity, and text alignment based on the sender
        if (isYou) {
            chatbox.setCardBackgroundColor(Color.parseColor("#AAAAFF"));
            layoutParams.gravity = Gravity.END;
        } else {
            chatbox.setCardBackgroundColor(Color.parseColor("#AAFFAA"));
            layoutParams.gravity = Gravity.START;
        }

        layoutParams.setMargins(0, 8, 0, 8);
        chatbox.setLayoutParams(layoutParams);
        chatbox.setRadius(10);

        // Create a TextView for the message
        TextView messageTextView = new TextView(this);
        messageTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        messageTextView.setText(msg);
        messageTextView.setTextColor(Color.parseColor("#000000"));
        messageTextView.setPadding(20, 10, 16, 20);

        chatbox.addView(messageTextView);
        chat.addView(chatbox);

        chat.post(new Runnable() {
            @Override
            public void run() {
                chatScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }


    public String chatGPT(String promptText) throws Exception {
        try {
            // Replace "YOUR_API_KEY" with your actual OpenAI API key
            String apiKey = "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";
            String model = "gpt-3.5-turbo";
            int maxTokens = 200;

            String apiUrl = "https://api.openai.com/v1/chat/completions";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            // Enable input/output streams
            connection.setDoOutput(true);

            // Construct the conversation array
            JSONArray conversationArray = new JSONArray();

            // Add user and assistant messages from the conversation history
            for (String message : conversationHistory) {
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", message);
                conversationArray.put(userMessage);
            }

            // Add the current user message to the conversation
            JSONObject currentUserMessage = new JSONObject();
            currentUserMessage.put("role", "user");
            currentUserMessage.put("content", promptText);
            conversationArray.put(currentUserMessage);

            // Create the request body
            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("model", model);
            requestBodyJson.put("messages", conversationArray);
            requestBodyJson.put("max_tokens", maxTokens);

            String requestBody = requestBodyJson.toString();

            // Write the request body to the connection
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                connection.disconnect();
                return response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }



}
