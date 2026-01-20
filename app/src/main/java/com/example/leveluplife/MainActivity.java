package com.example.leveluplife;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.leveluplife.data.entity.Task;
import com.example.leveluplife.data.repository.TaskRepository;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Тест Repository
        TaskRepository repository = new TaskRepository(getApplication());

        // Создаём тестовую задачу
        Task testTask = new Task(
                "Пробежать 5км",
                Task.TaskType.DAILY,
                Task.AttributeType.AGILITY,
                100,
                10
        );

        repository.insertTask(testTask);

        // TODO: позже подключим ViewModel + RecyclerView
    }
}
