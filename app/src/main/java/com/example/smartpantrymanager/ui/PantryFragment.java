package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.data.SettingsManager;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient stored in the database,
 * and opens the Add/Edit screen or deletes items.
 */
public class PantryFragment extends Fragment implements PantryAdapter.OnItemActionListener {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private TextView textEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        textEmpty = view.findViewById(R.id.textEmptyPantry);

        // Set up the RecyclerView with our custom adapter
        RecyclerView recyclerView = view.findViewById(R.id.recyclerPantry);
        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Add button: open the Add/Edit screen with an Intent (no extras = new item)
        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        return view;
    }

    /**
     * Reload the list every time the screen comes back into view.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }


    /**
     * Reads all items from the database and shows them (or the empty message).
     */
    private void loadItems() {
        // Apply the user's expiry settings before showing the list
        SettingsManager settings = new SettingsManager(requireContext());
        adapter.setExpiryAlert(settings.isExpiryAlertEnabled(), settings.getExpiryDays());

        List<PantryItem> items = dbHelper.getAllPantryItems();
        adapter.setItems(items);
        textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Tapping a row opens the Add/Edit screen and passes the item's id in the Intent.
     */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(PantryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
    }
}