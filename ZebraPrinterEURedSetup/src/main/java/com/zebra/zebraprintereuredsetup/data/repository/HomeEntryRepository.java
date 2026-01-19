package com.zebra.zebraprintereuredsetup.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.zebra.zebraprintereuredsetup.SettingsHelper;
import com.zebra.zebraprintereuredsetup.data.AppDatabase;
import com.zebra.zebraprintereuredsetup.data.dao.HomeEntryDao;
import com.zebra.zebraprintereuredsetup.data.entity.HomeEntry;

import java.util.List;
import java.util.UUID;

/**
 * Repository for HomeEntry operations.
 * Provides a clean API for the UI to interact with the database.
 */
public class HomeEntryRepository {

    private final HomeEntryDao homeEntryDao;
    private final Context context;

    // Cached LiveData
    private final LiveData<List<HomeEntry>> allEntriesLive;
    private final LiveData<List<HomeEntry>> systemEntriesLive;
    private final LiveData<List<HomeEntry>> customEntriesLive;

    public HomeEntryRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.context);
        this.homeEntryDao = database.homeEntryDao();

        this.allEntriesLive = homeEntryDao.getAllEntriesLive();
        this.systemEntriesLive = homeEntryDao.getSystemEntriesLive();
        this.customEntriesLive = homeEntryDao.getCustomEntriesLive();
    }

    // ==================== Query Methods ====================

    /**
     * Get visible entries filtered by advanced mode setting.
     * This is the main query for the home screen.
     */
    public LiveData<List<HomeEntry>> getVisibleHomeEntries() {
        boolean advancedMode = SettingsHelper.getAdvancedModeEnabled(context);
        return homeEntryDao.getVisibleEntriesWithAdvancedFilter(advancedMode);
    }

    /**
     * Get visible entries with specified advanced mode setting.
     */
    public LiveData<List<HomeEntry>> getVisibleHomeEntries(boolean advancedModeEnabled) {
        return homeEntryDao.getVisibleEntriesWithAdvancedFilter(advancedModeEnabled);
    }

    /**
     * Get visible entries (synchronous) - use on background thread
     */
    public List<HomeEntry> getVisibleHomeEntriesSync() {
        boolean advancedMode = SettingsHelper.getAdvancedModeEnabled(context);
        return homeEntryDao.getVisibleEntriesWithAdvancedFilterSync(advancedMode);
    }

    /**
     * Get all entries (for management/settings screen)
     */
    public LiveData<List<HomeEntry>> getAllEntries() {
        return allEntriesLive;
    }

    /**
     * Get system entries only
     */
    public LiveData<List<HomeEntry>> getSystemEntries() {
        return systemEntriesLive;
    }

    /**
     * Get custom entries only
     */
    public LiveData<List<HomeEntry>> getCustomEntries() {
        return customEntriesLive;
    }

    /**
     * Get entry by ID
     */
    public LiveData<HomeEntry> getEntryById(String id) {
        return homeEntryDao.getEntryByIdLive(id);
    }

    /**
     * Get entry by ID (synchronous - use on background thread)
     */
    public HomeEntry getEntryByIdSync(String id) {
        return homeEntryDao.getEntryById(id);
    }

    /**
     * Get hidden system entries (for "Add Library Script" dialog)
     */
    public LiveData<List<HomeEntry>> getHiddenSystemEntries() {
        return homeEntryDao.getHiddenSystemEntriesLive();
    }

    /**
     * Get hidden system entries (synchronous - use on background thread)
     */
    public List<HomeEntry> getHiddenSystemEntriesSync() {
        boolean advancedMode = SettingsHelper.getAdvancedModeEnabled(context);
        return homeEntryDao.getHiddenSystemEntriesWithAdvancedFilter(advancedMode);
    }

    /**
     * Get hidden custom entries (for "Show Hidden Scripts" dialog)
     */
    public LiveData<List<HomeEntry>> getHiddenCustomEntries() {
        return homeEntryDao.getHiddenCustomEntriesLive();
    }

    /**
     * Get hidden custom entries (synchronous - use on background thread)
     */
    public List<HomeEntry> getHiddenCustomEntriesSync() {
        return homeEntryDao.getHiddenCustomEntries();
    }

    // ==================== Visibility Operations ====================

    /**
     * Toggle visibility of an entry.
     * System entries can be hidden but not deleted.
     */
    public void toggleVisibility(String entryId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            HomeEntry entry = homeEntryDao.getEntryById(entryId);
            if (entry != null) {
                homeEntryDao.updateVisibility(
                        entryId,
                        !entry.isVisible(),
                        System.currentTimeMillis()
                );
            }
        });
    }

    /**
     * Hide an entry.
     */
    public void hideEntry(String entryId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            homeEntryDao.updateVisibility(entryId, false, System.currentTimeMillis());
        });
    }

    /**
     * Show an entry.
     */
    public void showEntry(String entryId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            homeEntryDao.updateVisibility(entryId, true, System.currentTimeMillis());
        });
    }

    /**
     * Show all system entries (reset visibility)
     */
    public void showAllSystemEntries() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<HomeEntry> systemEntries = homeEntryDao.getSystemEntries();
            for (HomeEntry entry : systemEntries) {
                homeEntryDao.updateVisibility(entry.getId(), true, System.currentTimeMillis());
            }
        });
    }

    // ==================== Order/Position Operations ====================

    /**
     * Update the order positions of entries.
     * @param orderedIds List of entry IDs in the desired order
     */
    public void updateOrder(List<String> orderedIds) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            for (int i = 0; i < orderedIds.size(); i++) {
                homeEntryDao.updateOrderPosition(orderedIds.get(i), i, now);
            }
        });
    }

    /**
     * Move an entry to a new position
     */
    public void moveEntry(String entryId, int newPosition) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            homeEntryDao.updateOrderPosition(entryId, newPosition, System.currentTimeMillis());
        });
    }

    // ==================== Custom Entry Operations ====================

    /**
     * Create a new custom entry.
     * @return The ID of the created entry
     */
    public String createCustomEntry(String title, String description, String scriptContent) {
        String id = "custom_" + UUID.randomUUID().toString();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int maxPosition = homeEntryDao.getMaxOrderPosition();

            HomeEntry entry = new HomeEntry(
                    id,
                    "",
                    "",
                    "ic_custom_script",
                    null,
                    HomeEntry.TYPE_CUSTOM,
                    true,
                    maxPosition + 1,
                    false
            );
            entry.setTitleCustom(title);
            entry.setDescriptionCustom(description);
            entry.setCustomScriptContent(scriptContent);

            homeEntryDao.insert(entry);
        });

        return id;
    }

    /**
     * Update a custom entry.
     * Only works for custom entries (type = TYPE_CUSTOM).
     */
    public void updateCustomEntry(String id, String title, String description, String scriptContent) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            HomeEntry entry = homeEntryDao.getEntryById(id);
            if (entry != null && entry.isCustomEntry()) {
                homeEntryDao.updateCustomEntry(
                        id, title, description, "ic_custom_script", scriptContent, System.currentTimeMillis()
                );
            }
        });
    }

    /**
     * Delete a custom entry.
     * Only custom entries can be deleted; system entries can only be hidden.
     */
    public void deleteEntry(String id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            homeEntryDao.deleteCustomEntry(id);
        });
    }

    /**
     * Delete all custom entries.
     */
    public void deleteAllCustomEntries() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            homeEntryDao.deleteAllCustomEntries();
        });
    }

    // ==================== Utility Methods ====================

    /**
     * Check if an entry can be deleted (only custom entries can be deleted)
     */
    public boolean canDelete(HomeEntry entry) {
        return entry != null && entry.isCustomEntry();
    }

    /**
     * Check if the Advanced entry should be shown based on current settings
     */
    public boolean shouldShowAdvancedEntry() {
        return SettingsHelper.getAdvancedModeEnabled(context);
    }
}
