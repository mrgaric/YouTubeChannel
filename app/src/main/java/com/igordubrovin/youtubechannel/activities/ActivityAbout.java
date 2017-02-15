package com.igordubrovin.youtubechannel.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.igordubrovin.youtubechannel.R;

public class ActivityAbout extends PreferenceActivity implements Preference.OnPreferenceClickListener{

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);

        Preference prefShareKey      = (Preference) findPreference(getString(R.string.pref_share_key));
        Preference prefRateReviewKey = (Preference) findPreference(getString(R.string.pref_rate_review_key));

        prefShareKey.setOnPreferenceClickListener(this);
        prefRateReviewKey.setOnPreferenceClickListener(this);
    }


    protected int getPreferencesXmlId()
    {
        return R.xml.pref_about;
    }

    @Override
    public boolean onPreferenceClick(android.preference.Preference preference) {
        if(preference.getKey().equals(getString(R.string.pref_share_key))) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message) +
                    " " + getString(R.string.googleplay_url));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
        }else if(preference.getKey().equals(getString(R.string.pref_rate_review_key))) {
            Intent rateReviewIntent = new Intent(Intent.ACTION_VIEW);
            rateReviewIntent.setData(Uri.parse(
                    getString(R.string.googleplay_url)));
            startActivity(rateReviewIntent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}
