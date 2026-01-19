package com.zebra.zebraprintereuredsetup.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.zebra.zebraprintereuredsetup.data.dao.HomeEntryDao;
import com.zebra.zebraprintereuredsetup.data.entity.HomeEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database for the application.
 * Handles database creation and provides DAOs.
 */
@Database(entities = {HomeEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "zeured_database";

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Executor for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Abstract DAO methods
    public abstract HomeEntryDao homeEntryDao();

    /**
     * Get singleton instance of the database.
     */
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addCallback(new DatabaseCallback())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback to seed the database with system entries on first creation.
     */
    private static class DatabaseCallback extends RoomDatabase.Callback {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Seed system entries on database creation
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    HomeEntryDao dao = INSTANCE.homeEntryDao();
                    seedSystemEntries(dao);
                }
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // Verify system entries exist (in case of upgrade scenarios)
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    HomeEntryDao dao = INSTANCE.homeEntryDao();
                    if (dao.getSystemEntryCount() == 0) {
                        seedSystemEntries(dao);
                    }
                }
            });
        }
    }

    /**
     * Seeds the database with pre-defined system entries.
     */
    private static void seedSystemEntries(HomeEntryDao dao) {
        List<HomeEntry> systemEntries = new ArrayList<>();

        // 1. EURED Setup
        HomeEntry euRedEntry = new HomeEntry(
                HomeEntry.SYSTEM_ID_EURED_SETUP,
                "launcher_eu_red_setup",
                "launcher_eu_red_description",
                "ic_eu_red",
                "nav_eu_red",
                HomeEntry.TYPE_SYSTEM,
                true,
                0,
                false
        );
        systemEntries.add(euRedEntry);

        // 2. Custom Script (hidden by default)
        HomeEntry customScriptEntry = new HomeEntry(
                HomeEntry.SYSTEM_ID_CUSTOM_SCRIPT,
                "nav_custom_script",
                "launcher_custom_script_description",
                "ic_custom_script",
                "nav_custom_script",
                HomeEntry.TYPE_SYSTEM,
                false,
                1,
                false
        );
        systemEntries.add(customScriptEntry);

        // 3. Documentation (hidden by default)
        HomeEntry documentationEntry = new HomeEntry(
                HomeEntry.SYSTEM_ID_DOCUMENTATION,
                "nav_script_documentation",
                "launcher_documentation_description",
                "ic_documentation",
                "nav_script_documentation",
                HomeEntry.TYPE_SYSTEM,
                false,
                2,
                false
        );
        systemEntries.add(documentationEntry);

        // 4. Advanced (requires advanced mode to be visible)
        HomeEntry advancedEntry = new HomeEntry(
                HomeEntry.SYSTEM_ID_ADVANCED,
                "nav_advanced",
                "launcher_advanced_description",
                "ic_advanced",
                "nav_advanced",
                HomeEntry.TYPE_SYSTEM,
                true,
                3,
                true
        );
        systemEntries.add(advancedEntry);

        // Insert all system entries
        dao.insertAll(systemEntries);
    }

    /**
     * Force close and re-initialize the database (for testing/reset scenarios).
     */
    public static void resetDatabase(Context context) {
        synchronized (AppDatabase.class) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
            context.deleteDatabase(DATABASE_NAME);
        }
    }
}
