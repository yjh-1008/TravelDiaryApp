package com.hansung.traveldiary.src.plan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hansung.traveldiary.databinding.FragmentPlacelistBinding
import com.hansung.traveldiary.databinding.FragmentScheduleBinding
import com.hansung.traveldiary.src.diary.write_diary.PlacelistAdapter
import com.hansung.traveldiary.src.diary.write_diary.ShowPlacelistActivity
import com.hansung.traveldiary.src.plan.TravelPlanBaseActivity.Companion.index
import com.hansung.traveldiary.src.plan.adapter.ScheduleAdapter
import com.hansung.traveldiary.src.plan.model.SharedPlaceViewModel

class PlacelistFragment() : Fragment(){
    private lateinit var binding : FragmentPlacelistBinding
    val userPlaceDataModel : SharedPlaceViewModel by activityViewModels()
    private var title : String? = null
    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    var index = 0

    constructor(title: String?) : this() {
        this.title = title
    }

    companion object{
        var checked = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlacelistBinding.inflate(inflater, container, false)
        user = Firebase.auth.currentUser
        db = Firebase.firestore

        binding.placelistRecyclerview.apply{
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            var scheduleadapter = PlacelistAdapter(userPlaceDataModel, binding.tvChecked)
            scheduleadapter.title = title
            adapter = scheduleadapter
        }

        binding.tvChecked.setOnClickListener {
            checked = false
            val userDocRef = db!!.collection("User").document("UserData")
            userDocRef.collection(user!!.email.toString()).document("Diary").collection(title!!).document("PlanPlaceInfo")
                .set(ShowPlacelistActivity.placeInfoFolder)
            binding.tvChecked.visibility = View.GONE
            binding.placelistRecyclerview.adapter?.notifyDataSetChanged()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        println("Placelist fragment start")


        if(userPlaceDataModel.items.dayPlaceList[index!!].placeInfoArray.size != 0){
            binding.scheduleNoPlan.isVisible = false
            binding.placelistRecyclerview.isVisible = true
        }else{
            binding.scheduleNoPlan.isVisible = true
            binding.placelistRecyclerview.isVisible = false
        }
//        if (checked) {
//            binding.tvChecked.visibility = View.VISIBLE
//        }else{
//            binding.tvChecked.visibility = View.GONE
//        }

    }

    fun setIdx(idx : Int){
        index = idx
    }
}