package com.hansung.traveldiary.src

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hansung.traveldiary.src.home.HomeFragment
import com.hansung.traveldiary.R
import com.hansung.traveldiary.databinding.ActivityMainBinding
import com.hansung.traveldiary.src.bulletin.BulletinFragment
import com.hansung.traveldiary.src.chat.ChatFragment
import com.hansung.traveldiary.src.home.HomeBulletinData
import com.hansung.traveldiary.src.profile.ProfileFragment
import com.hansung.traveldiary.src.travel.AddBook.AddTravelPlanActivity
import com.hansung.traveldiary.src.travel.TravelBaseFragment
import com.hansung.traveldiary.util.LoadingDialog
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class WeeklyWeatherData(var date: String, var icon: Drawable, var min: String, var max: String)

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null

    private lateinit var addNewPlanBookTask: ActivityResultLauncher<Intent>
    private lateinit var updatePlanBookTask: ActivityResultLauncher<Intent>

    private val TAG = "MainActivity"

    companion object {
        var firstStart = true
        var weatherId: String = "800"
        var weatherMain: String = "맑음"
        lateinit var weatherIcon: Drawable
        var tempText = "30°C"
        var maxTempText = "30°C"
        var minTempText = "30°C"
        var feelLikeTempText = "30°C"
        var humidityText = "66%"
        var windSpeedText = "2.57m/s"
        var cloudsText: String = "30%"
        var weeklyList = ArrayList<WeeklyWeatherData>()

        var myFriendList = FriendList()
        var userList = UserList() //유저 이메일 정보 리스트
        var userInfoList = ArrayList<UserInfo>() //유저 닉네임, 이미지 정보 리스트
        var idxList = IdxList() //전체 idx 리스트
        var myPlanIdxList = IdxList() //나의 plan idx 리스트
        var myDiaryIdxList = IdxList() //나의 diary idx 리스트
        var bulletinIdxList = IdxList()
        var userPlanArray = ArrayList<UserPlanData>() //나의 plan data 리스트
        var userDiaryArray = ArrayList<UserDiaryData>() //나의 diary data 리스트
        var bulletinDiaryArray = ArrayList<BulletinData>() //게시글에 들어가는 전체 diary 리스트

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var isSend = false

        lateinit var mLoadingDialog: LoadingDialog

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initList()
        initBulletinList()

        weatherIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_sunny_white, null)!!
        user = Firebase.auth.currentUser
        db = Firebase.firestore
        getDBData()

        val pref = applicationContext.getSharedPreferences("login", 0)
        Log.d("MainActivity", pref.getString("login", "")!!)

        supportFragmentManager.beginTransaction().replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBtmNav.setOnNavigationItemSelectedListener(
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.main_btm_home -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, HomeFragment())
                            .commitAllowingStateLoss()
                        Log.d("확인", "home")
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.main_btm_bulletin -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, BulletinFragment())
                            .commitAllowingStateLoss()
                        Log.d("확인", "bulletin")
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.main_btm_travel -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, TravelBaseFragment())
                            .commitAllowingStateLoss()
                        Log.d("확인", "travel")
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.main_btm_message -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, ChatFragment())
                            .commitAllowingStateLoss()
                        Log.d("확인", "message")
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.main_btm_profile -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, ProfileFragment())
                            .commitAllowingStateLoss()
                        Log.d("확인", "profile")
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            })

        addNewPlanBookTask = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                showLoadingDialog(this)
                println("resultcode 들어옴")
                val idx = result.data?.getLongExtra("idx", 0)!!
                var addPlanBaseData: PlanBaseData
                var addPlanPlaceArray = ArrayList<PlaceInfo>()
                val addPlanRef =
                    db!!.collection("Plan").document(user!!.email.toString()).collection("PlanData")
                        .document(idx.toString())
                addPlanRef.get()
                    .addOnSuccessListener { result ->
                        addPlanBaseData = result.toObject<PlanBaseData>()!!

                        for (i in 0..getCalcDate(
                            addPlanBaseData.startDate,
                            addPlanBaseData.endDate
                        )) {
                            addPlanRef.collection("PlaceInfo")
                                .document(afterDate(addPlanBaseData.startDate, i)).get()
                                .addOnSuccessListener { result ->
                                    val placeData = result.toObject<PlaceInfo>()
                                    if (placeData != null)
                                        addPlanPlaceArray.add(placeData)
                                }
                        }
                        userPlanArray.add(UserPlanData(addPlanBaseData, addPlanPlaceArray))
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, TravelBaseFragment())
                            .commitAllowingStateLoss()
                        dismissLoadingDialog()
                    }
            }
        }

        updatePlanBookTask = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val index = result.data?.getIntExtra("index", 0)!!
                val idx = result.data?.getLongExtra("idx", 0)!!
                userPlanArray.removeAt(index)

                var updatePlanBaseData: PlanBaseData
                var updatePlanPlaceArray = ArrayList<PlaceInfo>()

                val updatePlanRef =
                    db!!.collection("Plan").document(user!!.email.toString()).collection("PlanData")
                        .document(idx.toString())
                updatePlanRef.get()
                    .addOnSuccessListener { result ->
                        updatePlanBaseData = result.toObject<PlanBaseData>()!!

                        for (i in 0..getCalcDate(
                            updatePlanBaseData.startDate,
                            updatePlanBaseData.endDate
                        )) {
                            updatePlanRef.collection("PlaceInfo")
                                .document(afterDate(updatePlanBaseData.startDate, i)).get()
                                .addOnSuccessListener { result ->
                                    val placeData = result.toObject<PlaceInfo>()
                                    if (placeData != null)
                                        updatePlanPlaceArray.add(placeData)
                                }
                        }
                        userPlanArray.add(UserPlanData(updatePlanBaseData, updatePlanPlaceArray))
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, TravelBaseFragment())
                            .commitAllowingStateLoss()
                        dismissLoadingDialog()
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firstStart = true
    }

    private fun initList() {
        myFriendList.friendFolder.clear()
        userList.emailFolder.clear()
        userInfoList.clear()
        idxList.idxFolder.clear()
        myPlanIdxList.idxFolder.clear()
        myDiaryIdxList.idxFolder.clear()
        userPlanArray.clear()
        userDiaryArray.clear()
        bulletinDiaryArray.clear()
    }

    fun makePlanBook() {
        addNewPlanBookTask.launch(Intent(this@MainActivity, AddTravelPlanActivity::class.java))
    }

    fun updatePlanBook(index: Int, modify: Boolean) {
        val intent = Intent(this@MainActivity, AddTravelPlanActivity::class.java)
        intent.putExtra("index", index)
        intent.putExtra("modify", true)
        updatePlanBookTask.launch(intent)
    }

    fun removePlanBook(index: Int) {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val idx = userPlanArray[index].baseData.idx

        //userPlanArray에서 삭제
        userPlanArray.removeAt(index)
        idxList.idxFolder.remove(idx)

        //IdxDatabase에서 삭제
        var totalIdxList = IdxList()
        val totalIdxRef = db.collection("IdxDatabase").document("IdxData")
        totalIdxRef.get()
            .addOnSuccessListener { result ->
                totalIdxList = result.toObject<IdxList>()!!
                totalIdxList.idxFolder.remove(idx)
                totalIdxRef.set(totalIdxList)
                idxList = totalIdxList
            }

        //Plan - 유저 email에서 idxFolder / PlanData 안 document 삭제.
        val userRef = db.collection("Plan").document(user!!.email.toString())
        myPlanIdxList.idxFolder.remove(idx)
        userRef.set(myPlanIdxList)

        userRef.collection("PlanData").document(idx.toString())
            .delete()
            .addOnSuccessListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, TravelBaseFragment())
                    .commitAllowingStateLoss()
            }
    }

    fun removeDiary(index: Int) {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val idx = userDiaryArray[index].baseData.idx

        userDiaryArray.removeAt(index)
        idxList.idxFolder.remove(idx)

        var totalIdxList = IdxList()
        val totalIdxRef = db.collection("IdxDatabase").document("IdxData")
        totalIdxRef.get()
            .addOnSuccessListener { result ->
                totalIdxList = result.toObject<IdxList>()!!
                totalIdxList.idxFolder.remove(idx)
                totalIdxRef.set(totalIdxList)
                idxList = totalIdxList
            }

        //Diary - 유저 email에서 idxFolder / DiaryData 안 document 삭제.
        val userRef = db.collection("Diary").document(user!!.email.toString())
        myDiaryIdxList.idxFolder.remove(idx)
        userRef.set(myDiaryIdxList)

        userRef.collection("DiaryData").document(idx.toString())
            .delete()
            .addOnSuccessListener {
                println("삭제성공")
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, TravelBaseFragment())
//                    .commitAllowingStateLoss()
            }
    }

    private fun getDBData() {
        showLoadingDialog(this)
        getMyFriendList()
        getUserList()
        getTotalIdxList()
        getMyPlanData()
        getMyDiaryData()
        getBulletinData()
        dismissLoadingDialog() //임시
    }

    private fun getMyFriendList() {
        db!!.collection("UserInfo").document(user!!.email.toString())
            .get().addOnSuccessListener { result ->
                val data = result.toObject<UserInfo>()
                if (data != null) {
                    myFriendList = data.friendList
                } else {
                    println("친구 없어")
                }
            }
    }

    private fun getUserList() {
        db!!.collection("UserData").document("UserEmail")
            .get()
            .addOnSuccessListener { result ->
                val data = result.toObject<UserList>()
                if(data != null){
                    userList = data
                    getUserInfoList()
                }
//                getAllDiaryData()
            }
    }

    private fun getUserInfoList() {
        for (email in userList.emailFolder) {
            db!!.collection("UserInfo").document(email)
                .get()
                .addOnSuccessListener { result ->
                    val data = result.toObject<UserInfo>()!!
                    userInfoList.add(data)
                    Log.d("chat", data.nickname +" / " + data.email + " / " + data.profileImage)
                }
        }
    }

    private fun getTotalIdxList() {
        db!!.collection("IdxDatabase").document("IdxData")
            .get()
            .addOnSuccessListener { result ->
                val data = result.toObject<IdxList>()
                if(data != null) idxList = data
            }
    }

    fun getBulletinData() {
        val bulletinRef =
            db!!.collection("Bulletin").document("BulletinData")

        bulletinRef.get().addOnSuccessListener { result ->
            println("getBulletin성공")
            val idxData = result.toObject<IdxList>()
            if (idxData != null) {
                bulletinIdxList = idxData
                for (idx in bulletinIdxList.idxFolder) {
                    var diaryBaseData = DiaryBaseData()
                    bulletinRef.collection(idx.toString()).document("idxUserData")
                        .get().addOnSuccessListener { userResult ->
                            val userInfo = userResult.toObject<UserInfo>()!!
                            Log.d("BulletinGet", userInfo.email+"/"+idx)
                            val baseRef = db!!.collection("Diary").document(userInfo.email)
                                .collection("DiaryData").document(idx.toString())
                            baseRef.get().addOnSuccessListener { baseResult ->
                                val baseData = baseResult.toObject<DiaryBaseData>()
                                if (baseData != null && baseData.lock == "false") {
                                    Log.d("Bulletin Get", "${baseData.idx} / ${baseData.userEmail}")
                                    diaryBaseData = baseData
                                    var diaryArray = ArrayList<DiaryInfo>()
                                    val calcDate = getCalcDate(diaryBaseData.startDate, diaryBaseData.endDate)
                                    for (i in 0..calcDate) {
                                        val date = afterDate(diaryBaseData.startDate, i)
                                        baseRef.collection("DayList").document(date)
                                            .get().addOnSuccessListener { dayResult ->
                                                val diaryData = dayResult.toObject<DiaryInfo>()
                                                if (diaryData != null) {
                                                    diaryArray.add(diaryData)
                                                    if (diaryArray.size == calcDate + 1) {
                                                        diaryArray.sortBy { it.date }
                                                        println("---------------------Bulletin체크  idx:${idx}-----------------")
                                                        for (j in 0 until diaryArray.size) {
                                                            println(diaryArray[j].date + " / " + diaryArray[j].diaryInfo.diaryTitle + " / " + diaryArray[j].diaryInfo.diaryContents)
                                                        }

                                                        bulletinDiaryArray.add(
                                                            BulletinData(
                                                                UserDiaryData(
                                                                    diaryBaseData,
                                                                    diaryArray
                                                                ), userInfo
                                                            )
                                                        )
                                                        println("MyBulletin SIZE: ${bulletinDiaryArray.size}")
                                                        dismissLoadingDialog()
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    fun getMyDiaryData() {
        val myDiaryIdxRef =
            db!!.collection("Diary").document(user!!.email.toString())
        myDiaryIdxRef.get()
            .addOnSuccessListener { result ->
                val idxData = result.toObject<IdxList>()
                if (idxData != null) {
                    myDiaryIdxList = idxData

                    for (myIdx in myDiaryIdxList.idxFolder) {
                        var diaryBaseData = DiaryBaseData()
                        var diaryArray = ArrayList<DiaryInfo>()

                        val myDiaryRef =
                            myDiaryIdxRef.collection("DiaryData").document(myIdx.toString())
                        myDiaryRef.get().addOnSuccessListener { baseResult ->
                            val diaryData = baseResult.toObject<DiaryBaseData>()
                            if (diaryData != null) {
                                diaryBaseData = diaryData
                                Log.d("수정체크", diaryBaseData.startDate)
                                val calcDate =
                                    getCalcDate(diaryBaseData.startDate, diaryBaseData.endDate)
                                for (i in 0..calcDate) {
                                    myDiaryRef.collection("DayList")
                                        .document(afterDate(diaryBaseData.startDate, i))
                                        .get().addOnSuccessListener { infoResult ->
                                            val diaryInfoData = infoResult.toObject<DiaryInfo>()
                                            if (diaryInfoData != null) {
                                                Log.d("수정체크", diaryInfoData.date)
                                                diaryArray.add(diaryInfoData)

                                                if (diaryArray.size == calcDate + 1) {
                                                    diaryArray.sortBy { it.date }
                                                    println("---------------------MyDiary체크-----------------")
                                                    for (j in 0 until diaryArray.size) {
                                                        println(diaryArray[j].date + " / " + diaryArray[j].diaryInfo.diaryTitle + " / " + diaryArray[j].diaryInfo.diaryContents)
                                                    }
                                                    userDiaryArray.add(
                                                        UserDiaryData(
                                                            diaryBaseData,
                                                            diaryArray
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }
    }

    fun getMyPlanData() {
        val myPlanIdxRef =
            db!!.collection("Plan").document(user!!.email.toString())

        myPlanIdxRef.get()
            .addOnSuccessListener { result ->
                println("getPlan성공")
                val idxData = result.toObject<IdxList>()
                if (idxData != null) {
                    myPlanIdxList = idxData
                    println("플랜 idx: ${myPlanIdxList.idxFolder.size}")
                    for (myIdx in myPlanIdxList.idxFolder) {
                        var planBaseData = PlanBaseData()
                        var placeArray = ArrayList<PlaceInfo>()
                        val myPlanRef = myPlanIdxRef.collection("PlanData")
                            .document(myIdx.toString())
                        myPlanRef.get().addOnSuccessListener { result ->
                            val planData = result.toObject<PlanBaseData>()
                            if (planData != null) {
                                planBaseData = planData

                                for (i in 0..getCalcDate(
                                    planBaseData.startDate,
                                    planBaseData.endDate
                                )) {
                                    myPlanRef.collection("PlaceInfo")
                                        .document(afterDate(planBaseData.startDate, i)).get()
                                        .addOnSuccessListener { result ->
                                            val placeData = result.toObject<PlaceInfo>()
                                            if (placeData != null)
                                                placeArray.add(placeData)
                                        }
                                }

                                userPlanArray.add(UserPlanData(planBaseData, placeArray))
                            }
                        }
                    }
                } else {
                    println("plan Idx null 아님")
                }
            }
    }

    fun getCalcDate(startDate: String, endDate: String): Int {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val startDateFormat = simpleDateFormat.parse("${startDate} 00:00:00")!!
        val endDateFormat = simpleDateFormat.parse("${endDate} 00:00:00")!!
        val calcDate =
            ((endDateFormat.time - startDateFormat.time) / (60 * 60 * 24 * 1000)).toInt()
        return calcDate
    }

    fun afterDate(date: String, day: Int, pattern: String = "yyyy-MM-dd"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())

        val calendar = Calendar.getInstance()
        format.parse(date)?.let { calendar.time = it }
        calendar.add(Calendar.DAY_OF_YEAR, day)

        return format.format(calendar.time)
    }

    fun initBulletinList(){
        var likeList3 = LikeFolder()
        for(i in 0 until 5) likeList3.likeUserFolder.add("1")

        var commentsList3 = CommentsFolder()
        for(i in 0 until 2) commentsList3.commentsFolder.add(CommentsData())
        bulletinDiaryArray.add(
            BulletinData(
                UserDiaryData(DiaryBaseData(0, "제주도 핫한 여행지 5곳","https://api.cdn.visitjeju.net/photomng/imgpath/201911/28/1b150513-5d25-4212-826a-c70c6fd0ac78.jpg", "", "", "", "", "", "", FriendList(), likeList3, commentsList3,"")), UserInfo("", "이영진", "https://cdn.pixabay.com/photo/2017/04/25/22/28/despaired-2261021_960_720.jpg", FriendList())
            )
        )

        var likeList4 = LikeFolder()
        for(i in 0 until 2) likeList4.likeUserFolder.add("1")

        var commentsList4 = CommentsFolder()
        for(i in 0 until 2) commentsList4.commentsFolder.add(CommentsData())
        bulletinDiaryArray.add(
            BulletinData(
                UserDiaryData(DiaryBaseData(0, "국내 여행 : 여수, 순천 여행","https://www.yeosu.go.kr/tour/contents/7/odong1.jpg", "", "", "", "", "", "", FriendList(), likeList4, commentsList4,"")), UserInfo("", "한영원", "https://cdn.pixabay.com/photo/2012/02/23/08/38/rocks-15712_960_720.jpg", FriendList())
            )
        )

        var likeList1 = LikeFolder()
        for(i in 0 until 4) likeList1.likeUserFolder.add("1")

        var commentsList1 = CommentsFolder()
        for(i in 0 until 5) commentsList1.commentsFolder.add(CommentsData())

        bulletinDiaryArray.add(
            BulletinData(
                UserDiaryData(DiaryBaseData(0, "서울당일치기여행","https://data.si.re.kr/sites/default/files/2021-04/chimg_%281%29.png", "", "", "", "", "", "", FriendList(), likeList1, commentsList1,"")), UserInfo("", "박정근", "https://cdn.pixabay.com/photo/2014/11/16/15/15/field-533541_960_720.jpg", FriendList())
            )
        )



        var likeList5 = LikeFolder()
        for(i in 0 until 4) likeList5.likeUserFolder.add("1")

        var commentsList5 = CommentsFolder()
        for(i in 0 until 2) commentsList5.commentsFolder.add(CommentsData())
        bulletinDiaryArray.add(
            BulletinData(
                UserDiaryData(DiaryBaseData(0, "아름다운 풍경 가득한 강릉 여행 코스","https://www.gn.go.kr/tour/images/tour/sub03/sub030210_img01.jpg", "", "", "", "", "", "", FriendList(), likeList5, commentsList5,"")), UserInfo("", "정예은", "https://cdn.pixabay.com/photo/2017/07/25/01/22/cat-2536662_960_720.jpg", FriendList())
            )
        )



//        bulletinDiaryArray.add(
//            BulletinData(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.ig_home_busan,
//                    null
//                )!!, "잊지못할 부산 1박 2일", "부산 해운데 더베이 101, 주변 여행 코스"
//            )
//        )
//        bulletinDiaryArray.add(
//            BulletinData(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.ig_home_gangneung,
//                    null
//                )!!, "제주도 핫한 여행지 5곳", "천지연 폭포, 휴애리자연생활공원, 노형수퍼마켙, 제주 블라썸, 외돌개 황우지해안을 따라"
//            )
//        )
//        bulletinDiaryArray.add(
//            BulletinData(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.ig_home_seoul_nearby,
//                    null
//                )!!, "국내 여행 : 여수, 순천 여행", "아르떼 뮤지엄/구백식당/향일암/베네치아 호텔/이순신 광장 등 가볼만한 곳 추천"
//            )
//        )
//        bulletinDiaryArray.add(
//            BulletinData(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.ig_home_daejeon,
//                    null
//                )!!, "아름다운 풍경 가득한 강릉 여행 코스", "정동진 레일바이크로 예쁜 풍경 구경하며 시원한 바닷바람 즐기기"
//            )
//        )
    }

}


