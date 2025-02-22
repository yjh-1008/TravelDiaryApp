package com.hansung.traveldiary.src.travel.AddBook

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hansung.traveldiary.R
import com.hansung.traveldiary.databinding.ActivityAddTravelPlanBinding
import com.hansung.traveldiary.src.*
import com.hansung.traveldiary.src.travel.AddBook.adapter.SelectedFriendsAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddTravelPlanActivity : AppCompatActivity() {
    private val binding: ActivityAddTravelPlanBinding by lazy {
        ActivityAddTravelPlanBinding.inflate(layoutInflater)
    }

    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var emailList = UserList()
    private val TAG = "AddPlanActivity"
    private var color = "pink"
    private var area = ""
    var date = ""
    var startdate = ""
    var enddate = ""
    private val areaViewModel: AreaViewModel by viewModels()
    var activity = AtpAddFriendsActivity()

    private var isModify = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        user = Firebase.auth.currentUser
        db = Firebase.firestore

        isModify = intent.getBooleanExtra("modify", false)
        if (isModify) {
            val index = intent.getIntExtra("index", 0)
            binding.atpTitle.setText(MainActivity.userPlanArray[index].baseData.title)
            val data = MainActivity.userPlanArray[index].baseData
//            binding.atpPeople.setText(data.peopleCount.toString())
            //이미 선택된 친구 리스트 뜨게 : 친구 리스트 중에서 이메일 비교해서 같으면 추가?
            AtpAddFriendsActivity.selectedArray.clear()
            for (friendsemail in data.friendsList.friendFolder){
                Log.d("추가된 친구", friendsemail)
                for (i in 0.. MainActivity.myFriendList.friendFolder.size-1){
                    if (MainActivity.myFriendList.friendFolder[i] == friendsemail){
                        //userinfo랑 일치하는거 찾아서 add
                        for (j in 0 until MainActivity.userInfoList.size) {
                            if (MainActivity.userInfoList[j].email == friendsemail) {
                                val friendInfo = FriendInfo(
                                    MainActivity.userInfoList[j].email,
                                    MainActivity.userInfoList[j].nickname,
                                    MainActivity.userInfoList[j].profileImage,
                                    false
                                )
                                AtpAddFriendsActivity.selectedArray.add(friendInfo)
                            }
                        }
                    }
                }
            }
            areaViewModel.setArea(data.area)
            binding.atpStartDate.text = data.startDate
            binding.atpEndDate.text = data.endDate
            startdate = data.startDate
            enddate = data.endDate
            binding.addPlanBtn.text = "수정"
        }

        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding.atpTitle.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    inputMethodManager.hideSoftInputFromWindow(binding.atpTitle.windowToken, 0)
                }
            }

//        binding.atpPeople.onFocusChangeListener =
//            View.OnFocusChangeListener { v, hasFocus ->
//                if (!hasFocus) {
//                    inputMethodManager.hideSoftInputFromWindow(binding.atpTitle.windowToken, 0)
//                }
//            }

        binding.atpTvPlace.setOnClickListener {
            binding.atpTitle.clearFocus()
//            binding.atpPeople.clearFocus()
            val bottomDialog = SelectAreaBtmDialog()
            bottomDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomDialogTheme)
            bottomDialog.show(supportFragmentManager, "bottomPlanlistSheet")
        }

        areaViewModel.areaData.observe(this, androidx.lifecycle.Observer<String> {
            println("바뀜: + ${areaViewModel.areaData.value.toString()}")
            binding.atpTvPlace.text = areaViewModel.areaData.value
            println("text: ${binding.atpTvPlace.text}")
        })

        binding.atpStartDate.setOnClickListener {
            binding.atpTitle.clearFocus()
//            binding.atpPeople.clearFocus()
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            var listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                startdate = String.format("$y-%02d-%02d", m + 1, d)
                date = String.format("$y-%02d-%02d", m + 1, d)
                Log.d("시작날짜", date)
                binding.atpStartDate.setText(date)
                Log.d("달력", "OK")
            }

            val datePickerDialog =
                DatePickerDialog(this, listener, year, month, day)
            datePickerDialog.show()
        }

        binding.atpEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            var listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                enddate = String.format("$y-%02d-%02d", m + 1, d)
                Log.d("끝날짜", enddate)
                binding.atpEndDate.setText(enddate)
                if(startdate>enddate){
                    Log.d("날짜 뒤바뀜", "$startdate")
                    binding.atpStartDate.setText(enddate)
                    binding.atpEndDate.setText(startdate)
                }
            }

            val datePickerDialog =
                DatePickerDialog(this, listener, year, month, day)
            datePickerDialog.show()
        }

        binding.atpAddFriends.setOnClickListener {
//            supportFragmentManager?.beginTransaction()?.replace(R.id.main_frm, AtpAddFriendsFragment()).commit()
            val intent = Intent(it.context, activity::class.java)
            startActivity(intent)
        }

        setRadioButton()

        binding.addPlanBtn.setOnClickListener {
            val area = areaViewModel.areaData.value
            val title = binding.atpTitle.text.toString()
            val startDate = binding.atpStartDate.text.toString()
            val endDate = binding.atpEndDate.text.toString()
//            val peopleCount = binding.atpPeople.text.toString().toInt()
            //selectedarray 에 있는 친구 이메일만 추가, MainActivity, Dataclass 수정
            val friendList = FriendList()
            for (selectFriends in AtpAddFriendsActivity.selectedArray)
                friendList.friendFolder.add(selectFriends.email)


            if(!isModify){
                val totalIdxRef = db!!.collection("IdxDatabase").document("IdxData")
                var idx : Long = 0L
                while(true){
                    idx = makeIdx()
                    if(!MainActivity.idxList.idxFolder.contains(idx)) {
                        MainActivity.idxList.idxFolder.add(idx)
                        totalIdxRef.set(MainActivity.idxList)
                            .addOnSuccessListener {
                                println("여기 지나고 나서-success-${MainActivity.idxList.idxFolder.size}")
                            }
                        break
                    }
                }

                var userIdxList = IdxList()

                val userIdxRef = db!!.collection("Plan").document(user!!.email.toString())
                userIdxRef.get()
                    .addOnSuccessListener { result ->
                        val data = result.toObject<IdxList>()
                        if(data != null){
                            userIdxList = data
                        }
                        userIdxList.idxFolder.add(idx)
                        userIdxRef.set(userIdxList)
                    }
                uploadData(idx, title, color, startDate, endDate, area!!, friendList)
            }else{
                val index = intent.getIntExtra("index", 0)
                uploadData(MainActivity.userPlanArray[index].baseData.idx, title, color, startDate, endDate, area!!, friendList)
            }
        }

        binding.apaOutblock.setOnClickListener {
            println("아웃")
            finish()
            overridePendingTransition(0, 0)
        }

        binding.apaMainblock.setOnClickListener {
            println("메인")
            binding.atpTitle.clearFocus()
//            binding.atpPeople.clearFocus()
        }
    }

    override fun onStart() {
        super.onStart()


        binding.atpFriendsRv.adapter = SelectedFriendsAdapter(AtpAddFriendsActivity.selectedArray, activity, this)
        binding.atpFriendsRv.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.atpFriendsRv.adapter?.notifyDataSetChanged()
        if (AtpAddFriendsActivity.selectedArray.size != 0)
            binding.atpAddFriends.visibility = View.GONE
        Log.d("친구리스트", AtpAddFriendsActivity.selectedArray.size.toString())
    }

    override fun onDestroy() {
        super.onDestroy()

        AtpAddFriendsActivity.selectedArray.clear()
    }

    fun showBtn(){
        binding.atpAddFriends.visibility = View.VISIBLE
    }

    fun uploadData(idx: Long, title: String, color: String, startDate: String, endDate: String, area: String, friendList: FriendList){
        val planRef = db!!.collection("Plan").document(user!!.email.toString())
            .collection("PlanData").document(idx.toString())
        planRef.set(
            PlanBaseData(
                idx,
                title,
                color,
                startDate,
                endDate,
                area,
                friendList
            )
        )
        for (i in 0..getCalcDate(startDate, endDate)) {
            val placeRef = planRef.collection("PlaceInfo").document(afterDate(startDate,i))
            placeRef.set(
                PlaceInfo(
                    ArrayList()
                )
            )
        }

        val resultIntent = Intent()
        resultIntent.putExtra("idx", idx)
        if(isModify){
            val index = intent.getIntExtra("index", 0)
            resultIntent.putExtra("index", index)
        }

        println("여기 finish-${MainActivity.idxList.idxFolder.size}")
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    fun makeIdx() : Long{
        val str = StringBuilder()
        val random = Random()
        for(i in 0 until 8){
            val num = random.nextInt(9)+1
            str.append(num.toString())
        }
        return str.toString().toLong()
    }

    fun afterDate(date: String, day: Int, pattern: String = "yyyy-MM-dd"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())

        val calendar = Calendar.getInstance()
        format.parse(date)?.let { calendar.time = it }
        calendar.add(Calendar.DAY_OF_YEAR, day)

        return format.format(calendar.time)
    }

    fun getCalcDate(startDate: String, endDate: String): Int {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val startDateFormat = simpleDateFormat.parse("${startDate} 00:00:00")!!
        val endDateFormat = simpleDateFormat.parse("${endDate} 00:00:00")!!
        val calcDate =
            ((endDateFormat.time - startDateFormat.time) / (60 * 60 * 24 * 1000)).toInt()
        return calcDate
    }

    fun setRadioButton() {
        binding.addPlanRbPink.setOnClickListener {
            println("pink")
            color = "pink"
            binding.addPlanRbPink.isChecked = true
            binding.addPlanRbBlue.isChecked = false
            binding.addPlanRbYellow.isChecked = false
            binding.addPlanRbSky.isChecked = false
            binding.addPlanRbPurple.isChecked = false
            binding.addPlanRbOrange.isChecked = false
        }
        binding.addPlanRbBlue.setOnClickListener {
            println("blue")
            color = "blue"
            binding.addPlanRbPink.isChecked = false
            binding.addPlanRbBlue.isChecked = true
            binding.addPlanRbYellow.isChecked = false
            binding.addPlanRbSky.isChecked = false
            binding.addPlanRbPurple.isChecked = false
            binding.addPlanRbOrange.isChecked = false
        }
        binding.addPlanRbYellow.setOnClickListener {
            println("yellow")
            color = "yellow"
            binding.addPlanRbPink.isChecked = false
            binding.addPlanRbBlue.isChecked = false
            binding.addPlanRbYellow.isChecked = true
            binding.addPlanRbSky.isChecked = false
            binding.addPlanRbPurple.isChecked = false
            binding.addPlanRbOrange.isChecked = false
        }
        binding.addPlanRbSky.setOnClickListener {
            println("sky")
            color = "sky"
            binding.addPlanRbPink.isChecked = false
            binding.addPlanRbBlue.isChecked = false
            binding.addPlanRbYellow.isChecked = false
            binding.addPlanRbSky.isChecked = true
            binding.addPlanRbPurple.isChecked = false
            binding.addPlanRbOrange.isChecked = false
        }

        binding.addPlanRbPurple.setOnClickListener {
            println("purple")
            color = "purple"
            binding.addPlanRbPink.isChecked = false
            binding.addPlanRbBlue.isChecked = false
            binding.addPlanRbYellow.isChecked = false
            binding.addPlanRbSky.isChecked = false
            binding.addPlanRbPurple.isChecked = true
            binding.addPlanRbOrange.isChecked = false
        }

        binding.addPlanRbOrange.setOnClickListener {
            println("orange")
            color = "orange"
            binding.addPlanRbPink.isChecked = false
            binding.addPlanRbBlue.isChecked = false
            binding.addPlanRbYellow.isChecked = false
            binding.addPlanRbSky.isChecked = false
            binding.addPlanRbPurple.isChecked = false
            binding.addPlanRbOrange.isChecked = true
        }
    }
}
