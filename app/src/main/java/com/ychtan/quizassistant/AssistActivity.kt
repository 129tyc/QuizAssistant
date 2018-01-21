package com.ychtan.quizassistant

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.ychtan.quizassistant.adapter.CheckAdapter
import com.ychtan.quizassistant.data.Quiz
import kotlinx.android.synthetic.main.activity_assist.*
import org.jsoup.Jsoup
import org.jsoup.UncheckedIOException
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class AssistActivity : AppCompatActivity() {
    companion object {
        private const val QUERY_BAIDU = "http://www.baidu.com/s?wd="
        private const val QUERY_GOOGLE = "http://www.google.com/search?lr=lang_zh-CN|lang_zh-TW&hl=zh-CN&q="
        private const val QUERY_TIMEOUT = 2000
        private const val CSS_BAIDU = "div.nums"
        private const val CSS_GOOGLE = "div#resultStats"
        private const val DROP_FIRST_BAIDU = 15
        private const val DROP_LAST_BAIDU = 1
        private const val DROP_FIRST_GOOGLE = 4
        private const val DROP_LAST_GOOGLE = 4

        private val threadPool = ThreadPoolExecutor(6,
                10,
                10,
                TimeUnit.SECONDS,
                LinkedBlockingDeque<Runnable>(6))
    }

    @Volatile
    private var count = 0

    private val segList = mutableListOf<String>()
    private val checkQuestion = mutableListOf<Boolean>()
    private val checkOptions = mutableListOf<Boolean>()
    private var quiz: Quiz = Quiz("", mutableListOf(), linkedMapOf())
    private var integration = linkedMapOf<String, Double>()
    private var bAnswers = linkedMapOf<String, Double>()
    private var gAnswers = linkedMapOf<String, Double>()

    private var bEnable = true
    private var gEnable = true

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assist)
        bEnable = intent.getBooleanExtra("BAIDU", true)
        gEnable = intent.getBooleanExtra("GOOGLE", true)
        count = if (gEnable && bEnable) 0 else 3
        quiz = intent.getParcelableExtra("DATA")
        quiz.separateText.forEach {
            segList.add(it)
            checkQuestion.add(false)
        }
        if (checkOptions.isEmpty())
            quiz.options.forEach({
                checkOptions.add(false)
            })
        val optionsList = quiz
                .options
                .map { return@map it.key }
                .toList()
        val qAdapter = CheckAdapter(segList, this, checkQuestion)
        val oAdapter = CheckAdapter(optionsList, this, checkOptions)
        list_qusetion.adapter = qAdapter
        list_options.adapter = oAdapter
        btn_original.setOnClickListener {
            val optionsChecked = processOptions(optionsList)
            Log.v(this::class.simpleName, "oResult=" + optionsChecked)
            if (optionsChecked.isEmpty()) {
                showBrowser(quiz.questionText)
            } else
                queryResult(quiz.questionText + " ", optionsChecked)
        }
        btn_search.setOnClickListener {
            val result = StringBuilder()
            (0 until checkQuestion.size)
                    .filter { it < segList.size && checkQuestion[it] }
                    .forEach { result.append("\"" + segList[it] + "\"") }
            if (result.isEmpty()) {
                Toast.makeText(this, "Select Key Words!", Toast.LENGTH_SHORT)
                        .show()
                return@setOnClickListener
            }
            val optionsChecked = processOptions(optionsList)
            Log.v(this::class.simpleName, "qResult=" + result.toString())
            Log.v(this::class.simpleName, "oResult=" + optionsChecked)
            if (optionsChecked.isEmpty()) {
                showBrowser(result.toString())
            } else
                queryResult(result.toString(), optionsChecked)

        }
        btn_all_o.setOnClickListener({
            checkOptions.clear()
            quiz.options.forEach { checkOptions.add(true) }
            oAdapter.notifyDataSetChanged()
        })
        btn_none_o.setOnClickListener({
            checkOptions.clear()
            quiz.options.forEach { checkOptions.add(false) }
            oAdapter.notifyDataSetChanged()
        })
        btn_all_q.setOnClickListener({
            checkQuestion.clear()
            quiz.separateText.forEach { checkQuestion.add(true) }
            qAdapter.notifyDataSetChanged()
        })
        btn_none_q.setOnClickListener({
            checkQuestion.clear()
            quiz.separateText.forEach { checkQuestion.add(false) }
            qAdapter.notifyDataSetChanged()
        })
    }

    private fun processOptions(options: List<String>): List<String> {
        return (0 until checkOptions.size)
                .filter { it < options.size && checkOptions[it] }
                .map { return@map options[it] }
    }

    private fun showBrowser(question: String) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        if (gEnable)
            intent.data = Uri
                    .parse("http://www.google.com/search?lr=lang_zh-CN|lang_zh-TW&hl=zh-CN&q=" + question)
        else
            intent.data = Uri
                    .parse("http://www.baidu.com/s?wd=" + question)
        startActivity(intent)
    }

    private fun queryResult(question: String, options: List<String>) {
        bAnswers.clear()
        gAnswers.clear()
        options.forEach {
            if (bEnable)
                AsyncQueryTask(WeakReference(pb_search),
                        WeakReference(this),
                        bAnswers,
                        question,
                        QUERY_BAIDU,
                        CSS_BAIDU,
                        QUERY_TIMEOUT,
                        DROP_FIRST_BAIDU,
                        DROP_LAST_BAIDU,
                        6).executeOnExecutor(threadPool, it)
            if (gEnable)
                AsyncQueryTask(WeakReference(pb_search),
                        WeakReference(this),
                        gAnswers,
                        question,
                        QUERY_GOOGLE,
                        CSS_GOOGLE,
                        QUERY_TIMEOUT,
                        DROP_FIRST_GOOGLE,
                        DROP_LAST_GOOGLE,
                        6).executeOnExecutor(threadPool, it)

        }
    }

    private fun processResult() {
        Log.v(this::class.simpleName, "GoogleResult-->$gAnswers")
        Log.v(this::class.simpleName, "BaiduResult-->$bAnswers")
        integration.clear()
        if (bEnable) {
            var bCount = 0.0
            bAnswers.onEach { bCount += it.value }
                    .onEach { bAnswers.put(it.key, it.value / bCount) }
                    .forEach {
                        if (integration.containsKey(it.key))
                            integration.put(it.key, integration.getValue(it.key) + it.value)
                        else
                            integration.put(it.key, it.value)
                    }
        }
        if (gEnable) {
            var gCount = 0.0
            gAnswers.onEach { gCount += it.value }
                    .onEach { gAnswers.put(it.key, it.value / gCount) }
                    .forEach {
                        if (integration.containsKey(it.key))
                            integration.put(it.key, integration.getValue(it.key) + it.value)
                        else
                            integration.put(it.key, it.value)
                    }
        }
        Log.v(this::class.simpleName, "GoogleProcessResult-->$gAnswers")
        Log.v(this::class.simpleName, "BaiduProcessResult-->$bAnswers")
        if (gEnable && bEnable) {
            integration.forEach { integration.put(it.key, it.value / 2) }
        }
        Log.v(this::class.simpleName, integration.toString())
        showResult(integration)
    }

    private fun showResult(list: LinkedHashMap<String, Double>) {
        quiz.options.putAll(list)
        Log.v(this::class.simpleName, "ResultQuiz=$quiz")
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("RESULT", quiz)
        startActivity(intent)
    }

    class AsyncQueryTask(private val progressBar: WeakReference<ProgressBar>,
                         private val activity: WeakReference<AssistActivity>,
                         private val queryResult: LinkedHashMap<String, Double>,
                         private val queryQuestion: String,
                         private val queryUrl: String,
                         private val cssQuery: String,
                         private val timeout: Int,
                         private val dropFist: Int,
                         private val dropLast: Int,
                         private val countLimit: Int) :
            AsyncTask<String, Int, List<Pair<String, Double>>>() {
        companion object {
            private const val USER_AGENT = "Mozilla/5.0 (Windows; U; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)"
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val bar = progressBar.get()
            if (bar != null)
                bar.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: List<Pair<String, Double>>?) {
            super.onPostExecute(result)
            val bar = progressBar.get()
            val ay = activity.get()
            if (result == null || ay == null) {
                Log.e(this::class.simpleName, "error...")
                return
            }
            queryResult.putAll(result)
            ay.count++
            if (ay.count >= countLimit) {
                if (bar != null)
                    bar.visibility = View.INVISIBLE
                ay.count = if (ay.gEnable && ay.bEnable) 0 else countLimit / 2
                ay.processResult()
            }
        }

        override fun doInBackground(vararg params: String?): List<Pair<String, Double>> {
            val temp = mutableListOf<Pair<String, Double>>()
            params
                    .filterNotNull()
                    .forEach { temp.add(it to getQueryResult(it).toDouble()) }
            return temp

        }

        private fun getQueryResult(query: String): Int {
            Log.v(this::class.simpleName, "start query-->$queryUrl + $cssQuery + $queryQuestion + $query")
            val doc: Document?
            val conn = Jsoup
                    .connect(queryUrl +
                            queryQuestion + "\"" + query + "\"")
                    .userAgent(USER_AGENT)
                    .timeout(timeout)
            try {
                doc = conn?.get()
            } catch (e: UncheckedIOException) {
                Log.e(this::class.simpleName, e.message, e)
                return 1
            }
            if (doc != null) {
                val text = doc.select(cssQuery).first().text()
                Log.v(this::class.simpleName, "NumberText=" + text)
                try {
                    if (text != null && text.isNotEmpty())
                        return Integer.parseInt(text
                                .drop(dropFist)
                                .dropLast(dropLast)
                                .replace(",", ""))
                } catch (e: NumberFormatException) {
                    Log.e(this::class.simpleName, e.message, e)
                    return 1
                }
            }
            return 1
        }
    }

}

