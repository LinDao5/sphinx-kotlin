package chat.sphinx.concept_network_query_save_profile.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePeopleProfileDto(
    val id: Int,
    val host: String,
)