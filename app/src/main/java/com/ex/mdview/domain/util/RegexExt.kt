package com.ex.mdview.domain.util

fun MatchResult?.getGroupValue(index: Int): String {
    return this?.groups?.get(index)?.value ?: ""
}