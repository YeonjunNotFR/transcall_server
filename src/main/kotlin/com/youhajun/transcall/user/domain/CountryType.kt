package com.youhajun.transcall.user.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CountryType(val code: String) {
    ARGENTINA("AR"),
    AUSTRALIA("AU"),
    AUSTRIA("AT"),
    BELGIUM("BE"),
    BRAZIL("BR"),
    BULGARIA("BG"),
    CANADA("CA"),
    CHILE("CL"),
    CHINA("CN"),
    COLOMBIA("CO"),
    CROATIA("HR"),
    CZECH_REPUBLIC("CZ"),
    DENMARK("DK"),
    EGYPT("EG"),
    ESTONIA("EE"),
    FINLAND("FI"),
    FRANCE("FR"),
    GERMANY("DE"),
    GREECE("GR"),
    HONG_KONG("HK"),
    HUNGARY("HU"),
    ICELAND("IS"),
    INDIA("IN"),
    INDONESIA("ID"),
    IRELAND("IE"),
    ISRAEL("IL"),
    ITALY("IT"),
    JAPAN("JP"),
    KAZAKHSTAN("KZ"),
    KENYA("KE"),
    MALAYSIA("MY"),
    MEXICO("MX"),
    MOROCCO("MA"),
    NETHERLANDS("NL"),
    NEW_ZEALAND("NZ"),
    NIGERIA("NG"),
    NORWAY("NO"),
    PHILIPPINES("PH"),
    POLAND("PL"),
    PORTUGAL("PT"),
    ROMANIA("RO"),
    RUSSIA("RU"),
    SAUDI_ARABIA("SA"),
    SERBIA("RS"),
    SINGAPORE("SG"),
    SLOVAKIA("SK"),
    SLOVENIA("SI"),
    SOUTH_AFRICA("ZA"),
    SOUTH_KOREA("KR"),
    SPAIN("ES"),
    SWEDEN("SE"),
    SWITZERLAND("CH"),
    TAIWAN("TW"),
    THAILAND("TH"),
    TURKEY("TR"),
    UKRAINE("UA"),
    UNITED_ARAB_EMIRATES("AE"),
    UNITED_KINGDOM("GB"),
    UNITED_STATES("US"),
    VIETNAM("VN");

    @JsonValue
    override fun toString(): String = code

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String): CountryType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: UNITED_STATES
    }
}
