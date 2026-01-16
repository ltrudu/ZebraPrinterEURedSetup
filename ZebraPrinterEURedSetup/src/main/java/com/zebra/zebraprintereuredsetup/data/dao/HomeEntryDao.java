package com.zebra.zebraprintereuredsetup.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.zebra.zebraprintereuredsetup.data.entity.HomeEntry;

import java.util.List;

/**
 * Data Access Object for HomeEntry operations.
 */
@Dao
public interface HomeEntryDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(HomeEntry entry);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<HomeEntry> entries);

    // ==================== Update Operations ====================

    @Update
    void update(HomeEntry entry);

    @Query("UPDATE home_entries SET is_visible = :isVisible, updated_at = :updatedAt WHERE id = :id")
    void updateVisibility(String id, boolean isVisible, long updatedAt);

    @Query("UPDATE home_entries SET order_position = :position, updated_at = :updatedAt WHERE id = :id")
    void updateOrderPosition(String id, int position, long updatedAt);

    @Query("UPDATE home_entries SET title_custom = :title, description_custom = :description, " +
            "icon_res_name = :iconResName, custom_script_content = :scriptContent, updated_at = :updatedAt WHERE id = :id")
    void updateCustomEntry(String id, String title, String description,
                           String iconResName, String scriptContent, long updatedAt);

    // ==================== Delete Operations ====================

    @Delete
    void delete(HomeEntry entry);

    @Query("DELETE FROM home_entries WHERE id = :id AND entry_type = 1")
    void deleteCustomEntry(String id);

    @Query("DELETE FROM home_entries WHERE entry_type = 1")
    void deleteAllCustomEntries();

    // ==================== Query Operations ====================

    /**
     * Get all visible entries ordered by position.
     */
    @Query("SELECT * FROM home_entries WHERE is_visible = 1 ORDER BY order_position ASC")
    LiveData<List<HomeEntry>> getAllVisibleEntriesLive();

    /**
     * Get all visible entries (synchronous version for non-UI operations)
     */
    @Query("SELECT * FROM home_entries WHERE is_visible = 1 ORDER BY order_position ASC")
    List<HomeEntry> getAllVisibleEntries();

    /**
     * Get visible entries with advanced mode filter.
     * Shows entries that don't require advanced mode, plus advanced entries if mode is enabled.
     */
    @Query("SELECT * FROM home_entries WHERE is_visible = 1 AND " +
            "(requires_advanced_mode = 0 OR :advancedModeEnabled = 1) " +
            "ORDER BY order_position ASC")
    LiveData<List<HomeEntry>> getVisibleEntriesWithAdvancedFilter(boolean advancedModeEnabled);

    /**
     * Get visible entries with advanced mode filter (synchronous)
     */
    @Query("SELECT * FROM home_entries WHERE is_visible = 1 AND " +
            "(requires_advanced_mode = 0 OR :advancedModeEnabled = 1) " +
            "ORDER BY order_position ASC")
    List<HomeEntry> getVisibleEntriesWithAdvancedFilterSync(boolean advancedModeEnabled);

    /**
     * Get all entries (including hidden) for management screen
     */
    @Query("SELECT * FROM home_entries ORDER BY entry_type ASC, order_position ASC")
    LiveData<List<HomeEntry>> getAllEntriesLive();

    @Query("SELECT * FROM home_entries ORDER BY entry_type ASC, order_position ASC")
    List<HomeEntry> getAllEntries();

    /**
     * Get only system entries
     */
    @Query("SELECT * FROM home_entries WHERE entry_type = 0 ORDER BY order_position ASC")
    LiveData<List<HomeEntry>> getSystemEntriesLive();

    @Query("SELECT * FROM home_entries WHERE entry_type = 0 ORDER BY order_position ASC")
    List<HomeEntry> getSystemEntries();

    /**
     * Get hidden system entries (for "Add Library Script" dialog)
     */
    @Query("SELECT * FROM home_entries WHERE entry_type = 0 AND is_visible = 0 ORDER BY order_position ASC")
    LiveData<List<HomeEntry>> getHiddenSystemEntriesLive();

    @Query("SELECT * FROM home_entries WHERE entry_type = 0 AND is_visible = 0 ORDER BY order_position ASC")
    List<HomeEntry> getHiddenSystemEntries();

    /**
     * Get hidden system entries with advanced mode filter
     */
    @Query("SELECT * FROM home_entries WHERE entry_type = 0 AND is_visible = 0 AND " +
            "(requires_advanced_mode = 0 OR :advancedModeEnabled = 1) " +
            "ORDER BY order_position ASC")
    List<HomeEntry> getHiddenSystemEntriesWithAdvancedFilter(boolean advancedModeEnabled);

    /**
     * Get only custom entries
     */
    @Query("SELECT * FROM home_entries WHERE entry_type = 1 ORDER BY order_position ASC")
    LiveData<List<HomeEntry>> getCustomEntriesLive();

    @Query("SELECT * FROM home_entries WHERE entry_type = 1 ORDER BY order_position ASC")
    List<HomeEntry> getCustomEntries();

    /**
     * Get entry by ID
     */
    @Query("SELECT * FROM home_entries WHERE id = :id")
    LiveData<HomeEntry> getEntryByIdLive(String id);

    @Query("SELECT * FROM home_entries WHERE id = :id")
    HomeEntry getEntryById(String id);

    /**
     * Check if database has been seeded
     */
    @Query("SELECT COUNT(*) FROM home_entries WHERE entry_type = 0")
    int getSystemEntryCount();

    /**
     * Get the maximum order position (for adding new entries)
     */
    @Query("SELECT COALESCE(MAX(order_position), -1) FROM home_entries")
    int getMaxOrderPosition();

    /**
     * Check if an entry exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM home_entries WHERE id = :id)")
    boolean entryExists(String id);
}
