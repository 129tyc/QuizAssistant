package com.ychtan.quizassistant

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_splash.*
import org.ansj.domain.Term
import org.ansj.splitWord.analysis.ToAnalysis
import org.jsoup.Jsoup
import java.lang.ref.WeakReference

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AsyncLoadingTask(WeakReference(pb_splash), WeakReference(this))
                .execute("")
    }

    class AsyncLoadingTask(private val progressBar: WeakReference<ProgressBar>,
                           private val activity: WeakReference<SplashActivity>)
        : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            val bar = progressBar.get() ?: return
            bar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                ToAnalysis.parse("Magic Words 魔法单词")
                Jsoup.connect("https://www.baidu.com")
                        .get()
            } catch (e: Exception) {
                Log.e(this::class.simpleName, e.message, e)
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val bar = progressBar.get()
            val ay = activity.get()
            if (bar != null)
                bar.visibility = View.INVISIBLE
            if (result == null || ay == null) {
                Log.e(this::class.simpleName, "error...")
                return
            }
            ay.startActivity(Intent(ay, MainActivity::class.java))
            ay.finish()
        }


    }
}
