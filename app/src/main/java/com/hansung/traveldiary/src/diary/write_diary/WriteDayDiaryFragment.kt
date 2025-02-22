package com.hansung.traveldiary.src.diary.write_diary

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung.traveldiary.databinding.FragmentMakeDiaryDaySectionBinding
import com.hansung.traveldiary.src.MainActivity
import com.hansung.traveldiary.src.diary.write_diary.show_plan.DiaryImageEditActivity
import com.hansung.traveldiary.src.plan.TravelPlanMapFragment
import com.hansung.traveldiary.src.plan.model.SharedPlaceViewModel
import com.hansung.traveldiary.util.LoadingDialog

class WriteDayDiaryFragment(val index: Int, val day: Int) : Fragment() {
    private val binding: FragmentMakeDiaryDaySectionBinding by lazy {
        FragmentMakeDiaryDaySectionBinding.inflate(layoutInflater)
    }
    private lateinit var btmSheetFragment: SelectDayBtmSheetFragment
    private val viewModel: SelectDayViewModel by activityViewModels()
    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private val userPlanDataModel: SharedPlaceViewModel by viewModels()
    private lateinit var transaction: FragmentTransaction
    private lateinit var travelPlanMapFragment: TravelPlanMapFragment

    lateinit var mLoadingDialog: LoadingDialog

    private val diaryInfo = MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        user = Firebase.auth.currentUser
        db = Firebase.firestore

        if (diaryInfo.imagePathArray.size == 0) {
            binding.uploadViewPager.isVisible = false
            binding.indicator.isVisible = false
        } else {
            binding.uploadViewPager.isVisible = true
            binding.indicator.isVisible = true
            binding.uploadViewPager.adapter = WriteImageAdapter(diaryInfo.imagePathArray)
            binding.uploadViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            //인디케이터
            binding.indicator.setViewPager(binding.uploadViewPager)
        }

        binding.writeDiaryTitle.setText(MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.diaryTitle)
        binding.uploadContents.setText(MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.diaryContents)

        binding.uploadDiaryAddbtn.setOnClickListener {
            println("Fragment 버튼클릭")
            var intent = Intent(context, DiaryImageEditActivity::class.java)
            intent.putExtra("index", index)
            intent.putExtra("day", day)
            startActivity(intent)
        }

        binding.uploadDiaryCommitbtn.setOnClickListener{
            val idx = MainActivity.userDiaryArray[index].baseData.idx
            MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.diaryTitle = binding.writeDiaryTitle.text.toString()
            MainActivity.userDiaryArray[index].diaryArray[day].diaryInfo.diaryContents = binding.uploadContents.text.toString()

            db!!.collection("Diary").document(user!!.email.toString())
                .collection("DiaryData").document(MainActivity.userDiaryArray[index].baseData.idx.toString())
                .collection("DayList").document(MainActivity.userDiaryArray[index].diaryArray[day].date)
                .set(MainActivity.userDiaryArray[index].diaryArray[day]).addOnSuccessListener {
                    //전체 게시글에 저장
                    for(i in 0 until MainActivity.bulletinDiaryArray.size){
                        if(MainActivity.bulletinDiaryArray[i].userDiaryData.baseData.idx == idx){
                            MainActivity.bulletinDiaryArray[i].userDiaryData.diaryArray[day].diaryInfo.diaryTitle = binding.writeDiaryTitle.text.toString()
                            MainActivity.bulletinDiaryArray[i].userDiaryData.diaryArray[day].diaryInfo.diaryContents =binding.uploadContents.text.toString()
                            break
                        }
                    }
                    showCustomToast("저장되었습니다")
                }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        println("WriteDayFragment ${day} Start!!!!!!!!!!!!")
    }

    override fun onPause() {
        super.onPause()
        println("WriteDayFragment ${day} PAUSE!!!!!!!!!!!!")
    }

    override fun onStop() {
        super.onStop()
        println("WriteDayFragment ${day} PAUSE!!!!!!!!!!!!")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("WriteDayFragment ${day} DESTROY!!!!!!!!!!!!")
    }

    fun showCustomToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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