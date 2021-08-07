package com.hansung.traveldiary.src.bulletin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hansung.traveldiary.R
import com.hansung.traveldiary.databinding.ItemBulletinImageBinding
import com.hansung.traveldiary.src.DiaryDayInfo

class BulletinViewPagerAdapter(private val diaryinfoArray: ArrayList<DiaryDayInfo>): RecyclerView.Adapter<BulletinViewPagerAdapter.PagerViewHolder>() {

    class PagerViewHolder(val binding : ItemBulletinImageBinding) : RecyclerView.ViewHolder(binding.root) {
        var image : ImageView = binding.image

        var title = binding.btItemTvTitle
        var content=binding.btItmeTvContents
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val binding = ItemBulletinImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val context = holder.itemView.context
        val data=diaryinfoArray[position].diaryInfo
        println("길이")
        if(data.imagePathArray.size!=0) {
            val url = data.imagePathArray[0]
            Glide.with(context).load(url).apply(RequestOptions().fitCenter()).into(holder.image)
        }else{
            Glide.with(context).load(ResourcesCompat.getDrawable(context.resources,R.drawable.img_beach,null)).apply(RequestOptions().fitCenter()).into(holder.image)
        }
            holder.content.text=data.diaryContents
            holder.title.text=data.diaryTitle
    }

    override fun getItemCount(): Int = diaryinfoArray.size
}