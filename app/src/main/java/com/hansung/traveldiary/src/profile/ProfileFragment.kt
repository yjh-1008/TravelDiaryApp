package com.hansung.traveldiary.src.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hansung.traveldiary.databinding.FragmentHomeBinding
import com.hansung.traveldiary.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(){
    private lateinit var binding : FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        println("jyy test")
        println("push test")
        return binding.root
    }
}