//package com.example.photoblog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.os.AsyncTask;
//import android.view.View;
//
//import java.util.List;
//import java.util.ArrayList;
//import java.io.InputStream;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//
//
//public class MainActivity extends AppCompatActivity {
//
//    ImageView imgView;
//    TextView textView;
//    String site_url = "http://10.0.2.2:8000";
//    // token을 멤버 변수로 이동하여 다른 내부 클래스에서도 사용할 수 있도록 합니다.
//    String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
//    JSONObject post_json;
//    String imagUrl = null;
//    Bitmap bmImg = null;
//
//    CloadImage taskDonwload;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        textView = (TextView) findViewById(R.id.textView);
//    }
//
//    public void onClickDownload(View v) {
//        if(taskDonwload != null && taskDonwload.getStatus() == AsyncTask.Status.RUNNING){
//            taskDonwload.cancel(true);
//        }
//        taskDonwload= new CloadImage();
//        taskDonwload.execute(site_url+"/api_root/Post/");
//        Toast.makeText(getApplicationContext(),"Download",Toast.LENGTH_LONG).show();
//    }
//
//    public void onClickUpload(View v) {
//        String title = "새로운 포스트";
//        String text = "포스트 내용입니다.";
//        Bitmap imageToUpload = BitmapFactory.decodeResource(getResources(), R.drawable.sample_upload);
//
//        // AsyncTask 실행 시 URL을 파라미터로 전달합니다.
//        new PutPost(title, text, imageToUpload).execute(site_url + "/api_root/Post/");
//    }
//
//    private class CloadImage extends AsyncTask<String, Integer, List<Bitmap>> {
//        @Override
//        protected List<Bitmap> doInBackground(String... urls) {
//            List<Bitmap> bitmapList = new ArrayList<>();
//            HttpURLConnection conn = null;
//            try {
//                String apiUrl = urls[0];
//                URL urlAPI = new URL(apiUrl);
//                conn = (HttpURLConnection) urlAPI.openConnection();
//                conn.setRequestProperty("Authorization", "Token " + token); // 멤버 변수 token 사용
//                conn.setRequestMethod("GET");
//                conn.setConnectTimeout(3000);
//                conn.setReadTimeout(3000);
//
//                int responseCode = conn.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    InputStream is = conn.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                    StringBuilder result = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        result.append(line);
//                    }
//                    reader.close(); // reader를 닫아줍니다.
//
//                    String strJson = result.toString();
//                    JSONArray aryJson = new JSONArray(strJson);
//
//                    for (int i = 0; i < aryJson.length(); i++) {
//                        post_json = (JSONObject) aryJson.get(i);
//                        String imageUrl = post_json.getString("image");
//
//                        // "null" 문자열이거나 비어있는지 확인합니다.
//                        if (imageUrl != null && !imageUrl.equals("null") && !imageUrl.isEmpty()) {
//                            URL myImageUrl = new URL(imageUrl);
//                            // 이미지 다운로드를 위한 새 연결을 만듭니다.
//                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();
//                            InputStream imgStream = imgConn.getInputStream();
//                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
//                            bitmapList.add(imageBitmap);
//                            imgStream.close();
//                            imgConn.disconnect(); // 이미지 연결은 여기서 닫습니다.
//                        }
//                    }
//                }
//            } catch (IOException | JSONException e) {
//                e.printStackTrace();
//            } finally {
//                if(conn != null){
//                    conn.disconnect(); // 메인 연결은 여기서 닫습니다.
//                }
//            }
//            return bitmapList;
//        }
//
//        @Override
//        protected void onPostExecute(List<Bitmap> images){
//            if(images.isEmpty()){
//                textView.setText("불러올 이미지가 없습니다.");
//            }else{
//                textView.setText("이미지 로드 성공!");
//                RecyclerView recyclerView = findViewById(R.id.recyclerView);
//                ImageAdapter adapter = new ImageAdapter(images);
//
//                recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(MainActivity.this));
//                recyclerView.setAdapter(adapter);
//            }
//        }
//    }
//
//    private class PutPost extends AsyncTask<String, Void, Boolean> {
//        String title;
//        String text;
//        Bitmap bitmap; // 타입을 Bitmap으로 변경합니다.
//        String boundary;
//        String CRLF = "\r\n";
//
//        public PutPost(String title, String text, Bitmap bitmap) {
//            this.title = title;
//            this.text = text;
//            this.bitmap = bitmap;
//            // boundary 값을 생성자에서 초기화합니다.
//            this.boundary = java.util.UUID.randomUUID().toString();
//        }
//
//        @Override
//        // doInBackground의 파라미터를 AsyncTask<String, ...>에 맞게 String...으로 변경합니다.
//        protected Boolean doInBackground(String... urls) {
//            HttpURLConnection conn = null;
//
//            try {
//                // execute()로 전달받은 URL을 사용
//                URL url = new URL(urls[0]);
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Authorization", "Token " + token); // 멤버 변수 token 사용
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
//
//                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
//                // --- Author ---
//                request.writeBytes("--" + boundary + CRLF);
//                request.writeBytes("Content-Disposition: form-data; name=\"author\"" + CRLF);
//                request.writeBytes("Content-Type: text/plain; charset=UTF-8" + CRLF);
//                request.writeBytes(CRLF);
//                request.write("1".getBytes("UTF-8"));
//                request.writeBytes(CRLF);
//
//                // --- Title ---
//                request.writeBytes("--" + this.boundary + CRLF);
//                request.writeBytes("Content-Disposition: form-data; name=\"title\"" + CRLF);
//                request.writeBytes("Content-Type: text/plain; charset=UTF-8"+CRLF);
//                request.writeBytes(CRLF);
//                request.write(this.title.getBytes("UTF-8"));
//                request.writeBytes(CRLF);
//
//                // --- Text ---
//                request.writeBytes("--" + this.boundary + CRLF);
//                request.writeBytes("Content-Disposition: form-data; name=\"text\"" + CRLF);
//                request.writeBytes("Content-Type: text/plain; charset=UTF-8" + CRLF);
//                request.writeBytes(CRLF);
//                request.write(this.text.getBytes("UTF-8"));
//                request.writeBytes(CRLF);
//
//                // --- Image ---
//                if (this.bitmap != null) {
//                    request.writeBytes("--" + this.boundary + CRLF);
//                    request.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"" + CRLF);
//                    request.writeBytes("Content-Type: image/jpeg" + CRLF);
//                    request.writeBytes(CRLF);
//
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    this.bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
//                    byte[] imageData = stream.toByteArray();
//                    request.write(imageData);
//                    request.writeBytes(CRLF);
//                }
//
//                request.writeBytes("--" + this.boundary + "--" + CRLF);
//                request.flush();
//                request.close();
//
//                int responseCode = conn.getResponseCode();
//                return (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            } finally {
//                if (conn != null) {
//                    conn.disconnect(); // 연결을 종료합니다.
//                }
//            }
//        }
//
//        // onPostExecute를 추가하여 업로드 결과를 사용자에게 알려줍니다.
//        @Override
//        protected void onPostExecute(Boolean success) {
//            if (success) {
//                Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
//                onClickDownload(null);
//            } else {
//                Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}
