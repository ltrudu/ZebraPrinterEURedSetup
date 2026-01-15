package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.card.MaterialCardView;

public class AdvancedFragment extends Fragment {

    private FragmentContainerView fragmentContainer;
    private View mainContent;
    private MaterialCardView cardFactoryReset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);
        cardFactoryReset = view.findViewById(R.id.cardFactoryReset);

        // Factory Reset tool click listener
        cardFactoryReset.setOnClickListener(v -> openFactoryResetFragment());
    }

    private void openFactoryResetFragment() {
        fragmentContainer.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        FactoryResetFragment factoryResetFragment = new FactoryResetFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, factoryResetFragment)
                .addToBackStack("factory_reset")
                .commit();
    }

    private void showMainContent() {
        fragmentContainer.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            showMainContent();
            return true;
        }
        return false;
    }
}
