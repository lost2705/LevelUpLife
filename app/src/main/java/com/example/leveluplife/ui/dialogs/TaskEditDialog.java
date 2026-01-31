package com.example.leveluplife.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

    public void setTask(Task task) {
        this.task = task;
    }

    public void setOnTaskEditedListener(OnTaskEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_creation, null);

        EditText titleInput = view.findViewById(R.id.taskTitleInput);
        EditText xpInput = view.findViewById(R.id.taskXpInput);

        // Pre-fill current values
        if (task != null) {
            titleInput.setText(task.getTitle());
            xpInput.setText(String.valueOf(task.getXpReward()));
        }

        builder.setView(view)
                .setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, id) -> {
                    if (task == null) return;

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
                            xp = task.getXpReward();
                        }
                    }

                    task.setTitle(title);
                    task.setXpReward(xp);

                    if (listener != null) {
                        listener.onTaskEdited(task);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }
}
