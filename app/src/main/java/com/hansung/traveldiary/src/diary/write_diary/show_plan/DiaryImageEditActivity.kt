package com.hansung.traveldiary.src.diary.write_diary.show_plan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hansung.traveldiary.databinding.ActivityDiaryImageEditBinding
import com.hansung.traveldiary.src.MainActivity
import com.hansung.traveldiary.src.diary.write_diary.CountViewModel
import com.hansung.traveldiary.src.profile.gallery.SelectPictureActivity
import com.hansung.traveldiary.util.LoadingDialog
import com.hansung.traveldiary.util.StatusBarUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DiaryImageEditActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDiaryImageEditBinding.inflate(layoutInflater)
    }

    private lateinit var getResultImage: ActivityResultLauncher<Intent>
    private lateinit var imagePath: String
    private val countViewModel: CountViewModel by viewModels()
    private val imagePathList = ArrayList<String>()
    lateinit var mLoadingDialog: LoadingDialog

    private var index = 0
    private var day = 0
    private var title = ""
    private var user: FirebaseUser? = null

    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val context:Context = this
        StatusBarUtil.setStatusBarColor(this, StatusBarUtil.StatusBarColorType.WHITE_STATUS_BAR)
        user = Firebase.auth.currentUser
        db = Firebase.firestore
        index = intent.getIntExtra("index", 0)
        day = intent.getIntExtra("day", 0)


        val pathSize =
            MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.imagePathArray.size
            countViewModel.setCount(pathSize)
            for (i in 0 until pathSize) {
                imagePathList.add(MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.imagePathArray[i])
            }
            binding.textView5.isVisible=false

        countViewModel.imageCount.observe(this, Observer {
            binding.textView5.isVisible = countViewModel.imageCount.value == 0
        })

        binding.editImageRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = EditImageAdapter(countViewModel, imagePathList)
            setHasFixedSize(true)
        }

        getResultImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("갤러리", "OK")
                imagePath = result.data?.getStringExtra("imagePath")!!
                imagePathList.add(imagePath)
                countViewModel.countUp()
                binding.editImageRv.adapter?.notifyDataSetChanged()
            }
        }

        binding.addImageBtn.setOnClickListener {
            println("버튼클릭")
            if (countViewModel.imageCount.value!! >= 5) {
                showCustomToast("사진은 최대 5개까지 넣으실 수 있어요")
            } else {
                getResultImage.launch(Intent(this, SelectPictureActivity::class.java))

            }
        }


        binding.tvChecked.setOnClickListener {
            //이미지 메인 데이터에 넣고
            if (imagePathList.size != 0) {
                showLoadingDialog(this)
                for (i in 0 until imagePathList.size) {
                    if (!imagePathList[i].contains("https:")) {
                        val bitmap =
                            BitmapFactory.decodeFile(imagePathList[i], BitmapFactory.Options())
                        uploadFirebase2Image(bitmap, i + 1)
                    } else {
                        val url = URL(imagePathList[i])
                        val connection: HttpURLConnection =
                            url.openConnection() as HttpURLConnection
                        connection.setDoInput(true)
                        object : Thread() {
                            override fun run() {
                                try {
                                    connection.connect()
                                    val input: InputStream = connection.getInputStream()
                                    val bitmap = BitmapFactory.decodeStream(input)
                                    uploadFirebase2Image(bitmap, i + 1)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }.start()

                    }
                }
            } else {
                imagePathList.clear()
                MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.imagePathArray.clear()
                putImageIntoFirestore()
            }
        }
    }


    fun uploadFirebase2Image(bitmap: Bitmap, count: Int) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageStorageRef =
            storageRef.child("/diary/${user!!.email.toString()}/${MainActivity.userDiaryArray[index].baseData.idx}/day${day + 1}/image${count}.png")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, baos)
        val data = baos.toByteArray()

        val uploadTask = imageStorageRef.putBytes(data)
        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                imagePathList[count - 1] = downloadUri.toString()

                if (count == imagePathList.size) {
                    MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.imagePathArray.clear()
                    for (j in 0 until imagePathList.size) {
                        MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.imagePathArray.add(
                            imagePathList[j]
                        )
                    }
                    putImageIntoFirestore()
                }
            }
        }
    }
    
    fun putImageIntoFirestore(){
        showLoadingDialog(this)
        db!!.collection("Diary").document(user!!.email.toString())
            .collection("DiaryData")
            .document(MainActivity.userDiaryArray[index].baseData.idx.toString())
            .collection("DayList")
            .document(MainActivity.userDiaryArray[index].diaryArray[day].date)
            .set(MainActivity.userDiaryArray[index].diaryArray[day])
            .addOnSuccessListener {
                //전체 게시글도 갱신
                val idx = MainActivity.userDiaryArray[index].baseData.idx
                for(i in 0 until MainActivity.bulletinDiaryArray.size){
                    if(MainActivity.bulletinDiaryArray[i].userDiaryData.baseData.idx == idx){
                        MainActivity.bulletinDiaryArray[i].userDiaryData.diaryArray[day].diaryInfo.imagePathArray = imagePathList
                        break
                    }
                }
                dismissLoadingDialog()
                finish()
            }
    }

    fun showCustomToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showLoadingDialog(context: Context) {
        mLoadingDialog = LoadingDialog(context)
        mLoadingDialog.show()
    }

    fun dismissLoadingDialog() {
        if (mLoadingDialog.isShowing) {
            mLoadingDialog.dismiss()
        }

    }
}