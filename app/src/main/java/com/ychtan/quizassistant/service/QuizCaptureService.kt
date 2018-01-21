package com.ychtan.quizassistant.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.ychtan.quizassistant.MainActivity
import com.ychtan.quizassistant.data.Quiz

/**
 * Created by 谈永成 on 2018/1/17.
 */
class QuizCaptureService : AccessibilityService() {
    override fun onInterrupt() {
        Log.v(this::class.simpleName, "onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null)
            return
//        if (event.className.toString() != "android.support.v7.widget.RecyclerView" &&
//                event.className.toString() != "android.widget.TextView")
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED)
            Log.v(this::class.simpleName, event.className.toString())
        if (event.className.toString() == "com.chongdingdahui.app.dialog.quiz.QuizFragment")
            captureQuiz()
    }

    private fun captureQuiz() {
        Log.v(this::class.simpleName, "start capture...")
        var textQuestion = ""
        var textOption1 = ""
        var textOption2 = ""
        var textOption3 = ""
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val nodeQuestion = rootNode.findAccessibilityNodeInfosByViewId("@id/tvMessage")
            if (nodeQuestion != null && nodeQuestion.isNotEmpty()) {
                textQuestion = nodeQuestion.first().text?.toString() ?: ""
            }
            val nodeAnswer1 = rootNode.findAccessibilityNodeInfosByViewId("@id/answer0")
            if (nodeAnswer1 != null && nodeAnswer1.isNotEmpty()) {
                textOption1 = nodeAnswer1.first().text?.toString() ?: ""
            }
            val nodeAnswer2 = rootNode.findAccessibilityNodeInfosByViewId("@id/answer0")
            if (nodeAnswer2 != null && nodeAnswer2.isNotEmpty()) {
                textOption2 = nodeAnswer2.first().text?.toString() ?: ""
            }
            val nodeAnswer3 = rootNode.findAccessibilityNodeInfosByViewId("@id/answer0")
            if (nodeAnswer3 != null && nodeAnswer3.isNotEmpty()) {
                textOption3 = nodeAnswer3.first().text?.toString() ?: ""
            }
            rootNode.recycle()
        } else
            Log.e(this::class.simpleName, "can not get root node!")
        val quiz = Quiz(textQuestion, mutableListOf(),
                linkedMapOf(textOption1 to 1.0, textOption2 to 1.0, textOption3 to 1.0))
        Log.v(this::class.simpleName, "Capture Result-->$quiz")
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("QUIZ", Quiz(textQuestion, mutableListOf(),
                linkedMapOf(textOption1 to 1.0, textOption2 to 1.0, textOption3 to 1.0)))
        intent.putExtra("DIRECT", true)
        startActivity(intent)
    }
}