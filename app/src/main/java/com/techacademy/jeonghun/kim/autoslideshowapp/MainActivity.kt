package com.techacademy.jeonghun.kim.autoslideshowapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val imageURIs = arrayListOf<Uri>()

    private var currentImageIndex:Int = 0;

    private var mTimer: Timer? = null;
    private var mHandler = Handler()
    private var mTimerSec = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init onclick
        prev_button.setOnClickListener(this)
        start_pause_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
    }

    override fun onResume(){
        super.onResume()
        //reload URIs
        initURI()
        //pause
        prev_button.isEnabled = true
        next_button.isEnabled = true
        start_pause_button.text = "再生"
    }

    override fun onStop(){
        super.onStop()
        //disable timer
        if (mTimer != null){
            mTimer!!.cancel()
            mTimer = null
        }
        mTimerSec = 0.0;
    }

    private fun initURI(){
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        if(imageURIs.count() > 0)
        {
            currentImageIndex = 0;
            imageView.setImageURI(imageURIs[0])
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageURIs.add(imageUri)
                Log.d("ANDROIDURI", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    override fun onClick(v: View){
        if(imageURIs.count() <= 0) {
            // error
            Snackbar.make(v, "表示可能な写真がありません。", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return
        }

        when(v.id){
            R.id.next_button -> {
                currentImageIndex++
                if(currentImageIndex >= imageURIs.count()){
                    currentImageIndex = 0;
                }
                imageView.setImageURI(imageURIs[currentImageIndex])
                var ind = imageURIs[currentImageIndex]
                Log.d("ANDROIDURI","currentURI $ind")
            }
            R.id.prev_button -> {
                currentImageIndex--
                if(currentImageIndex < 0){
                    currentImageIndex = imageURIs.count()-1
                }
                imageView.setImageURI(imageURIs[currentImageIndex])
                var ind = imageURIs[currentImageIndex]
                Log.d("ANDROIDURI","currentURI $ind")
            }
            R.id.start_pause_button -> {
                Log.d("DEBUGTIMER", "Button Clicked")
                //set timer
                if(mTimer == null)
                {
                    //tap setting
                    prev_button.isEnabled = false
                    next_button.isEnabled = false
                    start_pause_button.text = "停止"

                    mTimer = Timer()
                    Log.d("DEBUGTIMER", "Timer Created")
                    mTimer!!.schedule(object : TimerTask(){
                        override fun run(){
                            mTimerSec += 2
                            currentImageIndex++
                            if(currentImageIndex >= imageURIs.count()){
                                currentImageIndex = 0;
                            }
                            mHandler.post{
                                imageView.setImageURI(imageURIs[currentImageIndex])
                                Log.d("DEBUGTIMER", "$currentImageIndex")
                            }
                        }
                    }, 2000,2000)
                    Snackbar.make(v, "スライドショー再生が始まりました。", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
                else
                {
                    //tap setting
                    prev_button.isEnabled = true
                    next_button.isEnabled = true
                    start_pause_button.text = "再生"
                    if (mTimer != null){
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    mTimerSec = 0.0;
                    Snackbar.make(v, "スライドショー再生が止まりました。", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
            }
        }
    }

}