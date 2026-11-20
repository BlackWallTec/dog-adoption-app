package com.app.android.domain.entities.entitiesCatalogo

sealed interface ShareUIState{
    object ShareMenuHidden: ShareUIState
    data class ShareMenu(val content: ShareContent): ShareUIState
}
enum class ShareEvent{
    CopiadoOk
}