package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.os.AsyncTask;
import android.widget.TextView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView responseTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View responseTextView = findViewById(R.id.button);

        View requestButton = findViewById(R.id.button2);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GptRequestTask((TextView) responseTextView).execute();
            }
        });
    }
    public class GptRequestTask extends AsyncTask<Void, Void, String> {
        private static final String API_KEY = "YOUR_OPENAI_API_KEY";
        private static final String MODEL_ENDPOINT = "https://api.openai.com/v1/completions";
        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        private TextView responseTextView;

        public GptRequestTask(TextView responseTextView) {
            this.responseTextView = responseTextView;
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("model", "text-davinci-003");
                jsonObject.put("prompt", "Your prompt here");
                jsonObject.put("max_tokens", 50);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(MODEL_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String completion = jsonObject.getJSONArray("choices").getJSONObject(0).getString("text");
                    responseTextView.setText(completion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                responseTextView.setText("Error fetching response");
            }
        }
    }
}