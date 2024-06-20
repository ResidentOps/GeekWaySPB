package com.university.geekwayspb

class Categories {

    var id: String = ""
    var categoryname: String = ""
    var cityId: String = ""
    var cityname: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor()
    constructor(id: String, categoryname: String, cityId: String, cityname: String, timestamp: Long, uid: String) {
        this.id = id
        this.categoryname = categoryname
        this.cityId = cityId
        this.cityname = cityname
        this.timestamp = timestamp
        this.uid = uid
    }
}