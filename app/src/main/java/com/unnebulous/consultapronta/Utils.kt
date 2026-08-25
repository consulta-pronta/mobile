package com.unnebulous.consultapronta

object Utils {
	enum class UserType { PATIENT, PROFESSIONAL }

	enum class NavbarButton(val id: String) {
		FIRST("first_element"),
		SECOND("second_element"),
		MAIN("main_button"),
		FOURTH("fourth_element"),
		FIFTH("fifth_element"),
	}
}