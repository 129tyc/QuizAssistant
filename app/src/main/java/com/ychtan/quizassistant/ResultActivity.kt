package com.ychtan.quizassistant

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ychtan.quizassistant.adapter.ResultAdapter
import com.ychtan.quizassistant.data.Quiz
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val result = intent.getParcelableExtra<Quiz>("RESULT")
        list_result.adapter = ResultAdapter(result.options, this)
    }
}
