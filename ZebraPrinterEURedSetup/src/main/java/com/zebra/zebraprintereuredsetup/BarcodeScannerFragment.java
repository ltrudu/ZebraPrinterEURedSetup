package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class BarcodeScannerFragment extends Fragment {

    public static final String ARG_TARGET_FIELD = "target_field";
    public static final String RESULT_KEY = "barcode_result";
    public static final String RESULT_BARCODE = "barcode_value";
    public static final String RESULT_TARGET_FIELD = "target_field";

    // Target field identifiers
    public static final int FIELD_MAC_ADDRESS = 0;
    public static final int FIELD_OLD_PASSWORD = 1;
    public static final int FIELD_NEW_PASSWORD = 2;
    public static final int FIELD_HTTP_ADMIN_PASSWORD = 3;
    public static final int FIELD_AUTH_PASSWORD = 4;
    public static final int FIELD_PASSWORD_CURRENT = 5;

    private DecoratedBarcodeView barcodeView;
    private MaterialButton flashButton;
    private int targetField;
    private boolean isFlashOn = false;

    public static BarcodeScannerFragment newInstance(int targetField) {
        BarcodeScannerFragment fragment = new BarcodeScannerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TARGET_FIELD, targetField);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetField = getArguments().getInt(ARG_TARGET_FIELD, FIELD_MAC_ADDRESS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText(getString(R.string.scanner_hint));

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    barcodeView.pause();

                    // Turn off flash when scan is complete
                    if (isFlashOn) {
                        barcodeView.setTorchOff();
                        isFlashOn = false;
                    }

                    // Send result back to MainActivity
                    Bundle resultBundle = new Bundle();
                    resultBundle.putString(RESULT_BARCODE, result.getText());
                    resultBundle.putInt(RESULT_TARGET_FIELD, targetField);
                    getParentFragmentManager().setFragmentResult(RESULT_KEY, resultBundle);

                    // Go back to previous fragment/activity
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional: handle possible result points for visual feedback
            }
        });

        // Flash toggle button
        flashButton = view.findViewById(R.id.buttonFlash);
        if (flashButton != null) {
            flashButton.setOnClickListener(v -> toggleFlash());
        }

        // Back button
        View backButton = view.findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Turn off flash when going back
                if (isFlashOn) {
                    barcodeView.setTorchOff();
                    isFlashOn = false;
                }
                getParentFragmentManager().popBackStack();
            });
        }
    }

    private void toggleFlash() {
        if (isFlashOn) {
            barcodeView.setTorchOff();
            flashButton.setIconResource(R.drawable.flashlight_off_icon);
            isFlashOn = false;
        } else {
            barcodeView.setTorchOn();
            flashButton.setIconResource(R.drawable.flashlight_on_icon);
            isFlashOn = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
        // Turn off flash when pausing
        if (isFlashOn) {
            barcodeView.setTorchOff();
            isFlashOn = false;
            if (flashButton != null) {
                flashButton.setIconResource(R.drawable.flashlight_off_icon);
            }
        }
    }
}
