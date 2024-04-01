package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import android.os.AsyncTask;
import android.widget.Toast;

import com.example.myapplication.DatabaseHelper;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPrompt;
    private TextView textViewOutput;
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    private DatabaseHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPrompt = findViewById(R.id.editTextPrompt);
        textViewOutput = findViewById(R.id.textViewOutput);
        dbHelper = new DatabaseHelper(this);
    }

    public void onGenerateClicked(View view) {
        String prompt = editTextPrompt.getText().toString().trim();
        if (!prompt.isEmpty()) {
            savePrompt(prompt);
            new OpenAIAsyncTask().execute(prompt);
        }

    }

    private class OpenAIAsyncTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String prompt = params[0];
            String responseText = "";

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"model\": \"text-davinci-003\", \"prompt\": \"" + prompt + "\", \"max_tokens\": 150}");
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer "+API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    responseText = response.body().string();
                } else {
                    responseText = "Failed to fetch response: " + response.message();
                }
            } catch (IOException e) {
                responseText = "Error occurred: " + e.getMessage();
            }

            return responseText;
        }

        protected void onPostExecute(String result) {
            textViewOutput.setText(result);
            saveResponse(result);
        }
    }

    public void save(View view){
        String prompt = editTextPrompt.getText().toString().trim();
        String response = textViewOutput.getText().toString().trim();
        if (!prompt.isEmpty() && !response.isEmpty()) {
            savePrompt(prompt);
            saveResponse(response);
            Toast.makeText(this, "Saved Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Prompt or Response is empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrompt(String prompt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "INSERT INTO AuditPrompt (DateTime, Prompt) VALUES (datetime('now'), ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, prompt);
        statement.execute();
        db.close();
    }

    private void saveResponse(String response) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "INSERT INTO Responses (DateTime, Response) VALUES (datetime('now'), ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, response);
        statement.execute();
        db.close();
    }


    public void showSavedData(View view) {
        StringBuilder savedData = new StringBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the AuditPrompt table and append results to savedData
        Cursor cursor = db.rawQuery("SELECT * FROM AuditPrompt", null);
        while (cursor.moveToNext()) {
            int sequenceNumber = cursor.getInt(Math.max(cursor.getColumnIndex("SequenceNumber"),0));
            String dateTime = cursor.getString(Math.max(cursor.getColumnIndex("DateTime"),0));
            String prompt = cursor.getString(Math.max(cursor.getColumnIndex("Prompt"),0));
            savedData.append("Sequence Number: ").append(sequenceNumber).append("\n");
            savedData.append("Date and Time: ").append(dateTime).append("\n");
            savedData.append("Prompt: ").append(prompt).append("\n\n");
        }
        cursor.close();

        // Query the Responses table and append results to savedData
        cursor = db.rawQuery("SELECT * FROM Responses", null);
        while (cursor.moveToNext()) {
            int sequenceNumber = cursor.getInt(Math.max(cursor.getColumnIndex("SequenceNumber"),0));
            String dateTime = cursor.getString(Math.max(cursor.getColumnIndex("DateTime"),0));
            String response = cursor.getString(Math.max(cursor.getColumnIndex("Response"),0));
            savedData.append("Sequence Number: ").append(sequenceNumber).append("\n");
            savedData.append("Date and Time: ").append(dateTime).append("\n");
            savedData.append("Response: ").append(response).append("\n\n");
        }
        cursor.close();
        db.close();

        // Display saved data in a dialog prompt
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saved Data");
        builder.setMessage(savedData.toString());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}