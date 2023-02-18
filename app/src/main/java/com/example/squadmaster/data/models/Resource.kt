package com.example.squadmaster.data.models

import com.example.squadmaster.data.enums.Status

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?): Resource<T> = Resource(Status.SUCCESS, data, null)

        fun <T> error(msg: String): Resource<T> = Resource(Status.ERROR, null, msg)

        fun <T> loading(): Resource<T> = Resource(Status.LOADING, null, null)
    }
}