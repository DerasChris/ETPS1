package com.cad.proyectofinaletps1.models
import android.os.Parcel
import android.os.Parcelable

data class Productos(
    val nombre: String?,
    val descripcion: String?,
    val precio: Double?,
    val imgurl: String?,
    val barcode: String? = null,
    val marca: String?,
    val categoria: String?,
    val key: String? = ""
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readValue(String::class.java.classLoader) as? String,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(descripcion)
        parcel.writeValue(precio)
        parcel.writeString(imgurl)
        parcel.writeValue(barcode)
        parcel.writeString(marca)
        parcel.writeString(categoria)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Productos> {
        override fun createFromParcel(parcel: Parcel): Productos {
            return Productos(parcel)
        }

        override fun newArray(size: Int): Array<Productos?> {
            return arrayOfNulls(size)
        }
    }

    constructor() : this(
        nombre = null,
        descripcion = null,
        precio = 0.0,
        imgurl = null,
        barcode = null,
        marca = null,
        categoria = null,
        key = null
    )
}