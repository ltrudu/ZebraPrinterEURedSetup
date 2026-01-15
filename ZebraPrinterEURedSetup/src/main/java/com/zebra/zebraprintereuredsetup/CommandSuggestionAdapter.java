package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandSuggestionAdapter extends RecyclerView.Adapter<CommandSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(CommandSuggestion suggestion);
    }

    private final Context context;
    private final List<CommandSuggestion> allSuggestions = new ArrayList<>();
    private final List<CommandSuggestion> filteredSuggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;
    private static final int MAX_SUGGESTIONS = 8;

    public CommandSuggestionAdapter(Context context) {
        this.context = context;
    }

    public void setSuggestions(List<CommandSuggestion> suggestions) {
        allSuggestions.clear();
        allSuggestions.addAll(suggestions);
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void filter(String query) {
        filteredSuggestions.clear();
        Set<String> addedCommands = new HashSet<>();

        if (query != null && query.length() >= 1) {
            int count = 0;
            for (CommandSuggestion suggestion : allSuggestions) {
                String key = suggestion.getCommand();
                if (suggestion.matchesQuery(query) && !addedCommands.contains(key) && count < MAX_SUGGESTIONS) {
                    filteredSuggestions.add(suggestion);
                    addedCommands.add(key);
                    count++;
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setSpecialSuggestions(List<CommandSuggestion> suggestions) {
        filteredSuggestions.clear();
        filteredSuggestions.addAll(suggestions);
        notifyDataSetChanged();
    }

    public void filterSgdOnly(String query) {
        filteredSuggestions.clear();
        Set<String> addedCommands = new HashSet<>();

        if (query != null && query.length() >= 1) {
            int count = 0;
            for (CommandSuggestion suggestion : allSuggestions) {
                String key = suggestion.getCommand();
                if ("SGD".equalsIgnoreCase(suggestion.getType()) &&
                    suggestion.matchesQuery(query) &&
                    !addedCommands.contains(key) &&
                    count < MAX_SUGGESTIONS) {
                    filteredSuggestions.add(suggestion);
                    addedCommands.add(key);
                    count++;
                }
            }
        }

        notifyDataSetChanged();
    }

    public boolean hasSuggestions() {
        return !filteredSuggestions.isEmpty();
    }

    public int getSuggestionsCount() {
        return filteredSuggestions.size();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        CommandSuggestion suggestion = filteredSuggestions.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return filteredSuggestions.size();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewType;
        private final TextView textViewCommand;
        private final TextView textViewFormat;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewCommand = itemView.findViewById(R.id.textViewCommand);
            textViewFormat = itemView.findViewById(R.id.textViewFormat);
        }

        void bind(CommandSuggestion suggestion) {
            textViewType.setText(suggestion.getType());
            setBadgeColor(suggestion.getType());

            textViewCommand.setText(suggestion.getCommand());

            String format = suggestion.getFormat();
            if (format != null && !format.isEmpty() && !format.equals(suggestion.getCommand())) {
                textViewFormat.setVisibility(View.VISIBLE);
                textViewFormat.setText(format);
            } else {
                textViewFormat.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuggestionClick(suggestion);
                }
            });
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
            GradientDrawable background = (GradientDrawable) textViewType.getBackground();
            background.setColor(ContextCompat.getColor(context, colorRes));
        }
    }
}
