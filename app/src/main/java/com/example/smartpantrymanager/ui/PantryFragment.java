package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient stored in the database
 * and lets the user delete items. Add/edit will be wired up next.
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

        // Add button: the Add/Edit screen is built in the next step
        FloatingActionButton fab = view.findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Add screen coming next", Toast.LENGTH_SHORT).show());

        return view;
    }

    /** Reload the list every time the screen comes back into view. */
    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }

    /** Reads all items from the database and shows them (or the empty message). */
    private void loadItems() {
        List<PantryItem> items = dbHelper.getAllPantryItems();
        adapter.setItems(items);
        textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(PantryItem item) {
        // Will open the Add/Edit screen in the next step
        Toast.makeText(requireContext(), "Edit " + item.getName(), Toast.LENGTH_SHORT).show();
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