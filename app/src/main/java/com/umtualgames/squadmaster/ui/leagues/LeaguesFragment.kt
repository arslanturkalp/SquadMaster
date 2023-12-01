package com.umtualgames.squadmaster.ui.leagues

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SquadMasterApp
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.FragmentLeaguesBinding
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.item.League
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.clubs.ClubsActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class LeaguesFragment : BaseFragment(), OnUserEarnedRewardListener {

    private val binding by lazy { FragmentLeaguesBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LeaguesViewModel>()

    private val leagueAdapter by lazy { LeaguesAdapter({ openClubs(it) }, { showRequireDialog(it) }) }

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    private lateinit var billingClient: BillingClient

    private lateinit var purchaseUpdateListener: PurchasesUpdatedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()
        loadAds()

        purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (pur in purchases) {
                    handlePurchase(pur)
                }
            }
        }

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()


        if (!isAdminUser()) {
            binding.apply {
                setVisibility(View.VISIBLE, rvLeagues, cvScore, ivRefresh)
                llShowLeague.setGone()
            }
        }

        viewModel.getUserPoint(getUserID())

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backToMainMenu()
                }
            })

        binding.apply {
            btnLoginOrRegister.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
            ivRefresh.setOnClickListener { viewModel.getUserPoint(getUserID()) }
        }
    }

    private fun receiveChampionsLeague() {
        val productList = ArrayList<QueryProductDetailsParams.Product>()

        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("championsleague")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, list ->
            launchPurchaseFlow(list[0])
        }
    }

    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        val productList = ArrayList<BillingFlowParams.ProductDetailsParams>()
        productList.add(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productList).build()
        billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) {

                if (it.responseCode == BillingResponseCode.OK) {
                    for (pur in purchase.products) {
                        if (pur.equals("championsleague")) {
                            consumePurchase(purchase)
                        }
                    }
                }
            }
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(params) { _, _ ->
            unlockChampionsLeague()
        }
    }

    private fun unlockChampionsLeague() {
        viewModel.unlockLeague(getUserID(), 27)
    }

    private fun backToMainMenu() {
        (activity as MainActivity).apply {
            showFragment(homeFragment)
            setItemInNavigation(homeFragment)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvLeagues.apply {
            adapter = leagueAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            LinearSnapHelper().attachToRecyclerView(this)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apply {
                    launch {
                        getLeaguesFlow.collect {
                            when (it) {
                                is Result.Error -> dismissProgressDialog()
                                is Result.Loading -> showProgressDialog()
                                is Result.Success -> {
                                    dismissProgressDialog()
                                    showLeagues(it.body!!.data)
                                }
                                is Result.Auth -> {
                                    dismissProgressDialog()
                                    refreshTokenLogin(getRefreshToken())
                                }
                            }
                        }
                    }

                    launch {
                        updatePointFlow.collect {
                            when (it) {
                                is Result.Error -> dismissProgressDialog()
                                is Result.Loading -> {}
                                is Result.Success -> dismissProgressDialog()
                                is Result.Auth -> {
                                    dismissProgressDialog()
                                    refreshTokenLogin(getRefreshToken())
                                }
                            }
                        }
                    }

                    launch {
                        getPointFlow.collect {
                            when (it) {
                                is Result.Error -> dismissProgressDialog()
                                is Result.Loading -> {}
                                is Result.Success -> {
                                    dismissProgressDialog()
                                    with(binding) {
                                        it.body!!.data.apply {
                                            tvBestScore.text = bestPoint.toString()
                                            tvTotalScore.text = point.toString()
                                        }
                                        getLeagues(getUserID())
                                    }
                                }
                                is Result.Auth -> {
                                    dismissProgressDialog()
                                    refreshTokenLogin(getRefreshToken())
                                }
                            }
                        }
                    }

                    launch {
                        refreshTokenFlow.collect {
                            when (it) {
                                is Result.Error -> dismissProgressDialog()
                                is Result.Loading -> {}
                                is Result.Success -> {
                                    dismissProgressDialog()
                                    it.body!!.apply {
                                        if (isSuccess) {
                                            updateToken(data.token.accessToken)
                                            updateRefreshToken(data.token.refreshToken)
                                        } else {
                                            returnToSplash()
                                        }
                                    }
                                }
                                is Result.Auth -> {
                                    dismissProgressDialog()
                                    returnToSplash()
                                }
                            }
                        }
                    }

                    launch {
                        unlockLeagueFlow.collect {
                            when (it) {
                                is Result.Error -> dismissProgressDialog()
                                is Result.Loading -> {}
                                is Result.Success -> {
                                    dismissProgressDialog()
                                    showAlertDialogTheme(getString(R.string.info), getString(R.string.league_unlocked))
                                    viewModel.getLeagues(getUserID())
                                }
                                is Result.Auth -> {
                                    dismissProgressDialog()
                                    returnToSplash()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun returnToSplash() = startActivity(SplashActivity.createIntent(requireContext(), false))

    private fun openClubs(league: League) {
        context?.startActivity((ClubsActivity.createIntent(context, league)))
    }

    private fun showRequireDialog(league: League) {
        if (league.name == "Champions League Legends") {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        receiveChampionsLeague()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    showAlertDialogTheme(getString(R.string.error), getString(R.string.unknown_host_exception))
                }
            })
        } else {
            showAlertDialogTheme(
                title = getString(R.string.warning),
                contentMessage = String.format(getString(R.string.need_point), league.name, league.point) + " " + getString(R.string.watch_and_earn),
                showNegativeButton = true,
                negativeButtonTitle = getString(R.string.watch_ad),
                onNegativeButtonClick = {
                    if (mRewardedInterstitialAd != null) {
                        mRewardedInterstitialAd?.show(activity as Activity, this@LeaguesFragment)
                    }
                })
        }
    }

    private fun showLeagues(leagues: List<League>) {
        leagueAdapter.updateAdapter(leagues.sortedBy { it.point })
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(requireContext(), "ca-app-pub-5776386569149871/5057308377", adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(SquadMasterApp.TAG, adError.toString())
                mRewardedInterstitialAd = null
            }

            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(SquadMasterApp.TAG, "Ad was loaded.")
                mRewardedInterstitialAd = ad
            }
        })
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        viewModel.updatePoint(UpdatePointRequest(getUserID(), 50))
        showAlertDialogTheme(getString(R.string.info), getString(R.string.point_50))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update" || event.message == "Score Update") {
            viewModel.getUserPoint(getUserID())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}