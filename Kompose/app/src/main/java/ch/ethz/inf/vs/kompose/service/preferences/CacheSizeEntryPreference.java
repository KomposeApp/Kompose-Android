package ch.ethz.inf.vs.kompose.service.preferences;


import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import ch.ethz.inf.vs.kompose.R;

public class CacheSizeEntryPreference extends DialogPreference {

    // allowed range
    public static final int MIN_VALUE = 4;
    public static final int MAX_VALUE = 2048;

    private EditText editText;
    private int value;

    public CacheSizeEntryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CacheSizeEntryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CacheSizeEntryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CacheSizeEntryPreference(Context context) {
        super(context);
    }


    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        editText = new EditText(getContext());
        editText.setLayoutParams(layoutParams);
        editText.setEms(4);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(5);
        editText.setFilters(filterArray);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(editText);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editText.setText(String.valueOf(getCacheValue()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            editText.clearFocus();
            int newValue = Integer.valueOf(editText.getText().toString());
            if (checkInputValidity(newValue) && callChangeListener(newValue)) {
                setValue(newValue);
            } else {
                Toast.makeText(this.getContext(), this.getContext().getText(R.string.setting_error_maxsize), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index,PreferenceUtility.DEFAULT_MAXDLSIZE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(PreferenceUtility.DEFAULT_MAXDLSIZE) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getCacheValue() {
        return this.value;
    }

    private boolean checkInputValidity(int cacheSize){
        return ((MIN_VALUE <= cacheSize) && (cacheSize <= MAX_VALUE));
    }

}
