package com.example.primjer_prijave.AddProduct;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.primjer_prijave.NotificationMessagingService.APIService;
import com.example.primjer_prijave.NotificationMessagingService.Client;
import com.example.primjer_prijave.NotificationMessagingService.Data;
import com.example.primjer_prijave.NotificationMessagingService.MyResponse;
import com.example.primjer_prijave.NotificationMessagingService.NotificationSender;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.example.primjer_prijave.RegistrationLogin.UserProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btn_upload,btn_back, btn_delete;

    private TextView tv_instruction;

    private String type, username, productName,productDescription,productPrice;
    private Uri mImageUri;
    private ImageView iv_image,iv_choose;

    private DatabaseReference dataRef,refUser, refProduct;
    private StorageReference storeRef;
    private APIService apiService;
    private ValueEventListener  userListener, listener, listener1;

    private List<String> tokens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setdata();
        InitializeUI();

    }



    private void setdata() {
        type = getIntent().getStringExtra("type");
        tv_instruction = findViewById(R.id.tv_instruction);
        if(type.equals("product")){
            productName = getIntent().getStringExtra("productName");
            productPrice = getIntent().getStringExtra("productPrice");
            productDescription = getIntent().getStringExtra("productDescription");

        }else{
            tv_instruction = findViewById(R.id.tv_instruction);
            tv_instruction.setText("Profilna fotografija");
            tv_instruction.setTextSize(20);
            tv_instruction.setAllCaps(true);
            btn_delete = findViewById(R.id.btn_delete);
            btn_delete.setVisibility(View.VISIBLE);
            btn_delete.setEnabled(true);
        }

    }

    private void InitializeUI() {

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        btn_back = findViewById(R.id.btn_back);
        btn_upload = findViewById(R.id.btn_upload);
        iv_image = findViewById(R.id.iv_image);
        iv_choose = findViewById(R.id.iv_choose);
        refUser = FirebaseDatabase.getInstance().getReference("Users");
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot != null) {
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    getSubsrcibersTokens();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        refUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(userListener);

        if(type.equals("profile")){
            storeRef = FirebaseStorage.getInstance().getReference("profilePics");
            dataRef = FirebaseDatabase.getInstance().getReference("profilePics");
        }

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBack();
            }
        });

        iv_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               upload();
            }
        });

        if(btn_delete != null){
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteProifilePic();
                }
            });
        }



    }

    private void upload(){
        if(type.equals("product")){
            addProduct(productName);
        } else uploadProfilePic();

        Intent intent = new Intent(ImageActivity.this , UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type","normal");
        finish();
        startActivity(intent);
    }

    private void getBack(){
        if(type.equals("product")){
            Intent intent = new Intent(ImageActivity.this , UserProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("type","upload");
            intent.putExtra("productName",productName);
            intent.putExtra("productDescription",productDescription);
            intent.putExtra("productPrice",productPrice);
            finish();
            startActivity(intent);
        } else{
            Intent intent = new Intent(ImageActivity.this , UserProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
        }
    }

    private void getSubsrcibersTokens(){
        FirebaseDatabase.getInstance().getReference("Subscribers").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot current:snapshot.getChildren()){
                        String currentUsername = current.getKey();
                        FirebaseDatabase.getInstance().getReference("Tokens").child(currentUsername).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(!snapshot.getValue().toString().equals(current.getValue().toString())){
                                        FirebaseDatabase.getInstance().getReference("Subscribers").child(username).child(currentUsername).setValue(snapshot.getValue().toString());
                                    }
                                    tokens.add(current.getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(userListener);
    }



    private void deleteProifilePic() {
       FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    deletePictureofUser(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });



        Intent intent = new Intent(ImageActivity.this , UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);

    }

    private void deletePictureofUser(User user){
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    listener1 = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String uri = snapshot.getValue().toString();
                                StorageReference storeRef = FirebaseStorage.getInstance().getReferenceFromUrl(uri);
                                storeRef.delete();
                                FirebaseDatabase.getInstance().getReference("profilePics").child(user.getUsername()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ImageActivity.this,"Profilna slika uspješno obrisana",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                FirebaseDatabase.getInstance().getReference("profilePics").child(user.getUsername()).removeEventListener(listener1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    FirebaseDatabase.getInstance().getReference("profilePics").child(user.getUsername()).addValueEventListener(listener1);
                }

                FirebaseDatabase.getInstance().getReference("profilePics").child(user.getUsername()).removeEventListener(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        FirebaseDatabase.getInstance().getReference("profilePics").child(user.getUsername()).addValueEventListener(listener);
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("Image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            super.onActivityResult(requestCode, resultCode, data);
            mImageUri = data.getData();
            Glide.with(this).load(mImageUri).into(iv_image);
        }

    }


    private String getFileExtension(Uri uri){
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void uploadProfilePic() {

        if(mImageUri != null){

            StorageReference fileRefrence = storeRef.child(username + "." + getFileExtension(mImageUri));
            fileRefrence.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(ImageActivity.this,"Uspješno uneseno", Toast.LENGTH_SHORT).show();

                            fileRefrence.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    FirebaseDatabase.getInstance().getReference("profilePics").child(username).setValue(uri.toString());
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ImageActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else Toast.makeText(this,"Niste odabrali sliku", Toast.LENGTH_LONG).show();

    }


    private void addProduct(String productName) {

        if (mImageUri != null) {
            storeRef = FirebaseStorage.getInstance().getReference("productPics").child(username);
            StorageReference fileRefrence = storeRef.child(productName + "." + getFileExtension(mImageUri));
            fileRefrence.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(ImageActivity.this, "Uspješno uneseno", Toast.LENGTH_SHORT).show();

                            fileRefrence.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    add(productName,productDescription,productPrice,uri.toString());
                                    
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }else{
            add(productName,productDescription,productPrice,"null");
        }
    }

    private void add(String name, String description, String price, String uri){



        refProduct = FirebaseDatabase.getInstance().getReference("Products").child(username);

        refProduct.child(name).setValue(new Product(description,name,price,uri)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ImageActivity.this, "Proizvod je uspješno dodan", Toast.LENGTH_LONG).show();
                        if(!tokens.isEmpty()){
                            for(String token:tokens){

                                sendNotification("Novi proizvod: " + name,"korisnika " + username,token,username);
                            }
                        }

                    }
                    else{
                        Toast.makeText(ImageActivity.this, "Proizvod nije dodan", Toast.LENGTH_LONG).show();

                    }

                }
            });

    }

    private void sendNotification(String mtitle, String mmessage, String mtoken, String sender){

        Data data = new Data("notification",mtitle, mmessage,sender," ");
        NotificationSender notificationSender = new NotificationSender(data,mtoken);

        apiService.SendNotification(notificationSender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

}