package com.zebra.zebraprintereuredsetup;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

public class FactoryResetFragment extends Fragment implements ToolbarConfigurable {

    public static final int FIELD_SERIAL_NUMBER = 100;

    private TextInputLayout textInputLayoutSerialNumber;
    private EditText editTextSerialNumber;
    private MaterialButton buttonScanSerialNumber;
    private MaterialButton buttonPerformFactoryReset;
    private TextView textViewStatus;
    private TextView textViewStatusDetails;

    private FragmentContainerView fragmentContainer;
    private View mainContent;

    private PrinterHelper printerHelper;
    private volatile boolean isOperationComplete = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_factory_reset, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        printerHelper = new PrinterHelper();
        setupViews(view);
        setupBarcodeResultListener();
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);

        textInputLayoutSerialNumber = view.findViewById(R.id.textInputLayoutSerialNumber);
        editTextSerialNumber = view.findViewById(R.id.editTextSerialNumber);
        buttonScanSerialNumber = view.findViewById(R.id.buttonScanSerialNumber);
        buttonPerformFactoryReset = view.findViewById(R.id.buttonPerformFactoryReset);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        textViewStatusDetails = view.findViewById(R.id.textViewStatusDetails);

        // Serial number text change listener
        editTextSerialNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        });

        // Scan barcode button
        buttonScanSerialNumber.setOnClickListener(v -> openBarcodeScanner(FIELD_SERIAL_NUMBER));

        // Factory Reset button
        buttonPerformFactoryReset.setOnClickListener(v -> {
            String serialNumber = editTextSerialNumber.getText().toString().trim();
            if (serialNumber.isEmpty()) {
                textInputLayoutSerialNumber.setError(getString(R.string.error_serial_number_required));
                return;
            }
            showFactoryResetConfirmationDialog(serialNumber);
        });

        validateForm();
    }

    private void validateForm() {
        String serialNumber = editTextSerialNumber.getText().toString().trim();
        boolean isValid = !serialNumber.isEmpty();

        buttonPerformFactoryReset.setEnabled(isValid);

        if (isValid) {
            textInputLayoutSerialNumber.setError(null);
        }
    }

    private void showFactoryResetConfirmationDialog(String serialNumber) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_factory_reset_confirmation);
        dialog.setCancelable(true);

        // Set dialog window background and border
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Create a card wrapper with red stroke
        MaterialCardView dialogCard = new MaterialCardView(requireContext());
        dialogCard.setCardElevation(8);
        dialogCard.setRadius(24);
        dialogCard.setStrokeColor(requireContext().getColor(android.R.color.holo_red_dark));
        dialogCard.setStrokeWidth(4);
        dialogCard.setCardBackgroundColor(requireContext().getColor(android.R.color.white));

        // Get the dialog content and wrap it
        View dialogContent = dialog.findViewById(android.R.id.content);
        if (dialogContent instanceof ViewGroup) {
            ViewGroup contentParent = (ViewGroup) dialogContent;
            if (contentParent.getChildCount() > 0) {
                View originalContent = contentParent.getChildAt(0);
                contentParent.removeView(originalContent);
                dialogCard.addView(originalContent);
                contentParent.addView(dialogCard);
            }
        }

        MaterialButton buttonCancel = dialog.findViewById(R.id.buttonDialogCancel);
        MaterialButton buttonConfirm = dialog.findViewById(R.id.buttonDialogConfirm);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performFactoryReset(serialNumber);
        });

        dialog.show();
    }

    private void performFactoryReset(String serialNumber) {
        // Disable button during operation
        buttonPerformFactoryReset.setEnabled(false);
        isOperationComplete = false;

        // Initial status
        setStatus(getString(R.string.status_factory_reset_starting),
            requireContext().getColor(android.R.color.holo_blue_dark));
        setStatusDetails(getString(R.string.status_factory_reset_initializing));

        PrinterHelper.PrinterHelperCallback callback = new PrinterHelper.PrinterHelperCallback() {
            @Override
            public void OnStatus(String message, int color) {
                requireActivity().runOnUiThread(() -> {
                    // Only update main status if operation is not complete
                    if (!isOperationComplete) {
                        setStatus(message, color);
                    }
                    updateStatusDetails(message);
                });
            }

            @Override
            public void onSuccess() {
                isOperationComplete = true;
                requireActivity().runOnUiThread(() -> {
                    setStatus(getString(R.string.status_factory_reset_completed),
                        requireContext().getColor(android.R.color.holo_green_dark));
                    setStatusDetails(getString(R.string.status_factory_reset_success_details));
                    buttonPerformFactoryReset.setEnabled(true);
                });
            }
        };

        // Call the printer helper method
        printerHelper.performFactoryResetPrinterUSB(requireContext(), serialNumber, callback);
    }

    private void setStatus(String message, int color) {
        textViewStatus.setText(message);
        textViewStatus.setTextColor(color);
    }

    private void setStatusDetails(String details) {
        textViewStatusDetails.setText(details);
        textViewStatusDetails.setVisibility(View.VISIBLE);
    }

    private void updateStatusDetails(String message) {
        // Build detailed status based on the operation phase
        String currentDetails = textViewStatusDetails.getText().toString();
        String timestamp = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
        String newDetails = timestamp + ": " + message;

        if (!currentDetails.isEmpty() && !currentDetails.equals(getString(R.string.status_factory_reset_initializing))) {
            newDetails = currentDetails + "\n" + newDetails;
        }

        // Limit to last 10 lines
        String[] lines = newDetails.split("\n");
        if (lines.length > 10) {
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - 10; i < lines.length; i++) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(lines[i]);
            }
            newDetails = sb.toString();
        }

        setStatusDetails(newDetails);
    }

    private void setupBarcodeResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                BarcodeScannerFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String barcode = result.getString(BarcodeScannerFragment.RESULT_BARCODE);
                    int targetField = result.getInt(BarcodeScannerFragment.RESULT_TARGET_FIELD);

                    if (barcode != null && targetField == FIELD_SERIAL_NUMBER) {
                        editTextSerialNumber.setText(barcode);
                    }

                    showMainContent();
                });

        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                showMainContent();
            }
        });
    }

    private void showMainContent() {
        fragmentContainer.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    private void openBarcodeScanner(int targetField) {
        fragmentContainer.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        BarcodeScannerFragment scannerFragment = BarcodeScannerFragment.newInstance(targetField);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scannerFragment)
                .addToBackStack("scanner")
                .commit();
    }

    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    // ToolbarConfigurable implementation
    @Override
    public int getToolbarTitleResId() {
        return R.string.title_factory_reset;
    }

    @Override
    public boolean showBackButton() {
        return true;
    }
}
