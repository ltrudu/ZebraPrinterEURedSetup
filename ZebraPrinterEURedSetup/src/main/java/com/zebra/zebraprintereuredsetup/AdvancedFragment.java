package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
        setupBackStackListener();
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);
        cardFactoryReset = view.findViewById(R.id.cardFactoryReset);

        // Factory Reset tool click listener
        cardFactoryReset.setOnClickListener(v -> openFactoryResetFragment());
    }

    private void setupBackStackListener() {
        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                showMainContent();
            }
        });
    }

    private void openFactoryResetFragment() {
        fragmentContainer.setVisibility(View.VISIBLE);

        // Animate mainContent out to the left
        Animation slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContent.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mainContent.startAnimation(slideOut);

        FactoryResetFragment factoryResetFragment = new FactoryResetFragment();
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,   // enter
                        R.anim.slide_out_left,   // exit
                        R.anim.slide_in_left,    // popEnter
                        R.anim.slide_out_right   // popExit
                )
                .replace(R.id.fragment_container, factoryResetFragment)
                .addToBackStack("factory_reset")
                .commit();
    }

    private void showMainContent() {
        mainContent.setVisibility(View.VISIBLE);

        // Animate mainContent back in from the left
        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                fragmentContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mainContent.startAnimation(slideIn);
    }

    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            // showMainContent will be called by the back stack listener
            return true;
        }
        return false;
    }
}
