package com.example.wheaton_market.ui.sell;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.wheaton_market.EditPage;
import com.example.wheaton_market.Item;
import com.example.wheaton_market.R;
import com.example.wheaton_market.SellPage;
import com.example.wheaton_market.databinding.FragmentSellBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SellFragment extends Fragment {

    private FragmentSellBinding binding;
    private ArrayList<Item> sellListView;
    private ArrayAdapter<Item> adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference Database;
    private FirebaseUser currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSellBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sellListView = new ArrayList<>();
        adapter = new CustomArrayAdapter(getContext(), sellListView);
        ListView listView = binding.sellListView;
        listView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Database = FirebaseDatabase.getInstance().getReference(); // Initialize Database reference

        Button sellButton = root.findViewById(R.id.sell_add_button);;
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SellPage.class);
                startActivity(intent);
            }
        });

        fetchItemsFromDatabase();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch items from the database when the fragment is resumed
        fetchItemsFromDatabase();
    }

    private void fetchItemsFromDatabase() {
        Database.child("user_items").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sellListView.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    final String itemId = itemSnapshot.getKey(); // Declare itemId as final
                    // Fetch the item details from the "items" node based on the itemId
                    Database.child("items").child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String description = dataSnapshot.child("description").getValue(String.class);
                                Double price = dataSnapshot.child("price").getValue(Double.class);
                                String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                                Log.d("SellFragment", "Image URL from Firebase: " + imageURL); // Add this line for debugging
                                if (name != null && price != null) {
                                    Item item = new Item(itemId, name, description, price, imageURL);
                                    sellListView.add(item);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.e("SellFragment", "Price is null for item: " + dataSnapshot.getKey());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to fetch items from database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch items from database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CustomArrayAdapter extends ArrayAdapter<Item> {

        private ArrayList<Item> itemList;
        private Context mContext;

        public CustomArrayAdapter(Context context, ArrayList<Item> itemList) {
            super(context, 0, itemList);
            this.mContext = context;
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(mContext).inflate(R.layout.sell_item, parent, false);
            }

            Item currentItem = itemList.get(position);

            TextView itemNameTextView = listItemView.findViewById(R.id.sellItem_productName_textView);
            itemNameTextView.setText(currentItem.getName());

            TextView itemDescriptionTextView = listItemView.findViewById(R.id.sellItem_productDesc_textView);
            itemDescriptionTextView.setText(currentItem.getDescription());

            TextView itemPriceTextView = listItemView.findViewById(R.id.sellItem_productPrice_textView);
            itemPriceTextView.setText(String.valueOf(currentItem.getPrice()));

            ImageView itemImageView = listItemView.findViewById(R.id.sellItem_marketplaceLogo_imageView);

            // Load and display the image using Glide
            Glide.with(mContext)
                    .load(currentItem.getImageURL()) // URL of the image
                    .placeholder(R.drawable.ic_sell_black_24dp) // Placeholder image while loading
                    .error(R.drawable.ic_sell_black_24dp) // Error image if loading fails
                    .into(itemImageView);

            // Add the edit button
            Button editButton = listItemView.findViewById(R.id.sellItem_edit_button);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle edit button click event
                    // Open the edit activity for the selected item
                    Intent editIntent = new Intent(mContext, EditPage.class);
                    editIntent.putExtra("itemId", currentItem.getItemId()); // Pass the itemId
                    editIntent.putExtra("itemName", currentItem.getName());
                    editIntent.putExtra("itemDescription", currentItem.getDescription());
                    editIntent.putExtra("itemPrice", currentItem.getPrice());
                    editIntent.putExtra("itemImageURL", currentItem.getImageURL());
                    mContext.startActivity(editIntent);
                }
            });

            return listItemView;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}