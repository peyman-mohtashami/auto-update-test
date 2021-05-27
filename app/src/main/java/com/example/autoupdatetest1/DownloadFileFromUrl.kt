package com.example.autoupdatetest1

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


//in async task

//usage
// DownloadVideoAsyncTask async = new DownloadVideoAsyncTask(this);
// async.execute("www.site.com/idvideo.mp4");

class DownloadFileFromUrl(context: Context, progressBar: ProgressBar, delegate: TaskDelegate) :
    AsyncTask<String?, Int?, String?>() {

    private val mDelegate: TaskDelegate

    private val mContext: Context

    var bar: ProgressBar? = null

    fun setProgressBar(bar: ProgressBar?) {
        this.bar = bar
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    protected override fun doInBackground(vararg params: String?): String? {
        println("999")
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() !== HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                    .toString() + " " + connection.getResponseMessage()
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength: Int = connection.getContentLength()

            // download the file
            input = connection.getInputStream()

            val outputFile = File(mContext.filesDir, "app-release.apk")
            if (outputFile.exists()) {
                outputFile.delete()
            }

//            output = new FileOutputStream("/data/data/com.example.vadym.test1/textfile.txt");
            Log.d("ptg", "HHH: " + mContext.filesDir.toString() + "/new.apk")
//            Log.v("HHH",mContext.getFilesDir().toString() + "/new.apk")
//            output = FileOutputStream(mContext.filesDir.toString() + "/app-release.apk")
            outputFile.setReadable(true, false)
            output = FileOutputStream(outputFile)

            val data = ByteArray(1024) //4096
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                // allow canceling with back button
                if (isCancelled) {
                    input.close()
                    return null
                }
                total += count.toLong()
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((total * 100 / fileLength).toInt())
                output.write(data, 0, count)
            }
        } catch (e: Exception) {
            return e.toString()
        } finally {
            try {
                if (output != null) output.close()
                if (input != null) input.close()
            } catch (ignored: IOException) {
            }
            if (connection != null) connection.disconnect()
        }
        return null
    }

    protected override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        Log.d("ptg", "onProgressUpdate: " + values[0])
        bar?.progress = values[0]!! // ? values[0] : 0

    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        mDelegate.taskCompletionResult("Post Exec");
    }

    init {
        mContext = context
        mDelegate = delegate;
    }
}

interface TaskDelegate {
    fun taskCompletionResult(result: String?)
}

