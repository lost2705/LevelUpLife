package com.example.leveluplife.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.CompletedTaskDao;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.CompletedTask;

import java.util.List;

public class DailyResetWorker extends Worker {

    private static final String TAG = "DailyResetWorker";
    private static final int PENALTY_PER_TASK = 5;
    private static final int MAX_PENALTY = 50;

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "═══════════════════════════════");
        Log.d(TAG, "🌅 DAILY RESET STARTED");
        Log.d(TAG, "═══════════════════════════════");

        try {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            TaskDao taskDao = db.taskDao();
            PlayerDao playerDao = db.playerDao();
            CompletedTaskDao completedTaskDao = db.completedTaskDao();

            List<Task> allDailyTasks = taskDao.getTasksByFrequencySync("DAILY");

            int completedCount = 0;
            for (Task task : allDailyTasks) {
                if (task.isCompleted()) {
                    completedCount++;
                }
            }

            int totalDailyTasks = allDailyTasks.size();
            int uncompletedCount = totalDailyTasks - completedCount;

            Log.d(TAG, "📊 Daily Tasks Summary:");
            Log.d(TAG, "   Total: " + totalDailyTasks);
            Log.d(TAG, "   ✅ Completed: " + completedCount);
            Log.d(TAG, "   ❌ Uncompleted: " + uncompletedCount);

            int newPenalty = Math.min(uncompletedCount * PENALTY_PER_TASK, MAX_PENALTY);

            Player player = playerDao.getPlayerSync();
            if (player != null) {
                int oldPenalty = player.getXpPenalty();
                player.setXpPenalty(newPenalty);
                playerDao.updatePlayer(player);

                Log.d(TAG, "⚡ XP Penalty Update:");
                Log.d(TAG, "   Old: -" + oldPenalty + "%");
                Log.d(TAG, "   New: -" + newPenalty + "%");

                if (newPenalty == 0 && totalDailyTasks > 0) {
                    Log.d(TAG, "🎉 PERFECT DAY! All tasks completed! No penalty!");
                } else if (newPenalty > 0) {
                    Log.d(TAG, "⚠️ WARNING: XP rewards reduced by " + newPenalty + "%");
                    Log.d(TAG, "   Complete all daily tasks tomorrow to remove penalty!");
                }
            }

            for (Task task : allDailyTasks) {
                if (task.isCompleted()) {
                    CompletedTask history = new CompletedTask(
                            task.getId(),
                            task.getTitle(),
                            task.getXpReward(),
                            task.getGoldReward(),
                            task.getFrequency()
                    );
                    completedTaskDao.insertCompletedTask(history);
                    Log.d(TAG, "💾 Saved to history: " + task.getTitle());

                    task.setCurrentStreak(task.getCurrentStreak() + 1);
                    if (task.getCurrentStreak() > task.getBestStreak()) {
                        task.setBestStreak(task.getCurrentStreak());
                        Log.d(TAG, "🔥 NEW BEST STREAK: " + task.getBestStreak() + " (" + task.getTitle() + ")");
                    }
                    taskDao.updateTask(task);
                } else {
                    if (task.getCurrentStreak() > 0) {
                        task.setCurrentStreak(0);
                        taskDao.updateTask(task);
                        Log.d(TAG, "💔 Streak reset: " + task.getTitle());
                    }
                }
            }

            long now = System.currentTimeMillis();
            taskDao.resetDailyTasks(now);
            Log.d(TAG, "🔄 All daily tasks reset to uncompleted");

            Log.d(TAG, "═══════════════════════════════");
            Log.d(TAG, "✅ DAILY RESET COMPLETED");
            Log.d(TAG, "═══════════════════════════════");

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "❌ Daily reset failed", e);
            return Result.failure();
        }
    }
}
