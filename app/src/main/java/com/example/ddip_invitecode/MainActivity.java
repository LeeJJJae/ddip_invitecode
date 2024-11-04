package com.example.ddip_invitecode;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView inviteCodeTextView;
    private Button generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inviteCodeTextView = findViewById(R.id.inviteCodeTextView);
        generateButton = findViewById(R.id.generateButton);

        // 초대 코드 생성 및 서버에 저장
        generateButton.setOnClickListener(view -> {
            String inviteCode = generateInviteCode();
            inviteCodeTextView.setText(inviteCode);
            saveInviteCodeToServer(inviteCode);
        });
    }

    // 6자리 영어 대문자와 숫자로 이루어진 초대 코드 생성 함수
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder inviteCode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            inviteCode.append(chars.charAt(index));
        }
        return inviteCode.toString();
    }

    // 초대 코드를 서버에 저장하는 함수
    private void saveInviteCodeToServer(String inviteCode) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/invite/create"); // 서버 주소 확인
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String jsonInputString = "\"" + inviteCode + "\""; // 서버가 문자열로 받기 때문에 따옴표 추가

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d("ResponseCode", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "초대 코드가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "초대 코드 저장에 실패했습니다. 응답 코드: " + responseCode, Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("NetworkError", "오류 발생: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
