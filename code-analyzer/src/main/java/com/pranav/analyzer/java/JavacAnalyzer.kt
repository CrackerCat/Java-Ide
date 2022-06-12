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
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.StandardLocation

class JavacAnalyzer(context: Context) {

    private val prefs: SharedPreferences
    private var diagnostics = DiagnosticCollector<JavaFileObject>()
    private var isFirstUse = true

    init {
        prefs = context.getSharedPreferences("compiler_settings", Context.MODE_PRIVATE)
    }

    @Throws(IOException::class)
    fun analyze() {

        val output = File(FileUtil.getBinDir(), "classes")
        output.mkdirs()
        val version = prefs.getString("version", "7")

        val javaFileObjects = ArrayList<JavaFileObject>()
        val javaFiles = getSourceFiles(File(FileUtil.getJavaDir()))
        for (val file in javaFiles) {
            javaFileObjects.add(
                    SimpleJavaFileObject(file.toURI(), JavaFileObject.Kind.SOURCE) {
                        @Throws(IOException::class)
                        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
                            return FileUtil.readFile(file)
                        }
                    })
        }

        val tool = JavacTool.create()

        val standardJavaFileManager =
                tool.getStandardFileManager(
                        diagnostics, Locale.getDefault(), Charset.defaultCharset())
        standardJavaFileManager.setLocation(
                StandardLocation.PLATFORM_CLASS_PATH, getPlatformClasspath())
        standardJavaFileManager.setLocation(StandardLocation.CLASS_PATH, getClasspath())
        standardJavaFileManager.setLocation(StandardLocation.SOURCE_PATH, javaFiles)

        val args = ArrayList<String>()

        args.add("-proc:none")
        args.add("-source")
        args.add(version)
        args.add("-target")
        args.add(version)

        val task =
                (JavacTask)
                        tool.getTask(
                                null,
                                standardJavaFileManager,
                                diagnostics,
                                args,
                                null,
                                javaFileObjects)

        task.parse()
        task.analyze()
        isFirstUse = false
    }

    fun isFirstRun(): Boolean {
        return isFirstUse
    }

    fun reset() {
        diagnostics = DiagnosticCollector<>()
    }

    fun getDiagnostics(): List<DiagnosticWrapper> {
        val problems = ArrayList<DiagnosticWrapper>()
        for (val diagnostic in diagnostics.getDiagnostics()) {
            problems.add(DiagnosticWrapper(diagnostic))
        }
        return problems
    }

    private fun getSourceFiles(path: File): ArrayList<File> {
        val sourceFiles = ArrayList<File>();
        val files = path.listFiles()
        if (files == null) {
            return ArrayList<File>()
        }
        for (val file in files) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    sourceFiles.add(file)
                }
            } else {
                sourceFiles.addAll(getSourceFiles(file))
            }
        }
        return sourceFiles
    }

    private fun getClasspath(): ArrayList<File> {
        val classpath = ArrayList<File>()
        val clspath = prefs.getString("classpath", "")

        if (!clspath.isEmpty()) {
            for (val clas in clspath.split(":")) {
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
