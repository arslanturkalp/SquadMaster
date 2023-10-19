package com.umtualgames.squadmaster.domain.entities.responses.projectsettingsresponses

data class ProjectSettingsResponse(
    val statusCode: Int,
    val message: String,
    val data: List<ProjectSettingsResponseItem>
)
