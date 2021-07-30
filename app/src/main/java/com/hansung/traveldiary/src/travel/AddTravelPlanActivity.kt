package com.hansung.traveldiary.src.travel

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.hansung.traveldiary.databinding.ActivityAddTravelPlanBinding
import com.hansung.traveldiary.src.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.typeOf

class AddTravelPlanActivity : AppCompatActivity() {
    private val binding: ActivityAddTravelPlanBinding by lazy {
        ActivityAddTravelPlanBinding.inflate(layoutInflater)
    }

    private var user : FirebaseUser? = null
    private var db : FirebaseFirestore? = null
    private var titleList = TitleList()
    private val TAG = "AddPlanActivity"
    private var color = "pink"
    var date = ""
    var startdate = ""
    var enddate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        user = Firebase.auth.currentUser
        db = Firebase.firestore

        getTitleList()

        binding.icDatepicker.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            var listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                startdate = String.format("$y-%02d-%02d", m + 1, d)
                date += String.format("$y-%02d-%02d", m + 1, d)
                Log.d("시작날짜", date)
                binding.editDate.setText(date)
                Log.d("달력", "OK")
                showDatepicker()
            }

            val datePickerDialog =
                DatePickerDialog(this, listener, year, month, day)
            datePickerDialog.show()
        }

        binding.editPlace.setOnClickListener {
            val bottomDialog = PlanlistBottomDialog()
            bottomDialog.show(supportFragmentManager, "bottomPlanlistSheet")
        }

        setRadioButton()

        binding.addPlanBtn.setOnClickListener {
            var title = binding.editTitle.text.toString()
            var startDate = startdate
            var endDate = enddate

            titleList.titleFolder.add(title)

            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val startDateFormat = simpleDateFormat.parse("$startDate 00:00:00")!!
            val endDateFormat = simpleDateFormat.parse("$endDate 00:00:00")!!
            val calcDate =
                ((endDateFormat.time - startDateFormat.time) / (60 * 60 * 24 * 1000)).toInt()

//            var planTotalData = PlanTotalData(color, startDate, endDate, dayList)
//            MainActivity.planBookList.add(PlanBookData(title, planTotalData))

            val docPlanRef = db!!.collection(user!!.email.toString()).document("Plan")
            docPlanRef.set(titleList)


            docPlanRef.collection(title).document("BaseData").set(BaseData(color, startDate, endDate))
            val dayList = ArrayList<PlaceDayInfo>()
            for (i in 0..calcDate) {
                dayList.add(PlaceDayInfo(afterDate(startDate, i), arrayListOf()))
            }
            docPlanRef.collection(title).document("PlaceInfo").set(PlaceInfoFolder(dayList))


            val intent = intent.putExtra("title", title)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.apaOutblock.setOnClickListener {
            println("아웃")
            finish()
            overridePendingTransition(0, 0)
        }

        binding.apaMainblock.setOnClickListener {
            println("메인")
        }
    }

    fun afterDate(date: String, day: Int, pattern: String = "yyyy-MM-dd"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())

        val calendar = Calendar.getInstance()
        format.parse(date)?.let { calendar.time = it }
        calendar.add(Calendar.DAY_OF_YEAR, day)

        return format.format(calendar.time)
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

    fun getTitleList(){
        db!!.collection(user!!.email.toString()).document("Plan")
            .get()
            .addOnSuccessListener { result ->
                val data = result.data?.get("titleFolder")
                if(data != null){
                    titleList.titleFolder = data as ArrayList<String>
                    println("size: ${titleList.titleFolder.size}")
                    println("content: ${titleList.titleFolder[0]}")
                }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun showDatepicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        var listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            enddate = String.format("$y-%02d-%02d", m + 1, d)
            date += " ~ " + String.format("$y-%02d-%02d", m + 1, d)
            Log.d("끝날짜", date)
            binding.editDate.setText(date)
        }

        val datePickerDialog =
            DatePickerDialog(this, listener, year, month, day)
        datePickerDialog.show()
    }

}
