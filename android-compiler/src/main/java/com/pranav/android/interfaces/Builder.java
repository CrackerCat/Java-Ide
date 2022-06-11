package com.pranav.android.interfaces;

import com.pranav.android.task.JavaBuilder;
import android.content.Context;

public sealed interface Builder permits JavaBuilder {

    public Context getContext();

    public ClassLoader getClassloader();
}
