package com.zebra.zebraprinterblesetup;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class AboutFragment extends Fragment {

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
