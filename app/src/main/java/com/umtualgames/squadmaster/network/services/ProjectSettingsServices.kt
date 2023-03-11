package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.responses.projectsettingsresponses.ProjectSettingsResponse
import io.reactivex.Single
import retrofit2.http.GET

interface ProjectSettingsServices {

    @GET("ProjectSettings/GetProjectSettings")
    fun getProjectSettings() : Single<ProjectSettingsResponse>
}