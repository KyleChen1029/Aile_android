package tw.com.chainsea.chat.network.contact

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.NetworkManager
import tw.com.chainsea.ce.sdk.network.services.ApiDataCountService
import tw.com.chainsea.ce.sdk.network.services.AvatarService
import tw.com.chainsea.ce.sdk.network.services.BindThirdPartService
import tw.com.chainsea.ce.sdk.network.services.BusinessCardService
import tw.com.chainsea.ce.sdk.network.services.LoginDevicesService
import tw.com.chainsea.ce.sdk.network.services.SelfProfileService
import tw.com.chainsea.ce.sdk.network.services.TenantService
import tw.com.chainsea.ce.sdk.network.services.TodoService
import tw.com.chainsea.ce.sdk.network.services.TokenService
import tw.com.chainsea.ce.sdk.network.services.VersionService
import tw.com.chainsea.ce.sdk.service.ManagementPermissionService
import tw.com.chainsea.chat.chatroomfilter.ChatRoomFilterRepository
import tw.com.chainsea.chat.chatroomfilter.ChatRoomFilterViewModel
import tw.com.chainsea.chat.mainpage.repository.MainPageRepository
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel
import tw.com.chainsea.chat.mediagallery.viewmodel.MediaGalleryViewModel
import tw.com.chainsea.chat.network.apicount.ApiDataCountRepository
import tw.com.chainsea.chat.network.logindevices.LoginDevicesRepository
import tw.com.chainsea.chat.network.mainroom.MainRoomListViewModel
import tw.com.chainsea.chat.network.selfprofile.SelfProfileRepository
import tw.com.chainsea.chat.network.selfprofile.SelfProfileViewModel
import tw.com.chainsea.chat.network.tenant.TenantRepository
import tw.com.chainsea.chat.network.tenant.TenantViewModel
import tw.com.chainsea.chat.network.todo.TodoRepository
import tw.com.chainsea.chat.network.version.VersionRepository
import tw.com.chainsea.chat.searchfilter.repository.ChatRoomSearchRepository
import tw.com.chainsea.chat.searchfilter.repository.ContactPersonClientRepository
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterSharedRepository
import tw.com.chainsea.chat.searchfilter.repository.ServiceNumberSearchRepository
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonClientSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberConsultationViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberSearchViewModel
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel
import tw.com.chainsea.chat.view.chat.ChatRepository
import tw.com.chainsea.chat.view.chat.ChatService
import tw.com.chainsea.chat.view.chat.ChatViewModel
import tw.com.chainsea.chat.view.consultai.ConsultAIRepository
import tw.com.chainsea.chat.view.consultai.ConsultAIService
import tw.com.chainsea.chat.view.consultai.ConsultAIViewModel
import tw.com.chainsea.chat.view.homepage.bind.BindThirdPartRepository
import tw.com.chainsea.chat.view.homepage.bind.BindThirdPartViewModel
import tw.com.chainsea.chat.view.qrcode.BusinessCardRepository
import tw.com.chainsea.chat.view.qrcode.ServiceNumberQrCodeViewModel
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberRoomRepository
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberRoomService
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberRoomViewModel
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageViewModel
import tw.com.chainsea.chat.view.service.ServiceNumberHomePageViewModel
import tw.com.chainsea.chat.view.service.ServiceNumberSettingRepository
import tw.com.chainsea.chat.view.service.ServiceNumberSettingService
import tw.com.chainsea.chat.view.setting.RepairsViewModel

/**
 * 全部 ViewModel 的 Factory
 * */
class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when (modelClass) {
            // 首頁
            HomeViewModel::class.java -> {
                HomeViewModel(
                    application,
                    provideContactPersonService(),
                    provideSelfProfileService(),
                    provideChatService(),
                    provideLoginDevicesInfoService(),
                    provideVersionService(),
                    provideTokenService(),
                    provideApiDataCountService(),
                    provideTodoService(),
                    provideServiceNumberAgentsManageService()
                ) as T
            }

            // 聯絡人頁面
            ContactPersonViewModel::class.java -> {
                ContactPersonViewModel(
                    application,
                    provideContactPersonService(),
                    provideSelfProfileService(),
                    provideChatService(),
                    provideTokenService()
                ) as T
            }

            // 聊天室
            ChatViewModel::class.java -> {
                ChatViewModel(
                    application,
                    provideChatService(),
                    provideContactPersonService(),
                    provideServiceNumberAgentsManageService(),
                    provideTokenService()
                ) as T
            }

            // 個人頁面
            SelfProfileViewModel::class.java -> {
                SelfProfileViewModel(
                    application,
                    provideSelfProfileService(),
                    provideTokenService()
                ) as T
            }

            // 服務號管理
            ServiceNumberAgentsManageViewModel::class.java -> {
                ServiceNumberAgentsManageViewModel(
                    application,
                    provideServiceNumberAgentsManageService(),
                    provideTokenService(),
                    provideServiceNumberHomePageSettingService()
                ) as T
            }

            // 團隊
            TenantViewModel::class.java -> {
                TenantViewModel(
                    application,
                    provideTenantService(),
                    provideTokenService()
                ) as T
            }

            // 全局搜尋
            SearchFilterSharedViewModel::class.java -> {
                SearchFilterSharedViewModel(
                    application,
                    SearchFilterSharedRepository(),
                    provideChatService(),
                    provideTokenService()
                ) as T
            }

            // 個人/社團主頁
            MainPageViewModel::class.java -> {
                MainPageViewModel(application, MainPageRepository(), provideChatService(), provideTokenService()) as T
            }

            // 一般聊天列表
            MainRoomListViewModel::class.java -> {
                MainRoomListViewModel(application, provideChatService(), provideTokenService()) as T
            }
            // 全局搜尋-聊天室
            ChatRoomSearchViewModel::class.java -> {
                ChatRoomSearchViewModel(application, ChatRoomSearchRepository(), SearchFilterSharedRepository(), provideChatService(), provideTokenService()) as T
            }
            // 全局搜尋-聯絡人
            ContactPersonClientSearchViewModel::class.java -> {
                ContactPersonClientSearchViewModel(application, ContactPersonClientRepository()) as T
            }
            ServiceNumberSearchViewModel::class.java -> {
                ServiceNumberSearchViewModel(application, ServiceNumberSearchRepository(), ChatRoomSearchRepository(), SearchFilterSharedRepository()) as T
            }
            ChatRoomViewModel::class.java -> {
                ChatRoomViewModel(ChatRoomSearchRepository()) as T
            }
            // 服務號AI諮詢
            ServiceNumberConsultationViewModel::class.java -> {
                ServiceNumberConsultationViewModel(application, provideServiceNumberAgentsManageService(), provideTokenService()) as T
            }
            // 聊天室內全屏圖片影片播放頁
            MediaGalleryViewModel::class.java -> {
                MediaGalleryViewModel(application, SearchFilterSharedRepository(), MainPageRepository()) as T
            }

            ConsultAIViewModel::class.java -> {
                ConsultAIViewModel(application, provideConsultAIService(), provideTokenService()) as T
            }

            BindThirdPartViewModel::class.java -> {
                BindThirdPartViewModel(application, provideTokenService(), provideBindService(), provideServiceNumberAgentsManageService()) as T
            }

            ServiceNumberQrCodeViewModel::class.java -> {
                ServiceNumberQrCodeViewModel(application, provideTokenService(), provideSelfProfileService(), provideServiceNumberAgentsManageService()) as T
            }

            ChatRoomFilterViewModel::class.java -> {
                ChatRoomFilterViewModel(application, provideTokenService(), provideChatRoomFilterService()) as T
            }

            RepairsViewModel::class.java -> {
                RepairsViewModel(application) as T
            }

            ServiceNumberRoomViewModel::class.java -> {
                ServiceNumberRoomViewModel(application, provideTokenService(), provideServiceNumberRoomService(), provideChatService()) as T
            }

            ServiceNumberHomePageViewModel::class.java -> {
                ServiceNumberHomePageViewModel(application, provideTokenService(), provideServiceNumberHomePageSettingService()) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

    // 聯絡人 Repository
    private fun provideContactPersonService(): ContactRepository =
        ContactRepository(
            NetworkManager
                .provideRetrofit(application)
                .create(ContactPersonService::class.java)
        )

    // 個人資料 Repository
    private fun provideSelfProfileService(): SelfProfileRepository =
        SelfProfileRepository(
            NetworkManager.provideRetrofit(application).create(SelfProfileService::class.java)
        )

    // 聊天室 Repository
    private fun provideChatService(): ChatRepository =
        ChatRepository(
            NetworkManager.provideRetrofit(application).create(ChatService::class.java)
        )

    // 裝置登入 Repository
    private fun provideLoginDevicesInfoService(): LoginDevicesRepository =
        LoginDevicesRepository(
            NetworkManager.provideCpRetrofit(application).create(LoginDevicesService::class.java)
        )

    // 服務號管理 Repository
    private fun provideServiceNumberAgentsManageService(): ServiceNumberAgentsManageRepository =
        ServiceNumberAgentsManageRepository(
            NetworkManager
                .provideRetrofit(application)
                .create(ManagementPermissionService::class.java)
        )

    // 團隊 Repository
    private fun provideTenantService(): TenantRepository =
        TenantRepository(
            NetworkManager.provideCpRetrofit(application).create(TenantService::class.java),
            NetworkManager.provideRetrofit(application).create(TenantService::class.java),
            NetworkManager.provideCpRetrofitWithoutEncrypt(application).create(AvatarService::class.java)
        )

    // 版本 Repository
    private fun provideVersionService(): VersionRepository = VersionRepository(NetworkManager.provideCpRetrofit(application).create(VersionService::class.java))

    private fun provideTokenService(): TokenRepository = TokenRepository(NetworkManager.provideCpRetrofit(application).create(TokenService::class.java), NetworkManager.provideRetrofit(application).create(TokenService::class.java))

    private fun provideApiDataCountService(): ApiDataCountRepository = ApiDataCountRepository(NetworkManager.provideRetrofit(application).create(ApiDataCountService::class.java))

    private fun provideTodoService(): TodoRepository = TodoRepository(NetworkManager.provideRetrofit(application).create(TodoService::class.java))

    private fun provideConsultAIService(): ConsultAIRepository = ConsultAIRepository(NetworkManager.provideRetrofit(application).create(ConsultAIService::class.java))

    private fun provideBindService(): BindThirdPartRepository = BindThirdPartRepository(NetworkManager.provideRetrofit(application).create(BindThirdPartService::class.java))

    private fun provideBusinessCardService(): BusinessCardRepository = BusinessCardRepository(NetworkManager.provideRetrofit(application).create(BusinessCardService::class.java))

    private fun provideChatRoomFilterService(): ChatRoomFilterRepository = ChatRoomFilterRepository()

    private fun provideServiceNumberRoomService(): ServiceNumberRoomRepository = ServiceNumberRoomRepository(NetworkManager.provideRetrofit(application).create(ServiceNumberRoomService::class.java))

    private fun provideServiceNumberHomePageSettingService(): ServiceNumberSettingRepository = ServiceNumberSettingRepository(NetworkManager.provideRetrofit(application).create(ServiceNumberSettingService::class.java))
}
