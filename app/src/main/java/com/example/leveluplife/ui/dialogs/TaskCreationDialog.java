package com.example.leveluplife.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(
                requireActivity(), R.style.DarkDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_creation, null);

        EditText titleInput = view.findViewById(R.id.taskTitleInput);
        EditText xpInput = view.findViewById(R.id.taskXpInput);
        EditText goldInput = view.findViewById(R.id.taskGoldInput);
        Spinner spinnerTaskType = view.findViewById(R.id.spinnerTaskType);
        Spinner spinnerAttributeType = view.findViewById(R.id.spinnerAttributeType);

        SwitchMaterial switchReminder = view.findViewById(R.id.switchReminder);
        LinearLayout layoutReminderTime = view.findViewById(R.id.layoutReminderTime);
        NumberPicker pickerHour = view.findViewById(R.id.pickerHour);
        NumberPicker pickerMinute = view.findViewById(R.id.pickerMinute);

        setupSpinners(spinnerTaskType, spinnerAttributeType);
        setupTimePickers(pickerHour, pickerMinute);

        switchReminder.setOnCheckedChangeListener((btn, isChecked) ->
                layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        builder.setView(view)
                .setTitle("Create New Task")
                .setPositiveButton("Create", (dialog, id) -> {

                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) title = "New Task";

                    int xp = 50;
                    String xpString = xpInput.getText().toString().trim();
                    if (!xpString.isEmpty()) {
                        try {
                            xp = Integer.parseInt(xpString);
                            xp = Math.max(1, Math.min(999, xp));
                        } catch (NumberFormatException e) { xp = 50; }
                    }

                    int gold = 10;
                    String goldString = goldInput.getText().toString().trim();
                    if (!goldString.isEmpty()) {
                        try {
                            gold = Integer.parseInt(goldString);
                            gold = Math.max(0, Math.min(999, gold));
                        } catch (NumberFormatException e) { gold = 10; }
                    }

                    Task newTask = new Task(
                            title,
                            (Task.TaskType) spinnerTaskType.getSelectedItem(),
                            (Task.AttributeType) spinnerAttributeType.getSelectedItem(),
                            xp,
                            gold
                    );

                    if (switchReminder.isChecked()) {
                        int hour   = pickerHour.getValue();
                        int minute = pickerMinute.getValue() * 5;

                        newTask.setReminderEnabled(true);
                        newTask.setReminderHour(hour);
                        newTask.setReminderMinute(minute);
                        newTask.setNextReminderTime(
                                calculateFirstReminderTime(hour, minute)
                        );
                    }

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

    private void setupTimePickers(NumberPicker pickerHour, NumberPicker pickerMinute) {
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        pickerHour.setFormatter(value -> String.format("%02d", value));

        String[] minuteValues = {"00","05","10","15","20","25","30","35","40","45","50","55"};
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(minuteValues.length - 1);
        pickerMinute.setDisplayedValues(minuteValues);

        Calendar cal = Calendar.getInstance();
        pickerHour.setValue((cal.get(Calendar.HOUR_OF_DAY) + 1) % 24);
        pickerMinute.setValue(0);
    }

    private long calculateFirstReminderTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTimeInMillis();
    }
}
