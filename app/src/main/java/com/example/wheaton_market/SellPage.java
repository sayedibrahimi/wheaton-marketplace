package com.example.wheaton_market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SellPage extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText itemDescriptionEditText;
    private EditText itemPriceEditText;
    private Button itemImageButton;

    private FirebaseAuth mAuth;
    private DatabaseReference Database;
    private FirebaseUser currentUser;

    private static final int PICK_IMAGE_REQUEST = 1;

    private String imageURL;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_page);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Get current user
        currentUser = mAuth.getCurrentUser();
        // Initialize Firebase Database
        Database = FirebaseDatabase.getInstance().getReference();

        // Push item to Firebase Realtime Database
        itemId = Database.child("items").push().getKey();


        itemNameEditText = findViewById(R.id.create_title_editText);
        itemDescriptionEditText = findViewById(R.id.create_desc_editText);
        itemPriceEditText = findViewById(R.id.create_price_editText);
        itemImageButton = findViewById(R.id.create_upload_button);
        itemImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(); // Open file chooser when the button is clicked
            }
        });

        Button submitButton = findViewById(R.id.create_post_Button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitItem();
            }
        });
    }

    // Method to open file chooser to select an image
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handling the result of the file chooser activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // You can now upload this image to Firebase Storage
            uploadImage(imageUri);
        }
    }

    // Method to upload the selected image to Firebase Storage
    private void uploadImage(Uri imageUri) {

        // Ensure that itemId is not null
        if (itemId == null) {
            Log.e("SellPage", "Item ID is null. Cannot upload image.");
            return;
        }

        // Initialize Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String userId = currentUser.getUid();
        final StorageReference imageRef = storageRef.child(userId).child(itemId);

        // Upload the file to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageURL = uri.toString();
                                Log.d("SellPage", "Image URL here: ." + imageURL);

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        Toast.makeText(SellPage.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void submitItem() {
        // Obtain item details
        String name = itemNameEditText.getText().toString().trim();
        String description = itemDescriptionEditText.getText().toString().trim();
        String priceStr = itemPriceEditText.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        // Create Item object
        Item newItem = new Item(itemId, name, description, price, imageURL);

        Database.child("items").child(itemId).setValue(newItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Item added successfully
                        Toast.makeText(SellPage.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        // Finish activity
                        // Redirect back to SellFragment auto
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while adding item
                        Toast.makeText(SellPage.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                });

        // Associate item with user's email ID
        if (currentUser != null) {
            Database.child("user_items").child(currentUser.getUid()).child(itemId).setValue(true);
        }
    }
}

