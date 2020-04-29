package com.example.agarapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.net.URL
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    //variables
    private lateinit var imageView: ImageView
    private lateinit var imageButton: Button
    private lateinit var sendButton: Button
    private var imageData: ByteArray? = null
    private val postURL: String = "https://ptsv2.com/t/uwa7m-1588121273"

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Colony Counter"
        uploadPhotoBtn.setOnClickListener {
            pickImageFromGallery();
        }

        takePhoto.setOnClickListener {
            dispatchTakePictureIntent();
        }

        floatingActionButton.setOnClickListener {
            VolleyFileUploadRequest(Request.Method.POST),;
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent);
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }
    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun uploadImage() {
        imageData?: return
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                println("response is: $it")
            },
            Response.ErrorListener {
                println("error is: $it")
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageView.setImageURI(data?.data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    open class VolleyFileUploadRequest(
        method: Int,
        url: String,
        listener: Response.Listener<NetworkResponse>,
        errorListener: Response.ErrorListener) : Request<NetworkResponse>(method, url, errorListener) {
        private var responseListener: Response.Listener<NetworkResponse>? = null
        init {
            this.responseListener = listener
        }

        private var headers: Map<String, String>? = null
        private val divider: String = "--"
        private val ending = "\r\n"
        private val boundary = "imageRequest${System.currentTimeMillis()}"


        override fun getHeaders(): MutableMap<String, String> =
            when(headers) {
                null -> super.getHeaders()
                else -> headers!!.toMutableMap()
            }

        override fun getBodyContentType() = "multipart/form-data;boundary=$boundary"


        @Throws(AuthFailureError::class)
        override fun getBody(): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            try {
                if (params != null && params.isNotEmpty()) {
                    processParams(dataOutputStream, params, paramsEncoding)
                }
                val data = getByteData() as? Map<String, FileDataPart>?
                if (data != null && data.isNotEmpty()) {
                    processData(dataOutputStream, data)
                }
                dataOutputStream.writeBytes(divider + boundary + divider + ending)
                return byteArrayOutputStream.toByteArray()

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return super.getBody()
        }

        @Throws(AuthFailureError::class)
        open fun getByteData(): Map<String, Any>? {
            return null
        }

        override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
            return try {
                Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
            } catch (e: Exception) {
                Response.error(ParseError(e))
            }
        }

        override fun deliverResponse(response: NetworkResponse) {
            responseListener?.onResponse(response)
        }

        override fun deliverError(error: VolleyError) {
            errorListener?.onErrorResponse(error)
        }

        @Throws(IOException::class)
        private fun processParams(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding: String) {
            try {
                params.forEach {
                    dataOutputStream.writeBytes(divider + boundary + ending)
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"$ending")
                    dataOutputStream.writeBytes(ending)
                    dataOutputStream.writeBytes(it.value + ending)
                }
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("Unsupported encoding not supported: $encoding with error: ${e.message}", e)
            }
        }

        @Throws(IOException::class)
        private fun processData(dataOutputStream: DataOutputStream, data: Map<String, FileDataPart>) {
            data.forEach {
                val dataFile = it.value
                dataOutputStream.writeBytes("$divider$boundary$ending")
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"; filename=\"${dataFile.fileName}\"$ending")
                if (dataFile.type != null && dataFile.type.trim().isNotEmpty()) {
                    dataOutputStream.writeBytes("Content-Type: ${dataFile.type}$ending")
                }
                dataOutputStream.writeBytes(ending)
                val fileInputStream = ByteArrayInputStream(dataFile.data)
                var bytesAvailable = fileInputStream.available()
                val maxBufferSize = 1024 * 1024
                var bufferSize = min(bytesAvailable, maxBufferSize)
                val buffer = ByteArray(bufferSize)
                var bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    dataOutputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }
                dataOutputStream.writeBytes(ending)
            }
        }
    }
}

class FileDataPart(var fileName: String?, var data: ByteArray, var type: String)