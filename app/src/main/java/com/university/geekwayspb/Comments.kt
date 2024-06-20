package com.university.geekwayspb

class Comments {
    var id = ""
    var placeId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    constructor()
    constructor(id: String, placeId: String, timestamp: String, comment: String, uid: String){
        this.id = id
        this.placeId = placeId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}