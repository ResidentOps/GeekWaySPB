package com.university.geekwayspb

class Users {
    private var Email: String = ""
    private var Name: String = ""

    constructor()
    constructor(Email: String, Name: String) {
        this.Email = Email
        this.Name = Name
    }

    fun getEmail(): String? {
        return Email
    }
    fun setEmail(Email: String) {
        this.Email = Email
    }

    fun getName(): String? {
        return Name
    }
    fun setName(Name: String) {
        this.Name = Name
    }
}