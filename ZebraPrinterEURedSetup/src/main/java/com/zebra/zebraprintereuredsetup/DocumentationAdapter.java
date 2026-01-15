package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class DocumentationAdapter extends RecyclerView.Adapter<DocumentationAdapter.CommandViewHolder> {

    private final List<DocumentationCommand> commands = new ArrayList<>();
    private final List<DocumentationCommand> filteredCommands = new ArrayList<>();
    private final Context context;

    // Filter states
    private boolean showZpl = true;
    private boolean showSgd = true;
    private boolean showZbi = true;
    private String searchQuery = "";

    // Track expanded items
    private int expandedPosition = -1;

    public DocumentationAdapter(Context context) {
        this.context = context;
    }

    public void setCommands(List<DocumentationCommand> newCommands) {
        commands.clear();
        commands.addAll(newCommands);
        applyFilters();
    }

    public void setFilters(boolean zpl, boolean sgd, boolean zbi) {
        this.showZpl = zpl;
        this.showSgd = sgd;
        this.showZbi = zbi;
        applyFilters();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
        applyFilters();
    }

    private void applyFilters() {
        filteredCommands.clear();
        expandedPosition = -1;

        for (DocumentationCommand cmd : commands) {
            // Check type filter
            boolean typeMatches = false;
            switch (cmd.getType()) {
                case ZPL:
                    typeMatches = showZpl;
                    break;
                case SGD:
                    typeMatches = showSgd;
                    break;
                case ZBI:
                    typeMatches = showZbi;
                    break;
            }

            if (typeMatches && cmd.matchesSearch(searchQuery)) {
                filteredCommands.add(cmd);
            }
        }

        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredCommands.size();
    }

    @NonNull
    @Override
    public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_documentation_command, parent, false);
        return new CommandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
        DocumentationCommand command = filteredCommands.get(position);
        boolean isExpanded = position == expandedPosition;

        holder.bind(command, isExpanded);

        holder.cardCommand.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            int previousExpanded = expandedPosition;
            if (expandedPosition == adapterPosition) {
                // Collapse
                expandedPosition = -1;
                notifyItemChanged(adapterPosition);
            } else {
                // Expand new, collapse old
                expandedPosition = adapterPosition;
                notifyItemChanged(adapterPosition);
                if (previousExpanded != -1) {
                    notifyItemChanged(previousExpanded);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredCommands.size();
    }

    class CommandViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCommand;
        TextView textViewTypeBadge;
        TextView textViewCommand;
        ImageView imageViewExpand;
        TextView textViewName;
        LinearLayout layoutExpandedContent;
        TextView textViewFormat;
        TextView textViewDescription;
        LinearLayout layoutParameters;
        TextView textViewParameters;
        LinearLayout layoutReturns;
        TextView textViewReturns;
        LinearLayout layoutActions;
        TextView textViewActions;
        LinearLayout layoutValues;
        TextView textViewValues;
        LinearLayout layoutDefault;
        TextView textViewDefault;
        TextView textViewPage;

        CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCommand = itemView.findViewById(R.id.cardCommand);
            textViewTypeBadge = itemView.findViewById(R.id.textViewTypeBadge);
            textViewCommand = itemView.findViewById(R.id.textViewCommand);
            imageViewExpand = itemView.findViewById(R.id.imageViewExpand);
            textViewName = itemView.findViewById(R.id.textViewName);
            layoutExpandedContent = itemView.findViewById(R.id.layoutExpandedContent);
            textViewFormat = itemView.findViewById(R.id.textViewFormat);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            layoutParameters = itemView.findViewById(R.id.layoutParameters);
            textViewParameters = itemView.findViewById(R.id.textViewParameters);
            layoutReturns = itemView.findViewById(R.id.layoutReturns);
            textViewReturns = itemView.findViewById(R.id.textViewReturns);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            textViewActions = itemView.findViewById(R.id.textViewActions);
            layoutValues = itemView.findViewById(R.id.layoutValues);
            textViewValues = itemView.findViewById(R.id.textViewValues);
            layoutDefault = itemView.findViewById(R.id.layoutDefault);
            textViewDefault = itemView.findViewById(R.id.textViewDefault);
            textViewPage = itemView.findViewById(R.id.textViewPage);
        }

        void bind(DocumentationCommand command, boolean isExpanded) {
            // Set type badge
            textViewTypeBadge.setText(command.getTypeString());
            setBadgeColor(command.getType());

            // Set command name
            textViewCommand.setText(command.getCommand());

            // Set name/short description
            String displayName = command.getName();
            if (displayName.equals(command.getCommand())) {
                // If name equals command, show description instead
                String desc = command.getDescription();
                if (desc != null && desc.length() > 100) {
                    desc = desc.substring(0, 100) + "...";
                }
                displayName = desc;
            }
            textViewName.setText(displayName);

            // Expand/collapse state
            imageViewExpand.setRotation(isExpanded ? 180 : 0);
            layoutExpandedContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                bindExpandedContent(command);
            }
        }

        private void setBadgeColor(DocumentationCommand.CommandType type) {
            int colorRes;
            switch (type) {
                case ZPL:
                    colorRes = R.color.badge_zpl;
                    break;
                case SGD:
                    colorRes = R.color.badge_sgd;
                    break;
                case ZBI:
                    colorRes = R.color.badge_zbi;
                    break;
                default:
                    colorRes = R.color.badge_default;
            }
            GradientDrawable background = (GradientDrawable) textViewTypeBadge.getBackground();
            background.setColor(ContextCompat.getColor(context, colorRes));
        }

        private void bindExpandedContent(DocumentationCommand command) {
            // Format
            textViewFormat.setText(command.getFormat());

            // Description
            textViewDescription.setText(command.getDescription());

            // Type-specific fields
            layoutParameters.setVisibility(View.GONE);
            layoutReturns.setVisibility(View.GONE);
            layoutActions.setVisibility(View.GONE);
            layoutValues.setVisibility(View.GONE);
            layoutDefault.setVisibility(View.GONE);

            switch (command.getType()) {
                case ZPL:
                    bindZplFields(command);
                    break;
                case ZBI:
                    bindZbiFields(command);
                    break;
                case SGD:
                    bindSgdFields(command);
                    break;
            }

            // Page reference
            textViewPage.setText(context.getString(R.string.label_page_number, command.getPage()));
        }

        private void bindZplFields(DocumentationCommand command) {
            String params = command.getParametersDisplayString();
            if (!params.isEmpty()) {
                layoutParameters.setVisibility(View.VISIBLE);
                textViewParameters.setText(params);
            }
        }

        private void bindZbiFields(DocumentationCommand command) {
            String returns = command.getReturns();
            if (returns != null && !returns.isEmpty() && !returns.equals("N/A")) {
                layoutReturns.setVisibility(View.VISIBLE);
                textViewReturns.setText(returns);
            }
        }

        private void bindSgdFields(DocumentationCommand command) {
            String actions = command.getActionsDisplayString();
            if (!actions.isEmpty()) {
                layoutActions.setVisibility(View.VISIBLE);
                textViewActions.setText(actions);
            }

            String values = command.getValuesDisplayString();
            if (!values.isEmpty()) {
                layoutValues.setVisibility(View.VISIBLE);
                textViewValues.setText(values);
            }

            String defaultVal = command.getDefaultValue();
            if (defaultVal != null && !defaultVal.isEmpty() && !defaultVal.equals("NA")) {
                layoutDefault.setVisibility(View.VISIBLE);
                textViewDefault.setText(defaultVal);
            }
        }
    }
}
