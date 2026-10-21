package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings screen: lets the user turn the "expiring soon" highlight on or off
 * and choose how many days ahead to warn. Choices are saved immediately.
 */
public class SettingsFragment extends Fragment {

    private SettingsManager settings;
    private RadioGroup radioGroupDays;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settings = new SettingsManager(requireContext());
        SwitchMaterial switchAlert = view.findViewById(R.id.switchExpiryAlert);
        radioGroupDays = view.findViewById(R.id.radioGroupDays);

        // Show the currently saved settings
        switchAlert.setChecked(settings.isExpiryAlertEnabled());
        int days = settings.getExpiryDays();
        radioGroupDays.check(days == 1 ? R.id.radioDays1
                : days == 7 ? R.id.radioDays7 : R.id.radioDays3);
        setRadioEnabled(settings.isExpiryAlertEnabled());

        // Save the choices as soon as the user changes them
        switchAlert.setOnCheckedChangeListener((button, checked) -> {
            settings.setExpiryAlertEnabled(checked);
            setRadioEnabled(checked);
        });

        radioGroupDays.setOnCheckedChangeListener((group, checkedId) -> {
            int chosen = checkedId == R.id.radioDays1 ? 1
                    : checkedId == R.id.radioDays7 ? 7 : 3;
            settings.setExpiryDays(chosen);
        });

        return view;
    }

    /**
     * Greys out the day options while the highlight is switched off.
     */
    private void setRadioEnabled(boolean enabled) {
        for (int i = 0; i < radioGroupDays.getChildCount(); i++) {
            radioGroupDays.getChildAt(i).setEnabled(enabled);
        }
    }
}