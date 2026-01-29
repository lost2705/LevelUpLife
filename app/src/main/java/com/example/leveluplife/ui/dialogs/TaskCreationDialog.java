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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_creation, null);

        EditText titleInput = view.findViewById(R.id.taskTitleInput);
        EditText xpInput = view.findViewById(R.id.taskXpInput);

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

                    Task newTask = new Task(
                            title,
                            Task.TaskType.DAILY,
                            Task.AttributeType.STRENGTH,
                            xp,
                            5
                    );

                    if (listener != null) {
                        listener.onTaskCreated(newTask);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        return builder.create();
    }
}
