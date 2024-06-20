package com.university.geekwayspb

class Places {

    var uid: String = ""
    var id: String = ""
    var placename: String = ""
    var placedescription: String = ""
    var categoryname: String = ""
    var categoryId: String = ""
    var cityname: String = ""
    var cityId: String = ""
    var placeImage: String = ""
    var placePublic: String = ""
    var placeWeb: String = ""
    var placeAddress: String = ""
    var placeTime: String = ""
    var placeTelephone: String = ""
    var placeLat: String = ""
    var placeLng: String = ""
    //var placeUrl: String = ""
    var timestamp: Long = 0
    var isFavorite = false

    constructor()
    constructor(
        uid: String,
        id: String,
        placename: String,
        placedescription: String,
        categoryname: String,
        categoryId: String,
        cityname: String,
        cityId: String,
        placeImage: String,
        placePublic: String,
        placeWeb: String,
        placeAddress: String,
        placeTime: String,
        placeTelephone: String,
        placeLat: String,
        placeLng: String,
        //placeUrl: String,
        timestamp: Long,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.placename = placename
        this.placedescription = placedescription
        this.categoryname = categoryname
        this.categoryId = categoryId
        this.cityname = cityname
        this.cityId = cityId
        this.placeImage = placeImage
        this.placePublic = placePublic
        this.placeWeb = placeWeb
        this.placeAddress = placeAddress
        this.placeTime = placeTime
        this.placeTelephone = placeTelephone
        this.placeLat = placeLat
        this.placeLng = placeLng
        //this.placeUrl = placeUrl
        this.timestamp = timestamp
        this.isFavorite = isFavorite
    }
}