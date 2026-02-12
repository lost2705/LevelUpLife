package com.example.leveluplife.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Task;

public class TaskEditDialog extends DialogFragment {

    public interface OnTaskEditedListener {
        void onTaskEdited(Task task);
    }

    private OnTaskEditedListener listener;
    private Task task;

    public static TaskEditDialog newInstance(Task task) {
        TaskEditDialog dialog = new TaskEditDialog();
        dialog.setTask(task);
        return dialog;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setOnTaskEditedListener(OnTaskEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.DarkDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_edit, null);

        EditText titleInput = view.findViewById(R.id.editTaskTitle);
        EditText xpInput = view.findViewById(R.id.editXpReward);
        EditText goldInput = view.findViewById(R.id.editGoldReward);
        Spinner taskTypeSpinner = view.findViewById(R.id.spinnerEditTaskType);
        Spinner attributeTypeSpinner = view.findViewById(R.id.spinnerEditAttributeType);

        setupSpinners(taskTypeSpinner, attributeTypeSpinner);

        if (task != null) {
            titleInput.setText(task.getTitle());
            xpInput.setText(String.valueOf(task.getXpReward()));
            goldInput.setText(String.valueOf(task.getGoldReward()));
            taskTypeSpinner.setSelection(task.getTaskType().ordinal());
            attributeTypeSpinner.setSelection(task.getAttributeType().ordinal());
        }

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Edit Task")
                .create();

        view.findViewById(R.id.btnSaveEdit).setOnClickListener(v -> {
            if (task == null) return;

            if (updateTaskFromInputs(titleInput, xpInput, goldInput, taskTypeSpinner, attributeTypeSpinner)) {
                if (listener != null) {
                    listener.onTaskEdited(task);
                }
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.btnCancelEdit).setOnClickListener(v -> dialog.dismiss());

        return dialog;
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

    private boolean updateTaskFromInputs(EditText titleInput, EditText xpInput,
                                         EditText goldInput, Spinner taskTypeSpinner,
                                         Spinner attributeTypeSpinner) {
        String title = titleInput.getText().toString().trim();
        String xpString = xpInput.getText().toString().trim();
        String goldString = goldInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Task title cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        int xp = task.getXpReward();
        if (!xpString.isEmpty()) {
            try {
                xp = Integer.parseInt(xpString);
                if (xp < 1) xp = 1;
                if (xp > 999) xp = 999;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid XP value", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        int gold = task.getGoldReward();
        if (!goldString.isEmpty()) {
            try {
                gold = Integer.parseInt(goldString);
                if (gold < 0) gold = 0;
                if (gold > 999) gold = 999;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid Gold value", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        task.setTitle(title);
        task.setXpReward(xp);
        task.setGoldReward(gold);
        task.setTaskType((Task.TaskType) taskTypeSpinner.getSelectedItem());
        task.setAttributeType((Task.AttributeType) attributeTypeSpinner.getSelectedItem());

        task.setFrequency(task.getTaskType().name());
        task.setLastUpdated(System.currentTimeMillis());

        return true;
    }
}
