# ISSUE-059: Profile Image Crop and Flip Feature

## Objective
Implement a crop and flip functionality for the user profile picture. After a user selects an image from their gallery, they should be presented with a UI to crop the image and optionally flip/rotate it before it is saved as their profile picture.

## Requirements
1. **Image Selection Intercept**: When the user picks an image from the gallery, intercept the result before saving it to the `ProfileViewModel`.
2. **Crop UI**: 
   - Display the selected image in a cropping view.
   - Show a crop grid overlay.
   - Enforce a 1:1 (square) aspect ratio for the profile picture, as it is displayed in a `CircleShape`.
3. **Flip/Rotate Actions**:
   - Provide a button to flip the image or rotate it (as seen in the reference design).
4. **Action Buttons**:
   - **"Cancel"** button to abort the operation and return to the profile screen without changes.
   - **"Done"** button to confirm the crop/flip and apply the new image as the profile picture.
5. **UI/UX Match**:
   - Ensure the UI matches the reference design provided (dark background, grid overlay, bottom action bar with Cancel, Flip icon, Done).

## Technical Considerations
- Consider using an existing robust Android image cropping library (e.g., `uCrop`, `Android-Image-Cropper` via Intent, or a Compose-native alternative) to handle the complex gesture math.
- Ensure the cropped image is saved securely in the app's internal storage or cache before updating the `userPhotoUri` in the DataStore to avoid memory issues with large bitmaps.

## Definition of Done
- User selects an image and is immediately shown the crop UI.
- User can pan, zoom, crop, and flip the image.
- Tapping "Cancel" discards changes and no image is updated.
- Tapping "Done" saves the edited image and updates the profile picture displayed in the Profile Header.
