package co.aerobotics.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import co.aerobotics.android.R;

public class TreeSizeDialog extends DialogFragment {

    private View view;
    private Context context;
    private RadioButton radiomin, radio1, radio2, radiomax;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_treesize, null);
        context = getActivity().getApplicationContext();
        initializeRadioViews();

        final AlertDialog dialog = buildDialog();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = (dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "works", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return dialog;
    }

    private AlertDialog buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton("GO", null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }

    private void initializeRadioViews(){
        radiomin = (RadioButton) view.findViewById(R.id.radioButtonMin);
        radio1 = (RadioButton) view.findViewById(R.id.radioButton1);
        radio2 = (RadioButton) view.findViewById(R.id.radioButton2);
        radiomax = (RadioButton) view.findViewById(R.id.radioButtonMax);
    }

}
