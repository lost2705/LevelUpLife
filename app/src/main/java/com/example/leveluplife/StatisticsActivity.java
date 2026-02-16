package com.example.leveluplife;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.leveluplife.data.entity.CompletedTask;
import com.example.leveluplife.viewModel.CompletedTaskViewModel;
import com.example.leveluplife.viewModel.PlayerViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    private CompletedTaskViewModel completedTaskViewModel;
    private PlayerViewModel playerViewModel;
    private ExecutorService executor;

    private TextView tvTotalCompleted;
    private TextView tvTotalXp;
    private TextView tvCurrentStreak;
    private TextView tvThisWeek;
    private TextView tvDailyCount;
    private TextView tvTodoCount;
    private TextView tvHabitCount;
    private BarChart chartWeekly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialize ViewModels
        completedTaskViewModel = new ViewModelProvider(this).get(CompletedTaskViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        executor = Executors.newSingleThreadExecutor();

        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Initialize Views
        tvTotalCompleted = findViewById(R.id.tv_total_completed);
        tvTotalXp = findViewById(R.id.tv_total_xp);
        tvCurrentStreak = findViewById(R.id.tv_current_streak);
        tvThisWeek = findViewById(R.id.tv_this_week);
        tvDailyCount = findViewById(R.id.tv_daily_count);
        tvTodoCount = findViewById(R.id.tv_todo_count);
        tvHabitCount = findViewById(R.id.tv_habit_count);
        chartWeekly = findViewById(R.id.chart_weekly);

        // Setup Chart
        setupWeeklyChart();

        // Load Statistics
        loadStatistics();
    }

    private void setupWeeklyChart() {
        chartWeekly.getDescription().setEnabled(false);
        chartWeekly.setDrawGridBackground(false);
        chartWeekly.setDrawBarShadow(false);
        chartWeekly.setDrawValueAboveBar(true);
        chartWeekly.setPinchZoom(false);
        chartWeekly.setScaleEnabled(false);

        // X Axis
        XAxis xAxis = chartWeekly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFFFFFFFF);

        // Y Axis
        chartWeekly.getAxisLeft().setTextColor(0xFFFFFFFF);
        chartWeekly.getAxisLeft().setAxisMinimum(0f);
        chartWeekly.getAxisRight().setEnabled(false);

        // Legend
        chartWeekly.getLegend().setEnabled(false);
    }

    private void loadStatistics() {
        executor.execute(() -> {
            // Get data for last 7 days
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long todayStart = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, -6); // 7 days ago
            long weekStart = calendar.getTimeInMillis();

            // Get completed tasks for this week
            int weekCount = completedTaskViewModel.getCompletedTasksCountSince(weekStart);

            // Get total completed tasks
            int totalCount = completedTaskViewModel.getCompletedTasksCountSince(0);

            runOnUiThread(() -> {
                tvTotalCompleted.setText(String.valueOf(totalCount));
                tvThisWeek.setText(String.valueOf(weekCount));
            });

            // Load chart data
            loadChartData(weekStart, todayStart);
        });

        // Load player stats
        playerViewModel.getPlayer().observe(this, player -> {
            if (player != null) {
                tvTotalXp.setText(String.valueOf(player.getCurrentXp() + (player.getLevel() * 100)));
                tvCurrentStreak.setText("0"); // TODO: Implement streak tracking
            }
        });

        // Load category breakdown
        loadCategoryBreakdown();
    }

    private void loadChartData(long startTime, long endTime) {
        executor.execute(() -> {
            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startTime);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

            for (int i = 0; i < 7; i++) {
                long dayStart = calendar.getTimeInMillis();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                long dayEnd = calendar.getTimeInMillis();

                // Count tasks completed on this day
                int count = completedTaskViewModel.getCompletedTasksCountSince(dayStart) -
                        completedTaskViewModel.getCompletedTasksCountSince(dayEnd);

                entries.add(new BarEntry(i, count));

                calendar.setTimeInMillis(dayStart);
                labels.add(dayFormat.format(calendar.getTime()));
                calendar.setTimeInMillis(dayEnd);
            }

            runOnUiThread(() -> {
                BarDataSet dataSet = new BarDataSet(entries, "Tasks");
                dataSet.setColor(0xFFBB86FC);
                dataSet.setValueTextColor(0xFFFFFFFF);
                dataSet.setValueTextSize(12f);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.8f);

                chartWeekly.setData(barData);
                chartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                chartWeekly.invalidate();
            });
        });
    }

    private void loadCategoryBreakdown() {
        executor.execute(() -> {
            List<CompletedTask> allTasks = completedTaskViewModel.getCompletedTasksSince(0);

            int dailyCount = 0;
            int todoCount = 0;
            int habitCount = 0;

            for (CompletedTask task : allTasks) {
                String frequency = task.getFrequency();
                if ("DAILY".equals(frequency)) {
                    dailyCount++;
                } else if ("TODO".equals(frequency)) {
                    todoCount++;
                } else if ("HABIT".equals(frequency)) {
                    habitCount++;
                }
            }

            int finalDailyCount = dailyCount;
            int finalTodoCount = todoCount;
            int finalHabitCount = habitCount;

            runOnUiThread(() -> {
                tvDailyCount.setText(String.valueOf(finalDailyCount));
                tvTodoCount.setText(String.valueOf(finalTodoCount));
                tvHabitCount.setText(String.valueOf(finalHabitCount));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
