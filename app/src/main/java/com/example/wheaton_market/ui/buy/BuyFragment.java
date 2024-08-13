package com.example.wheaton_market.ui.buy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.wheaton_market.BuyPage;
import com.example.wheaton_market.Item;
import com.example.wheaton_market.R;
import com.example.wheaton_market.databinding.FragmentBuyBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BuyFragment extends Fragment {

    private FragmentBuyBinding binding;
    private ArrayList<Item> buyListView;
    private ArrayAdapter<Item> adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference Database;
    private FirebaseUser currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBuyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        buyListView = new ArrayList<>();
        adapter = new CustomArrayAdapter(getContext(), buyListView);
        ListView listView = binding.buyListView;
        listView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Database = FirebaseDatabase.getInstance().getReference(); // Initialize Database reference

        fetchItemsFromDatabase();

        return root;
    }

    private void fetchItemsFromDatabase() {
        Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                buyListView.clear();
                for (DataSnapshot itemSnapshot : snapshot.child("items").getChildren()) {
                    String itemId = itemSnapshot.getKey();
                    boolean isCurrentUserItem = isCurrentUserItem(snapshot.child("user_items").child(currentUser.getUid()), itemId);
                    if (!isCurrentUserItem) {
                        String name = itemSnapshot.child("name").getValue(String.class);
                        String description = itemSnapshot.child("description").getValue(String.class);
                        double price = itemSnapshot.child("price").getValue(Double.class);
                        String imageURL = itemSnapshot.child("imageURL").getValue(String.class);
                        Item item = new Item(itemId, name, description, price, imageURL);
                        buyListView.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch items from database", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isCurrentUserItem(DataSnapshot userItemsSnapshot, String itemId) {
        return userItemsSnapshot.hasChild(itemId);
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
                listItemView = LayoutInflater.from(mContext).inflate(R.layout.buy_item, parent, false);
            }

            Item currentItem = itemList.get(position);

            TextView itemNameTextView = listItemView.findViewById(R.id.buyItem_productName_textView);
            itemNameTextView.setText(currentItem.getName());

            TextView itemDescriptionTextView = listItemView.findViewById(R.id.buyItem_productDesc_textView);
            itemDescriptionTextView.setText(currentItem.getDescription());

            TextView itemPriceTextView = listItemView.findViewById(R.id.buyItem_productPrice_textView);
            itemPriceTextView.setText(String.valueOf(currentItem.getPrice()));

            ImageView itemimageView = listItemView.findViewById(R.id.buy_marketplaceLogo_imageView);

            // Load image using Glide
            Glide.with(mContext)
                    .load(currentItem.getImageURL())
                    .placeholder(R.drawable.ic_sell_black_24dp) // Placeholder image while loading
                    .error(R.drawable.ic_sell_black_24dp) // Error image if loading fails
                    .into(itemimageView);

            // Add buy button click listener and intent
            Button buyItem_buy_button = listItemView.findViewById(R.id.buyItem_buy_button);
            buyItem_buy_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, BuyPage.class);
                    intent.putExtra("itemId", currentItem.getItemId());
                    intent.putExtra("itemName", currentItem.getName());
                    intent.putExtra("itemDesc", currentItem.getDescription());
                    intent.putExtra("itemPrice", currentItem.getPrice());
                    mContext.startActivity(intent);
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
