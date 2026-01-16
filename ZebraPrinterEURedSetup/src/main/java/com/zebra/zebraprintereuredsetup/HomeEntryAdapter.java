package com.zebra.zebraprintereuredsetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.zebra.zebraprintereuredsetup.data.entity.HomeEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecyclerView adapter for displaying home screen entries.
 */
public class HomeEntryAdapter extends RecyclerView.Adapter<HomeEntryAdapter.ViewHolder> {

    private List<HomeEntry> entries = new ArrayList<>();
    private final Context context;
    private OnEntryClickListener clickListener;
    private OnEntryLongClickListener longClickListener;
    private OnDragStartListener dragStartListener;
    private boolean editModeEnabled = false;

    public interface OnEntryClickListener {
        void onEntryClick(HomeEntry entry);
    }

    public interface OnEntryLongClickListener {
        boolean onEntryLongClick(View view, HomeEntry entry);
    }

    public interface OnDragStartListener {
        void onDragStart(RecyclerView.ViewHolder viewHolder);
    }

    public HomeEntryAdapter(Context context) {
        this.context = context;
    }

    public void setEditModeEnabled(boolean enabled) {
        if (this.editModeEnabled != enabled) {
            this.editModeEnabled = enabled;
            notifyDataSetChanged();
        }
    }

    public void setOnDragStartListener(OnDragStartListener listener) {
        this.dragStartListener = listener;
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnEntryLongClickListener(OnEntryLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setEntries(List<HomeEntry> newEntries) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return entries.size();
            }

            @Override
            public int getNewListSize() {
                return newEntries.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return entries.get(oldItemPosition).getId().equals(newEntries.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                HomeEntry oldEntry = entries.get(oldItemPosition);
                HomeEntry newEntry = newEntries.get(newItemPosition);
                return oldEntry.equals(newEntry);
            }
        });

        this.entries = new ArrayList<>(newEntries);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<HomeEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Move an item from one position to another.
     * @param fromPosition The position to move from
     * @param toPosition The position to move to
     */
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(entries, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(entries, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Get entry at specific position.
     * @param position The position
     * @return The HomeEntry at that position, or null if invalid
     */
    public HomeEntry getEntryAt(int position) {
        if (position >= 0 && position < entries.size()) {
            return entries.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardEntry;
        private final ImageView imageViewDragHandle;
        private final ImageView imageViewIcon;
        private final MaterialTextView textViewTitle;
        private final MaterialTextView textViewDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEntry = itemView.findViewById(R.id.cardEntry);
            imageViewDragHandle = itemView.findViewById(R.id.imageViewDragHandle);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }

        @SuppressLint("ClickableViewAccessibility")
        void bind(HomeEntry entry) {
            // Set title
            textViewTitle.setText(entry.getDisplayTitle(context));

            // Set description
            textViewDescription.setText(entry.getDisplayDescription(context));

            // Set icon
            int iconResId = entry.getIconResId(context);
            if (iconResId != 0) {
                imageViewIcon.setImageResource(iconResId);
            } else {
                // Default icon for custom entries
                imageViewIcon.setImageResource(R.drawable.ic_custom_script);
            }

            // Apply tint for icons that need it (not for EU Red which has its own colors)
            String iconResName = entry.getIconResName();
            boolean shouldTint = !"ic_eu_red".equals(iconResName);
            if (shouldTint) {
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                int primaryColor = typedValue.data;
                ImageViewCompat.setImageTintList(imageViewIcon, ColorStateList.valueOf(primaryColor));
            } else {
                // Remove tint to show original icon colors
                ImageViewCompat.setImageTintList(imageViewIcon, null);
            }

            // Show/hide drag handle based on edit mode
            imageViewDragHandle.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);

            // Drag handle touch listener
            imageViewDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (dragStartListener != null) {
                        dragStartListener.onDragStart(ViewHolder.this);
                    }
                }
                return false;
            });

            // Click listener
            cardEntry.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onEntryClick(entry);
                }
            });

            // Long click listener
            cardEntry.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onEntryLongClick(v, entry);
                }
                return false;
            });
        }
    }
}
