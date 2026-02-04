package com.example.leveluplife.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.leveluplife.data.dao.PlayerDao;
import com.example.leveluplife.data.dao.TaskDao;
import com.example.leveluplife.data.database.AppDatabase;
import com.example.leveluplife.data.entity.Player;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.model.LevelUpEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private Application application;
    private PlayerRepository playerRepository;

    public TaskRepository(Application application, PlayerRepository playerRepository) {
        this.application = application;
        this.playerRepository = playerRepository;  // ✅ Сохраняем
        AppDatabase database = AppDatabase.getDatabase(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasksLiveData();
    }


    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasksLiveData();
    }

    public void insertTask(final Task task) {
        executor.execute(() -> taskDao.insertTask(task));
    }

    public void updateTask(final Task task) {
        executor.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(final Task task) {
        executor.execute(() -> {
            if (task.isCompleted()) {
                AppDatabase db = AppDatabase.getDatabase(application);
                PlayerDao playerDao = db.playerDao();
                Player player = playerDao.getPlayerSync();

                if (player != null) {
                    player.currentXp = Math.max(0, player.currentXp - task.getXpReward());
                    player.gold = Math.max(0, player.gold - task.getGoldReward());
                    player.lastUpdated = System.currentTimeMillis();
                    playerDao.updatePlayer(player);
                }
            }

            taskDao.deleteTask(task);
        });
    }

    public LiveData<Integer> getTotalXp() {
        return taskDao.getTotalXp();
    }

    public LiveData<Integer> getCompletedTasksCount() {
        return taskDao.getCompletedTasksCount();
    }

    public LiveData<Integer> getPendingTasksCount() {
        return taskDao.getPendingTasksCount();
    }

    public Task getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }

    public void toggleTaskCompletedWithRewards(long taskId, boolean isCompleted, Application application) {
        executor.execute(() -> {
            Task task = taskDao.getTaskById(taskId);
            if (task == null) return;

            boolean wasCompleted = task.isCompleted();

            if (wasCompleted == isCompleted) {
                return;
            }

            task.setCompleted(isCompleted);
            taskDao.updateTask(task);

            AppDatabase db = AppDatabase.getDatabase(application);
            PlayerDao playerDao = db.playerDao();
            Player player = playerDao.getPlayerSync();

            if (player != null) {
                if (isCompleted && !wasCompleted) {
                    // Задача ВЫПОЛНЕНА
                    android.util.Log.d("TaskRepository", "Task completed! Adding XP: " + task.getXpReward());

                    LevelUpEvent levelUpEvent = player.addXp(task.getXpReward());  // ✅ Сохраняем результат!
                    player.gold += task.getGoldReward();

                    android.util.Log.d("TaskRepository", "After addXp - Level: " + player.level + ", XP: " + player.currentXp);

                    // Если был level-up — отправляем событие
                    if (levelUpEvent != null) {
                        android.util.Log.d("TaskRepository", "Level-Up detected! Level: " + levelUpEvent.newLevel);

                        // Уведомляем PlayerRepository о level-up
                        playerRepository.notifyLevelUp(levelUpEvent);
                    } else {
                        android.util.Log.d("TaskRepository", "No level-up happened");
                    }

                } else if (!isCompleted && wasCompleted) {
                    // Задача ОТМЕНЕНА
                    player.currentXp = Math.max(0, player.currentXp - task.getXpReward());
                    player.gold = Math.max(0, player.gold - task.getGoldReward());
                }

                player.lastUpdated = System.currentTimeMillis();
                playerDao.updatePlayer(player);
            }
        });
    }

}
