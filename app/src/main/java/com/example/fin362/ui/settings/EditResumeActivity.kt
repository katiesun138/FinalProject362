package com.example.fin362.ui.settings

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.fin362.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditResumeActivity : AppCompatActivity() {
    private var filepath: String? = null
    private var filename: String? = null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_resume)
        val uploadTextView = findViewById<TextView>(R.id.tv_upload)
        uploadTextView.setOnClickListener{
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
        val saveTextView = findViewById<TextView>(R.id.tv_save)
        saveTextView.setOnClickListener{

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val selectedFilename = data?.data
        if (requestCode == 111 && resultCode == RESULT_OK) {
            if (data !== null){
                filename = selectedFilename.toString()
                filepath = getFilePath(this, data.data!!) //
                uploadfile()
            }
        }
    }


    fun getFilePath(context: Context, uri: Uri): String? {
        try {
            val returnCursor: Cursor? =
                context.getContentResolver().query(uri, null, null, null, null)
            val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val file = File(context.getFilesDir(), name)
            val inputStream: InputStream? = context.getContentResolver().openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream!!.available()
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            returnCursor.close()
            inputStream.close()
            outputStream.close()
            return file.getPath()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun uploadfile(){
        val fileTextView = findViewById<TextView>(R.id.tv_file)
        fileTextView.visibility = View.GONE
        val uploadTextView = findViewById<TextView>(R.id.tv_upload)
        uploadTextView.text = "Uploading"
        uploadTextView.setTextColor(resources.getColor(R.color.darkgray))
        uploadTextView.setBackgroundColor(resources.getColor(R.color.white))
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        var i = progressBar!!.progress
        Thread(Runnable {
            while(i<100){
                i += 5
                handler.post(Runnable {
                    progressBar!!.progress = i
                })
                try{
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (i == 100){
                    handler.post {
                        progressBar.visibility = View.GONE
                        val fileinfoLL = findViewById<LinearLayout>(R.id.ll_fileinfo)
                        val fileImageView = findViewById<ImageView>(R.id.iv_file)
                        val filetype = filepath.toString().substringAfterLast('.', "")
                        if (filetype == "pdf") {
                            fileImageView.setImageDrawable(resources.getDrawable(R.drawable.pdf))
                        } else if (filetype == "doc") {
                            fileImageView.setImageDrawable(resources.getDrawable(R.drawable.doc))
                        } else if (filetype == "docx") {
                            fileImageView.setImageDrawable(resources.getDrawable(R.drawable.docx))
                        }
                        val filenameTextView = findViewById<TextView>(R.id.tv_filename)

                        filenameTextView.text = filename
                        val filesizeTextView = findViewById<TextView>(R.id.tv_filesize)
                        filesizeTextView.text = "287 KB"
                        fileinfoLL.visibility = View.VISIBLE
                        uploadTextView.visibility = View.GONE
                        val saveTextView = findViewById<TextView>(R.id.tv_save)
                        saveTextView.isEnabled = true
                        saveTextView.setBackgroundColor(resources.getColor(R.color.blue))
                    }
                }
            }
        }).start()
    }
}