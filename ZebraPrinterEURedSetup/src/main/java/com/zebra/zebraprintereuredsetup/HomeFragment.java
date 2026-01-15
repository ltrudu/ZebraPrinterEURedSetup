package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    public interface NavigationCallback {
        void navigateTo(int navItemId);
    }

    private NavigationCallback navigationCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Try to get navigation callback from activity
        if (getActivity() instanceof NavigationCallback) {
            navigationCallback = (NavigationCallback) getActivity();
        }

        setupLauncherCards(view);
    }

    private void setupLauncherCards(View view) {
        MaterialCardView cardEuRed = view.findViewById(R.id.cardEuRed);
        MaterialCardView cardCustomScript = view.findViewById(R.id.cardCustomScript);
        MaterialCardView cardDocumentation = view.findViewById(R.id.cardDocumentation);
        MaterialCardView cardSettings = view.findViewById(R.id.cardSettings);

        // EU Red Setup - Navigate to EURedFragment
        cardEuRed.setOnClickListener(v -> {
            if (navigationCallback != null) {
                navigationCallback.navigateTo(R.id.nav_eu_red);
            }
        });

        // Custom Script
        cardCustomScript.setOnClickListener(v -> {
            if (navigationCallback != null) {
                navigationCallback.navigateTo(R.id.nav_custom_script);
            }
        });

        // Script Documentation
        cardDocumentation.setOnClickListener(v -> {
            if (navigationCallback != null) {
                navigationCallback.navigateTo(R.id.nav_script_documentation);
            }
        });

        // Settings
        cardSettings.setOnClickListener(v -> {
            if (navigationCallback != null) {
                navigationCallback.navigateTo(R.id.nav_settings);
            }
        });
    }

    public boolean handleBackPress() {
        // No child fragments in launcher, return false to allow default behavior
        return false;
    }
}
