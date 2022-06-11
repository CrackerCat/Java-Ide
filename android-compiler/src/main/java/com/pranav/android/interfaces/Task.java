package com.pranav.android.interfaces;

import com.pranav.android.task.java.*;

public sealed interface Task permits ECJCompilationTask, JavacCompilationTask, JarTask, D8Task, ExecuteJavaTask {

    public String getTaskName();

    public void doFullTask() throws Exception;
}
