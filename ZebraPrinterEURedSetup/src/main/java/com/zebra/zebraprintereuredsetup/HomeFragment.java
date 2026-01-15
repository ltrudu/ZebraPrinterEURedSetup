package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface NavigationCallback {
        void navigateTo(int navItemId);
    }

    private NavigationCallback navigationCallback;
    private EditText editTextSearch;
    private List<MenuItemCard> menuItems;

    private static class MenuItemCard {
        MaterialCardView card;
        int navId;
        String title;
        String description;

        MenuItemCard(MaterialCardView card, int navId, String title, String description) {
            this.card = card;
            this.navId = navId;
            this.title = title;
            this.description = description;
        }

        boolean matches(String query) {
            if (query == null || query.isEmpty()) {
                return true;
            }
            String lowerQuery = query.toLowerCase();
            return title.toLowerCase().contains(lowerQuery) ||
                   description.toLowerCase().contains(lowerQuery);
        }
    }

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
        setupSearch(view);
    }

    private void setupLauncherCards(View view) {
        menuItems = new ArrayList<>();

        // EU Red Setup
        MaterialCardView cardEuRed = view.findViewById(R.id.cardEuRed);
        menuItems.add(new MenuItemCard(
            cardEuRed,
            R.id.nav_eu_red,
            getString(R.string.launcher_eu_red_setup),
            getString(R.string.launcher_eu_red_description)
        ));
        cardEuRed.setOnClickListener(v -> navigateTo(R.id.nav_eu_red));

        // Custom Script
        MaterialCardView cardCustomScript = view.findViewById(R.id.cardCustomScript);
        menuItems.add(new MenuItemCard(
            cardCustomScript,
            R.id.nav_custom_script,
            getString(R.string.nav_custom_script),
            getString(R.string.launcher_custom_script_description)
        ));
        cardCustomScript.setOnClickListener(v -> navigateTo(R.id.nav_custom_script));

        // Script Documentation
        MaterialCardView cardDocumentation = view.findViewById(R.id.cardDocumentation);
        menuItems.add(new MenuItemCard(
            cardDocumentation,
            R.id.nav_script_documentation,
            getString(R.string.nav_script_documentation),
            getString(R.string.launcher_documentation_description)
        ));
        cardDocumentation.setOnClickListener(v -> navigateTo(R.id.nav_script_documentation));
    }

    private void setupSearch(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterMenuItems(s.toString());
            }
        });
    }

    private void filterMenuItems(String query) {
        for (MenuItemCard item : menuItems) {
            if (item.matches(query)) {
                item.card.setVisibility(View.VISIBLE);
            } else {
                item.card.setVisibility(View.GONE);
            }
        }
    }

    private void navigateTo(int navItemId) {
        if (navigationCallback != null) {
            navigationCallback.navigateTo(navItemId);
        }
    }

    public boolean handleBackPress() {
        // No child fragments in launcher, return false to allow default behavior
        return false;
    }
}
