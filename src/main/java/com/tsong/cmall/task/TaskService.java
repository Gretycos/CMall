package com.tsong.cmall.task;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;

/**
 * @Author Tsong
 * @Date 2023/3/24 16:31
 */
@Component
public class TaskService {
    private final DelayQueue<Task> delayQueue = new DelayQueue<>();

    @PostConstruct
    private void init() {

        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    Task task = delayQueue.take();
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addTask(Task task) {
        if (delayQueue.contains(task)) {
            return;
        }
        delayQueue.add(task);
    }

    public void removeTask(Task task) {
        delayQueue.remove(task);
    }
}
