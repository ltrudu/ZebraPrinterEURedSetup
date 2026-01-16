package com.zebra.zebraprintereuredsetup;

/**
 * Interface for fragments that want to configure the MainActivity toolbar.
 * Fragments implementing this interface will have their title shown in the toolbar
 * and can specify whether to show a back button instead of the hamburger menu.
 */
public interface ToolbarConfigurable {
    /**
     * Returns the string resource ID for the toolbar title.
     * @return The toolbar title string resource ID (e.g., R.string.my_title)
     */
    int getToolbarTitleResId();

    /**
     * Returns a custom toolbar title string.
     * If this returns a non-null, non-empty string, it will be used instead of getToolbarTitleResId().
     * @return The custom toolbar title, or null to use getToolbarTitleResId()
     */
    default String getToolbarTitle() {
        return null;
    }

    /**
     * Returns whether to show a back button instead of the hamburger menu.
     * @return true to show back button, false to show hamburger menu
     */
    boolean showBackButton();
}
