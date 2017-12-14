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

import java.io.IOException;
import java.net.ServerSocket;

import ch.ethz.inf.vs.kompose.R;

public class PortEntryPreference extends DialogPreference{

    // allowed range
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 65535;

    private EditText editText;
    private int value;

    public PortEntryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PortEntryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PortEntryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortEntryPreference(Context context) {
        super(context);
    }


    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        editText = new EditText(getContext());
        editText.setLayoutParams(layoutParams);
        editText.setEms(5);
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
        editText.setText(String.valueOf(getPortValue()));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            editText.clearFocus();
            int newValue = Integer.valueOf(editText.getText().toString());
            if(checkPortValidity(newValue) && callChangeListener(newValue)){
                setValue(newValue);
            } else{
                Toast.makeText(this.getContext(), this.getContext().getText(R.string.setting_error_port), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(MIN_VALUE) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getPortValue() {
        return this.value;
    }

    /**
     * Checks whether the given port is not reserved or already in use.
     * Note that 0 tells the app to use a random open port.
     * @param port port to check
     * @return true iff port is usable
     */
    private boolean checkPortValidity(int port){
        boolean goahead = (0 == port) || ((1024 < port) && (port < 65535));
        if (goahead){
            try{
                new ServerSocket(port).close();
                return true;
            }catch(IOException io){
                return false;
            }
        }
        return false;
    }
}
