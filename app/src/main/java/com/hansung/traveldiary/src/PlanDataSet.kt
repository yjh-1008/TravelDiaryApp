package com.hansung.traveldiary.src

import com.google.firebase.auth.FirebaseUser


data class BaseData(var color: String = "", var startDate: String = "", var endDate: String = "")
data class TitleList(var titleFolder: ArrayList<String> = ArrayList())

data class PlaceInfo(var placeName: String? = null, var latitude: Double = 37.58842461354086, var longitude: Double = 127.00601781685579)
data class PlaceDayInfo(var date: String = "", var placeInfoArray : ArrayList<PlaceInfo> = ArrayList<PlaceInfo>())
data class PlaceInfoFolder(var dayPlaceList: ArrayList<PlaceDayInfo> = ArrayList())

//class PlanTotalData(
//    val titleData: TitleData = TitleData(),
//    val baseData: BaseData = BaseData(),
//    val placeInfoFolder: PlaceInfoFolder = PlaceInfoFolder()
//)


data class PlanData(
    var baseData: BaseData = BaseData(),
    var placeFolder: PlaceInfoFolder = PlaceInfoFolder()
)

data class PlanBookData(var title: String, var planData: PlanData)

