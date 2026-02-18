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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.leveluplife.R;
import com.example.leveluplife.data.entity.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(
                requireActivity(), R.style.DarkDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_edit, null);

        EditText titleInput = view.findViewById(R.id.editTaskTitle);
        EditText xpInput = view.findViewById(R.id.editXpReward);
        EditText goldInput = view.findViewById(R.id.editGoldReward);
        Spinner taskTypeSpinner = view.findViewById(R.id.spinnerEditTaskType);
        Spinner attributeTypeSpinner = view.findViewById(R.id.spinnerEditAttributeType);

        SwitchMaterial switchReminder = view.findViewById(R.id.switchEditReminder);
        LinearLayout layoutReminderTime = view.findViewById(R.id.layoutEditReminderTime);
        NumberPicker pickerHour = view.findViewById(R.id.pickerEditHour);
        NumberPicker pickerMinute = view.findViewById(R.id.pickerEditMinute);

        setupSpinners(taskTypeSpinner, attributeTypeSpinner);
        setupTimePickers(pickerHour, pickerMinute);

        if (task != null) {
            titleInput.setText(task.getTitle());
            xpInput.setText(String.valueOf(task.getXpReward()));
            goldInput.setText(String.valueOf(task.getGoldReward()));
            taskTypeSpinner.setSelection(task.getTaskType().ordinal());
            attributeTypeSpinner.setSelection(task.getAttributeType().ordinal());

            switchReminder.setChecked(task.isReminderEnabled());
            if (task.isReminderEnabled()) {
                layoutReminderTime.setVisibility(View.VISIBLE);
                pickerHour.setValue(task.getReminderHour());
                pickerMinute.setValue(task.getReminderMinute() / 5);
            }
        }

        switchReminder.setOnCheckedChangeListener((btn, isChecked) ->
                layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Edit Task")
                .create();

        view.findViewById(R.id.btnSaveEdit).setOnClickListener(v -> {
            if (task == null) return;

            if (updateTaskFromInputs(titleInput, xpInput, goldInput,
                    taskTypeSpinner, attributeTypeSpinner)) {
                applyReminderToTask(switchReminder, pickerHour, pickerMinute);
                if (listener != null) {
                    listener.onTaskEdited(task);
                }
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.btnCancelEdit).setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void applyReminderToTask(SwitchMaterial sw,
                                     NumberPicker pickerHour,
                                     NumberPicker pickerMinute) {
        boolean enabled = sw.isChecked();
        task.setReminderEnabled(enabled);

        if (enabled) {
            int hour   = pickerHour.getValue();
            int minute = pickerMinute.getValue() * 5;
            task.setReminderHour(hour);
            task.setReminderMinute(minute);
            task.setNextReminderTime(calculateNextReminderTime(hour, minute));
        } else {
            task.setNextReminderTime(0);
        }
    }

    private long calculateNextReminderTime(int hour, int minute) {
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

    private boolean updateTaskFromInputs(EditText titleInput, EditText xpInput,
                                         EditText goldInput, Spinner taskTypeSpinner,
                                         Spinner attributeTypeSpinner) {
        String title = titleInput.getText().toString().trim();
        String xpString = xpInput.getText().toString().trim();
        String goldString = goldInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Task title cannot be empty",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int xp = task.getXpReward();
        if (!xpString.isEmpty()) {
            try {
                xp = Integer.parseInt(xpString);
                xp = Math.max(1, Math.min(999, xp));
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid XP value",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        int gold = task.getGoldReward();
        if (!goldString.isEmpty()) {
            try {
                gold = Integer.parseInt(goldString);
                gold = Math.max(0, Math.min(999, gold));
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid Gold value",
                        Toast.LENGTH_SHORT).show();
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
