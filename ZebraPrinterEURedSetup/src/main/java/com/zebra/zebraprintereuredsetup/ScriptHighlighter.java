package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Applies syntax highlighting to script text using Spannable.
 * Supports color coding and clickable spans for ZPL, ZBI, and SGD commands.
 */
public class ScriptHighlighter {

    /**
     * Listener for command click events.
     */
    public interface OnCommandClickListener {
        void onCommandClick(ScriptAnalyzer.FoundCommand command, DocumentationCommand documentation);
    }

    private final Context context;
    private final int colorZpl;
    private final int colorSgd;
    private final int colorZbi;
    private OnCommandClickListener clickListener;
    private ScriptAnalyzer scriptAnalyzer;

    public ScriptHighlighter(Context context) {
        this.context = context;
        this.colorZpl = ContextCompat.getColor(context, R.color.syntax_zpl);
        this.colorSgd = ContextCompat.getColor(context, R.color.syntax_sgd);
        this.colorZbi = ContextCompat.getColor(context, R.color.syntax_zbi);
    }

    /**
     * Set the script analyzer for documentation lookup.
     */
    public void setScriptAnalyzer(ScriptAnalyzer analyzer) {
        this.scriptAnalyzer = analyzer;
    }

    /**
     * Set the listener for command click events.
     */
    public void setOnCommandClickListener(OnCommandClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Get the color for a command type.
     */
    public int getColorForType(ScriptAnalyzer.CommandType type) {
        switch (type) {
            case ZPL:
                return colorZpl;
            case SGD:
                return colorSgd;
            case ZBI:
                return colorZbi;
            default:
                return colorZpl;
        }
    }

    /**
     * Apply syntax highlighting to an EditText.
     * Returns true if any highlighting was applied.
     */
    public boolean applyHighlighting(EditText editText, List<ScriptAnalyzer.FoundCommand> commands) {
        if (editText == null || commands == null || commands.isEmpty()) {
            return false;
        }

        CharSequence currentText = editText.getText();
        if (currentText == null || currentText.length() == 0) {
            return false;
        }

        // Create a SpannableStringBuilder from current text
        SpannableStringBuilder spannable = new SpannableStringBuilder(currentText);

        // Remove existing command spans first
        removeAllCommandSpans(spannable);

        // Apply new spans for each found command
        for (ScriptAnalyzer.FoundCommand cmd : commands) {
            // Validate indices
            if (cmd.startIndex < 0 || cmd.endIndex > spannable.length() || cmd.startIndex >= cmd.endIndex) {
                continue;
            }

            int color = getColorForType(cmd.type);

            // Apply foreground color span
            spannable.setSpan(
                    new ForegroundColorSpan(color),
                    cmd.startIndex,
                    cmd.endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Apply clickable span if we have a click listener
            if (clickListener != null) {
                DocumentationCommand doc = null;
                if (scriptAnalyzer != null) {
                    doc = scriptAnalyzer.lookupCommand(cmd.normalizedCommand, cmd.type);
                }

                final DocumentationCommand finalDoc = doc;
                spannable.setSpan(
                        new CommandClickableSpan(cmd, finalDoc, clickListener, color),
                        cmd.startIndex,
                        cmd.endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        // Save cursor position
        int cursorPos = editText.getSelectionStart();

        // Update EditText with spannable text
        editText.setText(spannable);

        // Restore cursor position
        if (cursorPos >= 0 && cursorPos <= spannable.length()) {
            editText.setSelection(cursorPos);
        } else if (spannable.length() > 0) {
            editText.setSelection(spannable.length());
        }

        return true;
    }

    /**
     * Remove all existing command-related spans from the spannable.
     */
    private void removeAllCommandSpans(SpannableStringBuilder spannable) {
        // Remove ForegroundColorSpan instances
        ForegroundColorSpan[] colorSpans = spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : colorSpans) {
            spannable.removeSpan(span);
        }

        // Remove CommandClickableSpan instances
        CommandClickableSpan[] clickSpans = spannable.getSpans(0, spannable.length(), CommandClickableSpan.class);
        for (CommandClickableSpan span : clickSpans) {
            spannable.removeSpan(span);
        }
    }

    /**
     * Clear all highlighting from an EditText.
     */
    public void clearHighlighting(EditText editText) {
        if (editText == null) return;

        CharSequence text = editText.getText();
        if (text instanceof SpannableStringBuilder) {
            SpannableStringBuilder spannable = (SpannableStringBuilder) text;
            removeAllCommandSpans(spannable);

            int cursorPos = editText.getSelectionStart();
            editText.setText(spannable);
            if (cursorPos >= 0 && cursorPos <= spannable.length()) {
                editText.setSelection(cursorPos);
            }
        }
    }

    /**
     * Custom ClickableSpan for command clicks.
     */
    private static class CommandClickableSpan extends ClickableSpan {
        private final ScriptAnalyzer.FoundCommand command;
        private final DocumentationCommand documentation;
        private final OnCommandClickListener listener;
        private final int color;

        CommandClickableSpan(ScriptAnalyzer.FoundCommand command, DocumentationCommand documentation,
                           OnCommandClickListener listener, int color) {
            this.command = command;
            this.documentation = documentation;
            this.listener = listener;
            this.color = color;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (listener != null) {
                listener.onCommandClick(command, documentation);
            }
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            // Apply the color but don't underline
            ds.setColor(color);
            ds.setUnderlineText(false);
        }
    }
}
