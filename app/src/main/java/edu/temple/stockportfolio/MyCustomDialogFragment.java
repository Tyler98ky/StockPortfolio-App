package edu.temple.stockportfolio;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

public class MyCustomDialogFragment extends DialogFragment {
    private String mInput;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        // Set up the buttons
        builder.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInput = input.getText().toString();
                ((OnDialogTextEntryListener) getActivity()).onDialogTextEntryReceived(mInput);
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(input);
        builder.setMessage(R.string.alert_dialog_title);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface OnDialogTextEntryListener {
        void onDialogTextEntryReceived(String message);
    }
}
