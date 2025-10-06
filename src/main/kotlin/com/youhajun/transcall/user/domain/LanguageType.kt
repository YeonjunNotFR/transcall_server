package com.youhajun.transcall.user.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class LanguageType(val code: String, val whisperPort: Int) {
    AFRIKAANS("AF", 2100),
    ARABIC("AR", 2100),
    BENGALI("BN", 2100),
    BULGARIAN("BG", 2100),
    CATALAN("CA", 2100),
    CHINESE("ZH", 2100),
    CROATIAN("HR", 2100),
    CZECH("CS", 2100),
    DANISH("DA", 2100),
    DUTCH("NL", 2100),
    ENGLISH("EN", 2000),
    ESTONIAN("ET", 2100),
    FINNISH("FI", 2100),
    FRENCH("FR", 2100),
    GERMAN("DE", 2100),
    GREEK("EL", 2100),
    HEBREW("HE", 2100),
    HINDI("HI", 2100),
    HUNGARIAN("HU", 2100),
    INDONESIAN("ID", 2100),
    ITALIAN("IT", 2100),
    JAPANESE("JA", 2100),
    KOREAN("KO", 2001),
    MALAY("MS", 2100),
    NORWEGIAN("NO", 2100),
    POLISH("PL", 2100),
    PORTUGUESE("PT", 2100),
    ROMANIAN("RO", 2100),
    RUSSIAN("RU", 2100),
    SERBIAN("SR", 2100),
    SLOVAK("SK", 2100),
    SLOVENIAN("SL", 2100),
    SPANISH("ES", 2100),
    SWEDISH("SV", 2100),
    TAGALOG("TL", 2100),
    THAI("TH", 2100),
    TURKISH("TR", 2100),
    UKRAINIAN("UK", 2100),
    URDU("UR", 2100),
    VIETNAMESE("VI", 2100);

    @JsonValue
    override fun toString(): String = code

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String): LanguageType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}