package com.example.wheaton_market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditPage extends AppCompatActivity {

    private EditText itemNameEditText, itemDescriptionEditText, itemPriceEditText;
    private ImageView itemImageImageView;
    private Button editImageButton;

    private FirebaseAuth mAuth;
    private DatabaseReference Database;
    private FirebaseUser currentUser;

    private String itemId, itemImageURL;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Database = FirebaseDatabase.getInstance().getReference();

        itemNameEditText = findViewById(R.id.edit_title_editText);
        itemDescriptionEditText = findViewById(R.id.edit_desc_editText);
        itemPriceEditText = findViewById(R.id.edit_price_editText);
        itemImageImageView = findViewById(R.id.editImage_imageView);
        editImageButton = findViewById(R.id.editImage_button);

        // Retrieve the item details passed from SellFragment via intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemId = extras.getString("itemId");
            String itemName = extras.getString("itemName");
            String itemDescription = extras.getString("itemDescription");
            double itemPrice = extras.getDouble("itemPrice");
            itemImageURL = extras.getString("itemImageURL");


            // Set the text of the EditText fields with the retrieved item details
            itemNameEditText.setText(itemName);
            itemDescriptionEditText.setText(itemDescription);
            itemPriceEditText.setText(String.valueOf(itemPrice));

            // Load the image into the ImageView using Glide
            if (itemImageURL != null && !itemImageURL.isEmpty()) {
                Glide.with(this)
                        .load(itemImageURL)
                        .placeholder(R.drawable.marketplace_logo) // Optional placeholder
                        .error(R.drawable.marketplace_logo) // Optional error image
                        .into(itemImageImageView);
            } else {
                itemImageImageView.setImageResource(R.drawable.marketplace_logo); // Placeholder if no image URL
            }
        }

        Button submitButton = findViewById(R.id.edit_save_Button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitItem();
            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void submitItem() {
        String name = itemNameEditText.getText().toString().trim();
        String description = itemDescriptionEditText.getText().toString().trim();
        String priceStr = itemPriceEditText.getText().toString().trim();
        String imageURL = itemImageURL;

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        // Create Item object with updated information
        Item updatedItem = new Item(itemId, name, description, price, imageURL);

        // Update item in the Firebase Realtime Database
        Database.child("items").child(itemId).setValue(updatedItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditPage.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPage.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
            } else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                imageUri = data.getData();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
            }

            if (imageUri != null) {
                itemImageImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            String userId = currentUser.getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(userId).child(itemId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                itemImageURL = uri.toString();
                loadImage(itemImageURL); // Load new image immediately after upload
            }).addOnFailureListener(e -> Toast.makeText(EditPage.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show()));
        }
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.marketplace_logo) // Optional placeholder
                    .error(R.drawable.marketplace_logo) // Optional error image
                    .into(itemImageImageView);
        } else {
            itemImageImageView.setImageResource(R.drawable.marketplace_logo); // Placeholder if no image URL
        }
    }
}



