package com.ychtan.quizassistant

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.ychtan.quizassistant.data.Quiz
import kotlinx.android.synthetic.main.activity_main.*
import org.ansj.domain.Term
import org.ansj.splitWord.analysis.ToAnalysis
import java.lang.ref.WeakReference


open class MainActivity : AppCompatActivity() {

    private val defaultQuiz = Quiz("1799年乾隆皇帝去世，同年去世的另外一位美国总统是谁？",
            mutableListOf(),
            linkedMapOf("林肯" to 1.0, "华盛顿" to 1.0, "罗斯福" to 1.0))
    private var directed = false
    private var quiz = defaultQuiz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        quiz = intent?.getParcelableExtra("QUIZ") ?: defaultQuiz
        directed = intent.getBooleanExtra("DIRECT", false)
        edit_question.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        edit_question.gravity = Gravity.TOP
        edit_question.setSingleLine(false)
        edit_question.setHorizontallyScrolling(false)
        edit_question.setText(quiz.questionText)
        val options = quiz.options.toList()
        edit_option1.setText(options[0].first)
        edit_option2.setText(options[1].first)
        edit_option3.setText(options[2].first)
        btn_start.setOnClickListener {
            AsyncSegParseTask(WeakReference(progressBar), WeakReference(this))
                    .execute(quiz.questionText)
        }
    }

    override fun onResume() {
        super.onResume()
        if (directed) {
            directed = false
            AsyncSegParseTask(WeakReference(progressBar), WeakReference(this))
                    .execute(quiz.questionText)
        }
    }

    protected fun showAssist(list: MutableList<String>) {
        Log.v(this::class.simpleName, list.toString())
        if (!cb_sh_bd.isChecked && !cb_sh_gg.isChecked)
            Toast.makeText(this, "Select Search Engine!", Toast.LENGTH_SHORT)
                    .show()
        else {
            val intent = Intent(this, AssistActivity::class.java)
            intent.putExtra("DATA", Quiz(edit_question.text.toString(),
                    list,
                    linkedMapOf(edit_option1.text.toString() to 1.0,
                            edit_option2.text.toString() to 1.0,
                            edit_option3.text.toString() to 1.0)))
            intent.putExtra("BAIDU", cb_sh_bd.isChecked)
            intent.putExtra("GOOGLE", cb_sh_gg.isChecked)
            startActivity(intent)
        }
    }

    class AsyncSegParseTask(private val progressBar: WeakReference<ProgressBar>,
                            private val activity: WeakReference<MainActivity>)
        : AsyncTask<String, Int, List<Term>>() {

        private val filter = setOf("c", "dg", "d", "e", "ad", "a", "ag", "h", "p", "q", "rg", "u", "vd", "w", "yg", "y", "z")

        override fun onPreExecute() {
            super.onPreExecute()
            val bar = progressBar.get() ?: return
            bar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): List<Term> {
            return ToAnalysis.parse(params[0]).terms
        }

        override fun onPostExecute(result: List<Term>?) {
            super.onPostExecute(result)
            val bar = progressBar.get()
            val ay = activity.get()
            if (bar != null)
                bar.visibility = View.INVISIBLE
            if (result == null || ay == null) {
                Log.e(this::class.simpleName, "error...")
                return
            }
            ay.showAssist(
                    result
                            .filter { return@filter !filter.contains(it.natureStr) }
                            .map { return@map it.name }
                            .distinct()
                            .toMutableList())
        }


    }
}
