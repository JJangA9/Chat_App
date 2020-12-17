package com.example.chat_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    EditText nickName;
    CircleImageView profileImg;

    Uri imgUri;

    boolean isFirst = true;
    boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nickName = findViewById(R.id.nick_Name);
        profileImg = findViewById(R.id.profile_img);

        //phone에 저장되어 있는 프로필 읽어오기
        loadData();
        if(loginData.nickName != null) {
            nickName.setText(loginData.nickName);
            Picasso.get().load(loginData.profileUri).into(profileImg);

            //이미 접속한적이 있다면
            isFirst = false;

        }
    }

    public void clickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    imgUri = data.getData();
                    //Glide.with(this).load(imgUri).into(profileImg);
                    //gilde는 동적 퍼미션이 필요

                    Picasso.get().load(imgUri).into(profileImg);

                    //변경된 이미지가 있다면
                    isChanged = true;
                }
                break;
        }
    }

    public void clickBtn(View view) {

        //바꾼것도 없고, 처음 접속이 아니면
        if(!isChanged && !isFirst) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            //save 작업
            saveData();
        }
    }

    void saveData() {
        loginData.nickName = nickName.getText().toString();
        if (imgUri == null) return;

        //Firebase storage에 저장하기 위해 파일명 만들기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = sdf.format(new Date()) + ".png";

        //Firebase storage에 저장하기
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference imgRef = firebaseStorage.getReference("profileImages/" + fileName);

        //파일 업로드
        UploadTask uploadTask = imgRef.putFile(imgUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //firebase storage의 이미지 파일 다운로드 URL 얻어오기
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //파라미터로 firebase의 저장소에 저장되어 있는
                        //이미지에 대한 다운로드 주소(URL) 문자열로 얻어오기
                        loginData.profileUri = uri.toString();
                        Toast.makeText(MainActivity.this, "프로필 저장 완료", Toast.LENGTH_SHORT).show();

                        //1.Firebase Database에 nickName, profileUri 저장
                        //firebase DB관리자 객체 소환
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        //'profiles'라는 이름의 자식 노드 참조 객체 얻어오기
                        DatabaseReference profileRef = firebaseDatabase.getReference("profiles");

                        //닉네임을 key 식별자로 하고 프로필 이미지의 주소를 값으로 저장
                        profileRef.child(loginData.nickName).setValue(loginData.profileUri);

                        //2. 내 phone에 nickName, profileUri 저장
                        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putString("nickName", loginData.nickName);
                        editor.putString("profileUri", loginData.profileUri);

                        editor.commit();
                        //저장됐으니 chat Activity로 전환
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    //phone에 저장되어 있는 프로필정보 읽어오기
    void loadData() {
        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
        loginData.nickName = preferences.getString("nickName", null);
        loginData.profileUri = preferences.getString("profileUri", null);
    }
}
