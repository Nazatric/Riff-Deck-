package com.riffdeck.player.data

import android.os.Parcelable
import com.riffdeck.player.data.model.search.SearchQuery

interface SearchFilter : Parcelable {
    fun getName(): CharSequence

    fun getCompatibleModes(): List<SearchQuery.FilterMode>

    suspend fun getResults(searchMode: SearchQuery.FilterMode, query: String): List<Any>
}