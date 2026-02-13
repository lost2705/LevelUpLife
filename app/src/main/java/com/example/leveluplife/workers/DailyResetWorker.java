package com.example.leveluplife.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.entity.CompletedTask;

import java.util.List;

public class DailyResetWorker extends Worker {

    private static final String TAG = "DailyResetWorker";

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            TaskDao taskDao = db.taskDao();
            CompletedTaskDao completedTaskDao = db.completedTaskDao();

            List<Task> completedDailies = taskDao.getCompletedDailyTasks();

            long now = System.currentTimeMillis();

            for (Task task : completedDailies) {
                Log.d(TAG, "Processing task: " + task.getTitle());

                CompletedTask history = new CompletedTask(
                        task.getId(),
                        task.getTitle(),
                        task.getXpReward(),
                        task.getGoldReward(),
                        task.getFrequency()
                );
                completedTaskDao.insertCompletedTask(history);
                Log.d(TAG, "  → Saved to history");

                int oldStreak = task.getCurrentStreak();
                task.setCurrentStreak(oldStreak + 1);
                task.setLastCompletedDate(now);

                if (task.getCurrentStreak() > task.getBestStreak()) {
                    task.setBestStreak(task.getCurrentStreak());
                    Log.d(TAG, "  → NEW BEST STREAK: " + task.getBestStreak());
                }

                taskDao.updateTask(task);
                Log.d(TAG, "  → Streak updated: " + oldStreak + " → " + task.getCurrentStreak());
            }

            taskDao.resetDailyTasks(now);
            Log.d(TAG, "All DAILY tasks reset to uncompleted");

            Log.d(TAG, "===== DAILY RESET COMPLETED SUCCESSFULLY =====");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "===== DAILY RESET FAILED =====", e);
            return Result.failure();
        }
    }
}
