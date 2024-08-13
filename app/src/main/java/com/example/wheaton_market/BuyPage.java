package com.example.wheaton_market;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class BuyPage extends AppCompatActivity {
    private User currentUser;
    private DatabaseReference userRef;
    private DatabaseReference itemsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private String emailBody;

    private String itemName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            fetchCurrentUser();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        itemsRef = FirebaseDatabase.getInstance().getReference("items");
        usersRef = FirebaseDatabase.getInstance().getReference("users");


        // Get the Intent that started this activity
        Intent intent = getIntent();

        // Extract data from the Intent (if any)
        String itemId = intent.getStringExtra("itemId");
        fetchItemAndUserInfo(itemId);


        Button BuyButton = findViewById(R.id.examine_buy_Button);
        BuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailBody != null) {
                    buyButton();
                } else {
                    Toast.makeText(BuyPage.this, "Failed to fetch user information", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCurrentUser() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUser = dataSnapshot.getValue(User.class);
                } else {
                    Toast.makeText(BuyPage.this, "User data not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyPage.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchItemAndUserInfo(String itemId) {
        itemsRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Log the snapshot to see its structure
                    Log.d("DataSnapshot", dataSnapshot.toString());

                    itemName = dataSnapshot.child("name").getValue(String.class);
                    String itemDesc = dataSnapshot.child("description").getValue(String.class);
                    String itemPrice = String.valueOf(dataSnapshot.child("price").getValue(Double.class));

                    // Set item details to your views
                    TextView examineNameTextView = findViewById(R.id.examine_name_textVeiw);
                    examineNameTextView.setText(itemName);

                    TextView examineDescTextView = findViewById(R.id.examine_desc_textView);
                    examineDescTextView.setText(itemDesc);

                    TextView examine_price_textView = findViewById(R.id.examine_price_textView);
                    examine_price_textView.setText(itemPrice);

                    // Fetch user information
                    getSellerIdFromUserItems(itemId);
                } else {
                    Toast.makeText(BuyPage.this, "Item not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyPage.this, "Failed to fetch item details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSellerIdFromUserItems(String itemId) {
        DatabaseReference userItemsRef = FirebaseDatabase.getInstance().getReference("user_items");
        userItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userItemsSnapshot) {
                if (userItemsSnapshot.exists()) {

                    // Log the snapshot to see its structure
                    Log.d("DataSnapshot", userItemsSnapshot.toString());

                    // Loop through all the children of user_items
                    for (DataSnapshot userSnapshot : userItemsSnapshot.getChildren()) {
                        // Check if the current userSnapshot has a child with the given itemId
                        if (userSnapshot.child(itemId).exists()) {
                            // Return the key of the userSnapshot, which is the sellerId
                            String sellerId = userSnapshot.getKey();
                            Log.d("SellerId", "Found sellerId: " + sellerId);
                            fetchUserInfo(sellerId);
                            return;
                        }
                    }
                }
                // Log that sellerId was not found
                Log.d("SellerId", "Seller ID not found for item");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getSellerIdFromUserItems", "Failed to fetch user_items data", databaseError.toException());
            }
        });
        return null; // Seller ID not found (yet)
    }



    private void fetchUserInfo(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch user information
                    userEmail = dataSnapshot.child("email").getValue(String.class);
                    String userFirstName = dataSnapshot.child("firstName").getValue(String.class);

                    // Create email body with user information
                    emailBody = createEmailBody(userFirstName);
                } else {
                    Toast.makeText(BuyPage.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyPage.this, "Failed to fetch user information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buyButton() {
        String emailTitle = "PURCHASE REQUEST: " + itemName;

        String mailto = "mailto:" + userEmail +
                "?cc=" + " " +
                "&subject=" + Uri.encode(emailTitle) +
                "&body=" + Uri.encode(emailBody);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    // Method to create email body using user information
    private String createEmailBody(String userFirstName) {
        StringBuilder builder = new StringBuilder();
        if (currentUser != null) {
            builder.append("Hello ").append(userFirstName).append(",\n\n");
        } else {
            builder.append("Hello,\n\n");
        }
        builder.append("My name is ").append(currentUser.getFirstName()).append(" ").append(currentUser.getLastName());
        builder.append(" I'm interested in purchasing the item ").append("[").append(itemName).append("] ")
                .append("you listed on Wheaton Market App.\n");
        builder.append("Please contact me at ").append(currentUser.getEmail()).append(" at your earliest convenience\n\n");
        builder.append("Thank you.\n\n");
        builder.append("Best,\n").append(currentUser.getFirstName());
        return builder.toString();
    }
}
