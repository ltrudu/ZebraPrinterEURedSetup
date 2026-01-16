package com.zebra.zebraprintereuredsetup.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import android.content.Context;

/**
 * Entity representing a home screen entry.
 * Both system entries (pre-defined) and custom entries (user-created) are stored in this table.
 */
@Entity(tableName = "home_entries")
public class HomeEntry {

    /**
     * Entry types
     */
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_CUSTOM = 1;

    /**
     * System entry IDs (stable identifiers for system entries)
     */
    public static final String SYSTEM_ID_EURED_SETUP = "system_eured_setup";
    public static final String SYSTEM_ID_CUSTOM_SCRIPT = "system_custom_script";
    public static final String SYSTEM_ID_DOCUMENTATION = "system_documentation";
    public static final String SYSTEM_ID_ADVANCED = "system_advanced";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @NonNull
    @ColumnInfo(name = "title_res_name")
    private String titleResName;

    @ColumnInfo(name = "title_custom")
    private String titleCustom;

    @NonNull
    @ColumnInfo(name = "description_res_name")
    private String descriptionResName;

    @ColumnInfo(name = "description_custom")
    private String descriptionCustom;

    @NonNull
    @ColumnInfo(name = "icon_res_name")
    private String iconResName;

    @ColumnInfo(name = "navigation_target")
    private String navigationTarget;

    @ColumnInfo(name = "entry_type")
    private int entryType;

    @ColumnInfo(name = "is_visible")
    private boolean isVisible;

    @ColumnInfo(name = "order_position")
    private int orderPosition;

    @ColumnInfo(name = "requires_advanced_mode")
    private boolean requiresAdvancedMode;

    @ColumnInfo(name = "custom_script_content")
    private String customScriptContent;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructor
    public HomeEntry(@NonNull String id, @NonNull String titleResName,
                     @NonNull String descriptionResName, @NonNull String iconResName,
                     String navigationTarget, int entryType, boolean isVisible,
                     int orderPosition, boolean requiresAdvancedMode) {
        this.id = id;
        this.titleResName = titleResName;
        this.descriptionResName = descriptionResName;
        this.iconResName = iconResName;
        this.navigationTarget = navigationTarget;
        this.entryType = entryType;
        this.isVisible = isVisible;
        this.orderPosition = orderPosition;
        this.requiresAdvancedMode = requiresAdvancedMode;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getTitleResName() { return titleResName; }
    public void setTitleResName(@NonNull String titleResName) { this.titleResName = titleResName; }

    public String getTitleCustom() { return titleCustom; }
    public void setTitleCustom(String titleCustom) { this.titleCustom = titleCustom; }

    @NonNull
    public String getDescriptionResName() { return descriptionResName; }
    public void setDescriptionResName(@NonNull String descriptionResName) {
        this.descriptionResName = descriptionResName;
    }

    public String getDescriptionCustom() { return descriptionCustom; }
    public void setDescriptionCustom(String descriptionCustom) {
        this.descriptionCustom = descriptionCustom;
    }

    @NonNull
    public String getIconResName() { return iconResName; }
    public void setIconResName(@NonNull String iconResName) { this.iconResName = iconResName; }

    public String getNavigationTarget() { return navigationTarget; }
    public void setNavigationTarget(String navigationTarget) {
        this.navigationTarget = navigationTarget;
    }

    public int getEntryType() { return entryType; }
    public void setEntryType(int entryType) { this.entryType = entryType; }

    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }

    public int getOrderPosition() { return orderPosition; }
    public void setOrderPosition(int orderPosition) { this.orderPosition = orderPosition; }

    public boolean isRequiresAdvancedMode() { return requiresAdvancedMode; }
    public void setRequiresAdvancedMode(boolean requiresAdvancedMode) {
        this.requiresAdvancedMode = requiresAdvancedMode;
    }

    public String getCustomScriptContent() { return customScriptContent; }
    public void setCustomScriptContent(String customScriptContent) {
        this.customScriptContent = customScriptContent;
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isSystemEntry() {
        return entryType == TYPE_SYSTEM;
    }

    public boolean isCustomEntry() {
        return entryType == TYPE_CUSTOM;
    }

    /**
     * Get display title - returns custom title for custom entries,
     * or the localized string for system entries
     */
    public String getDisplayTitle(Context context) {
        if (isCustomEntry() && titleCustom != null && !titleCustom.isEmpty()) {
            return titleCustom;
        }
        int resId = context.getResources().getIdentifier(
                titleResName, "string", context.getPackageName()
        );
        return resId != 0 ? context.getString(resId) : titleResName;
    }

    /**
     * Get display description - returns custom description for custom entries,
     * or the localized string for system entries
     */
    public String getDisplayDescription(Context context) {
        if (isCustomEntry() && descriptionCustom != null && !descriptionCustom.isEmpty()) {
            return descriptionCustom;
        }
        int resId = context.getResources().getIdentifier(
                descriptionResName, "string", context.getPackageName()
        );
        return resId != 0 ? context.getString(resId) : descriptionResName;
    }

    /**
     * Get icon drawable resource ID
     */
    public int getIconResId(Context context) {
        int resId = context.getResources().getIdentifier(
                iconResName, "drawable", context.getPackageName()
        );
        return resId != 0 ? resId : android.R.drawable.ic_menu_help;
    }

    /**
     * Get navigation target resource ID
     */
    public int getNavigationResId(Context context) {
        if (navigationTarget == null || navigationTarget.isEmpty()) {
            return 0;
        }
        return context.getResources().getIdentifier(
                navigationTarget, "id", context.getPackageName()
        );
    }
}
