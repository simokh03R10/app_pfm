package com.example.apppfm

class ModelPost {
    var id: String = ""
    var uid: String = ""
    var categorie: String = ""
    var titre: String = ""
    var description: String = ""
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var adresse: String = ""
    var favorite: Boolean = false

    constructor()
    constructor(
        id: String,
        uid: String,
        categorie: String,
        titre: String,
        description: String,
        longitude: Double,
        latitude: Double,
        adresse: String,
        favorite: Boolean
    ) {
        this.id = id
        this.uid = uid
        this.categorie = categorie
        this.titre = titre
        this.description = description
        this.longitude = longitude
        this.latitude = latitude
        this.adresse = adresse
        this.favorite = favorite
    }


}