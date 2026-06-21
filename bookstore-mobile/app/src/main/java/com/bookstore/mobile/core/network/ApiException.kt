package com.bookstore.mobile.core.network

class ApiException(
    override val message: String,
    val statusCode: Int? = null,
) : Exception(message)
