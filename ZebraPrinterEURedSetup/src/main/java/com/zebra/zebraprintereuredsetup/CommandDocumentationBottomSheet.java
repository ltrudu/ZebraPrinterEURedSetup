package com.zebra.zebraprintereuredsetup;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

/**
 * Bottom sheet dialog fragment for displaying command documentation.
 * Used when user clicks on a highlighted command in the script editor.
 */
public class CommandDocumentationBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_COMMAND = "command";
    private static final String ARG_NAME = "name";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_FORMAT = "format";
    private static final String ARG_TYPE = "type";
    private static final String ARG_PAGE = "page";
    private static final String ARG_PARAMETERS = "parameters";
    private static final String ARG_RETURNS = "returns";
    private static final String ARG_ACTIONS = "actions";
    private static final String ARG_VALUES = "values";
    private static final String ARG_DEFAULT = "default";

    private TextView textViewTypeBadge;
    private TextView textViewCommand;
    private TextView textViewName;
    private TextView textViewFormat;
    private TextView textViewDescription;
    private TextView textViewParameters;
    private TextView textViewReturns;
    private TextView textViewActions;
    private TextView textViewValues;
    private TextView textViewDefault;
    private TextView textViewPage;

    private LinearLayout layoutParameters;
    private LinearLayout layoutReturns;
    private LinearLayout layoutActions;
    private LinearLayout layoutValues;
    private LinearLayout layoutDefault;

    /**
     * Create a new instance with documentation command data.
     */
    public static CommandDocumentationBottomSheet newInstance(DocumentationCommand command) {
        CommandDocumentationBottomSheet fragment = new CommandDocumentationBottomSheet();
        Bundle args = new Bundle();

        if (command != null) {
            args.putString(ARG_COMMAND, command.getCommand());
            args.putString(ARG_NAME, command.getName());
            args.putString(ARG_DESCRIPTION, command.getDescription());
            args.putString(ARG_FORMAT, command.getFormat());
            args.putString(ARG_TYPE, command.getTypeString());
            args.putInt(ARG_PAGE, command.getPage());

            // Type-specific fields
            if (command.getType() == DocumentationCommand.CommandType.ZPL) {
                args.putString(ARG_PARAMETERS, command.getParametersDisplayString());
            } else if (command.getType() == DocumentationCommand.CommandType.ZBI) {
                args.putString(ARG_RETURNS, command.getReturns());
            } else if (command.getType() == DocumentationCommand.CommandType.SGD) {
                args.putString(ARG_ACTIONS, command.getActionsDisplayString());
                args.putString(ARG_VALUES, command.getValuesDisplayString());
                args.putString(ARG_DEFAULT, command.getDefaultValue());
            }
        }

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create a new instance with basic command info (for commands not in documentation).
     */
    public static CommandDocumentationBottomSheet newInstance(String command, String type) {
        CommandDocumentationBottomSheet fragment = new CommandDocumentationBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_COMMAND, command);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_NAME, "Command not found in documentation");
        args.putString(ARG_DESCRIPTION, "This command was not found in the loaded documentation database.");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_ZebraPrinterEURedSetup_BottomSheet);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_command_documentation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        textViewTypeBadge = view.findViewById(R.id.textViewTypeBadge);
        textViewCommand = view.findViewById(R.id.textViewCommand);
        textViewName = view.findViewById(R.id.textViewName);
        textViewFormat = view.findViewById(R.id.textViewFormat);
        textViewDescription = view.findViewById(R.id.textViewDescription);
        textViewParameters = view.findViewById(R.id.textViewParameters);
        textViewReturns = view.findViewById(R.id.textViewReturns);
        textViewActions = view.findViewById(R.id.textViewActions);
        textViewValues = view.findViewById(R.id.textViewValues);
        textViewDefault = view.findViewById(R.id.textViewDefault);
        textViewPage = view.findViewById(R.id.textViewPage);

        layoutParameters = view.findViewById(R.id.layoutParameters);
        layoutReturns = view.findViewById(R.id.layoutReturns);
        layoutActions = view.findViewById(R.id.layoutActions);
        layoutValues = view.findViewById(R.id.layoutValues);
        layoutDefault = view.findViewById(R.id.layoutDefault);

        MaterialButton buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(v -> dismiss());

        // Populate data from arguments
        Bundle args = getArguments();
        if (args != null) {
            populateData(args);
        }

        // Configure bottom sheet behavior
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.getBehavior().setSkipCollapsed(true);
        }
    }

    private void populateData(Bundle args) {
        String command = args.getString(ARG_COMMAND, "");
        String name = args.getString(ARG_NAME, "");
        String description = args.getString(ARG_DESCRIPTION, "");
        String format = args.getString(ARG_FORMAT, "");
        String type = args.getString(ARG_TYPE, "ZPL");
        int page = args.getInt(ARG_PAGE, 0);

        View rootView = getView();
        if (rootView == null) return;

        // Set common fields
        textViewTypeBadge.setText(type);
        setBadgeColor(type);
        textViewCommand.setText(command);
        textViewName.setText(name.isEmpty() ? command : name);

        if (!format.isEmpty()) {
            textViewFormat.setText(format);
            textViewFormat.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.labelFormat).setVisibility(View.VISIBLE);
        } else {
            textViewFormat.setVisibility(View.GONE);
            rootView.findViewById(R.id.labelFormat).setVisibility(View.GONE);
        }

        if (!description.isEmpty()) {
            textViewDescription.setText(description);
            textViewDescription.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.labelDescription).setVisibility(View.VISIBLE);
        } else {
            textViewDescription.setVisibility(View.GONE);
            rootView.findViewById(R.id.labelDescription).setVisibility(View.GONE);
        }

        // Set type-specific fields
        switch (type.toUpperCase()) {
            case "ZPL":
                showZplFields(args);
                break;
            case "ZBI":
                showZbiFields(args);
                break;
            case "SGD":
                showSgdFields(args);
                break;
        }

        // Page reference
        if (page > 0) {
            textViewPage.setText(getString(R.string.label_page_number, page));
            textViewPage.setVisibility(View.VISIBLE);
        } else {
            textViewPage.setVisibility(View.GONE);
        }
    }

    private void showZplFields(Bundle args) {
        layoutReturns.setVisibility(View.GONE);
        layoutActions.setVisibility(View.GONE);
        layoutValues.setVisibility(View.GONE);
        layoutDefault.setVisibility(View.GONE);

        String parameters = args.getString(ARG_PARAMETERS, "");
        if (!parameters.isEmpty()) {
            textViewParameters.setText(parameters);
            layoutParameters.setVisibility(View.VISIBLE);
        } else {
            layoutParameters.setVisibility(View.GONE);
        }
    }

    private void showZbiFields(Bundle args) {
        layoutParameters.setVisibility(View.GONE);
        layoutActions.setVisibility(View.GONE);
        layoutValues.setVisibility(View.GONE);
        layoutDefault.setVisibility(View.GONE);

        String returns = args.getString(ARG_RETURNS, "");
        if (!returns.isEmpty() && !returns.equals("N/A")) {
            textViewReturns.setText(returns);
            layoutReturns.setVisibility(View.VISIBLE);
        } else {
            layoutReturns.setVisibility(View.GONE);
        }
    }

    private void showSgdFields(Bundle args) {
        layoutParameters.setVisibility(View.GONE);
        layoutReturns.setVisibility(View.GONE);

        String actions = args.getString(ARG_ACTIONS, "");
        if (!actions.isEmpty()) {
            textViewActions.setText(actions);
            layoutActions.setVisibility(View.VISIBLE);
        } else {
            layoutActions.setVisibility(View.GONE);
        }

        String values = args.getString(ARG_VALUES, "");
        if (!values.isEmpty()) {
            textViewValues.setText(values);
            layoutValues.setVisibility(View.VISIBLE);
        } else {
            layoutValues.setVisibility(View.GONE);
        }

        String defaultValue = args.getString(ARG_DEFAULT, "");
        if (!defaultValue.isEmpty() && !defaultValue.equals("NA")) {
            textViewDefault.setText(defaultValue);
            layoutDefault.setVisibility(View.VISIBLE);
        } else {
            layoutDefault.setVisibility(View.GONE);
        }
    }

    private void setBadgeColor(String type) {
        int colorRes;
        switch (type.toUpperCase()) {
            case "ZPL":
                colorRes = R.color.badge_zpl;
                break;
            case "SGD":
                colorRes = R.color.badge_sgd;
                break;
            case "ZBI":
                colorRes = R.color.badge_zbi;
                break;
            default:
                colorRes = R.color.badge_default;
        }
        GradientDrawable background = (GradientDrawable) textViewTypeBadge.getBackground();
        background.setColor(ContextCompat.getColor(requireContext(), colorRes));
    }

}
