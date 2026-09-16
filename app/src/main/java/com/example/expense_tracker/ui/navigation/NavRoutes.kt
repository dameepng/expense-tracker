package com.example.expense_tracker.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val AI_INPUT = "ai_input"
    const val RECEIPT_PICKER = "receipt_picker"
    const val RECEIPT_REVIEW = "receipt_review"
    const val CHAT = "chat"
    const val INPUT = "input?expenseId={expenseId}"
    const val SUMMARY = "summary"
    const val WALLET = "wallet"
    const val PROFILE = "profile"
    const val ONBOARDING = "onboarding"
    const val HELP_FAQ = "help_faq"
    const val PRIVACY_POLICY = "privacy_policy"

    const val REMINDER_LIST = "reminder_list"
    const val CATEGORY_DETAIL = "category_detail/{categoryId}?walletId={walletId}&startTime={startTime}&endTime={endTime}"

    val topLevelTabs = setOf(HOME, WALLET, SUMMARY, PROFILE)
    private val routesWithBottomBar = topLevelTabs

    fun shouldShowBottomBar(route: String?): Boolean {
        if (route == null) return true
        val baseRoute = route.substringBefore('?').substringBefore('/')
        return baseRoute in routesWithBottomBar
    }

    fun inputRoute(expenseId: Long? = null): String {
        return if (expenseId != null) "input?expenseId=$expenseId" else "input"
    }

    fun categoryDetailRoute(categoryId: Long, walletId: Long?, startTime: Long, endTime: Long): String {
        val walletParam = if (walletId != null) "&walletId=$walletId" else ""
        return "category_detail/$categoryId?startTime=$startTime&endTime=$endTime$walletParam"
    }
}
