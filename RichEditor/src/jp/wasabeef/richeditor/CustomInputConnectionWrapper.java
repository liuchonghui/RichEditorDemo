package jp.wasabeef.richeditor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class CustomInputConnectionWrapper extends InputConnectionWrapper {
	InputConnection target;
	public CustomInputConnectionWrapper(InputConnection target) {
		this(target, true);
	}

	public CustomInputConnectionWrapper(InputConnection target, boolean mutable) {
		super(target, true);
		this.target = target;
	}
	
	public BaseInputConnection getTarget() {
		BaseInputConnection ret = null;
		if (this.target instanceof BaseInputConnection) {
			return (BaseInputConnection) target;
		}
		return ret;
	}

	public CharSequence getTextBeforeCursor(int n, int flags) {
        return super.getTextBeforeCursor(n, flags);
    }
    
    public CharSequence getTextAfterCursor(int n, int flags) {
        return super.getTextAfterCursor(n, flags);
    }

    @SuppressLint("NewApi")
	public CharSequence getSelectedText(int flags) {
        return super.getSelectedText(flags);
    }

    public int getCursorCapsMode(int reqModes) {
        return super.getCursorCapsMode(reqModes);
    }
    
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return super.getExtractedText(request, flags);
    }

    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        return super.deleteSurroundingText(beforeLength, afterLength);
    }

    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return super.setComposingText(text, newCursorPosition);
    }

    @SuppressLint("NewApi")
	public boolean setComposingRegion(int start, int end) {
        return super.setComposingRegion(start, end);
    }

    public boolean finishComposingText() {
        boolean ret = false;
        try {
            ret = super.finishComposingText();
        } catch (Throwable t) {
            t.printStackTrace();
            ret = false;
        }
        return ret;
    }
    
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return super.commitText(text, newCursorPosition);
    }

    public boolean commitCompletion(CompletionInfo text) {
        return super.commitCompletion(text);
    }

    @SuppressLint("NewApi")
	public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return super.commitCorrection(correctionInfo);
    }

    public boolean setSelection(int start, int end) {
        return super.setSelection(start, end);
    }
    
    public boolean performEditorAction(int editorAction) {
        return super.performEditorAction(editorAction);
    }
    
    public boolean performContextMenuAction(int id) {
        return super.performContextMenuAction(id);
    }
    
    public boolean beginBatchEdit() {
        return super.beginBatchEdit();
    }
    
    public boolean endBatchEdit() {
        return super.endBatchEdit();
    }
    
    public boolean sendKeyEvent(KeyEvent event) {
        return super.sendKeyEvent(event);
    }

    public boolean clearMetaKeyStates(int states) {
        return super.clearMetaKeyStates(states);
    }
    
    public boolean reportFullscreenMode(boolean enabled) {
        return super.reportFullscreenMode(enabled);
    }
    
    public boolean performPrivateCommand(String action, Bundle data) {
        return super.performPrivateCommand(action, data);
    }
}
