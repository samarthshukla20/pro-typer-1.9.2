package com.samarthshukla.protyper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import android.widget.ImageButton;
import com.google.android.material.button.MaterialButton;
import com.samarthshukla.protyper.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup top toolbar back button.
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Social button: Instagram only.
        findViewById(R.id.btnInstagram).setOnClickListener(v ->
            openUrl("https://www.instagram.com/pro.typer.info?igsh=Z2NjNGcxaXJzemV0"));
        
        // Social button: Instagram only.
        findViewById(R.id.btnFaceBook).setOnClickListener(v ->
            openUrl("https://www.facebook.com/share/1A2Ar6a1Gp/"));


        // Donation card "Click here" button.
        findViewById(R.id.btnDonate).setOnClickListener(v ->
            openUrl("https://crowdfund.org/c/support-us-by-donating-a-little-share-of-yours/"));

        // Learn More buttons for profiles: redirect to LinkedIn.
        MaterialButton btnLearnMore1 = findViewById(R.id.btnLearnMore1);
        btnLearnMore1.setOnClickListener(v ->
            openUrl("https://linktr.ee/lyfofashu")); // Replace with your actual URL

        MaterialButton btnLearnMore2 = findViewById(R.id.btnLearnMore2);
        btnLearnMore2.setOnClickListener(v ->
            openUrl("https://beacons.ai/samarthshukla")); // Replace with your actual URL

        // Set lm.png icon next to text for both Learn More buttons.
        // ICON_GRAVITY_TEXT_START positions the icon to the left of the text.
        btnLearnMore1.setIconResource(R.drawable.lm);
        btnLearnMore1.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
        btnLearnMore1.setIconPadding(dpToPx(8));

        btnLearnMore2.setIconResource(R.drawable.lm);
        btnLearnMore2.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
        btnLearnMore2.setIconPadding(dpToPx(8));
    }

    /**
     * Utility function to convert dp to pixels.
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * Checks whether a given package is installed on the device.
     */
    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Opens the URL using deep linking to supported apps if available,
     * otherwise opens the link in an in-app browser using Custom Tabs.
     */
    private void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        // Deep link for Instagram
        if (url.contains("instagram.com") && isAppInstalled("com.instagram.android")) {
            intent.setPackage("com.instagram.android");
            try {
                startActivity(intent);
                return;
            } catch (Exception e) {
                // Fallback to in-app browser if something goes wrong.
            }
        }

        // Default: Open in-app browser using Custom Tabs.
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        customTabsIntent.launchUrl(this, uri);
    }
}