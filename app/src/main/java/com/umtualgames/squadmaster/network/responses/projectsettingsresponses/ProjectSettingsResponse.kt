package com.umtualgames.squadmaster.network.responses.projectsettingsresponses

data class ProjectSettingsResponse(
    val statusCode: Int,
    val message: String,
    val data: List<ProjectSettingsResponseItem>
)
