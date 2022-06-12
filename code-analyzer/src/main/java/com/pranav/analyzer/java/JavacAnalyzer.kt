package com.pranav.analyzer.java

import android.content.Context
import android.content.SharedPreferences

import com.pranav.common.util.DiagnosticWrapper
import com.pranav.common.util.FileUtil
import com.sun.source.util.JavacTask
import com.sun.tools.javac.api.JavacTool

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.List
import java.util.Locale

import javax.tools.DiagnosticCollector
import com.pranav.analyzer.JavaSourceFromString;
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class JavacAnalyzer(context: Context) {

    private val prefs: SharedPreferences
    private var diagnostics = DiagnosticCollector<JavaFileObject>()
    private var isFirstUse = true

    init {
        prefs = context.getSharedPreferences("compiler_settings", Context.MODE_PRIVATE)
    }

    @Throws(IOException::class)
    fun analyze(name: String, code: String) {
        val output = File(FileUtil.getBinDir(), "classes")
        output.mkdirs()
        val version = prefs.getString("version", "7")!!

        val javaFileObjects = ArrayList<JavaFileObject>()
        javaFileObjects.add(
                JavaSourceFromString(name, code, JavaFileObject.Kind.SOURCE)
        )

        val tool = JavacTool.create()

        val standardJavaFileManager =
                tool.getStandardFileManager(
                        diagnostics, Locale.getDefault(), Charset.defaultCharset())
        standardJavaFileManager.setLocation(
                StandardLocation.PLATFORM_CLASS_PATH, getPlatformClasspath())
        standardJavaFileManager.setLocation(StandardLocation.CLASS_PATH, getClasspath())

        val args = ArrayList<String>()

        args.add("-proc:none")
        args.add("-source")
        args.add(version)
        args.add("-target")
        args.add(version)

        val task =
                (tool.getTask(
                                null,
                                standardJavaFileManager,
                                diagnostics,
                                args,
                                null,
                                javaFileObjects) as JavacTask)

        task.parse()
        task.analyze()
        standardJavaFileManager.close()
        isFirstUse = false
    }

    fun isFirstRun(): Boolean {
        return isFirstUse
    }

    fun reset() {
        diagnostics = DiagnosticCollector<JavaFileObject>()
    }

    fun getDiagnostics(): ArrayList<DiagnosticWrapper> {
        val problems = ArrayList<DiagnosticWrapper>()
        for (diagnostic in diagnostics.getDiagnostics()) {
            // since we're not compiling the whole project, there might be some errors
            // from files that we skipped, so it should mostly be safe to ignore these
            if (!diagnostic.getCode().startsWith("compiler.err.cant.resolve")) {
                problems.add(DiagnosticWrapper(diagnostic))
            }
        }
        return problems
    }

    private fun getClasspath(): ArrayList<File> {
        val classpath = ArrayList<File>()
        val clspath = prefs.getString("classpath", "")

        if (!clspath!!.isEmpty()) {
            for (clas in clspath!!.split(":")) {
                classpath.add(File(clas))
            }
        }
        return classpath
    }

    private fun getPlatformClasspath(): ArrayList<File> {
        val classpath = ArrayList<File>()
        classpath.add(File(FileUtil.getClasspathDir(), "android.jar"))
        classpath.add(File(FileUtil.getClasspathDir(), "core-lambda-stubs.jar"))
        return classpath
    }
}
