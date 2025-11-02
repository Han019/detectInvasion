package com.example.photoblog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadActivity extends AppCompatActivity {

    // (서버 정보)
    String site_url = "http://10.0.2.2:8000";
    String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";

    // (UI 컴포넌트 변수)
    ImageView imgPreview;
    EditText etTitle;
    EditText etText;
    Button btnSelectImage;
    Button btnUploadFinal;

    // (선택된 이미지를 담을 변수)
    Bitmap selectedBitmap = null;

    // (1. 추가됨!) 갤러리 결과 처리를 위한 Launcher
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // (UI 컴포넌트 연결)
        imgPreview = findViewById(R.id.img_preview);
        etTitle = findViewById(R.id.et_title);
        etText = findViewById(R.id.et_text);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUploadFinal = findViewById(R.id.btn_upload_final);

        // (2. 추가됨!) 갤러리 Launcher 초기화
        // registerForActivityResult는 onCreate 안에 있어야 합니다.
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    // 사용자가 이미지를 선택했을 때 (uri != null)
                    if (uri != null) {
                        try {
                            // 갤러리에서 선택한 이미지(Uri)를 Bitmap으로 변환합니다.
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            selectedBitmap = BitmapFactory.decodeStream(inputStream);

                            // 1. ImageView에 미리보기로 보여줍니다.
                            imgPreview.setImageBitmap(selectedBitmap);

                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 사용자가 갤러리에서 아무것도 선택하지 않고 뒤로가기 했을 때
                        Toast.makeText(this, "이미지 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // (3. 수정됨!) "이미지 선택" 버튼 클릭 리스너
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 갤러리를 엽니다. "image/*"는 모든 이미지 파일을 의미합니다.
                imagePickerLauncher.launch("image/*");
            }
        });

        // (4. "게시하기" 버튼 클릭 리스너 - 4단계와 동일)
        btnUploadFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString();
                String text = etText.getText().toString();

                if (title.isEmpty() || text.isEmpty()) {
                    Toast.makeText(UploadActivity.this, "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // (중요) selectedBitmap이 null인지 (이미지가 선택됐는지) 확인합니다.
                if (selectedBitmap == null) {
                    Toast.makeText(UploadActivity.this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // PutPost 실행
                new PutPost(title, text, selectedBitmap).execute(site_url + "/api_root/Post/");
            }
        });
    }

    // (PutPost 클래스는 4단계와 완전히 동일합니다)
    private class PutPost extends AsyncTask<String, Void, Boolean> {
        String title;
        String text;
        Bitmap bitmap;
        String boundary;
        String CRLF = "\r\n";

        public PutPost(String title, String text, Bitmap bitmap) {
            this.title = title;
            this.text = text;
            this.bitmap = bitmap;
            this.boundary = java.util.UUID.randomUUID().toString();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);

                DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                // --- Author (필수) ---
                request.writeBytes("--" + boundary + CRLF);
                request.writeBytes("Content-Disposition: form-data; name=\"author\"" + CRLF);
                request.writeBytes("Content-Type: text/plain; charset=UTF-8" + CRLF);
                request.writeBytes(CRLF);
                request.write("1".getBytes("UTF-8"));
                request.writeBytes(CRLF);

                // --- Title (필수, 사용자 입력값) ---
                request.writeBytes("--" + this.boundary + CRLF);
                request.writeBytes("Content-Disposition: form-data; name=\"title\"" + CRLF);
                request.writeBytes("Content-Type: text/plain; charset=UTF-8" + CRLF);
                request.writeBytes(CRLF);
                request.write(this.title.getBytes("UTF-8"));
                request.writeBytes(CRLF);

                // --- Text (필수, 사용자 입력값) ---
                request.writeBytes("--" + this.boundary + CRLF);
                request.writeBytes("Content-Disposition: form-data; name=\"text\"" + CRLF);
                request.writeBytes("Content-Type: text/plain; charset=UTF-8" + CRLF);
                request.writeBytes(CRLF);
                request.write(this.text.getBytes("UTF-8"));
                request.writeBytes(CRLF);

                // --- Image (선택, 사용자 선택값) ---
                if (this.bitmap != null) {
                    request.writeBytes("--" + this.boundary + CRLF);
                    request.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"" + CRLF);
                    request.writeBytes("Content-Type: image/jpeg" + CRLF);
                    request.writeBytes(CRLF);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    this.bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    byte[] imageData = stream.toByteArray();
                    request.write(imageData);
                    request.writeBytes(CRLF);
                }

                request.writeBytes("--" + this.boundary + "--" + CRLF);
                request.flush();
                request.close();

                int responseCode = conn.getResponseCode();
                return (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(UploadActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                // 업로드 성공 시, 현재 Activity를 닫고 MainActivity로
                finish();
            } else {
                Toast.makeText(UploadActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}