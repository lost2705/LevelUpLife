package com.example.leveluplife.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.data.entity.Task;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Calendar;
import java.util.List;

public class DailyResetWorker extends Worker {

    private static final String TAG = "DailyResetWorker";

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Daily reset started at " + System.currentTimeMillis());

        try {
            // Get database instances
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            TaskDao taskDao = db.taskDao();
            CompletedTaskDao completedTaskDao = db.completedTaskDao();

            // Get all daily tasks
            List<Task> allDailyTasks = taskDao.getTasksByFrequencySync("DAILY");

            if (allDailyTasks == null || allDailyTasks.isEmpty()) {
                Log.d(TAG, "No daily tasks found");
                return Result.success();
            }

            long now = System.currentTimeMillis();
            long yesterdayStart = getYesterdayStartTimestamp();

            for (Task task : allDailyTasks) {
                if (task.isCompleted()) {
                    saveToHistory(task, completedTaskDao);
                    updateStreakCompleted(task, taskDao);
                } else {
                    resetStreak(task, taskDao);
                }
            }

            taskDao.resetDailyTasks(now);

            Log.d(TAG, "Daily reset completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error during daily reset: " + e.getMessage());
            return Result.failure();
        }
    }

    private void saveToHistory(Task task, CompletedTaskDao completedTaskDao) {
        CompletedTask history = new CompletedTask(
                task.getId(),
                task.getTitle(),
                task.getXpReward(),
                task.getGoldReward(),
                task.getFrequency()
        );
        completedTaskDao.insertCompletedTask(history);
        Log.d(TAG, "Saved to history: " + task.getTitle());
    }

    private void updateStreakCompleted(Task task, TaskDao taskDao) {
        task.setCurrentStreak(task.getCurrentStreak() + 1);

        if (task.getCurrentStreak() > task.getBestStreak()) {
            task.setBestStreak(task.getCurrentStreak());
        }

        taskDao.updateTask(task);
        Log.d(TAG, "Streak updated: " + task.getTitle() + " -> " + task.getCurrentStreak() + " days");
    }

    private void resetStreak(Task task, TaskDao taskDao) {
        if (task.getCurrentStreak() > 0) {
            Log.d(TAG, "Streak broken: " + task.getTitle() + " (was " + task.getCurrentStreak() + " days)");
            task.setCurrentStreak(0);
            taskDao.updateTask(task);
        }
    }

    private long getYesterdayStartTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
