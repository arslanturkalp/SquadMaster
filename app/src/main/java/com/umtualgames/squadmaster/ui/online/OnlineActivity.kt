package com.umtualgames.squadmaster.ui.online

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.adapter.PotentialAnswersAdapter
import com.umtualgames.squadmaster.application.SessionManager.clearUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.getUserName
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.enums.PositionIdStatus
import com.umtualgames.squadmaster.data.enums.PositionTypeIdStatus
import com.umtualgames.squadmaster.databinding.ActivityOnlineBinding
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.online.compare.CompareFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.squad.SquadAdapter
import com.umtualgames.squadmaster.utils.addOnBackPressedListener
import com.umtualgames.squadmaster.utils.ifExists10Number
import com.umtualgames.squadmaster.utils.ifTwoBack
import com.umtualgames.squadmaster.utils.ifTwoWinger
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.show
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@AndroidEntryPoint
class OnlineActivity : BaseActivity(), LifecycleObserver {

    private val binding by lazy { ActivityOnlineBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<OnlineViewModel>()

    private val onlineAdapter = OnlineAdapter()
    private val okHttpClient = OkHttpClient()

    private var rivalChooseImage: String? = null
    private var rivalChooseName: String? = null

    private var myChooseImage: String? = null
    private var myChooseName: String? = null

    private var correctAnswer: Player? = null

    private val goalkeeperAdapter by lazy { SquadAdapter() }
    private val defenceAdapter by lazy { SquadAdapter() }
    private val middleAdapter by lazy { SquadAdapter() }
    private val attackingMiddleAdapter by lazy { SquadAdapter() }
    private val forwardAdapter by lazy { SquadAdapter() }

    private var roomName: String = ""

    private var oneAnswerDialog: CompareFragment = CompareFragment()
    private var allAnswersDialog: CompareFragment = CompareFragment()

    private var timeoutRunnable: Runnable? = null
    private var timeoutHandler: Handler? = null

    private val potentialAnswersAdapter by lazy {
        PotentialAnswersAdapter(true) {
            Handler(Looper.getMainLooper()).post {
                controlAnswer(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        clearUnknownAnswer()
        lifecycle.addObserver(this)

        addOnBackPressedListener {
            timeoutHandler?.removeCallbacks(timeoutRunnable!!)
            backToMainMenu()
        }

        setupRecyclerView()
        setupObservers()

        viewModel.getAvailableRoomCount()
    }

    override fun onStop() {
        super.onStop()
        webSocket?.close(1000,"")
        viewModel.leaveRoom(getUserName())
    }

    private fun backToMainMenu() {
        showAlertDialogTheme(
            title = getString(R.string.back_to_main_menu),
            contentMessage = getString(R.string.online_loss_match),
            showNegativeButton = true,
            positiveButtonTitle = getString(R.string.yes),
            negativeButtonTitle = getString(R.string.no),
            onPositiveButtonClick = {
                viewModel.leaveRoom(getUserName())
                webSocket?.close(1000, "")
                webSocket?.send(String.format(getString(R.string.user_left_game), getUserName()))
                startActivity(MainActivity.createIntent(this))
            }
        )
    }

    private fun setupRecyclerView() {
        binding.rvGoalkeeper.apply {
            adapter = goalkeeperAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvDefence.apply {
            adapter = defenceAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvMiddle.apply {
            adapter = middleAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvAttackingMiddle.apply {
            adapter = attackingMiddleAdapter
            layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply {
                justifyContent = JustifyContent.SPACE_AROUND
                alignItems = AlignItems.CENTER
            }
        }

        binding.rvForwards.apply {
            adapter = forwardAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvPotentialAnswers.apply {
            adapter = potentialAnswersAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is OnlineViewState.LoadingState -> {
                    showProgressDialog()
                }

                is OnlineViewState.RoomCountState -> {
                    dismissProgressDialog()
                    if (state.response % 2 == 0) {
                        viewModel.createRoom(getUserName())
                    } else {
                        viewModel.getRooms()
                    }
                }

                is OnlineViewState.CreateRoomState -> {
                    dismissProgressDialog()
                    webSocket = okHttpClient.newWebSocket(createRequest(state.response.data.roomName), webSocketListener)
                    timeoutHandler = Handler(Looper.getMainLooper())
                    timeoutRunnable = Runnable {
                        binding.apply {
                            tvStatusOpponent.text = getString(R.string.rival_is_not_found)

                        }
                        Handler(Looper.getMainLooper()).postDelayed({ onBackPressedDispatcher.onBackPressed() }, 1000)
                    }

                    timeoutHandler?.postDelayed(timeoutRunnable!!, TIMEOUT * 10000)
                    webSocket!!.request()
                }

                is OnlineViewState.LeaveRoomState -> {
                    dismissProgressDialog()
                }

                is OnlineViewState.JoinRoomState -> {
                    dismissProgressDialog()
                    roomName = state.response.data.roomName
                    webSocket = okHttpClient.newWebSocket(createRequest(roomName), webSocketJoinerListener)
                }

                is OnlineViewState.RoomsState -> {
                    dismissProgressDialog()
                    viewModel.joinRoom()
                }

                is OnlineViewState.WarningState -> {}
                is OnlineViewState.ErrorState -> {}
                is OnlineViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)

                    viewModel.updatePoint(UpdatePointRequest(getUserID(), 50))
                    onBackPressedDispatcher.onBackPressed()
                }

                is OnlineViewState.ReturnSplashState -> {
                    dismissProgressDialog()
                    startActivity(SplashActivity.createIntent(this, false))
                }

                is OnlineViewState.UpdateState -> {
                    dismissProgressDialog()
                }

                is OnlineViewState.UserPointLoadingState -> {}
            }
        }
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {
        setupUI(squad)

        goalkeeperAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.GOALKEEPER.value })
        defenceAdapter.updateAdapter(ifTwoBack(squad.filter { it.positionTypeID == PositionTypeIdStatus.DEFENCE.value } as ArrayList<Player>))
        middleAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.MIDFIELDER.value && it.positionID != PositionIdStatus.ON.value })
        attackingMiddleAdapter.updateAdapter(if (ifExists10Number(squad)) squad.filter { it.positionID == PositionIdStatus.FA.value || it.positionID == PositionIdStatus.ON.value } else squad.filter { it.positionID == 11 })
        forwardAdapter.updateAdapter(if (ifExists10Number(squad)) ifTwoWinger(squad.filter { it.positionTypeID == PositionTypeIdStatus.FORWARD.value && it.positionID != PositionIdStatus.FA.value } as ArrayList<Player>) else ifTwoWinger(squad.filter { it.positionTypeID == 4 && it.positionID != 10 && it.positionID != 11 } as ArrayList<Player>))

        potentialAnswersAdapter.updateAdapter(potentialAnswers)
        binding.cdAnswer.setVisible()
    }

    private fun setupUI(squad: List<Player>) {
        binding.apply {

            correctAnswer = squad.first { !it.isVisible }

            ivTeam.apply {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(squad.first().squadImagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }

            setVisibility(View.VISIBLE, ivFootballPitch, ivFootballGoal, llHalfSquare, tvAnswerTitle)
        }
    }

    private fun controlAnswer(it: PotentialAnswer) {
        webSocket?.send("${getUserName()} Resim:${it.imagePath}")
        webSocket?.send("${getUserName()} İsim:${it.displayName}")
        binding.cdAnswer.isClickable = false
        oneAnswerDialog = CompareFragment.newInstance(
            myChooseImage = it.imagePath,
            myChooseName = it.displayName,
            rivalChooseImage = "",
            rivalChooseName = getString(R.string.answer_waiting)
        )
        oneAnswerDialog.show(this)
        control()
    }

    private fun control() {
        if (rivalChooseName != null && myChooseName != null) {
            allAnswersDialog = CompareFragment.newInstance(
                myChooseImage = myChooseImage!!,
                myChooseName = myChooseName!!,
                rivalChooseImage = rivalChooseImage!!,
                rivalChooseName = rivalChooseName!!,
                correctAnswer = correctAnswer,
            )
            oneAnswerDialog.dismiss()
            allAnswersDialog.show(this)
        }
    }

    private fun createRequest(roomID: String): Request {
        val webSocketUrl = "wss://squad-master-e391494f487b.herokuapp.com/joinOnline/$roomID"

        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }

    private val webSocketListener = object : WebSocketListener() {

        var squad: GetFirstElevenBySquadResponse? = null

        override fun onOpen(webSocket: WebSocket, response: Response) {
            messageList.clear()
            super.onOpen(webSocket, response)
            webSocket.send(String.format(getString(R.string.user_joined_game), getUserName()))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            messageList.add(text)
            runOnUiThread {
                onlineAdapter.updateAdapter(messageList)
                if (text.contains("{\"statusCode\":200")) {
                    squad = Gson().fromJson(text, GetFirstElevenBySquadResponse::class.java)

                    binding.apply {
                        tvTeamName.text = squad!!.data.squad.name
                        ivTeam.apply {
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(squad!!.data.playerList.first().squadImagePath)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(this)
                        }
                    }

                    object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val typeFace = ResourcesCompat.getFont(this@OnlineActivity, R.font.carter_one)
                            with(binding) {
                                tvStatusOpponent.apply {
                                    typeface = typeFace
                                    textSize = 30f
                                    setText((millisUntilFinished / 1000).toString())
                                }
                            }
                            webSocket.send(String.format(getString(R.string.user_joined_game), getUserName()))
                        }

                        override fun onFinish() {
                            with(binding) {
                                tvStatusOpponent.text = getString(R.string.match_start)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    llSearchingOpponent.setGone()
                                    setList(squad!!.data.playerList, squad!!.data.potentialAnswerList)

                                }, 500)
                            }
                        }
                    }.start()
                }

                if (messageList.any { it.contains("{\"statusCode\":200") }) {
                    val gson = Gson()
                    squad = gson.fromJson(messageList.first { it.contains("{\"statusCode\":200") }, GetFirstElevenBySquadResponse::class.java)

                    binding.apply {
                        tvTeamName.text = squad!!.data.squad.name
                        ivTeam.apply {
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(squad!!.data.playerList.first().squadImagePath)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(this)
                        }
                    }
                }

                if (text.contains("2 kullanıcı")) {
                    timeoutHandler?.removeCallbacks(timeoutRunnable!!)
                    object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val typeFace = ResourcesCompat.getFont(this@OnlineActivity, R.font.carter_one)
                            binding.tvStatusOpponent.typeface = typeFace
                            binding.tvStatusOpponent.textSize = 30f
                            binding.tvStatusOpponent.text = (millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            with(binding) {
                                tvStatusOpponent.text = getString(R.string.match_start)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    llSearchingOpponent.setGone()
                                    setList(squad!!.data.playerList, squad!!.data.potentialAnswerList)

                                }, 500)
                            }
                        }
                    }.start()
                }

                if (text.contains("odaya katıldı") && !text.startsWith(getUserName()) && binding.tvPlayerTwoName.text.toString() == "" && binding.tvPlayerOneName.text.toString() == "") {
                    binding.tvPlayerOneName.text = getUserName()
                    binding.tvPlayerOneFirstLetter.text = getUserName().substring(0, 1)
                    binding.tvPlayerTwoFirstLetter.text = text.substringBefore("odaya").substring(0, 1)
                    binding.tvPlayerTwoName.text = text.substringBefore("odaya")
                }

                if (text.startsWith(getUserName()) && text.contains("Resim:")) {
                    myChooseImage = text.substringAfter("Resim:")
                }
                if (text.startsWith(getUserName()) && text.contains("İsim:")) {
                    myChooseName = text.substringAfter("İsim:")
                    control()
                }
                if (!text.startsWith(getUserName()) && text.contains("Resim:")) {
                    rivalChooseImage = text.substringAfter("Resim:")
                }
                if (!text.startsWith(getUserName()) && text.contains("İsim:")) {
                    rivalChooseName = text.substringAfter("İsim:")
                    control()
                }
                if (text.contains("oyundan ayrıldı")) {
                    if (text.substringBefore("oyundan ayrıldı").trim() != getUserName()) {
                        showAlertDialogTheme(getString(R.string.rival_disconnect), String.format(getString(R.string.disconnect_won_10_point)), onPositiveButtonClick = {
                            viewModel.updatePoint(UpdatePointRequest(getUserID(), 10))
                            startActivity(MainActivity.createIntent(this@OnlineActivity))
                        })
                    }
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            showAlertDialogTheme(getString(R.string.error), getString(R.string.connection_shutdown_exception))
        }
    }

    private val webSocketJoinerListener = object : WebSocketListener() {
        var squad: GetFirstElevenBySquadResponse? = null

        override fun onOpen(webSocket: WebSocket, response: Response) {
            messageList.clear()
            super.onOpen(webSocket, response)
            webSocket.send("${getUserName()} odaya katıldı.")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            messageList.add(text)
            runOnUiThread {
                onlineAdapter.updateAdapter(messageList)
                if (text == "Yeni kullanıcı bağlandı. Şu anda odada 1 kullanıcı var.") {
                    okHttpClient.newWebSocket(createRequest(roomName), this)
                }

                if (text.contains("{\"statusCode\":200")) {
                    val gson = Gson()
                    squad = gson.fromJson(text, GetFirstElevenBySquadResponse::class.java)

                    binding.apply {
                        tvTeamName.text = squad!!.data.squad.name
                        ivTeam.apply {
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(squad!!.data.playerList.first().squadImagePath)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(this)
                        }
                    }
                }

                if (messageList.any { it.contains("{\"statusCode\":200") }) {
                    val gson = Gson()
                    squad = gson.fromJson(messageList.first { it.contains("{\"statusCode\":200") }, GetFirstElevenBySquadResponse::class.java)

                    binding.apply {
                        tvTeamName.text = squad!!.data.squad.name
                        ivTeam.apply {
                            Glide.with(applicationContext)
                                .asBitmap()
                                .load(squad!!.data.playerList.first().squadImagePath)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(this)
                        }
                    }
                }

                if (text.contains("2 kullanıcı")) {
                    timeoutHandler?.removeCallbacks(timeoutRunnable!!)
                    object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val typeFace = ResourcesCompat.getFont(this@OnlineActivity, R.font.carter_one)
                            binding.tvStatusOpponent.typeface = typeFace
                            binding.tvStatusOpponent.textSize = 30f
                            binding.tvStatusOpponent.text = (millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            with(binding){
                                tvStatusOpponent.text = getString(R.string.match_start)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    llSearchingOpponent.setGone()
                                    setList(squad!!.data.playerList, squad!!.data.potentialAnswerList)
                                }, 500)
                            }
                        }
                    }.start()
                }

                if (text.contains("odaya katıldı") && !text.startsWith(getUserName()) && binding.tvPlayerTwoName.text.toString() == "" && binding.tvPlayerOneName.text.toString() == "") {
                    binding.tvPlayerOneName.text = getUserName()
                    binding.tvPlayerOneFirstLetter.text = getUserName().substring(0, 1)
                    binding.tvPlayerTwoFirstLetter.text = text.substringBefore("odaya").substring(0, 1)
                    binding.tvPlayerTwoName.text = text.substringBefore("odaya")
                }

                if (text.startsWith(getUserName()) && text.contains("Resim:")) {
                    myChooseImage = text.substringAfter("Resim:")
                    control()
                }
                if (text.startsWith(getUserName()) && text.contains("İsim:")) {
                    myChooseName = text.substringAfter("İsim:")
                    control()
                }
                if (!text.startsWith(getUserName()) && text.contains("Resim:")) {
                    rivalChooseImage = text.substringAfter("Resim:")
                    control()
                }
                if (!text.startsWith(getUserName()) && text.contains("İsim:")) {
                    rivalChooseName = text.substringAfter("İsim:")
                    control()
                }
                if (text.contains("oyundan ayrıldı")) {
                    if (text.substringBefore("oyundan ayrıldı").trim() != getUserName()) {
                        showAlertDialogTheme(getString(R.string.rival_disconnect), String.format(getString(R.string.disconnect_won_10_point)), onPositiveButtonClick = {
                            viewModel.updatePoint(UpdatePointRequest(getUserID(), 10))
                            startActivity(MainActivity.createIntent(this@OnlineActivity))
                        })
                    }
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            showAlertDialogTheme(getString(R.string.error), getString(R.string.connection_shutdown_exception))
        }
    }

    companion object {
        private var webSocket: WebSocket? = null
        const val TIMEOUT: Long = 3

        val messageList = arrayListOf<String>()

        fun createIntent(context: Context?): Intent {
            messageList.clear()

            return Intent(context, OnlineActivity::class.java)
        }
    }
}