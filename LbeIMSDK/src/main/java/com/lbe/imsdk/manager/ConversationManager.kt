//package com.lbe.imsdk.manager
//
//import androidx.compose.runtime.mutableStateOf
//import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
//import com.lbe.imsdk.repository.remote.model.SourceUrl
//import com.lbe.imsdk.repository.remote.model.TimeOutConfigModel
//import com.lbe.imsdk.repository.remote.model.enumeration.FaqType
//import kotlinx.serialization.json.Json
//
//
///**
// *
// *
// * @Date 2025-08-18
// */
//@Deprecated("")
//object ConversationManager {
//    val imApiRepository get() = LbeIMSDKManager.imApiRepository
//
//    var currentSession: CreateSessionResModel.SessionData? = null
//        private set
//
//    internal val lbeToken: String? get() = currentSession?.token
//    internal val uid: String? get() = currentSession?.uid
//    internal val sessionId: String? get() = currentSession?.sessionId
//
//    var messageManager: ConversationMsgManager? = null
//        private set
//
//    val timeOutConfig = mutableStateOf<TimeOutConfigModel.TimeOutConfigData?>(null)
//
//    suspend fun initSession() {
//        currentSession = null
//        messageManager = null
//        val initConfig = LbeIMSDKManager.sdkInitConfig ?: return
//        currentSession = imApiRepository?.createSession(
//            device = initConfig.device,
//            email = initConfig.email,
//            extraInfo = Json.encodeToString(initConfig.extraInfo.map { it.key to it.value.toString() }
//                .toMap()),
//            groupID = initConfig.groupID,
//            headIcon = if (initConfig.parseHeaderIcon is SourceUrl) "" else initConfig.headerIcon,
//            language = initConfig.supportLanguage,
//            nickId = initConfig.nickId,
//            nickName = initConfig.nickName,
//            phone = initConfig.phone,
//            source = initConfig.source,
//            uid = ""
//        )?.also {
//            LbeIMSDKManager.socketManager?.initSessionSocket(it)
//            messageManager = ConversationMsgManager(it.sessionId)
//            initFaq()
//            timeOutConfig.value = getTimeoutConfig()
//        }
//    }
//
//    suspend fun initFaq() {
//        try {
//            faq(FaqType.KNOWLEDGE_BASE, "")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//
//    private suspend fun getTimeoutConfig(): TimeOutConfigModel.TimeOutConfigData? {
//        return imApiRepository?.getTimeoutConfig()
//    }
//
//    suspend fun serviceSupport() {
//        imApiRepository?.serviceSupport()
//    }
//
//    suspend fun faq(
//        faqType: FaqType,
//        id: String,
//    ) {
//        imApiRepository?.faq(faqType, id)
//    }
//
//}