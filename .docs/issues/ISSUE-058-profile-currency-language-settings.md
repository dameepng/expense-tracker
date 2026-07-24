# Profile: Currency and Language Settings

## Objective
Implement functionality for the "Mata Uang" (Currency) and "Bahasa" (Language) settings on the Profile page. Ensure that these settings are persisted using DataStore and affect the application's formatting and localization globally.

## Requirements
1. **Settings Persistence**:
   - Add preferences for Currency (e.g., IDR, USD) and Language (e.g., Indonesian, English) in `UserPreferencesRepository`.
   - Retrieve and update these values reactively using Flow.

2. **UI Implementation**:
   - Modify `ProfileScreen` to handle clicks on the "Mata Uang" and "Bahasa" list items.
   - Display bottom sheets or dialogs for selecting the desired Currency and Language.
   - Show the currently selected value in the trailing side of the list item.

3. **Global Application**:
   - **Currency**: Update the `CurrencyFormatter` utility to use the selected currency dynamically instead of a hardcoded value. This will affect Home, Wallet, Input, and Summary screens.
   - **Language**: Handle locale changes at the Activity or App level to support dynamic language switching.

## Files to Modify
- `app/src/main/java/com/example/expense_tracker/data/UserPreferencesRepository.kt`
- `app/src/main/java/com/example/expense_tracker/ui/profile/ProfileScreen.kt`
- `app/src/main/java/com/example/expense_tracker/ui/profile/ProfileViewModel.kt`
- `app/src/main/java/com/example/expense_tracker/ui/CurrencyFormatter.kt`
- Locale configurations (e.g., string resources)
