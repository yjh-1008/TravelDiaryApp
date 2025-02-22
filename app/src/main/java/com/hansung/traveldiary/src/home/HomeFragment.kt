package com.hansung.traveldiary.src.home

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hansung.traveldiary.R
import com.hansung.traveldiary.databinding.FragmentHomeBinding
import com.hansung.traveldiary.src.MainActivity
import com.hansung.traveldiary.src.WeeklyWeatherData
import com.hansung.traveldiary.src.home.adapter.*
import com.hansung.traveldiary.src.home.model.WeatherInfo
import com.hansung.traveldiary.src.home.weather.WeatherActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

data class  TipData(var image: Drawable, var content: String)
data class RecommandLocationData(val image: Drawable, val name: String)
data class HomeBulletinData(val image: String, val title: String, val contents: String, val nickname:String, val userImage:String, val likeCnt: Int, val commentsCnt: Int)

class HomeFragment : Fragment(), HomeView{
    private lateinit var binding : FragmentHomeBinding
    private val recommandLocationList = ArrayList<RecommandLocationData>()
    private val homeBulletinList = ArrayList<HomeBulletinData>()
    private val homeSaleList = ArrayList<SaleData>()
    private val homeTipList = ArrayList<TipData>()

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("Home Create")
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        activity?.window?.apply {
            this.statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        activity?.window?.apply {
            this.statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN }

        if(MainActivity.firstStart && MainActivity.weeklyList.size == 0){
            WeatherService(this).tryGetWeatherInfo()
            MainActivity.firstStart = false
        }else{
            binding.homeWeatherTemp.text = MainActivity.tempText
            binding.homeWeatherIcon.setImageDrawable(MainActivity.weatherIcon)
            binding.homeWeatherText.text= MainActivity.weatherMain
        }

        binding.homeClWeather.setOnClickListener{
            startActivity(Intent(context, WeatherActivity::class.java))
        }
//        // create our manager instance after the content view is set
//        val tintManager = SystemBarTintManager(activity)
//        // enable status bar tint\
//        tintManager.isStatusBarTintEnabled = true
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true)
//
//        // StatusBar를 20% 투명도를 가지게 합니다.
//        tintManager.setTintColor(Color.parseColor("#00000000"));


        initRecommandLocationList()
        initBulletinList()
        initSaleList()
        initTipList()
        binding.homeRvRecommand.apply{
            adapter = RecommandAdapter(recommandLocationList)
            setHasFixedSize(false)
            val horizontalManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            layoutManager = horizontalManager
        }


        binding.homeRvBulletin.apply{
            adapter = HomeBulletinAdapter(homeBulletinList)
            setHasFixedSize(false)
            val horizontalManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            layoutManager = horizontalManager
        }

        binding.homeSale.apply {
            val transaction =
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.home_sale, HomeSaleFragment()).commit()
        }

        binding.homeRvTip.apply{
            adapter = HomeTipAdapter(homeTipList)
            setHasFixedSize(false)
            val horizontalManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            layoutManager = horizontalManager
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        println("Home Start")
    }

    private fun initRecommandLocationList(){
        recommandLocationList.add(
            RecommandLocationData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_seoul,
                    null
                )!!, "서울"
            )
        )
        recommandLocationList.add(
            RecommandLocationData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.busan,
                    null
                )!!, "부산"
            )
        )
        recommandLocationList.add(
            RecommandLocationData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.forest   ,
                    null
                )!!, "강릉"
            )
        )
        recommandLocationList.add(
            RecommandLocationData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_jeju_island,
                    null
                )!!, "제주"
            )
        )
        recommandLocationList.add(
            RecommandLocationData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.seoul_ingen,
                    null
                )!!, "서울 근교"
            )
        )
    }

    fun initBulletinList(){
        homeBulletinList.add(
            HomeBulletinData(
                "https://api.cdn.visitjeju.net/photomng/imgpath/201911/28/1b150513-5d25-4212-826a-c70c6fd0ac78.jpg", "제주도 핫한 여행지 5곳", "천지연 폭포, 휴애리자연생활공원, 노형수퍼마켙, 제주 블라썸, 외돌개 황우지해안을 따라", "이영진", "https://cdn.pixabay.com/photo/2017/04/25/22/28/despaired-2261021_960_720.jpg"
                ,5,3)
        )
        homeBulletinList.add(
            HomeBulletinData(
                "https://www.yeosu.go.kr/tour/contents/7/odong1.jpg", "국내 여행 : 여수, 순천 여행", "아르떼 뮤지엄/구백식당/향일암/베네치아 호텔/이순신 광장 등 가볼만한 곳 추천", "한영원", "https://cdn.pixabay.com/photo/2012/02/23/08/38/rocks-15712_960_720.jpg"
                ,2,2)
        )

        homeBulletinList.add(
            HomeBulletinData(
                "https://data.si.re.kr/sites/default/files/2021-04/chimg_%281%29.png", "서울 당일치기 여행", "서울 데이트코스 덕수궁 돌담길 야경 맛집", "박정근", "https://cdn.pixabay.com/photo/2014/11/16/15/15/field-533541_960_720.jpg"
            ,4,5)
        )

        homeBulletinList.add(
            HomeBulletinData(
                "https://www.gn.go.kr/tour/images/tour/sub03/sub030210_img01.jpg", "아름다운 풍경 가득한 강릉 여행 코스", "정동진 레일바이크로 예쁜 풍경 구경하며 시원한 바닷바람 즐기기", "정예은", "https://cdn.pixabay.com/photo/2017/07/25/01/22/cat-2536662_960_720.jpg"
                ,4,2)
        )
    }

    private fun initSaleList(){
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
        homeSaleList.add(
            SaleData(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.img_sumset,
                    null
                )!!, "제주", "16,200원"
            )
        )
    }

    private fun initTipList(){
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip1, null)!!,
                "공항 이용 팁"
            )
        )
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip2, null)!!,
                "내일로 기차 여행"
            )
        )
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip3, null)!!,
                "짐가방 체크리스트"
            )
        )
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip4, null)!!,
                "지역 관광 사이트"
            )
        )
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip5, null)!!,
                "캠핑 준비하는 방법"
            )
        )
        homeTipList.add(
            TipData(
                ResourcesCompat.getDrawable(resources, R.drawable.img_tip6, null)!!,
                "별자리 구분법"
            )
        )
    }

    override fun onGetWeatherInfoSuccess(response: WeatherInfo) {
        val temp = floor(response.current.temp).toInt()
        val maxTemp = ceil(response.daily[0].temp.max).toInt()
        val minTemp = floor(response.daily[0].temp.min).toInt()
        val humidity = floor(response.current.humidity)
        val feels_like = ceil(response.current.feels_like)
        val windSpeed = ceil(response.current.wind_speed)
        val cloudy = response.current.clouds.toInt()

        MainActivity.cloudsText = "$cloudy%"
        MainActivity.maxTempText = "$maxTemp°C"
        MainActivity.minTempText = "$minTemp°C"
        MainActivity.tempText = "$temp°C"
        MainActivity.feelLikeTempText = "$feels_like°C"
        MainActivity.humidityText = "$humidity%"
        MainActivity.windSpeedText = "$windSpeed" + "m/s"
        MainActivity.weatherId = response.current.weather[0].id.toString()

        println("흐림: ${cloudy}")
        binding.homeWeatherTemp.text = MainActivity.tempText
        if(MainActivity.weatherId.substring(0, 1).equals("2")){
            MainActivity.weatherMain = "뇌우"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_thunderstorm_white,
                null
            )!!
        }else if(MainActivity.weatherId.substring(0, 1).equals("3")){
            MainActivity.weatherMain = "이슬비"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_drizzling_white,
                null
            )!!
        }else if(MainActivity.weatherId.substring(0, 1).equals("5")){
            MainActivity.weatherMain = "비"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_rain_white,
                null
            )!!
        }else if(MainActivity.weatherId.substring(0, 1).equals("6")){
            MainActivity.weatherMain = "눈"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_snow_white,
                null
            )!!
        }else if(MainActivity.weatherId.equals("800")){
            MainActivity.weatherMain = "맑음"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_sunny_white,
                null
            )!!
        }else if(MainActivity.weatherId.substring(0, 1).equals("8")){
            MainActivity.weatherMain = "구름"
            MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_cloudy_white,
                null
            )!!
        }else{
            if(MainActivity.weatherId.equals("771") || MainActivity.weatherId.equals("781")){
                MainActivity.weatherMain = "돌풍"
                MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.ic_windy_white,
                    null
                )!!
            }else if(MainActivity.weatherId.equals("731") || MainActivity.weatherId.equals("751") || MainActivity.weatherId.equals(
                    "761"
                ) || MainActivity.weatherId.equals("762")){
                MainActivity.weatherMain = "먼지"
                MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.ic_dust_white,
                    null
                )!!
            }else if(MainActivity.weatherId.substring(0, 1).equals("7")){
                MainActivity.weatherMain = "안개"
                MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.ic_fog_white,
                    null
                )!!
            }else{
                MainActivity.weatherMain = "맑음"
                MainActivity.weatherIcon = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.ic_sunny_white,
                    null
                )!!
            }
        }

        for(i in 1 until response.daily.size){
            val dt = response.daily[i].dt
            val simpleDateFragment = SimpleDateFormat("MM/dd")
            val date = Date(dt * 1000)

            val dateStr = simpleDateFragment.format(date)
            val weekId = response.daily[i].weather[0].id.toString()
            val icon = checkIcon(weekId)
            val maxTemp = ceil(response.daily[i].temp.max).toInt().toString() + "°"
            val minTemp = floor(response.daily[i].temp.min).toInt().toString() + "°"
            MainActivity.weeklyList.add(WeeklyWeatherData(dateStr, icon, minTemp, maxTemp))
        }
        binding.homeWeatherIcon.setImageDrawable(MainActivity.weatherIcon)
        binding.homeWeatherText.text= MainActivity.weatherMain
    }

    override fun onGetUserInfoFailure(message: String) {
        showCustomToast("오류 : $message")
    }

    private fun showCustomToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkIcon(weatherId: String) : Drawable{
        if(weatherId.substring(0, 1).equals("2")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_thunderstorm, null)!!
        }else if(weatherId.substring(0, 1).equals("3")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_drizzling, null)!!
        }else if(weatherId.substring(0, 1).equals("5")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_rain, null)!!
        }else if(weatherId.substring(0, 1).equals("6")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_snow_white, null)!!
        }else if(weatherId.equals("800")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_sunny, null)!!
        }else if(weatherId.substring(0, 1).equals("8")){
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_cloudy, null)!!
        }else{
            if(weatherId.equals("771") || MainActivity.weatherId.equals("781")){
                return ResourcesCompat.getDrawable(resources, R.drawable.ic_windy_white, null)!!
            }else if(weatherId.equals("731") || MainActivity.weatherId.equals("751") || MainActivity.weatherId.equals(
                    "761"
                ) || MainActivity.weatherId.equals("762")){
                return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_dust, null)!!
            }else if(weatherId.substring(0, 1).equals("7")){
                return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_fog, null)!!
            }else{
                return ResourcesCompat.getDrawable(resources, R.drawable.ic_new_sunny, null)!!
            }
        }
    }
}