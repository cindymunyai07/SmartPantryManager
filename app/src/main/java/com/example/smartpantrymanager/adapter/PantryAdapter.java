package com.example.smartpantrymanager.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.logic.ExpiryHelper;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Connects the list of PantryItem objects to the RecyclerView.
 * For each item it fills in one copy of the item_pantry.xml layout,
 * and highlights items that are expired or expiring soon (if enabled in Settings).
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /**
     * Lets the screen (fragment/activity) react when a row or delete button is tapped.
     */
    public interface OnItemActionListener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private static final int WARNING_COLOR = Color.parseColor("#B3261E");

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;
    private boolean alertEnabled = true;
    private int alertDays = 3;

    public PantryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the displayed list with fresh data from the database.
     */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Applies the user's expiry settings from the Settings screen.
     */
    public void setExpiryAlert(boolean enabled, int days) {
        this.alertEnabled = enabled;
        this.alertDays = days;
    }

    /**
     * Creates a new row view (called only when a new row is needed).
     */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /**
     * Fills a row with the data of the item at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
        bindExpiry(holder, item.getExpiryDate());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    /**
     * Shows the expiry text, in red if the item is expired or expiring soon.
     */
    private void bindExpiry(PantryViewHolder holder, String expiry) {
        // Always reset the colour first, because rows are reused while scrolling
        holder.textExpiry.setTextColor(holder.defaultExpiryColor);

        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setText("No expiry date");
            return;
        }

        int daysLeft = ExpiryHelper.daysUntil(expiry);
        if (alertEnabled && daysLeft < 0) {
            holder.textExpiry.setText("Expired: " + expiry);
            holder.textExpiry.setTextColor(WARNING_COLOR);
        } else if (alertEnabled && daysLeft <= alertDays) {
            holder.textExpiry.setText(daysLeft == 0
                    ? "Expires today!"
                    : "Expiring soon: " + expiry);
            holder.textExpiry.setTextColor(WARNING_COLOR);
        } else {
            holder.textExpiry.setText("Expires: " + expiry);
        }
    }

    /**
     * Tells the RecyclerView how many rows there are.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Shows 2 instead of 2.0 when the quantity is a whole number.
     */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    /**
     * Holds references to the views in one row so they are not looked up again and again.
     */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textQuantity;
        final TextView textExpiry;
        final ImageButton buttonDelete;
        final int defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            defaultExpiryColor = textExpiry.getCurrentTextColor();
        }
    }
}