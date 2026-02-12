package com.example.leveluplife.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Task;

public class TaskCreationDialog extends DialogFragment {

    public interface OnTaskCreatedListener {
        void onTaskCreated(Task task);
    }

    private OnTaskCreatedListener listener;

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_creation, null);

        EditText titleInput = view.findViewById(R.id.taskTitleInput);
        EditText xpInput = view.findViewById(R.id.taskXpInput);
        Spinner spinnerTaskType = view.findViewById(R.id.spinnerTaskType);
        Spinner spinnerAttributeType = view.findViewById(R.id.spinnerAttributeType);

        setupSpinners(spinnerTaskType, spinnerAttributeType);

        builder.setView(view)
                .setTitle("Create New Task")
                .setPositiveButton("Create", (dialog, id) -> {
                    String title = titleInput.getText().toString().trim();
                    String xpString = xpInput.getText().toString().trim();

                    if (title.isEmpty()) {
                        title = "New Task";
                    }

                    int xp = 50;
                    if (!xpString.isEmpty()) {
                        try {
                            xp = Integer.parseInt(xpString);
                            if (xp < 1) xp = 1;
                            if (xp > 999) xp = 999;
                        } catch (NumberFormatException e) {
                            xp = 50;
                        }
                    }

                    EditText goldInput = view.findViewById(R.id.taskGoldInput);

                    int gold = 10;
                    String goldString = goldInput.getText().toString().trim();
                    if (!goldString.isEmpty()) {
                        try {
                            gold = Integer.parseInt(goldString);
                            if (gold < 0) gold = 0;
                            if (gold > 999) gold = 999;
                        } catch (NumberFormatException e) {
                            gold = 10;
                        }
                    }

                    Task newTask = new Task(
                            title,
                            (Task.TaskType) spinnerTaskType.getSelectedItem(),
                            (Task.AttributeType) spinnerAttributeType.getSelectedItem(),
                            xp,
                            gold
                    );


                    if (listener != null) {
                        listener.onTaskCreated(newTask);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }

    private void setupSpinners(Spinner taskTypeSpinner, Spinner attributeTypeSpinner) {
        ArrayAdapter<Task.TaskType> taskTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Task.TaskType.values()
        );
        taskTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTypeSpinner.setAdapter(taskTypeAdapter);

        ArrayAdapter<Task.AttributeType> attributeTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Task.AttributeType.values()
        );
        attributeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        attributeTypeSpinner.setAdapter(attributeTypeAdapter);
    }

}
