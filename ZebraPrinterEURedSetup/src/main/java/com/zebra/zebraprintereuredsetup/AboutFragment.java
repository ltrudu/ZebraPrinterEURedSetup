package com.zebra.zebraprintereuredsetup;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class AboutFragment extends Fragment {

    private static final int CLICKS_TO_ENABLE_ADVANCED = 5;
    private int developerClickCount = 0;
    private int copyrightClickCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View view) {
        // Set version text
        MaterialTextView textViewVersion = view.findViewById(R.id.textViewVersion);
        String versionName = getVersionName();
        textViewVersion.setText(getString(R.string.about_version, versionName));

        // Developer text - hidden click handler for advanced mode
        MaterialTextView textViewDeveloper = view.findViewById(R.id.textViewDeveloper);
        textViewDeveloper.setOnClickListener(v -> onDeveloperClicked());

        // Copyright text - hidden click handler for advanced mode
        MaterialTextView textViewCopyright = view.findViewById(R.id.textViewCopyright);
        textViewCopyright.setOnClickListener(v -> onCopyrightClicked());

        // GitHub button
        MaterialButton buttonGitHub = view.findViewById(R.id.buttonGitHub);
        buttonGitHub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_github_url)));
            startActivity(intent);
        });

        // ZXing button
        MaterialButton buttonZxing = view.findViewById(R.id.buttonZxing);
        buttonZxing.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/journeyapps/zxing-android-embedded"));
            startActivity(intent);
        });

        // Zebra SDK button
        MaterialButton buttonZebraSdk = view.findViewById(R.id.buttonZebraSdk);
        buttonZebraSdk.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zebra.com/us/en/support-downloads/printer-software/link-os-multiplatform-sdk.html"));
            startActivity(intent);
        });

        // Room button
        MaterialButton buttonRoom = view.findViewById(R.id.buttonRoom);
        buttonRoom.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com/jetpack/androidx/releases/room"));
            startActivity(intent);
        });
    }

    private void onDeveloperClicked() {
        // Already enabled - do nothing
        if (SettingsHelper.getAdvancedModeEnabled(requireContext())) {
            return;
        }

        developerClickCount++;

        if (developerClickCount >= CLICKS_TO_ENABLE_ADVANCED) {
            showAdvancedModeWarningDialog();
            developerClickCount = 0;
        }
    }

    private void showAdvancedModeWarningDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.advanced_mode_warning_title)
                .setMessage(R.string.advanced_mode_warning_message)
                .setPositiveButton(R.string.button_i_understand, (dialog, which) -> {
                    enableAdvancedMode();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    private void enableAdvancedMode() {
        SettingsHelper.saveAdvancedModeEnabled(requireContext(), true);
        Toast.makeText(requireContext(), R.string.advanced_mode_enabled, Toast.LENGTH_SHORT).show();

        // Notify MainActivity to update navigation drawer
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshNavigationDrawer();
        }
    }

    private void onCopyrightClicked() {
        // Already enabled - do nothing
        if (SettingsHelper.getAdvancedModeEnabled(requireContext())) {
            return;
        }

        copyrightClickCount++;

        if (copyrightClickCount >= CLICKS_TO_ENABLE_ADVANCED) {
            showAdvancedModeWarningDialog();
            copyrightClickCount = 0;
        }
    }

    private String getVersionName() {
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }
}
