package com.example.myapplication.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ai.AiAssistantManager
import com.example.myapplication.ai.AiSummaryResponse
import com.example.myapplication.audit.AuditLogEntry
import com.example.myapplication.audit.AuditLogManager
import com.example.myapplication.audio.SpeechRecognizerManager
import com.example.myapplication.audio.TextToSpeechManager
import com.example.myapplication.data.models.*
import com.example.myapplication.location.LocationManager
import com.example.myapplication.location.UserLocation
import com.example.myapplication.network.ConnectionState
import com.example.myapplication.network.NetworkConfig
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.WebSocketManager
import com.example.myapplication.network.WebRtcSignalingClient
import com.example.myapplication.notifications.NotificationHelper
import com.example.myapplication.qrcode.QrCodeManager
import com.example.myapplication.reports.AnalyticsData
import com.example.myapplication.reports.AnalyticsReportManager
import com.example.myapplication.reports.DataExportManager
import com.example.myapplication.security.UserRole
import com.example.myapplication.storage.FileAttachment
import com.example.myapplication.storage.StorageManager
import com.example.myapplication.sync.MultiDeviceSyncManager
import com.example.myapplication.translation.TranslationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class VoxBridgeViewModel : ViewModel() {

    private val translationRepo = TranslationRepository()
    private val webSocketManager = WebSocketManager()
    private val webRtcClient = WebRtcSignalingClient()

    private var speechRecognizerManager: SpeechRecognizerManager? = null
    private var ttsManager: TextToSpeechManager? = null
    private var notificationHelper: NotificationHelper? = null
    private var storageManager: StorageManager? = null

    // User Preferences & Role
    private val _participantId = MutableStateFlow(UUID.randomUUID().toString().take(8))
    val participantId: StateFlow<String> = _participantId

    private val _participantName = MutableStateFlow("User_${(1000..9999).random()}")
    val participantName: StateFlow<String> = _participantName

    private val _userRole = MutableStateFlow(UserRole.HOST)
    val userRole: StateFlow<UserRole> = _userRole

    private val _userLocation = MutableStateFlow(LocationManager.getCurrentLocation())
    val userLocation: StateFlow<UserLocation> = _userLocation

    private val _preferredLanguage = MutableStateFlow("en")
    val preferredLanguage: StateFlow<String> = _preferredLanguage

    private val _communicationMode = MutableStateFlow(CommunicationMode.VOICE_AND_TEXT)
    val communicationMode: StateFlow<CommunicationMode> = _communicationMode

    // GitHub Account State
    private val _githubUser = MutableStateFlow<GitHubUser?>(null)
    val githubUser: StateFlow<GitHubUser?> = _githubUser

    private val _isSignedInWithGitHub = MutableStateFlow(false)
    val isSignedInWithGitHub: StateFlow<Boolean> = _isSignedInWithGitHub

    // Online Class State
    private val _currentClass = MutableStateFlow<OnlineClass?>(null)
    val currentClass: StateFlow<OnlineClass?> = _currentClass

    private val _raisedHandsQueue = MutableStateFlow<List<HandRaiseEvent>>(emptyList())
    val raisedHandsQueue: StateFlow<List<HandRaiseEvent>> = _raisedHandsQueue

    // Accessibility & Settings
    private val _autoSpeakAudio = MutableStateFlow(true)
    val autoSpeakAudio: StateFlow<Boolean> = _autoSpeakAudio

    private val _captionsEnabled = MutableStateFlow(true)
    val captionsEnabled: StateFlow<Boolean> = _captionsEnabled

    private val _textSizeMultiplier = MutableStateFlow(1.0f)
    val textSizeMultiplier: StateFlow<Float> = _textSizeMultiplier

    private val _highContrast = MutableStateFlow(false)
    val highContrast: StateFlow<Boolean> = _highContrast

    private val _serverUrl = MutableStateFlow(NetworkConfig.getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl

    // Active Room State
    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _attachments = MutableStateFlow<List<FileAttachment>>(emptyList())
    val attachments: StateFlow<List<FileAttachment>> = _attachments

    val connectionState: StateFlow<ConnectionState> = webSocketManager.connectionState
    val audioCallState = webRtcClient.callState

    // Speech & Audio States
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _interimTranscript = MutableStateFlow("")
    val interimTranscript: StateFlow<String> = _interimTranscript

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Demo Mode States
    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode

    private val _demoStepIndex = MutableStateFlow(0)
    val demoStepIndex: StateFlow<Int> = _demoStepIndex

    private val _isNetworkInterrupted = MutableStateFlow(false)
    val isNetworkInterrupted: StateFlow<Boolean> = _isNetworkInterrupted

    private var speechCollectJob: Job? = null

    init {
        observeWebSocketEvents()
    }

    fun initAudioManagers(context: Context) {
        if (notificationHelper == null) {
            notificationHelper = NotificationHelper(context.applicationContext)
        }

        if (storageManager == null) {
            storageManager = StorageManager(context.applicationContext)
        }

        if (speechRecognizerManager == null) {
            speechRecognizerManager = SpeechRecognizerManager(context.applicationContext)
            speechCollectJob?.cancel()
            speechCollectJob = viewModelScope.launch {
                launch {
                    speechRecognizerManager?.isListening?.collect { listening ->
                        _isListening.value = listening
                    }
                }
                launch {
                    speechRecognizerManager?.interimTranscript?.collect { interim ->
                        _interimTranscript.value = interim
                        if (interim.isNotBlank() && _currentRoomId.value != null) {
                            webSocketManager.sendInterimTranscript(
                                roomId = _currentRoomId.value!!,
                                senderId = _participantId.value,
                                senderName = _participantName.value,
                                text = interim
                            )
                        }
                    }
                }
                launch {
                    speechRecognizerManager?.finalTranscript?.collect { final ->
                        if (final.isNotBlank()) {
                            sendFinalMessage(final, isTyped = false)
                            speechRecognizerManager?.clearTranscripts()
                        }
                    }
                }
            }
        }

        if (ttsManager == null) {
            ttsManager = TextToSpeechManager(context.applicationContext)
            viewModelScope.launch {
                ttsManager?.isSpeaking?.collect { speaking ->
                    _isSpeaking.value = speaking
                }
            }
        }
    }

    fun createOnlineClass(subjectName: String, instructorName: String) {
        val classId = "CLASS-${(1000..9999).random()}"
        val onlineClass = OnlineClass(
            classId = classId,
            subjectName = subjectName,
            instructorName = instructorName,
            scheduledTime = "Live Lecture Now",
            attendeesCount = 1,
            isLectureActive = true
        )
        _currentClass.value = onlineClass
        _userRole.value = UserRole.HOST
        createRoom(instructorName)
        AuditLogManager.logAction(_participantId.value, instructorName, "HOST", "CREATE_CLASS", subjectName)
    }

    fun joinOnlineClass(classId: String) {
        joinRoom(classId, _participantName.value)
        _currentClass.value = OnlineClass(
            classId = classId,
            subjectName = "Multilingual Live Lecture",
            instructorName = "Professor",
            scheduledTime = "Live Lecture",
            attendeesCount = _participants.value.size + 1
        )
    }

    fun raiseHandInClass() {
        val studentName = _participantName.value
        val event = HandRaiseEvent(
            studentId = _participantId.value,
            studentName = studentName,
            status = "RAISED"
        )
        _raisedHandsQueue.value = _raisedHandsQueue.value + event
        sendTypedMessage("✋ Raised Hand: Student $studentName requested permission to speak.")
        AuditLogManager.logAction(_participantId.value, studentName, "PARTICIPANT", "RAISE_HAND", _currentClass.value?.classId ?: "")
    }

    fun acknowledgeHandRaise(studentId: String) {
        _raisedHandsQueue.value = _raisedHandsQueue.value.filter { it.studentId != studentId }
        notificationHelper?.showDataChangeAlert("Hand Acknowledged", "Instructor permitted student to speak.")
    }

    fun generateQrCodeBitmap(roomId: String): Bitmap {
        return QrCodeManager.generateQrCodeBitmap(roomId)
    }

    suspend fun getAiSummary(): AiSummaryResponse {
        return AiAssistantManager.summarizeConversation(_messages.value)
    }

    suspend fun askAi(question: String): String {
        return AiAssistantManager.answerQuestion(question, _messages.value)
    }

    fun getAuditLogs(): List<AuditLogEntry> {
        return AuditLogManager.getAuditLogs()
    }

    fun uploadAttachment(uri: Uri, name: String) {
        storageManager?.let { sm ->
            val att = sm.uploadFile(uri, name)
            _attachments.value = _attachments.value + att
            sendTypedMessage("📎 Attached File: ${att.fileName} (${att.fileType})")
            AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "UPLOAD_FILE", att.fileName)
        }
    }

    fun loginWithGitHub(username: String = "octocat") {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.getService()
                val response = api.authenticateGitHub(GitHubAuthRequest(username = username))
                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user!!
                    _githubUser.value = user
                    _isSignedInWithGitHub.value = true
                    _participantName.value = user.name
                } else {
                    val fallback = GitHubUser(
                        id = 100001,
                        login = username,
                        name = username.replaceFirstChar { it.uppercase() },
                        avatarUrl = "https://avatars.githubusercontent.com/u/583231?v=4",
                        email = "$username@github.com"
                    )
                    _githubUser.value = fallback
                    _isSignedInWithGitHub.value = true
                    _participantName.value = fallback.name
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val fallback = GitHubUser(
                    id = 100001,
                    login = username,
                    name = username.replaceFirstChar { it.uppercase() },
                    avatarUrl = "https://avatars.githubusercontent.com/u/583231?v=4",
                    email = "$username@github.com"
                )
                _githubUser.value = fallback
                _isSignedInWithGitHub.value = true
                _participantName.value = fallback.name
            }
            AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "GITHUB_LOGIN", username)
        }
    }

    fun logoutGitHub() {
        AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "GITHUB_LOGOUT", _githubUser.value?.login ?: "")
        _githubUser.value = null
        _isSignedInWithGitHub.value = false
        _participantName.value = "User_${(1000..9999).random()}"
    }

    fun setParticipantName(name: String) {
        if (name.isNotBlank()) _participantName.value = name.trim()
    }

    fun setPreferredLanguage(langCode: String) {
        _preferredLanguage.value = langCode
    }

    fun setCommunicationMode(mode: CommunicationMode) {
        _communicationMode.value = mode
    }

    fun setAutoSpeakAudio(enabled: Boolean) {
        _autoSpeakAudio.value = enabled
    }

    fun setCaptionsEnabled(enabled: Boolean) {
        _captionsEnabled.value = enabled
    }

    fun setTextSizeMultiplier(multiplier: Float) {
        _textSizeMultiplier.value = multiplier
    }

    fun setHighContrast(enabled: Boolean) {
        _highContrast.value = enabled
    }

    fun setServerUrl(url: String) {
        NetworkConfig.setServerUrl(url)
        _serverUrl.value = NetworkConfig.getServerUrl()
    }

    fun getAnalyticsReport(): AnalyticsData {
        return AnalyticsReportManager.generateAnalyticsReport(_messages.value, _participants.value)
    }

    fun exportCsvReport(context: Context): File? {
        val rId = _currentRoomId.value ?: "GENERAL"
        AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "EXPORT_CSV", rId)
        return DataExportManager.exportToCsv(context, rId, _messages.value)
    }

    fun exportPdfReport(context: Context): File? {
        val rId = _currentRoomId.value ?: "GENERAL"
        val analytics = getAnalyticsReport()
        AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "EXPORT_PDF", rId)
        return DataExportManager.exportToPdf(context, rId, _messages.value, analytics)
    }

    fun triggerAnnouncementNotification(title: String, content: String) {
        notificationHelper?.showAnnouncement(title, content)
    }

    fun triggerReminderNotification(roomId: String) {
        notificationHelper?.scheduleRoomReminder(roomId)
    }

    fun createRoom(hostName: String? = null) {
        val name = hostName ?: _participantName.value
        setParticipantName(name)
        val roomId = "ROOM-${(1000..9999).random()}"
        _currentRoomId.value = roomId
        _userRole.value = UserRole.HOST

        val host = Participant(
            id = _participantId.value,
            name = name,
            preferredLanguage = _preferredLanguage.value,
            mode = _communicationMode.value,
            isHost = true
        )
        _participants.value = listOf(host)
        _messages.value = emptyList()

        connectToWebSocket(roomId)
        webRtcClient.startCall(roomId, _participantId.value)
        notificationHelper?.showDataChangeAlert("Room Created", "Host session $roomId started successfully.")
        AuditLogManager.logAction(_participantId.value, name, "HOST", "CREATE_ROOM", roomId)
        MultiDeviceSyncManager.syncDeviceSession("dev_${_participantId.value}", name)
    }

    fun joinRoom(roomId: String, name: String? = null) {
        val cleanRoomId = roomId.trim().uppercase()
        val pName = name ?: _participantName.value
        setParticipantName(pName)

        _currentRoomId.value = cleanRoomId
        _userRole.value = UserRole.PARTICIPANT

        val user = Participant(
            id = _participantId.value,
            name = pName,
            preferredLanguage = _preferredLanguage.value,
            mode = _communicationMode.value,
            isHost = false
        )

        val existing = _participants.value.toMutableList()
        if (existing.none { it.id == user.id }) existing.add(user)
        _participants.value = existing

        connectToWebSocket(cleanRoomId)
        webRtcClient.startCall(cleanRoomId, _participantId.value)
        notificationHelper?.showDataChangeAlert("Joined Room", "Connected to live conversation $cleanRoomId.")
        AuditLogManager.logAction(_participantId.value, pName, "PARTICIPANT", "JOIN_ROOM", cleanRoomId)
    }

    fun leaveRoom() {
        _currentRoomId.value?.let { roomId ->
            AuditLogManager.logAction(_participantId.value, _participantName.value, _userRole.value.name, "LEAVE_ROOM", roomId)
            webSocketManager.disconnect()
            webRtcClient.endCall()
        }
        _currentRoomId.value = null
        _messages.value = emptyList()
        _participants.value = emptyList()
    }

    fun sendTypedMessage(text: String) {
        if (text.isBlank()) return
        sendFinalMessage(text, isTyped = true)
    }

    private fun sendFinalMessage(text: String, isTyped: Boolean) {
        val roomId = _currentRoomId.value ?: return
        val senderId = _participantId.value
        val senderName = _participantName.value
        val sourceLang = _preferredLanguage.value

        val targetLangs = _participants.value.map { it.preferredLanguage }.distinct()

        viewModelScope.launch {
            val pendingMessage = Message(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                senderId = senderId,
                senderName = senderName,
                originalText = text,
                sourceLanguage = sourceLang,
                status = MessageStatus.SENDING,
                isTyped = isTyped
            )

            addOrUpdateMessage(pendingMessage)

            val translationRes = translationRepo.translateText(
                text = text,
                sourceLanguage = sourceLang,
                targetLanguages = targetLangs
            )

            val finalMessage = pendingMessage.copy(
                translations = translationRes.translations,
                status = MessageStatus.SENT
            )

            addOrUpdateMessage(finalMessage)
            webSocketManager.sendMessage(finalMessage)
            AuditLogManager.logAction(senderId, senderName, _userRole.value.name, "SEND_MESSAGE", text.take(30))
        }
    }

    fun startSpeechRecognition(context: Context) {
        initAudioManagers(context)
        speechRecognizerManager?.startListening(_preferredLanguage.value)
    }

    fun stopSpeechRecognition() {
        speechRecognizerManager?.stopListening()
    }

    fun speakMessage(message: Message) {
        val translatedText = message.getTranslationFor(_preferredLanguage.value)
        ttsManager?.speak(translatedText, _preferredLanguage.value)
    }

    fun stopAudioPlayback() {
        ttsManager?.stop()
    }

    fun toggleMute(): Boolean {
        return webRtcClient.toggleMute()
    }

    fun retryFailedMessage(messageId: String) {
        val msg = _messages.value.find { it.id == messageId } ?: return
        sendFinalMessage(msg.originalText, msg.isTyped)
    }

    private fun connectToWebSocket(roomId: String) {
        webSocketManager.connect(
            roomId = roomId,
            participantId = _participantId.value,
            name = _participantName.value,
            language = _preferredLanguage.value
        )
    }

    private fun observeWebSocketEvents() {
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                when (event.type) {
                    "MESSAGE" -> {
                        event.messageObj?.let { msg ->
                            addOrUpdateMessage(msg)
                            if (_autoSpeakAudio.value && msg.senderId != _participantId.value) {
                                speakMessage(msg)
                            }
                        }
                    }
                    "JOIN" -> {
                        event.participantObj?.let { p ->
                            val list = _participants.value.toMutableList()
                            if (list.none { it.id == p.id }) {
                                list.add(p)
                                _participants.value = list
                            }
                        }
                    }
                    "LEAVE" -> {
                        val list = _participants.value.filter { it.id != event.senderId }
                        _participants.value = list
                    }
                }
            }
        }
    }

    private fun addOrUpdateMessage(msg: Message) {
        val currentList = _messages.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == msg.id }
        if (index >= 0) {
            currentList[index] = msg
        } else {
            currentList.add(msg)
        }
        currentList.sortBy { it.sequenceNumber }
        _messages.value = currentList
    }

    // DEMO MODE IMPLEMENTATION
    fun startDemoMode(context: Context) {
        _isDemoMode.value = true
        _demoStepIndex.value = 0
        _isNetworkInterrupted.value = false
        initAudioManagers(context)

        _currentRoomId.value = "DEMO-VOX"
        _participantId.value = "p_user"
        _participantName.value = "You (Tamil)"
        _preferredLanguage.value = "ta"
        _userRole.value = UserRole.HOST

        val demoParticipants = listOf(
            Participant("p_user", "You (Tamil)", "ta", CommunicationMode.VOICE_AND_TEXT, isHost = true),
            Participant("p_john", "John (English)", "en", CommunicationMode.VOICE_AND_TEXT),
            Participant("p_priya", "Priya (Text-Only)", "hi", CommunicationMode.TEXT_ONLY),
            Participant("p_alex", "Alex (Captions)", "es", CommunicationMode.CAPTIONS_ONLY)
        )
        _participants.value = demoParticipants
        _messages.value = emptyList()

        runDemoSequence()
    }

    fun nextDemoStep() {
        _demoStepIndex.value = (_demoStepIndex.value + 1)
        runDemoSequence()
    }

    fun toggleDemoNetworkInterruption() {
        _isNetworkInterrupted.value = !_isNetworkInterrupted.value
    }

    private fun runDemoSequence() {
        viewModelScope.launch {
            when (_demoStepIndex.value) {
                0 -> {
                    delay(500)
                    val msg1 = Message(
                        id = "demo_1",
                        roomId = "DEMO-VOX",
                        senderId = "p_user",
                        senderName = "You (Tamil)",
                        originalText = "வணக்கம், அனைவருக்கும் நல்வரவு!",
                        sourceLanguage = "ta",
                        translations = mapOf(
                            "en" to "Hello everyone, welcome!",
                            "hi" to "नमस्ते सभी, आपका स्वागत है!",
                            "es" to "¡Hola a todos, bienvenidos!",
                            "ta" to "வணக்கம், அனைவருக்கும் நல்வரவு!"
                        ),
                        isTyped = false
                    )
                    addOrUpdateMessage(msg1)
                    if (_autoSpeakAudio.value) speakMessage(msg1)
                }
                1 -> {
                    val msg2 = Message(
                        id = "demo_2",
                        roomId = "DEMO-VOX",
                        senderId = "p_john",
                        senderName = "John (English)",
                        originalText = "Hello! I am receiving your message in English in real-time.",
                        sourceLanguage = "en",
                        translations = mapOf(
                            "ta" to "வணக்கம்! உங்கள் செய்தியை நான் நேரலையில் ஆங்கிலத்தில் பெறுகிறேன்.",
                            "hi" to "नमस्ते! मुझे आपका संदेश वास्तविक समय में अंग्रेजी में मिल रहा है।",
                            "es" to "¡Hola! Estoy recibiendo tu mensaje en inglés en tiempo real.",
                            "en" to "Hello! I am receiving your message in English in real-time."
                        ),
                        isTyped = true
                    )
                    addOrUpdateMessage(msg2)
                    if (_autoSpeakAudio.value) speakMessage(msg2)
                }
                2 -> {
                    val msg3 = Message(
                        id = "demo_3",
                        roomId = "DEMO-VOX",
                        senderId = "p_priya",
                        senderName = "Priya (Text-Only)",
                        originalText = "मैं इस चर्चा में केवल टाइप करके भाग ले रही हूँ।",
                        sourceLanguage = "hi",
                        translations = mapOf(
                            "ta" to "நான் தட்டச்சு செய்வதன் மூலம் மட்டுமே இந்த உரையாடலில் பங்கேற்கிறேன்.",
                            "en" to "I am participating in this conversation by typing only.",
                            "es" to "Estoy participando en esta conversación solo escribiendo.",
                            "hi" to "मैं इस चर्चा में केवल टाइप करके भाग ले रही हूँ।"
                        ),
                        isTyped = true
                    )
                    addOrUpdateMessage(msg3)
                    if (_autoSpeakAudio.value) speakMessage(msg3)
                }
                3 -> {
                    _isNetworkInterrupted.value = true
                    delay(1500)
                    _isNetworkInterrupted.value = false

                    val msg4 = Message(
                        id = "demo_4",
                        roomId = "DEMO-VOX",
                        senderId = "p_alex",
                        senderName = "Alex (Captions)",
                        originalText = "Captions synced successfully after re-connection!",
                        sourceLanguage = "en",
                        translations = mapOf(
                            "ta" to "மீண்டும் இணைக்கப்பட்ட பிறகு தலைப்புகள் வெற்றிகரமாக ஒத்திசைக்கப்பட்டன!",
                            "hi" to "पुनः कनेक्शन के बाद कैप्शन सफलतापूर्वक सिंक हो गए!",
                            "es" to "¡Los subtítulos se sincronizaron con éxito después de la reconexión!",
                            "en" to "Captions synced successfully after re-connection!"
                        ),
                        isTyped = true
                    )
                    addOrUpdateMessage(msg4)
                    if (_autoSpeakAudio.value) speakMessage(msg4)
                }
            }
        }
    }
}
