package com.bookstore.mobile.core.network

import com.bookstore.mobile.feature.auth.data.dto.LoginRequest
import com.bookstore.mobile.feature.auth.data.dto.LoginResponse
import com.bookstore.mobile.feature.auth.data.dto.LogoutRequest
import com.bookstore.mobile.feature.auth.data.dto.RefreshTokenRequest
import com.bookstore.mobile.feature.auth.data.dto.RegisterRequest
import com.bookstore.mobile.feature.auth.data.dto.RegisterResponse
import com.bookstore.mobile.feature.auth.data.dto.RequestRegistrationOtpRequest
import com.bookstore.mobile.feature.auth.data.dto.UserDto
import com.bookstore.mobile.feature.auth.data.dto.VerifyOtpRequest
import com.bookstore.mobile.feature.book.data.dto.AuthorDto
import com.bookstore.mobile.feature.book.data.dto.BookDto
import com.bookstore.mobile.feature.book.data.dto.BookPageDetailDto
import com.bookstore.mobile.feature.book.data.dto.CategoryDto
import com.bookstore.mobile.feature.book.data.dto.PublisherDto
import com.bookstore.mobile.feature.cart.data.dto.AddToCartRequest
import com.bookstore.mobile.feature.cart.data.dto.CartDto
import com.bookstore.mobile.feature.cart.data.dto.UpdateCartItemRequest
import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import com.bookstore.mobile.feature.checkout.data.dto.CheckoutResponse
import com.bookstore.mobile.feature.order.data.dto.OrderDto
import com.bookstore.mobile.feature.profile.data.dto.CreateUserAddressRequest
import com.bookstore.mobile.feature.profile.data.dto.ProfileDto
import com.bookstore.mobile.feature.profile.data.dto.UpdateProfileRequest
import com.bookstore.mobile.feature.profile.data.dto.UpdateUserRequest
import com.bookstore.mobile.feature.profile.data.dto.UserAddressDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): ApiResponse<JsonElement>

    @POST("api/otp/request")
    suspend fun requestRegistrationOtp(@Body request: RequestRegistrationOtpRequest): ApiResponse<JsonElement>

    @POST("api/otp/verify")
    suspend fun verifyRegistrationOtp(@Body request: VerifyOtpRequest): ApiResponse<JsonElement>

    @GET("api/users/me")
    suspend fun getCurrentUser(): ApiResponse<UserDto>

    @PUT("api/users/me")
    suspend fun updateCurrentUser(@Body request: UpdateUserRequest): ApiResponse<UserDto>

    @GET("api/profiles/me")
    suspend fun getCurrentProfile(): ApiResponse<ProfileDto>

    @PUT("api/profiles/me")
    suspend fun updateCurrentProfile(@Body request: UpdateProfileRequest): ApiResponse<ProfileDto>

    @GET("api/books")
    suspend fun getBooks(): ApiResponse<List<BookDto>>

    @GET("api/books/search")
    suspend fun searchBooks(@Query("keyword") keyword: String): ApiResponse<List<BookDto>>

    @GET("api/books/{id}")
    suspend fun getBook(@Path("id") id: String): ApiResponse<BookDto>

    @GET("api/books/{id}/page-detail")
    suspend fun getBookPageDetail(@Path("id") id: String): ApiResponse<BookPageDetailDto>

    @GET("api/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @GET("api/authors")
    suspend fun getAuthors(): ApiResponse<List<AuthorDto>>

    @GET("api/publishers")
    suspend fun getPublishers(): ApiResponse<List<PublisherDto>>

    @GET("api/cart")
    suspend fun getCart(): ApiResponse<CartDto>

    @POST("api/cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): ApiResponse<CartDto>

    @PUT("api/cart/items/{bookId}")
    suspend fun updateCartItem(
        @Path("bookId") bookId: String,
        @Body request: UpdateCartItemRequest,
    ): ApiResponse<CartDto>

    @DELETE("api/cart/items/{bookId}")
    suspend fun removeCartItem(@Path("bookId") bookId: String): ApiResponse<JsonElement>

    @DELETE("api/cart/items")
    suspend fun clearCart(): ApiResponse<JsonElement>

    @GET("api/user-addresses")
    suspend fun getAddresses(): ApiResponse<List<UserAddressDto>>

    @POST("api/user-addresses")
    suspend fun createAddress(@Body request: CreateUserAddressRequest): ApiResponse<UserAddressDto>

    @POST("api/orders/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): ApiResponse<CheckoutResponse>

    @GET("api/orders/my")
    suspend fun getOrders(): ApiResponse<List<OrderDto>>

    @GET("api/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): ApiResponse<OrderDto>
}
