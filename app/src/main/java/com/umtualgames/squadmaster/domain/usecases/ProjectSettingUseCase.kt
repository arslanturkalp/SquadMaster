package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class ProjectSettingUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<Any, ProjectSettingsResponse>() {
    override suspend fun getData(params: Any?) = repository.getProjectSettings()
}