package com.umtualgames.squadmaster.network

import android.annotation.SuppressLint
import com.umtualgames.squadmaster.data.models.Resource
import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.requests.RegisterRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.RegisterResponse
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.network.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.network.services.*
import io.reactivex.Observable

class RemoteDataSource {

    private val serviceProvider = ServiceProvider()

    @SuppressLint("CheckResult")
    fun getSquad(squadName: String): Observable<Resource<GetFirstElevenBySquadResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(PlayerServices::class.java)
                .getFirstElevenBySquadName(squadName)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getSquadList(): Observable<Resource<GetSquadListResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(SquadServices::class.java)
                .getSquadList()
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getSquadListByLeague(leagueID: Int, userID: Int): Observable<Resource<GetSquadListResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(SquadServices::class.java)
                .getSquadListByLeague(leagueID, userID)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getLeagues(userID: Int): Observable<Resource<GetLeaguesResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(LeagueServices::class.java)
                .getLeagues(userID)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun login(loginRequest: LoginRequest): Observable<Resource<LoginResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofitIsLogin()
                .create(LoginServices::class.java)
                .login(loginRequest)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun register(registerRequest: RegisterRequest): Observable<Resource<RegisterResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(LoginServices::class.java)
                .register(registerRequest)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun signInRefreshToken(refreshToken: String): Observable<Resource<RefreshTokenResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofitIsLogin()
                .create(LoginServices::class.java)
                .refreshTokenLogin(refreshToken)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getPoint(userID: Int): Observable<Resource<UserPointResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(UserPointServices::class.java)
                .getUserPoint(userID)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )

        }
    }

    @SuppressLint("CheckResult")
    fun updatePoint(updatePointRequest: UpdatePointRequest): Observable<Resource<UserPointResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(UserPointServices::class.java)
                .updatePoint(updatePointRequest)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getRankList(): Observable<Resource<GetRankListResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(UserPointServices::class.java)
                .getRankList()
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getRandomSquad(): Observable<Resource<GetFirstElevenBySquadResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(PlayerServices::class.java)
                .getFirstElevenByRandomSquad()
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun levelPass(levelPassRequest: LevelPassRequest): Observable<Resource<LevelPassResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(UnlockSquadServices::class.java)
                .levelPass(levelPassRequest)
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }

    @SuppressLint("CheckResult")
    fun getProjectSettings(): Observable<Resource<ProjectSettingsResponse>> {

        return Observable.create { emitter ->

            emitter.onNext(Resource.loading())

            serviceProvider
                .getRetrofit()
                .create(ProjectSettingsServices::class.java)
                .getProjectSettings()
                .subscribe(
                    {
                        emitter.onNext(Resource.success(it))
                        emitter.onComplete()
                    },
                    {
                        it.printStackTrace()
                        emitter.onNext(Resource.error(it.message!!))
                        emitter.onComplete()
                    }
                )
        }
    }
}