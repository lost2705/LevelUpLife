package com.example.leveluplife.workers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.leveluplife.MainActivity;
import com.example.leveluplife.R;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.entity.Task;

import java.util.Calendar;
import java.util.List;

public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";
    private static final int NOTIFICATION_ID = 1001;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "🔊 Checking for due reminders...");

        try {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            TaskDao taskDao = db.taskDao();

            List<Task> dueTasks = taskDao.getDueReminders(System.currentTimeMillis());

            Log.d(TAG, "Found " + dueTasks.size() + " due reminders");

            for (Task task : dueTasks) {
                showTaskReminder(task);
                scheduleNextReminder(task);
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Reminder failed", e);
            return Result.retry();
        }
    }

    private void showTaskReminder(Task task) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                "task_reminders"
        )
                .setSmallIcon(R.drawable.ic_task)
                .setContentTitle("⏰ Task Reminder!")
                .setContentText(task.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't forget to complete: " + task.getTitle() +
                                "\n\nReward: " + task.getXpReward() + " XP + " + task.getGoldReward() + " Gold"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(task.getId(), builder.build());

        Log.d(TAG, "🔔 Reminder sent: " + task.getTitle());
    }

    private void scheduleNextReminder(Task task) {
        long nextTime = calculateNextReminderTime(task);
        if (nextTime > 0) {
            AppDatabase.getDatabase(getApplicationContext())
                    .taskDao()
                    .updateNextReminderTime(task.getId(), nextTime);
        }
    }

    private long calculateNextReminderTime(Task task) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, task.getReminderHour());
        calendar.set(Calendar.MINUTE, task.getReminderMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar.getTimeInMillis();
    }
}
