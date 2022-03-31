package com.example.primjer_prijave.AddProduct;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.primjer_prijave.NotificationMessagingService.APIService;
import com.example.primjer_prijave.NotificationMessagingService.Client;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddProductFragment extends Fragment {


    private APIService apiService;
    private String username;
    private Button btn_add_product;
    private FirebaseUser user;
    private DatabaseReference refUser,dataRef;
    private TextView tv_username;
    private String check = "true";
    private ValueEventListener listener, listenerUsername;
    private EditText et_name, et_description, et_price;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        InitializeUI(view);

        setArguments();

        return view;
    }

    private void setArguments() {
        String type = getArguments().getString("type");
        if (type.equals("upload")) {

            String name, description, price;
            name = getArguments().getString("productName");
            description = getArguments().getString("productDescription");
            price = getArguments().getString("productPrice");
            Intent intent = new Intent();



            et_name.setText(name);
            et_description.setText(description);
            et_price.setText(price);

        }
    }

    private void InitializeUI(View view) {

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        tv_username = view.findViewById(R.id.tv_username);
        btn_add_product = view.findViewById(R.id.btn_add_product);
        et_description = view.findViewById(R.id.et_description);
        et_price = view.findViewById(R.id.et_price);
        et_name = view.findViewById(R.id.et_name);
        user = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        listenerUsername = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    User user = snapshot.getValue(User.class);
                    username = user.getUsername();
                    tv_username.setText("korisnika " + username);
                }
                refUser.removeEventListener(listenerUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        refUser.addValueEventListener(listenerUsername);


        btn_add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }

        });


    }

    private void next() {
        String productName = et_name.getText().toString().trim();
        String productDescription = et_description.getText().toString().trim();
        String productPrice = et_price.getText().toString().trim();

        if (productName.isEmpty() || productDescription.isEmpty() || productPrice.isEmpty()) {
            Toast.makeText(getContext(), "Niste popunili sva polja", Toast.LENGTH_LONG).show();
            return;
        } else if (productName.contains(".") || productName.contains("#") || productName.contains("$") || productName.contains("[") || productName.contains("]")) {
            et_name.setError("Korisničko ime ne smije sadržavati '.', '#', '$', '[', ili ']'");
            et_name.requestFocus();
            return;

        } else {
            dataRef = FirebaseDatabase.getInstance().getReference("Products").child(username).child(productName);
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        et_name.setError("Ovaj proizvod ste već dodali");
                        et_name.requestFocus();
                        return;
                    }
                    String name = et_name.getText().toString();
                    Intent intent = new Intent(getActivity(), ImageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", "product");
                    intent.putExtra("productName", name);
                    intent.putExtra("productDescription", et_description.getText().toString());
                    intent.putExtra("productPrice", et_price.getText().toString());
                    getActivity().finish();
                    startActivity(intent);
                    dataRef.removeEventListener(listener);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            dataRef.addValueEventListener(listener);


        }

    }
}
