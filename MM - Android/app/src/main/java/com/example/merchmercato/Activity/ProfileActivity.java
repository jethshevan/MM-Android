package com.example.merchmercato.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.merchmercato.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText dobEditText, addressEditText, postalCodeEditText;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Ensure this is your profile layout

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish(); // Finish ProfileActivity to prevent returning to it
            return; // Exit onCreate
        } else {
            // Initialize EditTexts and Back button
            dobEditText = findViewById(R.id.dob); // EditText for DOB
            addressEditText = findViewById(R.id.address); // EditText for address
            postalCodeEditText = findViewById(R.id.postalcode); // EditText for postal code
            ImageView backButton = findViewById(R.id.imageView3);
            backButton.setOnClickListener(v -> onBackPressed());

            findViewById(R.id.save).setOnClickListener(v -> updateUserData());
            findViewById(R.id.logout).setOnClickListener(v -> logout());

            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            loadUserData();
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference.child(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        RegisterActivity.User user = task.getResult().getValue(RegisterActivity.User.class);
                        if (user != null) {
                            // Convert dob to String safely
                            String dob = user.getDob() != 0 ? String.valueOf(user.getDob()) : ""; // Default to empty string if 0

                            // Check if address is a String or handle accordingly
                            String address = String.valueOf(user.getAddress()); // Change this to getAddress() if it's a string
                            String postalCode = user.getPostalCode() != 0 ? String.valueOf(user.getPostalCode()) : ""; // Assuming postalCode is an int

                            Log.d("ProfileActivity", "User Data: DOB: " + dob + ", Address: " + address + ", Postal Code: " + postalCode);

                            dobEditText.setText(dob); // Set DOB
                            addressEditText.setText(address); // Set address
                            postalCodeEditText.setText(postalCode); // Set postal code
                        }
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to load user data: " + task.getException().getMessage());
                }
            });
        }
    }

    private void updateUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String dob = dobEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String postalCode = postalCodeEditText.getText().toString().trim();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("dob", dob);
            userMap.put("address", address);
            userMap.put("postalCode", postalCode);

            databaseReference.child(uid).updateChildren(userMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "User data updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
