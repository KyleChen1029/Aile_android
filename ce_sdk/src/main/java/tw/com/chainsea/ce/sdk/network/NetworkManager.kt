package tw.com.chainsea.ce.sdk.network

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tw.com.chainsea.ce.sdk.bean.account.Gender.GenderAdapter
import tw.com.chainsea.ce.sdk.bean.account.UserType.UserTypeAdapter
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag.MessageFlagAdapter
import tw.com.chainsea.ce.sdk.bean.msg.MessageType.MessageTypeAdapter
import tw.com.chainsea.ce.sdk.bean.msg.SourceType.SourceTypeAdapter
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus.ServiceNumberStatusTypeAdapter
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType.ServiceNumberTypeAdapter
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.model.Member
import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase
import java.io.IOException
import java.lang.reflect.Type
import io.sentry.okhttp.SentryOkHttpEventListener
import io.sentry.okhttp.SentryOkHttpInterceptor


object NetworkManager {

    private fun provideHttpLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private fun provideOkhttpClient(context: Context): OkHttpClient {
        return getOkHttpClientBuilder()
            .addInterceptor(CeApiRequestInterceptor(context))
            .addInterceptor(CeApiResponseInterceptor())
            .addInterceptor(SentryOkHttpInterceptor(
                captureFailedRequests = false
            ))
            .build()
    }

    private fun provideCpOkHttpClient(application: Application): OkHttpClient {
        return getOkHttpClientBuilder()
            .addInterceptor(EncryptInterceptor(application))
            .addInterceptor(DecryptInterceptor())
            .addInterceptor(SentryOkHttpInterceptor(
                captureFailedRequests = false
            ))
            .build()
    }

    private fun getOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .eventListener(SentryOkHttpEventListener())
            .addInterceptor(SentryOkHttpInterceptor(captureFailedRequests = false))
    }

    private fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory())
            .add(ServiceNumberTypeAdapter())
            .add(GenderAdapter())
            .add(ServiceNumberPrivilege.ServiceNumberPrivilegeAdapter())
            .add(UserTypeAdapter())
            .add(MessageFlagAdapter())
            .add(MessageTypeAdapter())
            .add(ServiceNumberStatusTypeAdapter())
            .add(SourceTypeAdapter())
            .add(ChatRoomType.ChatRoomTypeAdapter())
            .add(ChannelType.ChannelTypeAdapter())
            .add(TodoStatus.TodoStatusTypeAdapter())
            .add(Member.MemberTypeAdapter())
            .build()

    fun provideRetrofit(ctx: Context): Retrofit = Retrofit.Builder()
        .client(provideOkhttpClient(ctx))
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .baseUrl(TokenPref.getInstance(ctx).currentTenantUrl + "/")
        .build()

    fun provideCpRetrofit(application: Application): Retrofit = Retrofit.Builder()
        .client(provideCpOkHttpClient(application))
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .baseUrl(CpNewRequestBase.BASE_URL)
        .build()

    fun provideCpRetrofitWithoutEncrypt(application: Application): Retrofit = Retrofit.Builder()
        .client(provideOkhttpClient(application.applicationContext))
        .addConverterFactory(BitmapConverterFactory.create())
        .baseUrl(CpNewRequestBase.BASE_URL)
        .build()


    class BitmapConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation?>?,
            retrofit: Retrofit?
        ): Converter<ResponseBody, *>? {
            if (type.toString().contains("Bitmap")) {
                return BitmapResponseBodyConverter()
            }
            return MoshiConverterFactory.create(provideMoshi()).responseBodyConverter(type, annotations, retrofit)
        }

        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<out Annotation>,
            methodAnnotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<*, RequestBody>? {
            return MoshiConverterFactory.create(provideMoshi()).requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }

        override fun stringConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<*, String>? {
            return MoshiConverterFactory.create(provideMoshi()).stringConverter(type, annotations, retrofit)
        }

        companion object {
            fun create(): BitmapConverterFactory {
                return BitmapConverterFactory()
            }
        }
    }
    class BitmapResponseBodyConverter : Converter<ResponseBody, Bitmap> {
        @Throws(IOException::class)
        override fun convert(value: ResponseBody): Bitmap {
            return getBitmap(value)
        }


        private fun getBitmap(responseBody: ResponseBody): Bitmap {
            var bytes = ByteArray(0)
            try {
                bytes = responseBody.bytes()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}
