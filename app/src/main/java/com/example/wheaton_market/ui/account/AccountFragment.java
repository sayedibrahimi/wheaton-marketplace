package com.example.wheaton_market.ui.account;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.wheaton_market.AccountEditPage;
import com.example.wheaton_market.Login;
import com.example.wheaton_market.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AccountFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference database;
    private ImageView userImageView;
    private TextView logTextView, firstNameTextView, lastNameTextView, emailTextView, widTextView;
    private Button editAccountButton, logoutButton, contactUsButton;
    private Context applicationContext;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = getActivity().getApplicationContext();
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        logTextView = root.findViewById(R.id.account_log_texTView);
        userImageView = root.findViewById(R.id.account_user_imageView);
        firstNameTextView = root.findViewById(R.id.account_first_textView);
        lastNameTextView = root.findViewById(R.id.account_last_textView);
        emailTextView = root.findViewById(R.id.account_email_textView);
        widTextView = root.findViewById(R.id.account_id_textView);
        editAccountButton = root.findViewById(R.id.account_change_Button);
        logoutButton = root.findViewById(R.id.account_logout_Button);
        contactUsButton = root.findViewById(R.id.account_contact_Button);

        // Set user info if logged in
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String wid = dataSnapshot.child("wheatonID").getValue(String.class);
                        String imageUrl = dataSnapshot.child("userPictureURL").getValue(String.class);

                        logTextView.setText("Logged in as: " + firstName + " " + lastName);
                        firstNameTextView.setText("First name: " + firstName);
                        lastNameTextView.setText("Last name: " + lastName);
                        emailTextView.setText("Email: " + email);
                        widTextView.setText("WID: " + wid);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d("imageUrl: ", imageUrl);
                            loadProfileImage(imageUrl);
                            Log.d("AccountFragment", "Image loaded successfully");
                        } else {
                            userImageView.setImageResource(R.drawable.marketplace_logo); // Placeholder if no image URL
                            Log.d("AccountFragment", "No profile image URL found");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Edit account button
        editAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountEditPage.class);
            startActivity(intent);
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Contact us button
        contactUsButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:koushik_tushar@wheatoncollege.edu,lopez_anton@gmail.com,ibrahi_sayed@wheatoncollege.edu"));
            startActivity(emailIntent);
        });

        // User image view click listener for image selection
        userImageView.setOnClickListener(v -> selectImage());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileImageFromDatabase();
    }

    private void loadProfileImageFromDatabase() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String imageUrl = dataSnapshot.child("userPictureURL").getValue(String.class);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d("imageUrl: ", imageUrl);
                            loadProfileImage(imageUrl);
                            Log.d("AccountFragment", "Image loaded successfully");
                        } else {
                            userImageView.setImageResource(R.drawable.marketplace_logo); // Placeholder if no image URL
                            Log.d("AccountFragment", "No profile image URL found");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 1);
            } else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 2);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == 1 && data != null) {
                imageUri = data.getData();
            } else if (requestCode == 2 && data != null) {
                imageUri = data.getData();
            }

            if (imageUri != null) {
                userImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            String userId = currentUser.getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(userId).child(userId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                updateImageUrlInDatabase(imageUrl);
                loadProfileImage(imageUrl); // Load new image immediately after upload
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show()));
        }
    }

    private void updateImageUrlInDatabase(String imageUrl) {
        if (currentUser != null) {
            database.child(currentUser.getUid()).child("userPictureURL").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> Toast.makeText(applicationContext, "Image saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(applicationContext, "Failed to save image URL", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (getContext() != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.marketplace_logo) // Optional placeholder while image loads
                    .error(R.drawable.marketplace_logo) // Optional error placeholder if load fails
                    .into(userImageView);
        }
    }
}
